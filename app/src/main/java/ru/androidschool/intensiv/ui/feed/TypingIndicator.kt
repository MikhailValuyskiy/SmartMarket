package ru.androidschool.intensiv.ui.feed

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import ru.androidschool.intensiv.R

class TypingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var dotSize = 0
    private var dotMargin = 0
    private var frequency = 0
    private var dotColor = 0
    private var isAnimating = false
    private var dots: List<DotView>

    init {
        orientation = HORIZONTAL
        context.theme.obtainStyledAttributes(attrs, R.styleable.TypingIndicator, 0, 0).apply {
            dotSize = getDimensionPixelOffset(R.styleable.TypingIndicator_dotSize, 24)
            dotMargin = getDimensionPixelOffset(R.styleable.TypingIndicator_dotMargin, 10)
            frequency = getInt(R.styleable.TypingIndicator_animationFrequency, 500)
            dotColor = getColor(R.styleable.TypingIndicator_dotColor, Color.BLACK)
            recycle()
        }
        clipToPadding = false
        clipChildren = false
        dots = mutableListOf<DotView>().apply {
            repeat(3) { i ->
                val lp = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    val left = if (i == 0) 0 else dotMargin
                    val right = if (i == 2) 0 else dotMargin
                    setMargins(left, 0, right, 0)
                }
                val dot = DotView(context)
                dot.dotColor = dotColor
                dot.animationTotalDuration = frequency.toLong()
                add(dot)
                addView(dot, lp)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAnimating = true
        startAnimation(0)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    private fun startAnimation(i: Int) {
        if (isAnimating) {
            dots[i].startAnimation()
            postDelayed((frequency - (frequency / 4)).toLong()) {
                if (i != dots.size - 1) {
                    startAnimation(i + 1)
                }
            }
        }
    }

    private fun stopAnimation() {
        isAnimating = false
        dots.forEach { it.stopAnimation() }
    }
}

fun Drawable.invisible() {
    alpha = 0
}

fun Drawable.visible() {
    alpha = 255
}

fun View.postDelayed(delayMills: Long, r: () -> Unit) {
    postDelayed(r, delayMills)
}
