package com.aluma.laundry.data.income.remote

import android.util.Log
import com.aluma.laundry.data.income.model.IncomeRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class IncomeRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : IncomeRemoteRepository {

    private val collection = "LaundryIncome"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createIncome(income: IncomeRemote) {
        try {
            client.records.create<IncomeRemote>(
                collection,
                json.encodeToString(income)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("IncomeRepository", "❌ Create Income failed", e)
            throw e
        }
    }

    override suspend fun updateIncome(incomeId: String, income: String) {
        try {
            val body = json.encodeToString(
                IncomeRemote.serializer(),
                IncomeRemote(total = income)
            )

            client.records.update<IncomeRemote>(
                sub = collection,
                id = incomeId,
                body = body
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("IncomeRepository", "❌ Update Income failed", e)
            throw e
        }
    }

    override suspend fun getIncomeByDate(storeId: String, date: String): List<IncomeRemote> {
        return client.records.getList<IncomeRemote>(
            sub = collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"$storeId\" && date~\"$date\"")
        ).items
    }


}