package com.aluma.laundry.data.logmachine.remote

import android.util.Log
import com.aluma.laundry.data.logmachine.model.LogMachineRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class LogMachineRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : LogMachineRemoteRepository {

    private val collection = "LaundryLogMachine"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createLogMachine(logMachineRemote: LogMachineRemote) {
        try {
            client.records.create<Record>(
                collection,
                json.encodeToString(logMachineRemote)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Create Log Machine failed", e)
            throw e
        }
    }
}