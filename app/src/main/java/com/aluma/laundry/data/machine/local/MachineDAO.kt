package com.aluma.laundry.data.machine.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.laundry.data.machine.model.MachineLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDAO {
    @Query("SELECT * FROM machines")
    fun getAllMachine(): Flow<List<MachineLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(machineLocal: MachineLocal)

    @Delete
    suspend fun delete(machineLocal: MachineLocal)

    @Update
    suspend fun update(machineLocal: MachineLocal)

    @Query("SELECT * FROM machines WHERE id = :id LIMIT 1")
    suspend fun getMachineById(id: String): MachineLocal?
}