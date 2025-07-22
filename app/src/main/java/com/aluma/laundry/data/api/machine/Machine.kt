package com.aluma.laundry.data.api.machine

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Machine(
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val order: String? = null,
    val bluetoothAddress: String? = null,
    val inUse: Boolean = false,
    val timer: Int = 1,
    val timeOn: String? = null
) : Record()