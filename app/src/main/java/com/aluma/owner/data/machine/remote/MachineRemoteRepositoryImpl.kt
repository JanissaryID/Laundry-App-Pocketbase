package com.aluma.owner.data.machine.remote

import android.util.Log
import com.aluma.owner.data.machine.model.MachineRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class MachineRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : MachineRemoteRepository {

    private val collection = "Machine"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchRemoteMachines(storeID: String): List<MachineRemote> {
        val result = client.records.getList<MachineRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"${storeID}\"")
        )
        return result.items.reversed()
    }

    override suspend fun updateMachineTimer(machineId: String, newTimer: Int) {
        try {
            val body = json.encodeToString(
                MachineRemote.serializer(),
                MachineRemote(timer = newTimer)
            )

            client.records.update<MachineRemote>(
                sub = collection,
                id = machineId,
                body = body
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("MachineRepo", "❌ Update Machine Timer failed", e)
            throw e
        }
    }
}