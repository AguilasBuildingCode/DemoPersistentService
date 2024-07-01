package com.apisap.persistentservice.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

abstract class PersistentServiceConnection<S : PersistentService> : ServiceConnection {

    abstract fun onPersistentServiceConnected(
        name: ComponentName?,
        persistentService: S
    )

    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        service?.let { safeBinder ->
            val binder = safeBinder as PersistentServiceBinder<*>
            (binder.persistentService as? S)?.let { safePersistentService ->
                onPersistentServiceConnected(
                    name,
                    safePersistentService
                )
            }
        }
    }
}