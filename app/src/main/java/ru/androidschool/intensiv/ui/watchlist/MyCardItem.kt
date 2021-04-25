package ru.androidschool.intensiv.ui.watchlist

import android.content.Context
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.online_card.view.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.ui.feed.bindData
import ru.androidschool.intensiv.ui.feed.visible
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


class MyCardItem(
    private val number: String,
    private val resourceManager: Context,
    private val balance: Balance?,
    private val onButtonClick: () -> Unit
) : Item() {

    override fun getLayout() = R.layout.online_card

    override fun bind(viewHolder: GroupieViewHolder, position: Int) = bindData(viewHolder) {
        if (balance != null) {
            balance_text.text = balance.getFormattedBalance(resourceManager)
        } else {
            card_unavailable_label.visible()
        }
        val mask = MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDARD)
        val watcher: FormatWatcher = MaskFormatWatcher(mask)
        watcher.installOn(card_title)
        card_title.text = number
        setOnClickListener { onButtonClick.invoke() }
    }
}

data class Balance(
    val rubles: Int = 0,
    val kop: Int = 0,
    val currency: String = ""
) {

    fun getFormattedBalance(resourceManager: Context): String {
        val currency = currency.getCurrencySymbol(resourceManager)
        return if (rubles > 0) {
            formatAsMoney() + " " + currency
        } else {
            ZERO_BALANCE + " " + currency
        }
    }

    private fun formatAsMoney(): String {
        val value = this.rubles.plus(kop.times(0.01))
        return MoneyUtils.formatPrice(value)
            .replace(".", ",")
    }

    companion object {
        private const val ZERO_BALANCE = "0"
    }
}

object CurrencyUtils {
    fun getCurrencySymbol(currency: String, resourceManager: Context): String {
        return when (currency) {
            RUB, RUR -> resourceManager.getString(R.string.currency_symbol_rub)
            USD -> resourceManager.getString(R.string.currency_symbol_usd)
            EUR -> resourceManager.getString(R.string.currency_symbol_eur)
            GBP -> resourceManager.getString(R.string.currency_symbol_gbp)
            else -> currency
        }
    }

    const val RUB = "RUB"
    const val RUR = "RUR"
    const val USD = "USD"
    const val EUR = "EUR"
    const val GBP = "GBP"
}

typealias Currency = String

fun Currency.getCurrencySymbol(resourceManager: Context): String {
    return CurrencyUtils.getCurrencySymbol(this, resourceManager)
}


fun Currency?.orDefault() = this ?: CurrencyUtils.RUB


object MoneyUtils {

    const val PENNY = 100.0

    fun formatPrice(price: Double): String {
        val separator = DecimalFormatSymbols().apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        return DecimalFormat("###,##0.00", separator).format(price).replace(",00", "")
    }

    fun formatPrice(price: String?): String? {
        return price?.replace(",", ".")?.toDoubleOrNull()?.let {
            formatPrice(it)
        } ?: price
    }

    fun convertPenyToRubbles(sum: Double?, default: Double): Double = sum?.div(PENNY) ?: default

}

