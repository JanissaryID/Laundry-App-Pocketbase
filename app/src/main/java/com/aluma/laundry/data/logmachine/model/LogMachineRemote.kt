package com.aluma.laundry.data.logmachine.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class LogMachineRemote(
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val date: String? = null,
) : Record()