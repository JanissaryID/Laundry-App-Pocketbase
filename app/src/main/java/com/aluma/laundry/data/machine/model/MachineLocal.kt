package com.aluma.laundry.data.machine.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineLocal(
    @PrimaryKey val id: String,
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val order: String? = null,
    val bluetoothAddress: String? = null,
    val inUse: Boolean = false,
    val timer: Int = 1,
    val timeOn: String? = null,
    val needsVerification: Boolean = false
)