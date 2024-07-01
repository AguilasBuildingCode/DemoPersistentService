package com.apisap.persistentservice.permissions

data class PersistentServicePermissionStatus(
    var permissionStatus: BasePermissions.PermissionStatus,
    var requestStatus: BasePermissions.RequestStatus,
    var minimumAPILevel: Int,
    var unsupportedPermission: BasePermissions.RequestStatus,
)
