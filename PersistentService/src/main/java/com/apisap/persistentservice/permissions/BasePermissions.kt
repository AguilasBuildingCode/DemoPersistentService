package com.apisap.persistentservice.permissions

open class BasePermissions {
    protected val requestCode: Int = 1

    enum class PermissionStatus {
        REQUESTED,
        PENDING
    }

    enum class RequestStatus {
        GRANTED,
        DENIED,
        NEVER,
        NOT_REQUIRED,
        NOT_SUPPORTED,
        UNKNOWN;

        companion object {
            fun isPermissionOK(requestStatus: RequestStatus): Boolean {
                return requestStatus == GRANTED || requestStatus == NOT_REQUIRED
            }

            fun arePermissionsOK(requestsStatus: List<RequestStatus>): Boolean {
                return requestsStatus.filter { requestStatus -> isPermissionOK(requestStatus) }.size == requestsStatus.size
            }
        }
    }
}