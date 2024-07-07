package com.apisap.persistentservice.services

import android.os.Binder

/**
 * This class [PersistentServiceBinder] extend of [Binder], it define the binder wrapper for [PersistentService].
 *
 * * Define your service by type params.
 *
 * @param [persistentService][S] where [S] is your service that extend of [PersistentService], define it in type params.
 *
 */
class PersistentServiceBinder<S : PersistentService>(val persistentService: S) : Binder()