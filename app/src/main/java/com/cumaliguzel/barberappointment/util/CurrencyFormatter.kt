package com.cumaliguzel.barberappointment.util

object CurrencyFormatter {
    fun formatPrice(price: Double): String {
        return "₺${String.format("%.2f", price)}"
    }

    fun formatPriceWithSpace(price: Double): String {
        return "${String.format("%.2f", price)} TL"
    }
} 