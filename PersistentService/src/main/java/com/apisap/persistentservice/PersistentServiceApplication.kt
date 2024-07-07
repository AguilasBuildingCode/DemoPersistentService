package com.apisap.persistentservice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 *  This abstract class [PersistentServiceApplication] extend of [Application], it's define the constants
 *  necessaries to create the notification channel, necessary to post notifications and last one is necessary
 *  to run foreground services, this class create your notification channel automatically at [onCreate] cycle.
 *
 * @property [notificationChannelId] override to define your notification channel id.
 * @property [notificationChannelName] override to define your notification channel name.
 * @property [notificationChannelDescription] override define your notification channel description.
 *
 */
abstract class PersistentServiceApplication : Application() {

    abstract val notificationChannelId: String
    abstract val notificationChannelName: String
    abstract val notificationChannelDescription: String

    /**
     * Here, this abstract class are creating your notification channel by the overrides properties.
     */
    override fun onCreate() {
        super.onCreate()
        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(
                    notificationChannelId,
                    notificationChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = notificationChannelDescription
                }.also {
                    createNotificationChannel(it)
                }
            }
        }
    }
}