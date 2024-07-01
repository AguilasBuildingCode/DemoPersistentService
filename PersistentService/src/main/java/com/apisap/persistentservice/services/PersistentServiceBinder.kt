package com.apisap.persistentservice.services

import android.os.Binder

class PersistentServiceBinder<S : PersistentService>(val persistentService: S) : Binder()