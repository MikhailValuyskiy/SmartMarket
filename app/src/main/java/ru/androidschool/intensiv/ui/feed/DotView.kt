package ru.androidschool.intensiv.ui.feed

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.airbnb.lottie.LottieAnimationView

class DotView(context: Context) : View(context) {

    var dotColor = Color.BLACK
    var radius = 0f
    var targetScale = .7f
    var animationTotalDuration = 500L
        set(value) {
            field = value / 2
        }

    private val ovalRect = RectF()
    private val paint = Paint()
    private var centerX = 0
    private var centerY = 0
    private var animation: Animator? = null
    private var scale = targetScale
    private val dotFirstColor by lazy { dotColor and 0x00FFFFFF + 0x44000000 }
    private val dotSecondColor by lazy { dotColor }
    private var color = dotFirstColor

    fun startAnimation() {
        animation = animatorSet {
            playTogether {
                animation {
                    ValueAnimator.ofFloat(targetScale, 1f).apply {
                        addUpdateListener {
                            scale = it.animatedValue as Float
                            ovalRect.updateOval()
                            invalidate()
                        }
                        duration = animationTotalDuration
                        interpolator = FastOutSlowInInterpolator()
                        repeatCount = ValueAnimator.INFINITE
                        repeatMode = ValueAnimator.REVERSE
                    }
                }
                animation {
                    ValueAnimator.ofObject(ArgbEvaluator(), dotFirstColor, dotSecondColor).apply {
                        addUpdateListener {
                            color = it.animatedValue as Int
                        }
                        duration = animationTotalDuration
                        interpolator = FastOutSlowInInterpolator()
                        repeatCount = ValueAnimator.INFINITE
                        repeatMode = ValueAnimator.REVERSE
                    }
                }
            }
        }.apply { start() }
    }

    fun stopAnimation() {
        animation?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = color
        canvas.drawOval(ovalRect, paint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = width / 2
        centerY = height / 2
        radius = (Math.min(width, height) / 2).toFloat()
        ovalRect.updateOval()
    }

    private fun RectF.updateOval() {
        left = centerX - (radius * scale)
        top = centerY - (radius * scale)
        right = centerX + (radius * scale)
        bottom = centerY + (radius * scale)
    }
}

fun LottieAnimationView.onAnimationEnd(listener: (Animator) -> Unit) {
    addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator) {
            listener.invoke(animation)
        }
    })
}

fun Animator.onAnimationEnd(listener: (Animator) -> Unit) {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator) {
            listener.invoke(animation)
        }
    })
}

fun animatorSet(block: AnimatorSet.() -> Unit): AnimatorSet = AnimatorSet().apply(block)

fun AnimatorSet.playTogether(block: TogetherBuilder.() -> Unit) {
    val builder = TogetherBuilder()
    builder.block()
    playTogether(builder.animations)
}

class TogetherBuilder {

    internal val animations = mutableListOf<Animator>()

    fun animation(block: () -> Animator) {
        animations.add(block.invoke())
    }
}
