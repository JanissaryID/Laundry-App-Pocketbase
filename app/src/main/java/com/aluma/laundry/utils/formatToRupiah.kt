package com.aluma.laundry.utils

import java.text.NumberFormat
import java.util.Locale

fun String?.formatToRupiahRead(): String {
    return try {
        val number = this?.toDoubleOrNull() ?: return "Rp -"
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.format(number).replace(",00", "")
    } catch (e: Exception) {
        "Rp -"
    }
}

fun Long.formatToRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun String.formatToRupiah(): String {
    return this.filter { it.isDigit() }.toLongOrNull()?.formatToRupiah() ?: this
}


