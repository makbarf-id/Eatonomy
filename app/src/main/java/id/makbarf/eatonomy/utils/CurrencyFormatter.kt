package id.makbarf.eatonomy.utils

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {
    private val currencyFormats = mutableMapOf<String, NumberFormat>()

    fun format(amount: Double, currencyCode: String): String {
        val format = currencyFormats.getOrPut(currencyCode) {
            NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(currencyCode)
            }
        }
        return format.format(amount)
    }

    fun parse(amount: String, currencyCode: String): Double {
        val format = currencyFormats.getOrPut(currencyCode) {
            NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(currencyCode)
            }
        }
        return format.parse(amount)?.toDouble() ?: 0.0
    }
} 