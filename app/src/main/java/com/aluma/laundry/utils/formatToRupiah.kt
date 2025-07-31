package com.aluma.laundry.utils

import java.text.NumberFormat
import java.util.Locale

fun Long.formatToRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun String.formatToRupiah(): String {
    return this.filter { it.isDigit() }.toLongOrNull()?.formatToRupiah() ?: this
}

fun Int.formatRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(this).replace(",00", "").replace("Rp", "Rp.")
}



