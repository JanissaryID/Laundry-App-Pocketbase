package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.model.IncomeRemote
import com.aluma.laundry.data.income.remote.IncomeRemoteRepository
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.local.toRemoteModel
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.utils.SyncStatus
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val orderRepository: OrderLocalRepository by inject()
    private val orderRemoteRepository: OrderRemoteRepository by inject()
    private val pocketbaseClient: PocketbaseClient by inject()
    private val storePreferences: StorePreferences by inject()
    private val incomeRemoteRepository: IncomeRemoteRepository by inject()

    override suspend fun doWork(): Result {
        // Periksa token dari StorePreferences
        val token = storePreferences.userToken.firstOrNull()
        val storeID = storePreferences.userIdStore.firstOrNull().orEmpty()
        val userID = storePreferences.userIdUser.firstOrNull().orEmpty()
        if (token.isNullOrEmpty()) {
            Log.e("SyncWorker", "🔒 No valid token available, cannot sync")
            return Result.failure()
        }

        // Coba login ke Pocketbase
        try {
            pocketbaseClient.login(token)
            Log.d("SyncWorker", "✅ Login successful with token: $token")
        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Login failed: ${e.message}")
            return Result.retry()
        }

        return try {
            // Ambil order dengan status PENDING atau FAILED
            val pendingOrders = orderRepository.getPendingOrFailedOrders()
            if (pendingOrders.isEmpty()) {
                Log.d("SyncWorker", "No orders to sync")
                return Result.success()
            }

            var hasFailures = false

            // Kirim setiap order ke server
            coroutineScope {
                pendingOrders.map { order ->
                    async {
                        try {
                            delay(200L)
                            Log.d("SyncWorker", "🔄 Syncing order ${order.id}")
                            orderRemoteRepository.createOrder(order.toRemoteModel())
                            orderRepository.updateSyncStatusOnly(order.id, SyncStatus.SYNCED)
                            Log.d("SyncWorker", "✅ Synced order ${order.id}")
                        } catch (e: io.github.agrevster.pocketbaseKotlin.PocketbaseException) {
                            orderRepository.updateSyncStatusOnly(order.id, SyncStatus.FAILED)
                            Log.e("SyncWorker", "❌ Failed to sync order ${order.id}: ${e.message}, Data: ${e.reason}")
                            hasFailures = true
                        } catch (e: Exception) {
                            orderRepository.updateSyncStatusOnly(order.id, SyncStatus.FAILED)
                            Log.e("SyncWorker", "❌ Unexpected error for order ${order.id}: ${e.message}")
                            hasFailures = true
                        }
                    }
                }.awaitAll()

                if (!hasFailures) {
                    val cleanupJob = async {
                        orderRepository.deleteOldSyncedOrders()
                        Log.d("SyncWorker", "✅ Deleted old synced orders")
                    }
                    cleanupJob.await()
                }

                // Group orders by date for income calculation (yyyy-MM-dd)
                val ordersByDate = pendingOrders.groupBy { it.date?.take(10) ?: LocalDate.now().toString() }

                ordersByDate.forEach { (date, orders) ->
                    val dailyTotal = orders.sumOf { it.price?.toIntOrNull() ?: 0 }
                    if (dailyTotal > 0) {
                        try {
                            val existingIncomes = incomeRemoteRepository.getIncomeByDate(storeId = storeID, date = date)
                            if (existingIncomes.isEmpty()) {
                                val incomeRemote = IncomeRemote(
                                    store = storeID,
                                    user = userID,
                                    date = date,
                                    total = dailyTotal.toString()
                                )
                                incomeRemoteRepository.createIncome(incomeRemote)
                                Log.d("SyncWorker", "➕ Created income for $date: Rp$dailyTotal")
                            } else {
                                val currentTotal = existingIncomes[0].total?.toIntOrNull() ?: 0
                                val newTotal = currentTotal + dailyTotal
                                incomeRemoteRepository.updateIncome(incomeId = existingIncomes[0].id.orEmpty(), income = newTotal.toString())
                                Log.d("SyncWorker", "🔄 Updated income for $date: Rp$newTotal (added Rp$dailyTotal)")
                            }
                        } catch (e: Exception) {
                            Log.e("SyncWorker", "❌ Failed to update income for $date", e)
                        }
                    }
                }
            }
            // Jika ada kegagalan, coba lagi nanti
            if (hasFailures) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Unexpected error syncing orders: ${e.message}")
            Result.retry()
        }
    }
}