package com.aluma.laundry.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepository

class SyncOrderWorkerFactory(
    private val localRepo: OrderLocalRepository,
    private val remoteRepo: OrderRemoteRepository
) : ChildWorkerFactory {
    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker {
        return SyncOrderWorker(appContext, params, localRepo, remoteRepo)
    }
}
