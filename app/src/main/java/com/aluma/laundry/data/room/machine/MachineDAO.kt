package com.aluma.laundry.data.room.machine

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDAO {
    @Query("SELECT * FROM machines")
    fun getAllMachine(): Flow<List<MachineRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(machineRoom: MachineRoom)

    @Delete
    suspend fun delete(machineRoom: MachineRoom)

    @Update
    suspend fun update(machineRoom: MachineRoom)

    @Query("SELECT * FROM machines WHERE id = :id LIMIT 1")
    suspend fun getMachineById(id: String): MachineRoom?
}