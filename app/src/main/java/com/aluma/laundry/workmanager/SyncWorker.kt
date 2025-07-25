package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.local.toRemoteModel
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.utils.SyncStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val orderRepository: OrderLocalRepository by inject()
    private val orderRemoteRepository: OrderRemoteRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            // Ambil order dengan status PENDING atau FAILED
            val pendingOrders = orderRepository.getPendingOrFailedOrders()
            if (pendingOrders.isEmpty()) {
                Log.d("SyncWorker", "No orders to sync")
                return Result.success()
            }

            var hasFailures = false

            // Kirim setiap order ke server
            for (order in pendingOrders) {
                try {
                    orderRemoteRepository.createOrder(order.toRemoteModel())
                    orderRepository.updateSyncStatusOnly(order.id, SyncStatus.SYNCED)
                    Log.d("SyncWorker", "✅ Synced order ${order.id}")
                } catch (e: Exception) {
                    orderRepository.updateSyncStatusOnly(order.id, SyncStatus.FAILED)
                    Log.e("SyncWorker", "❌ Failed to sync order ${order.id}: ${e.message}")
                    hasFailures = true // Tandai bahwa ada kegagalan
                }
            }

            // Jika ada kegagalan, kembalikan Result.retry() untuk mencoba lagi nanti
            if (hasFailures) Result.retry() else Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing orders: ${e.message}")
            Result.retry() // Coba lagi jika terjadi error di luar loop
        }
    }
}