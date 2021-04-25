package ru.androidschool.intensiv.ui.feed

import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_with_text.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.data.Goods

class GoodsItem(
    private val content: Goods,
    private val onClick: (goods: Goods) -> Unit
) : Item() {

    override fun getLayout() = R.layout.item_with_text

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.description.text = content.title
        viewHolder.movie_rating.rating = content.rating
        viewHolder.content.setOnClickListener {
            onClick.invoke(content)
        }

        Picasso.get()
            .load("https://cdn.uumarket.ru/z/st/40702a/c1552e/80e504/NTkxMjIyNTIaNDIyMzAxOGh0dHBzOi8vc2FudGEtYXJ0LnJ1L3BpY3R1cmVzL3Byb2R1Y3QvYmlnLzk4MDM3ODE3X2JpZy-qcGca.jpg")
            .into(viewHolder.image_preview)
    }
}
