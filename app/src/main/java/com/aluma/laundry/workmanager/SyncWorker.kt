package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.datastore.StorePreferences
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

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val orderRepository: OrderLocalRepository by inject()
    private val orderRemoteRepository: OrderRemoteRepository by inject()
    private val pocketbaseClient: PocketbaseClient by inject()
    private val storePreferences: StorePreferences by inject()

    override suspend fun doWork(): Result {
        // Periksa token dari StorePreferences
        val token = storePreferences.userToken.firstOrNull()
        if (token.isNullOrEmpty()) {
            Log.e("SyncWorker", "🔒 No valid token available, cannot sync")
            return Result.retry()
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
            }
            // Jika ada kegagalan, coba lagi nanti
            if (hasFailures) {
                Result.retry()
            } else {
                orderRepository.deleteOldSyncedOrders()
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Unexpected error syncing orders: ${e.message}")
            Result.retry()
        }
    }
}