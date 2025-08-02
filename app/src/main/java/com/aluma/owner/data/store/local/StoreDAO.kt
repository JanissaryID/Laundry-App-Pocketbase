package com.aluma.owner.data.store.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.owner.data.store.model.StoreLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDAO {
    @Query("SELECT * FROM store")
    fun getAllStores(): Flow<List<StoreLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(storeLocal: StoreLocal)

    @Delete
    suspend fun delete(storeLocal: StoreLocal)

    @Query("DELETE FROM store")
    suspend fun deleteAll()

    @Update
    suspend fun update(storeLocal: StoreLocal)

    @Query("SELECT * FROM store WHERE id = :id LIMIT 1")
    suspend fun getStoresById(id: String): StoreLocal?
}