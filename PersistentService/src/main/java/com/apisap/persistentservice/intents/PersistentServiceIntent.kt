package com.apisap.persistentservice.intents

import android.content.Context
import android.content.Intent
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceActions

/**
 * This class [PersistentServiceIntent] help to run [PersistentService], extend your service of it to use this
 * class, define your service as type.
 *
 * @param [packageContext][Context]
 * @param [action][PersistentServiceActions] can be [PersistentServiceActions.ON_FOREGROUND] or [PersistentServiceActions.ON] or [PersistentServiceActions.OFF].
 * @param [clazz][Class] class of your service.
 *
 */
class PersistentServiceIntent<S : PersistentService>(
    packageContext: Context,
    action: PersistentServiceActions,
    clazz: Class<S>
) : Intent(packageContext, clazz) {
    init {
        setAction(action.name)
    }
}