package com.apisap.demopersistentservice.services

import android.app.Notification
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.apisap.demopersistentservice.DemoPersistentServiceActivity
import com.apisap.demopersistentservice.R
import com.apisap.persistentservice.services.PersistentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DemoPersistentService : PersistentService() {

    private var counter = 0
    override val notificationId: Int = 1
    override val notificationChannelId: String by lazy { resources.getString(R.string.notification_channel_id) }

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var logsCallback: ((log: String) -> Unit)? = null

    fun onNewLog(logsCallback: (log: String) -> Unit) {
        this.logsCallback = logsCallback
    }

    override fun baseNotificationBuilder(): NotificationCompat.Builder {
        val persistentServicePendingIntent =
            getPersistentServiceOffPendingIntent<DemoPersistentService>()
        return super.baseNotificationBuilder()
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentIntent(getPersistentServiceActivityOpenPendingIntent<DemoPersistentServiceActivity>())
            .addAction(
                R.drawable.ic_launcher_foreground,
                resources.getString(R.string.text_off),
                persistentServicePendingIntent
            )
            .setDeleteIntent(persistentServicePendingIntent)
    }

    override fun getNotification(): Notification {
        return baseNotificationBuilder()
            .setContentText(
                resources.getString(
                    R.string.notification_counter_text,
                    counter
                )
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        job = scope.launch {
            while (true) {
                delay(5000)
                counter++
                val log = resources.getString(
                    R.string.notification_counter_text,
                    counter
                )
                logsCallback?.let { it(log) }
                updateNotification(
                    baseNotificationBuilder()
                        .setContentText(log)
                        .build()
                )
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logsCallback = null
        return super.onUnbind(intent)
    }

    override fun stopPersistentService() {
        job?.cancel()
        logsCallback = null
        super.stopPersistentService()
    }

    override fun onDestroy() {
        job?.cancel()
        logsCallback = null
        super.onDestroy()
    }
}