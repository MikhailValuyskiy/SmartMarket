package ru.androidschool.intensiv.ui.feed

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_typing.view.*
import ru.androidschool.intensiv.R


data class CarrotData(
    @DrawableRes val icon: Int,
    @StringRes val question: Int,
    val firstAnswer: Pair<Int, LinkedResult>,
    val secondAnswer: Pair<Int, LinkedResult>,
    val thirdAnswer: Pair<Int, LinkedResult>
)

data class LinkedResult(
    @StringRes val text: Int,
    val linkPos: Pair<Int, Int>,
    val onClick: (CarrotItem) -> Unit
)

inline fun bindData(viewHolder: GroupieViewHolder, bind: View.() -> Unit) {
    bind.invoke(viewHolder.containerView)
}

@Suppress("UNCHECKED_CAST")
inline fun <T : View> typedBindData(viewHolder: GroupieViewHolder, bind: T.() -> Unit) {
    bind.invoke(viewHolder.containerView as T)
}

class CarrotAnalytics()

class CarrotItem(
    private val carrotData: CarrotData,
    private val start: Boolean,
    private val onClose: (CarrotItem) -> Unit
) : Item() {

    private var startAnimation: (() -> Unit)? = null
    private var coinAnimation: (() -> Unit)? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) = bindData(viewHolder) {
        coins.setImageResource(carrotData.icon)
        question_text_view.setText(carrotData.question)
        yes_button_text.setText(carrotData.firstAnswer.first)
        very_button_text.setText(carrotData.secondAnswer.first)
//        meh_button_text.setText(carrotData.thirdAnswer.first)
        coinAnimation = {
            carrot_layout.coins.apply {
                translationX = -width.toFloat()
            }
            ObjectAnimator.ofFloat(carrot_layout.coins, "translationX", 0f).apply {
                interpolator = BounceInterpolator()
                duration = 1000
                onAnimationEnd {
                    carrot_layout.applyState(CarrotState.Loading)
                }
            }.start()
        }
        if (start) {
            carrot_layout.coins.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                coinAnimation?.invoke()
            }
        }
        startAnimation = {
            postDelayed(3500) {

                carrot_layout.apply {
                    applyState(CarrotState.Question)
                }
            }
        }
        if (start) {
            startAnimation?.invoke()
        }
        carrot_layout.yes_button.setOnClickListener {
            initResultText(carrotData.firstAnswer.second)
            carrot_layout.applyState(CarrotState.Result)
        }
        carrot_layout.very_button.setOnClickListener {
            initResultText(carrotData.secondAnswer.second)


            carrot_layout.applyState(CarrotState.Result)
        }
        carrot_layout.meh_button.setOnClickListener {
            initResultText(carrotData.thirdAnswer.second)

            carrot_layout.applyState(CarrotState.Result)
        }
        carrot_layout.close_result_button.setOnClickListener {

            hideItem(viewHolder)
        }
        carrot_layout.close_button.setOnClickListener {

            hideItem(viewHolder)
        }
    }

    fun start() {
        if (!start) {
            coinAnimation?.invoke()
            startAnimation?.invoke()
        }
    }

    override fun getLayout(): Int = R.layout.item_typing

    private fun View.initResultText(linkedResult: LinkedResult) {
        linkedResult.onClick.invoke(this@CarrotItem)
        val startLinkPos = linkedResult.linkPos.first
        val endLinkPos = linkedResult.linkPos.second
        carrot_layout.result_text.movementMethod = LinkMovementMethod.getInstance()
        carrot_layout.result_text.text =
            SpannableString(context.getString(linkedResult.text)).apply {
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        linkedResult.onClick.invoke(this@CarrotItem)
                    }

                    override fun updateDrawState(textPaint: TextPaint) {
                        textPaint.linkColor = ContextCompat.getColor(context, R.color.black)
                        textPaint.isUnderlineText = true
                        super.updateDrawState(textPaint)
                    }
                }, startLinkPos, endLinkPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }

    private fun View.applyState(state: CarrotState) {
        state.apply(this)
    }

    private fun View.hideItem(viewHolder: GroupieViewHolder) {
        val h = viewHolder.itemView.height
        ValueAnimator.ofInt(h, 0).apply {
            addUpdateListener { valueAnimator ->
                layoutParams.height = valueAnimator.animatedValue as Int
                viewHolder.itemView.layoutParams = layoutParams
            }
            duration = 500
            onAnimationEnd {
                layoutParams.height = h
                viewHolder.itemView.let { view ->
                    view.layoutParams = layoutParams
                    view.loading_card.invisible()
                    view.question_card.invisible()
                    view.yes_button.invisible()
                    view.very_button.invisible()
                    view.meh_button.invisible()
                    view.result_card.invisible()
                }
                onClose.invoke(this@CarrotItem)
            }
        }.start()
    }
}

sealed class CarrotState {

    abstract fun apply(view: View)

    object Loading : CarrotState() {
        override fun apply(view: View) {
            if (!view.loading_card.isVisible) {
                view.loading_card.fadeIn()
                view.question_card.invisible()
                view.yes_button.invisible()
                view.very_button.invisible()
                view.meh_button.invisible()
                view.result_card.invisible()
            }
        }
    }

    object Question : CarrotState() {
        override fun apply(view: View) {
            if (!view.question_card.isVisible) {
                view.loading_card.fadeOut {
                    view.question_card.fadeIn()
                    view.postDelayed(500) {
                        view.yes_button.fadeIn()
                        view.postDelayed(200) {
                            view.very_button.fadeIn()
                        }
                        view.postDelayed(400) {
                            view.meh_button.fadeIn()
                        }
                    }
                }
                view.result_card.invisible()
            }
        }
    }

    object Result : CarrotState() {
        override fun apply(view: View) {
            view.loading_card.invisible()
            view.question_card.fadeOut {
                view.result_card.fadeIn()
            }
            view.yes_button.fadeOut()
            view.very_button.fadeOut()
            view.meh_button.fadeOut()
        }
    }

    protected fun View.fadeIn(onEnd: (() -> Unit)? = null) {
        visible()
        alpha = 0f
        animate().apply {
            alpha(1f)
            duration = 250
            withEndAction {
                onEnd?.invoke()
            }
        }.start()
    }

    protected fun View.fadeOut(onEnd: (() -> Unit)? = null) {
        visible()
        alpha = 1f
        animate().apply {
            alpha(0f)
            duration = 250
            withEndAction {
                invisible()
                onEnd?.invoke()
            }
        }.start()
    }
}


fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

val View.isVisible get() = visibility == View.VISIBLE