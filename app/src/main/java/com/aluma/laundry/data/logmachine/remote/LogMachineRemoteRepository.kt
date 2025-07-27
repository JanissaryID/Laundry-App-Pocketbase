package com.aluma.laundry.data.logmachine.remote

import com.aluma.laundry.data.logmachine.model.LogMachineRemote

interface LogMachineRemoteRepository {
    suspend fun createLogMachine(logMachineRemote: LogMachineRemote)
}