package com.apisap.persistentservice.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

/**
 * This abstract class [PersistentServiceConnection] extend of [ServiceConnection], it redefine the [onServiceConnected]
 * with [onPersistentServiceConnected] method, now you can get your service directly in [onPersistentServiceConnected].
 *
 * * Define your service in type params.
 *
 */
abstract class PersistentServiceConnection<S : PersistentService> : ServiceConnection {

    /**
     * This method [onPersistentServiceConnected] give you service in the param [persistentService].
     *
     * @param [name][ComponentName] is your component name.
     * @param [persistentService][S] where [S] is your service. it's defined in type params.
     *
     * @return [Unit]
     *
     */
    abstract fun onPersistentServiceConnected(
        name: ComponentName?,
        persistentService: S?
    )

    /**
     * This method [onServiceConnected] is originally of [ServiceConnection], it's used to get and cast the [IBinder] to your service and then pass it
     * by [onPersistentServiceConnected] method.
     *
     * @param [name][ComponentName] is your component name.
     * @param [service][IBinder] it's the wrapper that contain your service.
     *
     * @return [Unit]
     *
     */
    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? PersistentServiceBinder<*>
        onPersistentServiceConnected(
            name,
            binder?.persistentService as? S
        )
    }
}