package com.apisap.persistentservice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

abstract class PersistentServiceApplication : Application() {

    abstract val notificationChannelId: String
    abstract val notificationChannelName: String
    abstract val notificationChannelDescription: String

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