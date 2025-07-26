package com.aluma.laundry.data.service.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.laundry.data.service.model.ServiceLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDAO {
    @Query("SELECT * FROM services")
    fun getAllService(): Flow<List<ServiceLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(serviceLocal: ServiceLocal)

    @Delete
    suspend fun delete(serviceLocal: ServiceLocal)

    @Query("DELETE FROM services")
    suspend fun deleteAll()

    @Update
    suspend fun update(serviceLocal: ServiceLocal)

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: String): ServiceLocal?
}