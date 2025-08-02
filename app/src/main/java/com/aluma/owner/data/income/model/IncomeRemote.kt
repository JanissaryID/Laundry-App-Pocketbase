package com.aluma.owner.data.income.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class IncomeRemote(
    val total: String? = null,
    val date: String? = null,
    val user: String? = null,
    val store: String? = null,
) : Record()