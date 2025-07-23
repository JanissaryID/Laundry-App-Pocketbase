package com.aluma.laundry.utils

import androidx.room.TypeConverter
import com.aluma.laundry.data.order.utils.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.value

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus =
        SyncStatus.entries.firstOrNull { it.value == value } ?: SyncStatus.PENDING
}
