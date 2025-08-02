package com.aluma.owner.data.machine.remote

import com.aluma.owner.data.machine.model.MachineRemote

interface MachineRemoteRepository {
    suspend fun fetchRemoteMachines(storeID: String): List<MachineRemote>
    suspend fun updateMachineTimer(machineId: String, newTimer: Int)
}