package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.model.OrderRemote
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlin.coroutines.cancellation.CancellationException

class SyncOrderWorker(
    context: Context,
    params: WorkerParameters,
    private val localRepository: OrderLocalRepository,
    private val remoteRepository: OrderRemoteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val orders = localRepository.getPendingOrFailedOrders()
            var allSuccess = true
            for (order in orders) {
                try {
                    val remote = OrderRemote(
                        customerName = order.customerName,
                        serviceName = order.serviceName,
                        sizeMachine = order.sizeMachine,
                        typeMachineService = order.typeMachineService,
                        price = order.price,
                        typePayment = order.typePayment,
                        user = order.user,
                        store = order.store
                    )

                    remoteRepository.createOrder(remote)

                    localRepository.updateOrderWithResult(
                        order.copy(syncStatus = SyncStatus.SYNCED)
                    )

                    Log.d("SyncOrderWorker", "✅ Synced order ${order.id}")
                } catch (e: CancellationException) {
                    throw e // penting untuk keluar kalau coroutine dibatalkan
                } catch (e: Exception) {
                    allSuccess = false

                    localRepository.updateOrderWithResult(
                        order.copy(syncStatus = SyncStatus.FAILED)
                    )

                    Log.e("SyncOrderWorker", "❌ Failed to sync order ${order.id}", e)
                }
            }
            if (allSuccess) Result.success()
            else Result.success()
        } catch (e: Exception) {
            Log.e("SyncOrderWorker", "❌ Error syncing order", e)
            Result.retry()
        }
    }
}