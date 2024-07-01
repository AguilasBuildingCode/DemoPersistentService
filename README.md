# DemoPersistentService (V1.0.1)

This library expouse all necesary to make a persistent service, usefull to make a long time process execution.

[Requirements:](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/app/src/main/AndroidManifest.xml)

```
<!--AndroidManifest.xml-->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

<!--Application who handle NotificationChannel-->
<application
    android:name="YOUR_APPLICATION"
    [...]>
    <!--Activity who handle the persistent service-->
    <activity
            [...]
            android:name="YOUR_ACTIVITY"
            android:excludeFromRecents="true"
            android:exported="true"
            android:launchMode="singleTask"
            android:taskAffinity="">
            [...]
    </activity>
        <!--Service who estend of PersistentService-->
        <service
            android:name="YOUR_SERVICE"
            android:foregroundServiceType="specialUse"
            android:stopWithTask="false" />
        <receiver
            android:name="com.apisap.persistentservice.broadcastreceiver.PersistentServiceBroadcastReceiver"
            android:enabled="true"
            android:exported="false" />
</application>
```

What do you need to start?

Application who extend of [PersistentServiceApplication](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/PersistentService/src/main/java/com/apisap/persistentservice/PersistentServiceApplication.kt) (see a [demo](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/app/src/main/java/com/apisap/demopersistentservice/DemoServiceApplication.kt))

Service who extend of [PersistentService](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/PersistentService/src/main/java/com/apisap/persistentservice/services/PersistentService.kt) (see a [demo](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/app/src/main/java/com/apisap/demopersistentservice/services/DemoPersistentService.kt))

Activity who extend of [PersistentServiceActivity](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/PersistentService/src/main/java/com/apisap/persistentservice/activities/PersistentServiceActivity.kt) (see a [demo](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/app/src/main/java/com/apisap/demopersistentservice/DemoPersistentServiceActivity.kt))

ViewModel who extend of [PersistentServiceViewModel](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/PersistentService/src/main/java/com/apisap/persistentservice/viewmodels/PersistentServiceViewModel.kt) (see a [demo](https://github.com/AguilasBuildingCode/DemoPersistentService/blob/main/app/src/main/java/com/apisap/demopersistentservice/viewmodels/DemoPersistentServiceViewModel.kt)) and used by Activity who handle the persistent service.

<img src="assets/Screenshot_01.jpg" height="450" />
<img src="assets/Screenshot_02.jpg" height="450" />
<img src="assets/Screenshot_03.jpg" height="450" />
