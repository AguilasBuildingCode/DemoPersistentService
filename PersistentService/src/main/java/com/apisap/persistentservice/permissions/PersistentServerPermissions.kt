package com.apisap.persistentservice.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.apisap.persistentservice.R

open class PersistentServerPermissions protected constructor() :
    BasePermissions() {

    companion object {
        private val instance: PersistentServerPermissions = PersistentServerPermissions()
        fun getInstance(): PersistentServerPermissions {
            return instance
        }
    }

    private var onRequireUserExplanationCallback: ((permission: String, continueRequest: () -> Unit) -> Unit)? =
        null
    private var onPermissionsChangedStatusCallback: ((permission: String, newRequestStatus: RequestStatus) -> Unit)? =
        null

    @SuppressLint("InlinedApi")
    protected var persistentServicePermissions: MutableMap<String, PersistentServicePermissionStatus> =
        hashMapOf(
            Pair(
                Manifest.permission.POST_NOTIFICATIONS,
                PersistentServicePermissionStatus(
                    permissionStatus = PermissionStatus.PENDING,
                    requestStatus = RequestStatus.UNKNOWN,
                    minimumAPILevel = Build.VERSION_CODES.TIRAMISU,
                    unsupportedPermission = RequestStatus.NOT_REQUIRED,
                )
            ),
            Pair(
                Manifest.permission.FOREGROUND_SERVICE,
                PersistentServicePermissionStatus(
                    permissionStatus = PermissionStatus.PENDING,
                    requestStatus = RequestStatus.UNKNOWN,
                    minimumAPILevel = Build.VERSION_CODES.P,
                    unsupportedPermission = RequestStatus.NOT_SUPPORTED,
                )
            ),
            Pair(
                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
                PersistentServicePermissionStatus(
                    permissionStatus = PermissionStatus.PENDING,
                    requestStatus = RequestStatus.UNKNOWN,
                    minimumAPILevel = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                    unsupportedPermission = RequestStatus.NOT_SUPPORTED,
                )
            ),
        )

    protected fun addPermissions(newPermissions: MutableMap<String, PersistentServicePermissionStatus>) {
        persistentServicePermissions =
            persistentServicePermissions.plus(newPermissions).toMutableMap()
    }

    fun setRequireUserExplanationCallback(onRequireUserExplanationCallback: (permission: String, continueRequest: () -> Unit) -> Unit) {
        this.onRequireUserExplanationCallback = onRequireUserExplanationCallback
    }

    fun setPermissionsChangedStatusCallback(onPermissionsChangedStatusCallback: (permission: String, newRequestStatus: RequestStatus) -> Unit) {
        this.onPermissionsChangedStatusCallback = onPermissionsChangedStatusCallback
    }

    open fun bindRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: IntArray,
    ) {
        if (this.requestCode != requestCode) {
            return
        }
        permissions.forEachIndexed { index, permission ->
            val requestStatus =
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) RequestStatus.GRANTED else RequestStatus.DENIED

            persistentServicePermissions[permission]?.permissionStatus = PermissionStatus.REQUESTED
            persistentServicePermissions[permission]?.requestStatus = requestStatus

            onPermissionsChangedStatusCallback?.let { it(permission, requestStatus) }
        }
    }

    open fun requestPermissions(activity: Activity) {
        val permissionsPending = mutableListOf<String>()
        persistentServicePermissions.entries.forEachIndexed { index, (permission, permissionPayload) ->
            if (permissionPayload.permissionStatus != PermissionStatus.PENDING) {
                return
            }
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    permissionPayload.permissionStatus = PermissionStatus.REQUESTED
                    permissionPayload.requestStatus = RequestStatus.GRANTED
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, permission
                ) -> {
                    onRequireUserExplanationCallback?.let {
                        it(permission) {
                            requestPermissions(activity, permission)
                        }
                    }
                }

                else -> {
                    if (Build.VERSION.SDK_INT >= permissionPayload.minimumAPILevel) {
                        permissionsPending.add(permission)
                    } else {
                        permissionPayload.permissionStatus = PermissionStatus.REQUESTED
                        permissionPayload.requestStatus = permissionPayload.unsupportedPermission
                    }
                }
            }
            if (index == persistentServicePermissions.size - 1 && permissionsPending.size > 0) {
                activity.requestPermissions(permissionsPending.toTypedArray(), requestCode)
            }
        }
    }

    private fun getPermissionStatus(): Map<String, RequestStatus> {
        return persistentServicePermissions.mapValues { (_, value) -> value.requestStatus }
    }

    open fun checkPermissionsStatus(context: Context): Map<String, RequestStatus> {
        for ((permission, status) in persistentServicePermissions.entries) {
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

    open fun requestPermissions(activity: Activity, permission: String) {
        if (persistentServicePermissions.containsKey(permission)) {
            activity.requestPermissions(arrayOf(permission), requestCode)
            return
        }
        throw IllegalArgumentException(
            activity.resources.getString(
                R.string.illegal_argument_exception_on_persistent_server_permissions,
                permission
            )
        )
    }
}