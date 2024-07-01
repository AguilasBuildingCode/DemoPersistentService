package com.apisap.persistentservice.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apisap.persistentservice.activities.PersistentServiceActivity
import com.apisap.persistentservice.broadcastreceiver.PersistentServiceBroadcastReceiver
import com.apisap.persistentservice.intents.PersistentServiceIntent
import com.apisap.persistentservice.permissions.BasePermissions
import com.apisap.persistentservice.permissions.BasePermissions.RequestStatus.Companion.arePermissionsOK
import com.apisap.persistentservice.permissions.PersistentServerPermissions

abstract class PersistentService : Service() {
    companion object {
        var isBound: Boolean = false
        var isServiceRunning: Boolean = false
        var isServiceForeground: Boolean = false
        var isServicePersistenceOn: Boolean = false
    }

    private var stoppedServiceCallback: (() -> Unit)? = null
    private lateinit var binder: PersistentServiceBinder<PersistentService>
    private val persistentServerPermissions: PersistentServerPermissions by lazy { PersistentServerPermissions.getInstance() }

    private var postNotificationPermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN
    private var foregroundPermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN
    private var foregroundSpecialUsePermissionRequestStatus: BasePermissions.RequestStatus =
        BasePermissions.RequestStatus.UNKNOWN

    protected abstract val notificationId: Int
    protected abstract val notificationChannelId: String
    protected abstract fun getNotification(): Notification
    protected abstract fun getTurnOnPersistentServicePendingIntent(): PendingIntent

    protected open fun baseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
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
        isServiceRunning = true
        isServiceForeground = true
        isServicePersistenceOn = true
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
        isServicePersistenceOn = false
    }

    open fun onStoppedService(stoppedServiceCallback: () -> Unit) {
        this.stoppedServiceCallback = stoppedServiceCallback
    }

    open fun stopPersistentService() {
        isServicePersistenceOn = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isServiceRunning = false
        stoppedServiceCallback?.let { it() }
        stoppedServiceCallback = null
    }

    @SuppressLint("MissingPermission")
    protected fun updateNotification(notification: Notification) {
        with(NotificationManagerCompat.from(this)) {
            notify(
                notificationId, notification
            )
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
        when (intent.action) {
            PersistentServiceActions.ON.name -> startPersistentService()
            PersistentServiceActions.OFF.name -> stopPersistentService()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        binder = PersistentServiceBinder(this)
    }

    override fun onBind(intent: Intent): IBinder {
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
            stopSelf()
        }
        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!(isServiceForeground && isServicePersistenceOn)) {
            return
        }

        (getSystemService(ALARM_SERVICE) as AlarmManager).setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            300000L,
            getTurnOnPersistentServicePendingIntent()
        )
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        isServiceRunning = false
        if (isServicePersistenceOn) {
            sendBroadcast(
                Intent(
                    this,
                    PersistentServiceBroadcastReceiver::class.java
                )
            )
        }
        stoppedServiceCallback?.let { it() }
        stoppedServiceCallback = null
        super.onDestroy()
    }
}