package ru.androidschool.intensiv.ui.watchlist

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_diagram.*
import kotlinx.android.synthetic.main.item_portfolio_diagram.view.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.ui.feed.bindData
import ru.androidschool.intensiv.ui.watchlist.DiagramUtils.onDiagramSectorClicked
import ru.androidschool.intensiv.ui.watchlist.MoneyUtils.formatPrice
import kotlin.math.*


private val colorList = listOf(
    R.color.colorAccent,
    R.color.blue,
    R.color.red,
    R.color.dark,
    R.color.asphalt,
    R.color.green,
    R.color.redOrange,
    R.color.orange,
    R.color.darkBlue,
    R.color.darkAsphalt,
    R.color.yellow,
    R.color.stock_color,
    R.color.stock_color1,
    R.color.stock_color2,
    R.color.stock_color3,
    R.color.stock_color,
    R.color.stock_color4,
    R.color.stock_color5
)


data class Holder(
    val totalValue: Double,
    val value:Double,
    val category:String
)

class PortfolioDividendDiagramFragment : Fragment() {

    private var portfolioId: Long = 0L




    private val adapter by lazy {
        GroupAdapter<GroupieViewHolder>()
    }

    private lateinit var portfolioStockList: List<Holder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


   }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_diagram, container, false)
    }


    override fun onResume() {
        super.onResume()

        val portfolioWithStocks = listOf<Holder>(Holder(1500.0,100.0, "Торты"),Holder(3500.0,100.0, "Игрушки"),Holder(1000.0,100.0, "Торты"),Holder(4500.0,100.0, "Открытки"))

        if (portfolioWithStocks != null) {
            portfolioStockList = portfolioWithStocks
            val totalValue = getTotalValue()
            val list = mutableListOf<Group>()

            list.addAll(listOf(DiagramItem(Money(totalValue), getSectors(portfolioStockList))))



            showItems(list)
        }
    }

    private fun getTotalValue(): Double {
        return portfolioStockList.sumByDouble { it.totalValue }
    }




    private fun getSectors(stocksFromPortfolio: List<Holder>): List<CircleDiagramView.Sector> {
        val sectors = mutableListOf<CircleDiagramView.Sector>()

        val context = activity?.baseContext!!

        if (stocksFromPortfolio.isEmpty()) {
            sectors.add(DiagramUtils.createEmptyDiagram(context))
        } else {
            val totalValue = getTotalValue()

            stocksFromPortfolio.forEach {
                sectors.add(
                    DiagramUtils.createSector(
                        DiagramUtils.getProgress(it.totalValue, totalValue),
                        ContextCompat.getColor(context, colorList.shuffled()[(0 until 10).random()])
                    ) { position -> onDiagramSectorClicked(stocksFromPortfolio, position, context) }
                )
            }
        }
        return sectors
    }

    private fun showItems(items: List<Group>) {
        adapter.clear()
        if (items.isNotEmpty()) {
            portfolio_list_container.adapter = adapter
            adapter.addAll(items)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(portfolioId: Long) =
            PortfolioDividendDiagramFragment().apply {

            }
    }
}
data class Money(
    val amount: Double,
    val currency: String = RUB
) {
    companion object {
        const val RUB = "₽"
    }
}

fun Money?.format(context: Context): String {
    val resourceManager = context
    return this?.let {
        "${formatAmount()} ${CurrencyUtils.getCurrencySymbol(this.currency, resourceManager)}"
    } ?: ""
}
fun Money?.formatAmount(): String {
    return this?.let { formatPrice(amount) }.orEmpty()
}



class DiagramItem(
    private val sum: Money,
    private val sectors: List<CircleDiagramView.Sector>
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) = bindData(viewHolder) {
        sum_text_view.text = sum.format(context)
        circle_diagram.sectors = sectors
    }

    override fun getLayout(): Int = R.layout.item_portfolio_diagram
}

class CircleDiagramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var sectors = listOf<Sector>()
        set(value) {
            field = value
            invalidate()
        }
    private val selectedOval by lazy {
        RectF().apply {
            val margin = width / 4f - 16f.convertDPtoPX()
            top = 0f
            left = margin
            right = width.toFloat() - margin
            bottom = height.toFloat()
        }
    }
    private var selectedIndex = -1
    private val oval by lazy {
        RectF().apply {
            val marginHorizontal = width / 4f
            top = 16f.convertDPtoPX()
            left = marginHorizontal
            right = width.toFloat() - marginHorizontal
            bottom = height.toFloat() - 16f.convertDPtoPX()
        }
    }
    private val insideOval by lazy {
        RectF().apply {
            val margin = width / 4f + 32f.convertDPtoPX()
            top = 48f.convertDPtoPX()
            left = margin
            right = width.toFloat() - margin
            bottom = height.toFloat() - 48f.convertDPtoPX()
        }
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var sum = 270f
        sectors.forEachIndexed { i, sector ->
            paint.color = sector.color
            canvas.drawArc(
                if (i == selectedIndex) selectedOval
                else oval,
                sum,
                sector.angle,
                true,
                paint
            )
            sum += sector.angle
            sum %= 360f
        }
        paint.color = ContextCompat.getColor(context, R.color.white)
        canvas.drawArc(insideOval, 0f, 360f, true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec)),
            MeasureSpec.makeMeasureSpec(width / 2 + 32f.convertDPtoPX().toInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val i = findSectorPosition(event.x, event.y)
                sectors.getOrNull(i)?.onClick?.invoke(i)
                animateSector(i)
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return false
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun findSectorPosition(x: Float, y: Float): Int {
        val centerX = width / 2f
        val centerY = height / 2f
        val ovalRadius = (oval.right - oval.left) / 2f
        if (centerX + ovalRadius < x || centerY + ovalRadius < y || centerX - ovalRadius > x || centerY - ovalRadius > y) {
            return -1
        }

        val r = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))

        var angle = acos(
            sqrt(
                (centerX - x).pow(2) + (centerY - r - y).pow(2)
            ) / (2f * r)
        ) * 360f / PI
        if (centerX > x) {
            angle += 180f
        } else {
            angle = 180f - angle
        }
        var sum = 0f
        sectors.forEachIndexed { i, sector ->
            sum += sector.angle
            if (angle < sum) {
                return i
            }
        }
        return -1
    }

    private fun animateSector(pos: Int) {
        animateHidePrev {
            selectedIndex = pos
            ValueAnimator.ofFloat(0f, 16f.convertDPtoPX()).apply {
                interpolator = FastOutSlowInInterpolator()
                duration = 500
                addUpdateListener {
                    val animatedValue = it.animatedValue as Float
                    val margin = width / 4f - animatedValue
                    selectedOval.apply {
                        top = 16f.convertDPtoPX() - animatedValue
                        left = margin
                        right = width.toFloat() - margin
                        bottom = height.toFloat() - 16f.convertDPtoPX() + animatedValue
                    }
                    invalidate()
                }
            }.start()
        }
    }

    private fun animateHidePrev(onComplete: () -> Unit) {
        ValueAnimator.ofFloat(16f.convertDPtoPX(), 0f).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 200
            addUpdateListener {
                val animatedValue = it.animatedValue as Float
                val margin = width / 4f - animatedValue
                selectedOval.apply {
                    top = 16f.convertDPtoPX() - animatedValue
                    left = margin
                    right = width.toFloat() - margin
                    bottom = height.toFloat() - 16f.convertDPtoPX() + animatedValue
                }
                invalidate()
            }
            onAnimationEnd {
                onComplete.invoke()
            }
        }.start()
    }

    class Sector(
        percent: Double,
        val color: Int,
        val onClick: (position:Int) -> Unit
    ) {
        val angle: Float = 3.6f * percent.toFloat()
    }

    fun Float.convertDPtoPX(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }
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

object DiagramUtils {

    fun getProgress(currentValue: Double, totalValue: Double): Double {
        return totalValue.takeIf { it != 0.0 }?.let { _ ->
            (currentValue.div(totalValue) * 100.0).absoluteValue
        } ?: 0.0
    }

    fun createEmptyDiagram(context: Context): CircleDiagramView.Sector {
        return CircleDiagramView.Sector(
            100.0,
            ContextCompat.getColor(context, R.color.grey)
        ) {
        }
    }

    fun createSector(
        percent: Double,
        color: Int,
        onClick: (position: Int) -> Unit
    ): CircleDiagramView.Sector {
        return CircleDiagramView.Sector(
            percent,
            color
        ) { position ->
            onClick.invoke(position)
        }
    }

    fun onDiagramSectorClicked(stocks: List<Holder>, position: Int, context:Context ) {
        Toast.makeText(
            context,
            stocks[position].category,
            Toast.LENGTH_LONG
        ).show()
    }
}
