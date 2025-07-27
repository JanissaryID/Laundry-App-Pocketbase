package com.aluma.laundry.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aluma.laundry.data.logmachine.local.LogMachineDAO
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.machine.local.MachineDAO
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.local.OrderDAO
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.service.local.ServiceDAO
import com.aluma.laundry.data.service.model.ServiceLocal
import com.aluma.laundry.utils.SyncStatusConverter

@Database(entities = [OrderLocal::class, MachineLocal::class, ServiceLocal::class, LogMachineLocal::class], version = 2, exportSchema = false)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDAO
    abstract fun orderDao(): OrderDAO
    abstract fun serviceDao(): ServiceDAO
    abstract fun logMachineDao(): LogMachineDAO
}