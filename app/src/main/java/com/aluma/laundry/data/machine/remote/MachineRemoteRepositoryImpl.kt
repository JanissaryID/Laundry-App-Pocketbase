package com.aluma.laundry.data.machine.remote

import com.aluma.laundry.data.machine.model.MachineRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter

class MachineRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : MachineRemoteRepository {

    private val collection = "LaundryMachine"

    override suspend fun fetchRemoteMachines(storeID: String): List<MachineRemote> {
        val result = client.records.getList<MachineRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"${storeID}\"")
        )
        return result.items.reversed()
    }
}