package com.apisap.persistentservice.activities

import androidx.activity.ComponentActivity
import com.apisap.persistentservice.permissions.PersistentServerPermissions

abstract class PersistentServiceActivity : ComponentActivity() {

    private val persistentServerPermissions: PersistentServerPermissions by lazy { PersistentServerPermissions.getInstance() }

    override fun onStart() {
        super.onStart()
        persistentServerPermissions.requestPermissions(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        persistentServerPermissions.bindRequestPermissionsResult(
            requestCode,
            permissions.toList(),
            grantResults
        )
    }
}