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

    private val collection = "Income"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchRemoteIncome(date: String): List<IncomeRemote> {
        return try {
            client.records.getList<IncomeRemote>(
                collection,
                page = 1,
                perPage = 100,
                filterBy = Filter("date~\"$date\"")
            ).items.reversed()
        } catch (e: Exception) {
            Log.e("IncomeRepo", "❌ fetchRemoteIncome failed", e)
            emptyList()
        }
    }

    override suspend fun createIncome(income: IncomeRemote) {
        try {
            val body = json.encodeToString(IncomeRemote.serializer(), income)
            client.records.create<IncomeRemote>(collection, body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("IncomeRepo", "❌ createIncome failed", e)
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
            Log.e("IncomeRepo", "❌ updateIncome failed", e)
            throw e
        }
    }

    override suspend fun getIncomeByDate(storeId: String, date: String): List<IncomeRemote> {
        return try {
            client.records.getList<IncomeRemote>(
                sub = collection,
                page = 1,
                perPage = 100,
                filterBy = Filter("store=\"$storeId\" && date~\"$date\"")
            ).items
        } catch (e: Exception) {
            Log.e("IncomeRepo", "❌ getIncomeByDate failed", e)
            emptyList()
        }
    }

    override suspend fun getIncomeByStore(storeId: String): List<IncomeRemote> {
        return try {
            Log.d("IncomeRepo", "🚀 Fetching income for storeId=$storeId")

            val result = client.records.getList<IncomeRemote>(
                sub = collection,
                page = 1,
                perPage = 400,
                filterBy = Filter("store='$storeId'")
            )

            Log.d("IncomeRepo", "✅ Total income fetched: ${result.items.size}")
            result.items
        } catch (e: Exception) {
            Log.e("IncomeRepo", "❌ getIncomeByStore failed", e)
            emptyList()
        }
    }

}
