package com.apisap.persistentservice.activities

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.apisap.persistentservice.intents.PersistentServiceIntent
import com.apisap.persistentservice.permissions.PersistentServicePermissions
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceActions
import com.apisap.persistentservice.services.PersistentServiceConnection

/**
 * This abstract class [PersistentServiceActivity] extend of [ComponentActivity], it expose functionality to
 * start, stop, bind and/or unbind your service, also, it request the permissions required to run service in
 * foreground mode at [onStart] cycle.
 *
 * Remember define as type parameter your [PersistentService] and your [PersistentServicePermissions], is necessary create a new
 * service that extend of [PersistentService] and for your [PersistentServicePermissions] can be the same.
 *
 * @param [clazz][Class] define your service class.
 *
 * @property [persistentServerPermissions][P] when [P] can be [PersistentServicePermissions] or other class
 * that extend of [PersistentServicePermissions], useful if you need add more permissions, you can define it as type parameter.
 *
 * @property [persistentServiceConnection][PersistentServiceConnection] useful to bind with your service and get information
 * about of your logic, by default, the service is bind at [onStart] and it's unbind at [onStop] cycles.
 *
 */
abstract class PersistentServiceActivity<S : PersistentService, P : PersistentServicePermissions>(
    private val clazz: Class<S>
) :
    ComponentActivity() {

    protected abstract val persistentServerPermissions: P
    protected abstract val persistentServiceConnection: PersistentServiceConnection<S>

    /**
     * This method [startPersistentServiceForegroundAndUnbind] start service as foreground mode and unbind
     * it, this combination are made for [onStop] cycle, when the user leave the app, the process now run in
     * foreground mode.
     *
     * @return [Unit]
     *
     */
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

    /**
     * This method [startPersistentServiceAndBind] start and bind the service without foreground mode, this is used to run at
     * first time or to pass from foreground to bound at [onStart] cycle.
     *
     *  * @return [Unit]
     *
     */
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

    /**
     * This method [stopPersistentServiceAndUnbind] stop and unbind service, this method is used by own criteria.
     *
     *  * @return [Unit]
     *
     */
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

    /**
     * This method [unbindPersistentService] stop and unbind service, this method is used by own criteria.
     *
     *  * @return [Unit]
     *
     */
    protected fun unbindPersistentService() {
        Intent(
            this,
            clazz
        ).let {
            unbindService(persistentServiceConnection)
        }
    }

    override fun onStart() {
        super.onStart()
        persistentServerPermissions.requestPermissions(this)
        if (PersistentService.isRunning()) {
            startPersistentServiceAndBind()
        }
    }

    override fun onStop() {
        super.onStop()
        if (PersistentService.isNotRunning() && PersistentService.isBound()) {
            unbindPersistentService()
        }

        if (PersistentService.isRunning()) {
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