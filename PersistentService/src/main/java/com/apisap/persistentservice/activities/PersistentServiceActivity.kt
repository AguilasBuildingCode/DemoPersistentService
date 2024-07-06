package com.apisap.persistentservice.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.apisap.persistentservice.intents.PersistentServiceIntent
import com.apisap.persistentservice.permissions.PersistentServerPermissions
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceActions
import com.apisap.persistentservice.services.PersistentServiceConnection
import javax.inject.Inject

abstract class PersistentServiceActivity<S : PersistentService>(private val clazz: Class<S>) :
    ComponentActivity() {

    @Inject
    lateinit var persistentServerPermissions: PersistentServerPermissions

    protected abstract val persistentServiceConnection: PersistentServiceConnection<S>

    private fun startPersistentServiceForegroundAndUnbind() {
        PersistentServiceIntent(
            this,
            PersistentServiceActions.ON_FOREGROUND,
            clazz
        ).let {
            startService(it)
            unbindService(persistentServiceConnection)
        }
    }

    protected fun startPersistentServiceAndBind() {
        PersistentServiceIntent(
            this,
            PersistentServiceActions.ON,
            clazz
        ).let {
            startService(it)
            bindService(it, persistentServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    protected fun stopPersistentServiceAndUnbind() {
        PersistentServiceIntent(
            this,
            PersistentServiceActions.OFF,
            clazz
        ).let {
            startService(it)
            unbindService(persistentServiceConnection)
        }
    }

    override fun onStart() {
        super.onStart()
        persistentServerPermissions.requestPermissions(this)
        if (PersistentService.isServiceRunning) {
            startPersistentServiceAndBind()
        }
    }

    override fun onStop() {
        super.onStop()
        if (PersistentService.isServiceRunning) {
            startPersistentServiceForegroundAndUnbind()
        }
    }

    @Deprecated("Deprecated in Java")
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