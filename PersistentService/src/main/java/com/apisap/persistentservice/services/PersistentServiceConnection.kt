package com.apisap.persistentservice.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

abstract class PersistentServiceConnection<S : PersistentService> : ServiceConnection {

    abstract fun onPersistentServiceConnected(
        name: ComponentName?,
        persistentService: S?
    )

    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? PersistentServiceBinder<*>
        onPersistentServiceConnected(
            name,
            binder?.persistentService as? S
        )
    }
}