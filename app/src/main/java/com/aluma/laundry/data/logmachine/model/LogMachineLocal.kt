package com.aluma.laundry.data.logmachine.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.utils.IdGenerator

@Entity(tableName = "log_machine")
data class LogMachineLocal(
    @PrimaryKey val id: String = IdGenerator.generateId(),
    val numberMachine: Int = 0,
    val typeMachine: Boolean = false,
    val sizeMachine: Boolean = false,
    val user: String? = null,
    val store: String? = null,
    val date: String? = null,
    @ColumnInfo(defaultValue = "PENDING")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)