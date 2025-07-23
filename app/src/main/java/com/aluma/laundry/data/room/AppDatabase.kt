package com.aluma.laundry.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aluma.laundry.data.room.machine.MachineDAO
import com.aluma.laundry.data.room.machine.MachineRoom
import com.aluma.laundry.data.room.order.OrderDAO
import com.aluma.laundry.data.room.order.OrderRoom

@Database(entities = [OrderRoom::class, MachineRoom::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDAO
    abstract fun orderDao(): OrderDAO
}