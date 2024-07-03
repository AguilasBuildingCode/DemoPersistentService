package com.apisap.persistentservice.activities

import androidx.activity.ComponentActivity
import com.apisap.persistentservice.permissions.PersistentServerPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class PersistentServiceActivity : ComponentActivity() {

    @Inject
    lateinit var persistentServerPermissions: PersistentServerPermissions

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