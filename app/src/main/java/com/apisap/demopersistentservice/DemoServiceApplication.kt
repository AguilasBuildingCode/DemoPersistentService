package com.apisap.demopersistentservice

import com.apisap.persistentservice.PersistentServiceApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoServiceApplication : PersistentServiceApplication() {
    override val notificationChannelId: String by lazy { resources.getString(R.string.notification_channel_id) }
    override val notificationChannelName: String  by lazy { resources.getString(R.string.notification_channel_name) }
    override val notificationChannelDescription: String  by lazy { resources.getString(R.string.notification_channel_description) }
}