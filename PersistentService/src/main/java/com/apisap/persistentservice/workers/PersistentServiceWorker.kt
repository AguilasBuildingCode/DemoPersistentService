package com.apisap.persistentservice.workers

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceActions

class PersistentServiceWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        if (PersistentService.isServiceRunning) {
            return Result.success()
        }
        return if (context.startService(
                Intent(context, PersistentService::class.java)
                    .apply {
                        action = PersistentServiceActions.ON.name
                    }) != null
        ) Result.success() else Result.failure()
    }
}