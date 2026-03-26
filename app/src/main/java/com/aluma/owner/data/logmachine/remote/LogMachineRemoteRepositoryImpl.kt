package com.aluma.owner.data.logmachine.remote

import android.util.Log
import com.aluma.owner.data.logmachine.model.LogMachineRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LogMachineRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : LogMachineRemoteRepository {

    private val collection = "LaundryLogMachine"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createLogMachine(logMachineRemote: LogMachineRemote) {
        try {
            client.records.create<LogMachineRemote>(
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

    override suspend fun fetchLogMachine(storeID: String, date: LocalDate?): List<LogMachineRemote> {
        val baseFilter = "store=\"$storeID\""

        val dateFilter = date?.let {
            val dateStr = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            "date~\"$dateStr\""
        }

        val combinedFilter = listOfNotNull(baseFilter, dateFilter).joinToString(" && ")

        return client.records.getList<LogMachineRemote>(
            collection,
            page = 1,
            perPage = 200,
            filterBy = Filter(combinedFilter)
        ).items.reversed()
    }
}