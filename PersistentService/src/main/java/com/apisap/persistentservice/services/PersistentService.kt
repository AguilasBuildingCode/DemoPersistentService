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
import com.apisap.persistentservice.permissions.PersistentServicePermissions

/**
 * This [PersistentService] extend of [Service], it's made to handle foreground logic, only extend, and override
 * necessary and now your service can be foreground service.
 *
 * It can be in three status:
 *  * Start in foreground: with Intent action [PersistentServiceActions.ON_FOREGROUND]
 *  * Start without foreground: with Intent action [PersistentServiceActions.ON]
 *  * Stop (don't care if is running foreground or not): with Intent action [PersistentServiceActions.OFF]
 *
 * To run the service use [PersistentServiceIntent] or extend of [PersistentServiceActivity] into your activity
 * to get the logic to handle the service.
 *
 * @property [isServiceBound] handle in the lifecycle to expose if this service is bound or not.
 * @property [isServiceRunning] handle in the lifecycle to expose if this service is running or not.
 * @property [isServiceForeground] handle in the lifecycle to expose if this service is in foreground mode or not.
 * @property [persistentServicePermissions] this property is an instance of [PersistentServicePermissions], it allows
 * get current permissions status.
 * @property [stoppedServiceCallback] this property is a callback to notify when the service is stopped, you can set it with method [onStoppedService],
 * to do it, bind your activity with the service, extend of [PersistentServiceActivity] into your Activity to handle it automatically.
 * @property [binder] to permit bind service with your activity.
 * @property [postNotificationPermissionRequestStatus] handle current post notification permission [Manifest.permission.POST_NOTIFICATIONS].
 * @property [foregroundPermissionRequestStatus] handle current foreground permission [Manifest.permission.FOREGROUND_SERVICE].
 * @property [foregroundSpecialUsePermissionRequestStatus] handle current foreground with special use permission [Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE].
 * @property [notificationId] override this property to define your notification id.
 * @property [notificationChannelId] override this property to define your notification channel id.
 *
 */
abstract class PersistentService : Service() {
    companion object {
        protected var isServiceBound: Boolean = false
        protected var isServiceRunning: Boolean = false
        protected var isServiceForeground: Boolean = false

        fun isBound(): Boolean {
            return isServiceBound
        }

        fun isNotBound(): Boolean {
            return !isBound()
        }

        fun isRunning(): Boolean {
            return isServiceRunning
        }

        fun isNotRunning(): Boolean {
            return !isRunning()
        }

        fun isForeground(): Boolean {
            return isServiceForeground
        }
    }

    private val persistentServicePermissions: PersistentServicePermissions =
        PersistentServicePermissions.getInstance()

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

    /**
     * Override this method [getNotification] to get first notification on foreground mode.
     *
     * @return [Notification]
     */
    protected abstract fun getNotification(): Notification

    /**
     * Override this method [baseNotificationBuilder] to add your notification configuration, by default,
     * it has:
     *  * Priority: [NotificationCompat.PRIORITY_HIGH]
     *  * AutoCancel: false
     *  * Ongoing: true
     *  * Silent: true
     *
     *  @return [NotificationCompat.Builder]
     */
    protected open fun baseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSilent(true)
    }

    /**
     * This method [getPersistentServiceOffPendingIntent] create a [PendingIntent] to off this service, it's useful
     * to add a 'Off' button into foreground notification. Pass your Service as type.
     *
     * @return [PendingIntent]
     */
    protected inline fun <reified S : PersistentService> getPersistentServiceOffPendingIntent(): PendingIntent {
        val startIntent = PersistentServiceIntent(this, PersistentServiceActions.OFF, S::class.java)
        return PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * This method [getPersistentServiceActivityOpenPendingIntent] create a [PendingIntent] to open your Activity, usually, when the users
     * click into notification content, it open an Activity.
     *
     * @return [PendingIntent]
     */
    protected inline fun <reified A : PersistentServiceActivity<*, *>> getPersistentServiceActivityOpenPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, A::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * This method [startPersistentService] start service on foreground mode if is possible, to run service
     * on foreground mode is necessary [Manifest.permission.POST_NOTIFICATIONS] permission and [Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE]
     * or [Manifest.permission.FOREGROUND_SERVICE] permission. If they're not granted, the service run without foreground mode.
     *
     * @return [Unit]
     */
    @SuppressLint("NewApi")
    protected open fun startPersistentService() {
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

    /**
     * This method [stopForeground] stop foreground mode, it's mean that service can be running if it's required.
     *
     * @return [Unit]
     */
    protected open fun stopForeground() {
        isServiceForeground = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    /**
     * This method [stopPersistentService] stop foreground mode and stop the service.
     *
     * @return [Unit]
     */
    protected open fun stopPersistentService() {
        isServiceRunning = false
        stopSelf()
        stopForeground()
        stoppedServiceCallback?.let { it() }
        stoppedServiceCallback = null
    }

    /**
     * This method [updateNotification] update the notification by [getNotification] method.
     *
     */
    @SuppressLint("MissingPermission")
    protected fun updateNotification() {
        if (!isForeground()) {
            return
        }
        with(NotificationManagerCompat.from(this)) {
            notify(
                notificationId, getNotification()
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        for ((permission, status) in persistentServicePermissions.checkPermissionsStatus(this).entries) {
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
        when (intent?.action) {
            PersistentServiceActions.ON.name -> stopForeground()
            PersistentServiceActions.OFF.name -> stopPersistentService()
            else -> startPersistentService()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        binder = PersistentServiceBinder(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        isServiceBound = true
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        isServiceBound = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceBound = false
        if (!isForeground()) {
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