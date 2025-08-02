package com.aluma.owner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aluma.owner.data.store.local.StoreDAO
import com.aluma.owner.data.store.model.StoreLocal

@Database(entities = [
    StoreLocal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDAO
}