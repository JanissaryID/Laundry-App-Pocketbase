package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.model.OrderRemote
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.utils.SyncStatus

class SyncOrderWorker(
    context: Context,
    params: WorkerParameters,
    private val localRepository: OrderLocalRepository,
    private val remoteRepository: OrderRemoteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val orders = localRepository.getPendingOrFailedOrders()
            for (order in orders) {
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

                localRepository.updateOrder(
                    order.copy(syncStatus = SyncStatus.SYNCED)
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncOrderWorker", "❌ Error syncing order", e)
            Result.retry()
        }
    }
}