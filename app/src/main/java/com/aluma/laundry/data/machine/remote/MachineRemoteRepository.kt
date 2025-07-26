package com.aluma.laundry.data.machine.remote

import com.aluma.laundry.data.machine.model.MachineRemote

interface MachineRemoteRepository {
    suspend fun fetchRemoteMachines(storeID: String): List<MachineRemote>
}