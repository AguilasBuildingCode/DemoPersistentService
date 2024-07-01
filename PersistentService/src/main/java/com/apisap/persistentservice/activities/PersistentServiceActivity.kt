package com.apisap.persistentservice.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.apisap.persistentservice.permissions.PersistentServerPermissions
import com.apisap.persistentservice.workers.PersistentServiceWorker
import java.util.concurrent.TimeUnit

abstract class PersistentServiceActivity : ComponentActivity() {

    protected abstract val uniquePersistentServiceWorkName: String
    private val persistentServerPermissions: PersistentServerPermissions by lazy { PersistentServerPermissions.getInstance() }

    private fun startWorker() {
        val workManager = WorkManager.getInstance(this)
        val request: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(
                PersistentServiceWorker::class.java,
                15,
                TimeUnit.MINUTES
            )
                .build()

        workManager.enqueueUniquePeriodicWork(
            uniquePersistentServiceWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startWorker()
    }

    override fun onStart() {
        super.onStart()
        persistentServerPermissions.requestPermissions(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        persistentServerPermissions.bindRequestPermissionsResult(
            requestCode,
            permissions.toList(),
            grantResults
        )
    }
}