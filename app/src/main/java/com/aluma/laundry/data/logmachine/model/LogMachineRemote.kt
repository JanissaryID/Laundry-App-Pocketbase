package com.aluma.laundry.data.logmachine.model

import kotlinx.serialization.Serializable

@Serializable
data class LogMachineRemote(
    val id: String = "",
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val date: String? = null,
)