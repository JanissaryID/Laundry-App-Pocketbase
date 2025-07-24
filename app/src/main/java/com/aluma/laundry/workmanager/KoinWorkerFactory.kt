package com.aluma.laundry.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class KoinWorkerFactory(
    private val workerMap: Map<Class<out ListenableWorker>, () -> ChildWorkerFactory>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry = workerMap.entries.find {
            Class.forName(workerClassName).isAssignableFrom(it.key)
        }
        val factory = foundEntry?.value ?: return null
        return factory().create(appContext, workerParameters)
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker
}
