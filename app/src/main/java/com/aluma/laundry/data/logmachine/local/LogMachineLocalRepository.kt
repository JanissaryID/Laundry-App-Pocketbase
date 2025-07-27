package com.aluma.laundry.data.logmachine.local

import android.util.Log
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset

class LogMachineLocalRepository(private val dao: LogMachineDAO) {
    val logMachine: Flow<List<LogMachineLocal>> = dao.getAllLogMachine()

    suspend fun addLogMachine(logMachineLocal: LogMachineLocal) = dao.insert(logMachineLocal)
    suspend fun deleteLogMachine(logMachineLocal: LogMachineLocal) = dao.delete(logMachineLocal)
    suspend fun deleteAllLogMachines() = dao.deleteAll()
    suspend fun deleteOldSyncedLogMachine() {
        val today = LocalDate.now(ZoneOffset.UTC).toString() // yyyy-MM-dd
        dao.deleteOldSyncedLogMachine(today)
    }
    suspend fun updateLogMachineWithResult(logMachineLocal: LogMachineLocal): Int {
        val result = dao.update(logMachineLocal)
        return result
    }
    suspend fun updateSyncStatusOnly(id: String, syncStatus: SyncStatus): Int {
        Log.d("RepoUpdate", "🔄 Sync only: setting syncStatus of $id to $syncStatus")
        return dao.updateSyncStatusOnly(id, syncStatus)
    }
    suspend fun getLogMachineById(id: String): LogMachineLocal? = dao.getLogMachineById(id)

    suspend fun getPendingOrFailedLogMachines(): List<LogMachineLocal> {
        return dao.getPendingOrFailedLogMachine(SyncStatus.PENDING, SyncStatus.FAILED)
    }
}