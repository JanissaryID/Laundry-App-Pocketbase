package com.aluma.laundry.di

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aluma.laundry.workmanager.SyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class App : Application(){

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, workManagerModule))
        }

        val workManager = WorkManager.getInstance(this)
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PeriodicTask", // Nama unik untuk pekerjaan
            ExistingPeriodicWorkPolicy.KEEP, // Kebijakan untuk menangani pekerjaan yang sudah ada
            workRequest // Request yang akan dijalankan
        )
    }
}
