package com.apisap.persistentservice.intents

import android.content.Context
import android.content.Intent
import com.apisap.persistentservice.services.PersistentServiceActions

class PersistentServiceIntent(
    packageContext: Context,
    action: PersistentServiceActions,
    clazz: Class<*>
) : Intent(packageContext, clazz) {
    init {
        setAction(action.name)
    }
}