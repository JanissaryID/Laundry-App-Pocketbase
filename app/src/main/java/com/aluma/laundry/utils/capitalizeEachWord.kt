package com.aluma.laundry.utils

fun String.capitalizeEachWord(): String =
    split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
