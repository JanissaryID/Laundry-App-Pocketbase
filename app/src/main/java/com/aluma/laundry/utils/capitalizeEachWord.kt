package com.aluma.laundry.utils

import java.text.NumberFormat
import java.util.Locale

fun String.capitalizeEachWord(): String =
    split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

fun String.toTitleCase(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun Long.formatWithSeparator(): String {
    val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return format.format(this)
}
