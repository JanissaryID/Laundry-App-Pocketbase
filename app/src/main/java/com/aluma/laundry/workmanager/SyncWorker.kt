package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.model.IncomeRemote
import com.aluma.laundry.data.income.remote.IncomeRemoteRepository
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.logmachine.local.LogMachineLocalRepository
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.local.toRemoteModel
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
    private val logMachineLocalRepository: LogMachineLocalRepository by inject()
    private val logMachineRemoteRepository: LogMachineRemoteRepository by inject()

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
            val pendingOrders = orderRepository.getPendingOrFailedOrders()
            val pendingLogs = logMachineLocalRepository.getPendingOrFailedLogMachines()

            if (pendingOrders.isEmpty() && pendingLogs.isEmpty()) {
                Log.d("SyncWorker", "No orders or logs to sync")
                return Result.success()
            }

            var hasFailures = false
            val successfullySyncedOrders = mutableListOf<com.aluma.laundry.data.order.model.OrderLocal>()

            coroutineScope {
                // 1. Sync Log Machines
                pendingLogs.forEach { log ->
                    try {
                        logMachineRemoteRepository.createLogMachine(log.toRemoteModel())
                        logMachineLocalRepository.updateSyncStatusOnly(log.id, SyncStatus.SYNCED)
                        Log.d("SyncWorker", "✅ Synced Log Machine ${log.id}")
                    } catch (e: Exception) {
                        logMachineLocalRepository.updateSyncStatusOnly(log.id, SyncStatus.FAILED)
                        Log.e("SyncWorker", "❌ Failed to sync Log Machine ${log.id}: ${e.message}")
                        hasFailures = true
                    }
                }

                // 2. Sync Orders
                pendingOrders.map { order ->
                    async {
                        try {
                            delay(100L)
                            orderRemoteRepository.createOrder(order.toRemoteModel())
                            orderRepository.updateSyncStatusOnly(order.id, SyncStatus.SYNCED)
                            synchronized(successfullySyncedOrders) {
                                successfullySyncedOrders.add(order)
                            }
                            Log.d("SyncWorker", "✅ Synced order ${order.id}")
                        } catch (e: Exception) {
                            orderRepository.updateSyncStatusOnly(order.id, SyncStatus.FAILED)
                            Log.e("SyncWorker", "❌ Failed to sync order ${order.id}: ${e.message}")
                            hasFailures = true
                        }
                    }
                }.awaitAll()

                // 3. Process Income for successfully synced orders ONLY
                if (successfullySyncedOrders.isNotEmpty()) {
                    val ordersByDate = successfullySyncedOrders.groupBy { it.date?.take(10) ?: LocalDate.now().toString() }
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
                                    incomeRemoteRepository.updateIncome(existingIncomes[0].id.orEmpty(), newTotal.toString())
                                    Log.d("SyncWorker", "🔄 Updated income for $date: Rp$newTotal (added Rp$dailyTotal)")
                                }
                            } catch (e: Exception) {
                                Log.e("SyncWorker", "❌ Failed to update income for $date: ${e.message}")
                                hasFailures = true
                            }
                        }
                    }
                }

                // 4. Cleanup
                if (!hasFailures) {
                    orderRepository.deleteOldSyncedOrders()
                    logMachineLocalRepository.deleteOldSyncedLogMachine()
                    Log.d("SyncWorker", "✅ Cleanup completed")
                }
            }

            if (hasFailures) Result.retry() else Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Unexpected error during doWork: ${e.message}")
            Result.retry()
        }
    }
}