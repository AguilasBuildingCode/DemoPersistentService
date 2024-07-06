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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PersistentServerPermissions @Inject constructor() : BasePermissions() {

    private var onRequireUserExplanationCallback: ((permission: String, continueRequest: () -> Unit) -> Unit)? =
        null
    private var onPermissionsChangedStatusCallback: ((permission: String, newRequestStatus: RequestStatus) -> Unit)? =
        null

    fun setRequireUserExplanationCallback(onRequireUserExplanationCallback: (permission: String, continueRequest: () -> Unit) -> Unit) {
        this.onRequireUserExplanationCallback = onRequireUserExplanationCallback
    }

    fun setPermissionsChangedStatusCallback(onPermissionsChangedStatusCallback: (permission: String, newRequestStatus: RequestStatus) -> Unit) {
        this.onPermissionsChangedStatusCallback = onPermissionsChangedStatusCallback
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
        permissions.forEachIndexed { index, permission ->
            val requestStatus =
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) RequestStatus.GRANTED else RequestStatus.DENIED

            currentPermissions[permission]?.permissionStatus = PermissionStatus.REQUESTED
            currentPermissions[permission]?.requestStatus = requestStatus

            onPermissionsChangedStatusCallback?.let { it(permission, requestStatus) }
        }
    }

    fun requestPermissions(activity: Activity) {
        val permissionsPending = mutableListOf<String>()
        currentPermissions.entries.forEachIndexed { index, (permission, permissionPayload) ->
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

    fun requestPermissions(activity: Activity, permission: String) {
        if (currentPermissions.containsKey(permission)) {
            activity.requestPermissions(arrayOf(permission), requestCode)
            return
        }
        throw IllegalArgumentException(activity.resources.getString(R.string.illegal_argument_exception_on_persistent_server_permissions, permission))
    }
}