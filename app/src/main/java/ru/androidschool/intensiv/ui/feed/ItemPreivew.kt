package ru.androidschool.intensiv.ui.feed

import android.view.View
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_preview.view.*
import kotlinx.android.synthetic.main.item_preview.view.date_value
import kotlinx.android.synthetic.main.item_preview.view.percent_in_portfolio_value
import kotlinx.android.synthetic.main.item_preview.view.stock_code
import kotlinx.android.synthetic.main.item_preview.view.stock_name
import kotlinx.android.synthetic.main.item_transaction.view.*
import ru.androidschool.intensiv.R

data class Preview(
    val name: String,
    val category: String,
    val count: String,
    val buy: String,
    val url:String,
    val image: Int? = null
)

class PreviewItem(
    private val stockInfo: Preview,
    private val onClick: (Preview) -> Unit
) : Item() {
    override fun getLayout() = R.layout.item_preview

    override fun bind(viewHolder: GroupieViewHolder, position: Int) =
        bindData(viewHolder) {

            content.setOnClickListener {
                onClick.invoke(stockInfo)
            }
            stock_name.text = stockInfo.name
            stock_code.text = stockInfo.category
            percent_in_portfolio_value.text = stockInfo.count

            date_value.text = stockInfo.buy
            date_value.visibility = View.VISIBLE

            if (stockInfo.image!=null){
                Picasso.get().load(stockInfo.image).into(image_preview2)
            }
        }
}

class TransItem(
    private val stockInfo: Preview,
    private val onClick: (Preview) -> Unit
) : Item() {
    override fun getLayout() = R.layout.item_transaction

    override fun bind(viewHolder: GroupieViewHolder, position: Int) =
        bindData(viewHolder) {

            t_content.setOnClickListener {
                onClick.invoke(stockInfo)
            }
            stock_name.text = stockInfo.name
            stock_code.text = stockInfo.category
            percent_in_portfolio_value.text = stockInfo.count

            date_value.text = stockInfo.buy
            date_value.visibility = View.VISIBLE
        }
}