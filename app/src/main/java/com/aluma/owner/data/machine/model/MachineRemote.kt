package com.aluma.owner.data.machine.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class MachineRemote(
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val bluetoothAddress: String? = null,
    val timer: Int = 1,
) : Record()