package com.apisap.persistentservice.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PersistentServerPermissions @Inject constructor() : BasePermissions() {
    companion object {
        private val instance = PersistentServerPermissions()

        fun getInstance(): PersistentServerPermissions {
            return instance
        }
    }

    @SuppressLint("InlinedApi")
    protected open val currentPermissions: HashMap<String, PersistentServicePermissionStatus> =
        hashMapOf(
            Pair(
                Manifest.permission.POST_NOTIFICATIONS,
                PersistentServicePermissionStatus(
                    PermissionStatus.PENDING,
                    RequestStatus.UNKNOWN,
                    Build.VERSION_CODES.TIRAMISU,
                    RequestStatus.NOT_REQUIRED,
                )
            ),
            Pair(
                Manifest.permission.FOREGROUND_SERVICE,
                PersistentServicePermissionStatus(
                    PermissionStatus.PENDING,
                    RequestStatus.UNKNOWN,
                    Build.VERSION_CODES.P,
                    RequestStatus.NOT_SUPPORTED,
                )
            ),
            Pair(
                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
                PersistentServicePermissionStatus(
                    PermissionStatus.PENDING,
                    RequestStatus.UNKNOWN,
                    Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                    RequestStatus.NOT_SUPPORTED,
                )
            ),
        )

    fun bindRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: IntArray,
    ) {
        if (this.requestCode != requestCode) {
            return
        }
        permissions.forEachIndexed { i, s ->
            val requestStatus =
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) RequestStatus.GRANTED else RequestStatus.DENIED

            currentPermissions[s]?.permissionStatus = PermissionStatus.REQUESTED
            currentPermissions[s]?.requestStatus = requestStatus
        }
    }

    fun requestPermissions(activity: Activity) {
        val permissionsPending = mutableListOf<String>()
        currentPermissions.entries.forEachIndexed { index, (key, value) ->
            if (value.permissionStatus != PermissionStatus.PENDING) {
                return
            }
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    key
                ) == PackageManager.PERMISSION_GRANTED -> {
                    value.permissionStatus = PermissionStatus.REQUESTED
                    value.requestStatus = RequestStatus.GRANTED
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, key
                ) -> {
                    value.permissionStatus = PermissionStatus.REQUESTED
                    value.requestStatus = RequestStatus.NEVER
                }

                else -> {
                    if (Build.VERSION.SDK_INT >= value.minimumAPILevel) {
                        permissionsPending.add(key)
                    } else {
                        value.permissionStatus = PermissionStatus.REQUESTED
                        value.requestStatus = value.unsupportedPermission
                    }
                }
            }
            if (index == currentPermissions.size - 1 && permissionsPending.size > 0) {
                activity.requestPermissions(permissionsPending.toTypedArray(), requestCode)
            }
        }
    }

    private fun getPermissionStatus(): Map<String, RequestStatus> {
        return currentPermissions.mapValues { (_, value) -> value.requestStatus }
    }

    fun checkPermissionsStatus(context: Context): Map<String, RequestStatus> {
        for ((permission, status) in currentPermissions.entries) {
            val (_, _, minimumAPILevel, unsupportedPermission) = status

            when {
                Build.VERSION.SDK_INT < minimumAPILevel -> {
                    status.permissionStatus = PermissionStatus.REQUESTED
                    status.requestStatus = unsupportedPermission
                }

                ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    status.permissionStatus = PermissionStatus.REQUESTED
                    status.requestStatus = RequestStatus.GRANTED
                }

                else -> {
                    status.permissionStatus = PermissionStatus.REQUESTED
                    status.requestStatus = RequestStatus.DENIED
                }
            }
        }

        return getPermissionStatus()
    }
}