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

/**
 * This class [PersistentServicePermissions] extend of [BasePermissions], and it handle the permissions necessaries to run the service as foreground mode.
 *
 * @property [onRequireUserExplanationCallback] this property is a callback, used to notify if the permissions required an
 * explanation about it use in the app.
 * @property [onPermissionsChangedStatusCallback] this property is a callback, used to notify if any permission is channing their status.
 * @property [persistentServicePermissions][MutableMap] this Map is used to define the permissions to request, if you need add some permissions,
 * you can create other class that extend of it, and add more permissions in init cycle by method [addPermissions].
 *
 */
open class PersistentServicePermissions protected constructor() :
    BasePermissions() {

    companion object {
        private val instance: PersistentServicePermissions = PersistentServicePermissions()
        fun getInstance(): PersistentServicePermissions {
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

    /**
     * This method [addPermissions] is used to add new permissions in child classes.
     *
     * @param [newPermissions][MutableMap] this map define the new permissions to add.
     *
     * @return [Unit]
     *
     */
    protected fun addPermissions(newPermissions: MutableMap<String, PersistentServicePermissionStatus>) {
        persistentServicePermissions =
            persistentServicePermissions.plus(newPermissions).toMutableMap()
    }

    /**
     * This method [setRequireUserExplanationCallback] permit set [onRequireUserExplanationCallback].
     *
     * @param [onRequireUserExplanationCallback] is a callback to get notifications about permissions that required be explained to user.
     *
     * @return [Unit]
     *
     */
    fun setRequireUserExplanationCallback(onRequireUserExplanationCallback: (permission: String, continueRequest: () -> Unit) -> Unit) {
        this.onRequireUserExplanationCallback = onRequireUserExplanationCallback
    }

    /**
     * This method [setPermissionsChangedStatusCallback] permit set [onRequireUserExplanationCallback].
     *
     * @param [onPermissionsChangedStatusCallback] is a callback to get notifications about permissions that are changing their status.
     *
     * @return [Unit]
     *
     */
    fun setPermissionsChangedStatusCallback(onPermissionsChangedStatusCallback: (permission: String, newRequestStatus: RequestStatus) -> Unit) {
        this.onPermissionsChangedStatusCallback = onPermissionsChangedStatusCallback
    }

    /**
     * This method [bindRequestPermissionsResult] permit get the permissions status.
     *
     * @param [requestCode][Int] represent who's made the permissions request, it's defined in [BasePermissions].
     * @param [permissions][List] is the list of permissions requested.
     * @param [grantResults][IntArray] is the reference of the status of permissions requested.
     *
     * @return [Unit]
     *
     */
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

    /**
     * This method [requestPermissions] handle the permissions request by [requestCode] and [persistentServicePermissions].
     *
     * @param [activity][Activity] is the activity where the permissions are request.
     *
     * @return [Unit]
     *
     */
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

    /**
     * This method [getPermissionStatus] return the current permissions status.
     *
     * @return [Map] of permissions and RequestStatus
     *
     */
    private fun getPermissionStatus(): Map<String, RequestStatus> {
        return persistentServicePermissions.mapValues { (_, value) -> value.requestStatus }
    }

    /**
     * This method [checkPermissionStatus] only check the current status by system and update the [persistentServicePermissions] list.
     *
     * @param [context][Context] who's making the permissions check.
     * @param [permission][String] permission to check check.
     *
     * @return [PersistentServicePermissionStatus] of permissions and RequestStatus
     *
     */
    open fun checkPermissionStatus(
        context: Context,
        permission: String
    ): PersistentServicePermissionStatus {
        if (persistentServicePermissions.containsKey(permission)) {
            persistentServicePermissions[permission]?.let { status ->
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
                return status
            }
        }
        throw IllegalArgumentException("Unknown permission $permission")
    }

    /**
     * This method [checkPermissionsStatus] only check the current status by system and update the [persistentServicePermissions] list.
     *
     * @param [context][Context] who's making the permissions check.
     *
     * @return [Map] of permissions and RequestStatus
     *
     */
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

    /**
     * This method [requestPermissions] request the permission, usually used in second request permissions at [onRequireUserExplanationCallback]
     * and [onPermissionsChangedStatusCallback].
     *
     * @param [activity][Activity] who's making the permissions request.
     * @param [permission][String] the permissions to request.
     *
     * @return [Unit]
     *
     */
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