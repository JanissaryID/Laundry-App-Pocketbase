package com.aluma.laundry.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aluma.laundry.data.machine.local.MachineDAO
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.local.OrderDAO
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.utils.SyncStatusConverter

@Database(entities = [OrderLocal::class, MachineLocal::class], version = 1, exportSchema = false)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDAO
    abstract fun orderDao(): OrderDAO
}