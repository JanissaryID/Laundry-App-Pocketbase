package com.aluma.laundry.di

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aluma.laundry.workmanager.SyncOrderWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, workerModule))
        }

        schedulePeriodicOrderSync(this)
    }

    override val workManagerConfiguration: Configuration
        get() {
            val factory: KoinWorkerFactory = GlobalContext.get().get()
            return Configuration.Builder()
                .setWorkerFactory(factory)
                .build()
        }

    private fun schedulePeriodicOrderSync(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<SyncOrderWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SyncOrderWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        fun enqueueSyncNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncOrderWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncOrderNow",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
