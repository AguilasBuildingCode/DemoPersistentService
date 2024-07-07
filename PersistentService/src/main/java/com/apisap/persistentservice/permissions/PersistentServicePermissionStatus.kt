package com.apisap.persistentservice.permissions

import com.apisap.persistentservice.permissions.BasePermissions.PermissionStatus
import com.apisap.persistentservice.permissions.BasePermissions.RequestStatus

/**
 * This data class [PersistentServicePermissionStatus] define the information required to handle permissions.
 *
 * @param [permissionStatus][PermissionStatus] can be [PermissionStatus.PENDING] or [PermissionStatus.REQUESTED]
 * @param [requestStatus][RequestStatus] can be [RequestStatus.GRANTED] or [RequestStatus.DENIED] or [RequestStatus.NOT_REQUIRED] or [RequestStatus.NOT_SUPPORTED] or [RequestStatus.UNKNOWN]
 * @param [minimumAPILevel][Int] define the minimum level to use the functionality, see the documentation about the permission.
 * @param [unsupportedPermission][RequestStatus] can be [RequestStatus.NOT_REQUIRED] or [RequestStatus.NOT_SUPPORTED]
 *
 */
data class PersistentServicePermissionStatus(
    var permissionStatus: PermissionStatus,
    var requestStatus: RequestStatus,
    var minimumAPILevel: Int,
    var unsupportedPermission: RequestStatus,
)
