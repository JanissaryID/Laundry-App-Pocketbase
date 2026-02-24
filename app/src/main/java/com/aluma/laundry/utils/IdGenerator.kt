package com.aluma.laundry.utils

import kotlin.random.Random

object IdGenerator {
    private val charPool: List<Char> = ('a'..'z') + ('0'..'9')

    fun generateId(length: Int = 15): String {
        return (1..length)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
    }
}
