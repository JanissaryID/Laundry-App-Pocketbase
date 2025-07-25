package com.aluma.laundry.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class SimpleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("SimpleWorker", "Background task is running at ${System.currentTimeMillis()}")
        // Lakukan tugas di sini, misalnya panggil API atau proses data
        return Result.success()
    }
}