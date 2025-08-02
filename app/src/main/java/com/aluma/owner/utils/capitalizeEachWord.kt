package com.aluma.owner.utils

import java.text.NumberFormat
import java.util.Locale

private val indonesiaLocale: Locale = Locale.forLanguageTag("id-ID")

fun String.toTitleCase(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun Long.formatWithSeparator(): String {
    val format = NumberFormat.getNumberInstance(indonesiaLocale)
    return format.format(this)
}
