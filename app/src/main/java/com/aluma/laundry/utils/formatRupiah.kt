package com.aluma.laundry.utils


import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(value: String?): String {
    val number = value?.toLongOrNull() ?: return "-"
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${formatter.format(number)}"
}