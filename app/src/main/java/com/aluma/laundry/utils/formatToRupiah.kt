package com.aluma.laundry.utils

import java.text.NumberFormat
import java.util.Locale

private val indonesiaLocale: Locale = Locale.forLanguageTag("id-ID")

fun Long.formatToRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(indonesiaLocale)
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun Int.formatToRupiah(): String {
    return this.toLong().formatToRupiah()
}

fun String.formatToRupiah(): String {
    return this.filter { it.isDigit() }.toLongOrNull()?.formatToRupiah() ?: this
}



