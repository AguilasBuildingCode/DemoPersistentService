package com.apisap.persistentservice.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.apisap.persistentservice.intents.PersistentServiceIntent
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceActions
import com.apisap.persistentservice.services.PersistentServiceConnection

abstract class PersistentServiceViewModel<S : PersistentService> : ViewModel(),
    DefaultLifecycleObserver {
    protected abstract val persistentServiceConnection: PersistentServiceConnection<S>

    protected inline fun <reified S : PersistentService> startPersistentService(activity: Activity) {
        PersistentServiceIntent(
            activity,
            PersistentServiceActions.ON,
            S::class.java
        ).let {
            if (!PersistentService.isServiceRunning) {
                activity.startService(it)
            }
            if (!PersistentService.isBound) {
                activity.bindService(it, persistentServiceConnection, Context.BIND_AUTO_CREATE)

            }
        }
    }

    protected inline fun <reified S : PersistentService> stopPersistentService(activity: Activity) {
        PersistentServiceIntent(
            activity,
            PersistentServiceActions.OFF,
            S::class.java
        ).let {
            if (PersistentService.isServiceRunning) {
                activity.startService(it)
            }
            if (PersistentService.isBound) {
                activity.unbindService(persistentServiceConnection)
            }
        }
    }

    protected inline fun <reified S : PersistentService> bindPersistentService(activity: Activity) {
        if (PersistentService.isServiceRunning && !PersistentService.isBound) {
            Intent(
                activity,
                S::class.java
            ).let {
                activity.bindService(it, persistentServiceConnection, Context.BIND_AUTO_CREATE)
            }

        }
    }

    protected inline fun <reified S : PersistentService> unBindPersistentService(activity: Activity) {
        if (PersistentService.isServiceRunning && PersistentService.isBound) {
            Intent(
                activity,
                S::class.java
            ).let {
                activity.bindService(it, persistentServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }
}