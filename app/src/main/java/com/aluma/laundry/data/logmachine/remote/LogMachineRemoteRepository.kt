package com.aluma.laundry.data.logmachine.remote

import com.aluma.laundry.data.logmachine.model.LogMachineRemote
import java.time.LocalDate

interface LogMachineRemoteRepository {
    suspend fun createLogMachine(logMachineRemote: LogMachineRemote)
    suspend fun fetchLogMachine(storeID: String, date: LocalDate? = null): List<LogMachineRemote>
}