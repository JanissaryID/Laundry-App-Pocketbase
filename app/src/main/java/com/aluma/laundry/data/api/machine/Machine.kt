package com.aluma.laundry.data.api.machine

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class Machine(
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val inUse: Boolean = false,
) : Record()