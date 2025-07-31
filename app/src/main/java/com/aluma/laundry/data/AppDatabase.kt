package com.aluma.laundry.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aluma.laundry.data.store.local.StoreDAO
import com.aluma.laundry.data.store.model.StoreLocal

@Database(entities = [
    StoreLocal::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDAO
}