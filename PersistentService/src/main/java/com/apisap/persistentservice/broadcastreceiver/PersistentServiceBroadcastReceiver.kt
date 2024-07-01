package com.apisap.persistentservice.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.apisap.persistentservice.workers.PersistentServiceWorker

class PersistentServiceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val workManager = WorkManager.getInstance(it)
            val startServiceRequest: OneTimeWorkRequest =
                OneTimeWorkRequest.Builder(PersistentServiceWorker::class.java)
                    .build()
            workManager.enqueue(startServiceRequest)
        }
    }
}