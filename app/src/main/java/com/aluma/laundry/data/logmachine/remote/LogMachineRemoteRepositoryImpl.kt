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
        val body = json.encodeToString(logMachineRemote)
        try {
            client.records.create<Record>(collection, body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Create Log Machine failed, attempting update", e)
            try {
                if (logMachineRemote.id.isNotEmpty()) {
                    client.records.update<Record>(
                        sub = collection,
                        id = logMachineRemote.id,
                        body = body
                    )
                    Log.d("OrderRepository", "✅ Successfully updated existing log machine ${logMachineRemote.id}")
                } else {
                    throw e
                }
            } catch (updateE: Exception) {
                Log.e("OrderRepository", "❌ Update Log Machine failed as well", updateE)
                throw e
            }
        }
    }
}