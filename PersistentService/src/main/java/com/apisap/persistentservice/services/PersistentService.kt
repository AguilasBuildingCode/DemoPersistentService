package com.apisap.persistentservice.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apisap.persistentservice.activities.PersistentServiceActivity
import com.apisap.persistentservice.intents.PersistentServiceIntent
import com.apisap.persistentservice.permissions.BasePermissions
import com.apisap.persistentservice.permissions.BasePermissions.RequestStatus.Companion.arePermissionsOK
import com.apisap.persistentservice.permissions.PersistentServerPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class PersistentService : Service() {
    companion object {
        var isBound: Boolean = false
        var isServiceRunning: Boolean = false
        var isServiceForeground: Boolean = false
    }

    @Inject
    lateinit var persistentServerPermissions: PersistentServerPermissions

    private var stoppedServiceCallback: (() -> Unit)? = null
    private var binder: PersistentServiceBinder<PersistentService>? = null

    private var postNotificationPermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN
    private var foregroundPermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN
    private var foregroundSpecialUsePermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN

    protected abstract val notificationId: Int
    protected abstract val notificationChannelId: String
    protected abstract fun getNotification(): Notification

    protected open fun baseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSilent(true)
    }

    protected inline fun <reified S : PersistentService> getPersistentServiceOffPendingIntent(): PendingIntent {
        val startIntent = PersistentServiceIntent(this, PersistentServiceActions.OFF, S::class.java)
        return PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    protected inline fun <reified A : PersistentServiceActivity> getPersistentServiceActivityOpenPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, A::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("NewApi")
    open fun startPersistentService() {
        isServiceForeground = true
        if (arePermissionsOK(
                listOf(
                    postNotificationPermissionRequestStatus,
                    foregroundSpecialUsePermissionRequestStatus
                )
            )
        ) {
            startForeground(
                notificationId,
                getNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
            return
        }

        if (arePermissionsOK(
                listOf(
                    postNotificationPermissionRequestStatus,
                    foregroundPermissionRequestStatus
                )
            )
        ) {
            startForeground(
                notificationId,
                getNotification()
            )
            return
        }

        isServiceForeground = false
    }

    open fun onStoppedService(stoppedServiceCallback: () -> Unit) {
        this.stoppedServiceCallback = stoppedServiceCallback
    }

    open fun stopForeground() {
        isServiceForeground = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    open fun stopPersistentService() {
        isServiceRunning = false
        stopSelf()
        stopForeground()
        stoppedServiceCallback?.let { it() }
        stoppedServiceCallback = null
    }

    @SuppressLint("MissingPermission")
    protected fun updateNotification(notification: Notification) {
        if (!isServiceForeground) {
            return
        }
        with(NotificationManagerCompat.from(this)) {
            notify(
                notificationId, notification
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        for ((permission, status) in persistentServerPermissions.checkPermissionsStatus(this).entries) {
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    postNotificationPermissionRequestStatus = status
                }

                Manifest.permission.FOREGROUND_SERVICE -> {
                    foregroundPermissionRequestStatus = status
                }

                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE -> {
                    foregroundSpecialUsePermissionRequestStatus = status
                }
            }
        }
        if (intent != null) {
            when (intent.action) {
                PersistentServiceActions.ON_FOREGROUND.name -> startPersistentService()
                PersistentServiceActions.ON.name -> stopForeground()
                PersistentServiceActions.OFF.name -> stopPersistentService()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        binder = PersistentServiceBinder(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        isBound = true
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        isBound = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        if (!isServiceForeground) {
            stopPersistentService()
        }
        stoppedServiceCallback = null
        return true
    }

    override fun onDestroy() {
        isServiceRunning = false
        stopForeground()
        stoppedServiceCallback?.let { it() }
        stoppedServiceCallback = null
        binder = null
        super.onDestroy()
    }
}