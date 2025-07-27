package com.aluma.laundry.data.logmachine.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LogMachineDAO {
    @Query("SELECT * FROM log_machine")
    fun getAllLogMachine(): Flow<List<LogMachineLocal>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(logMachineLocal: LogMachineLocal)

    @Delete
    suspend fun delete(logMachineLocal: LogMachineLocal)

    @Query("DELETE FROM log_machine")
    suspend fun deleteAll()

    @Query("""
    DELETE FROM log_machine 
    WHERE syncStatus = :status 
      AND date IS NOT NULL 
      AND DATE(date) < DATE(:today)
    """)
    suspend fun deleteOldSyncedLogMachine(today: String, status: SyncStatus = SyncStatus.SYNCED)

    @Update
    suspend fun update(logMachineLocal: LogMachineLocal): Int

    @Query("UPDATE log_machine SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatusOnly(id: String, status: SyncStatus): Int

    @Query("SELECT * FROM log_machine WHERE id = :id LIMIT 1")
    suspend fun getLogMachineById(id: String): LogMachineLocal?

    @Query("SELECT * FROM log_machine WHERE syncStatus IN (:pending, :failed)")
    suspend fun getPendingOrFailedLogMachine(pending: SyncStatus, failed: SyncStatus): List<LogMachineLocal>
}