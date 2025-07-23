package com.aluma.laundry.data.machine.remote

import com.aluma.laundry.data.machine.model.MachineRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login

class MachineRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : MachineRemoteRepository {

    private val collection = "Machine"

    override suspend fun fetchRemoteMachines(): List<MachineRemote> {
        val result = client.records.getList<MachineRemote>(
            collection,
            page = 1,
            perPage = 100
        )
        return result.items.reversed()
    }
}