package com.apisap.persistentservice.permissions

/**
 * This class [BasePermissions] define the fundamental permissions status, [PermissionStatus] and [RequestStatus]:
 * * [PermissionStatus] can be [PermissionStatus.PENDING] or [PermissionStatus.REQUESTED]
 * * [RequestStatus] can be [RequestStatus.GRANTED] or [RequestStatus.DENIED] or [RequestStatus.NOT_REQUIRED] or [RequestStatus.NOT_SUPPORTED] or [RequestStatus.UNKNOWN]
 *
 * * Also, this class contain static method to validate the right permissions status: isPermissionOK and arePermissionsOK.
 *
 * @property [requestCode] is a constant that define the request code permissions.
 *
 */
open class BasePermissions {
    protected val requestCode: Int = 1

    enum class PermissionStatus {
        /**
         * [REQUESTED] define that the permission is requested
         */
        REQUESTED,
        /**
         * [PENDING] define that the permission isn't requested
         */
        PENDING
    }

    enum class RequestStatus {
        /**
         * [GRANTED] if the user grant the permission
         */
        GRANTED,
        /**
         * [DENIED] if the user don't grant the permission
         */
        DENIED,
        /**
         * [NOT_REQUIRED] if by Android Level isn't required request the permission.
         */
        NOT_REQUIRED,
        /**
         * [NOT_SUPPORTED] if by Android Level isn't supported the functionality.
         */
        NOT_SUPPORTED,
        /**
         * [UNKNOWN] if is necessary request the permissions to know the new status.
         */
        UNKNOWN;

        companion object {
            /**
             * This method [isPermissionOK] evaluate if the permission is okay to use the functionality.
             *
             * @param [requestStatus][RequestStatus] the permission status.
             *
             * @return [Boolean] if it's true, the permissions can be used, if it's false you can't use it.
             *
             */
            fun isPermissionOK(requestStatus: RequestStatus): Boolean {
                return requestStatus == GRANTED || requestStatus == NOT_REQUIRED
            }

            /**
             * This method [arePermissionsOK] evaluate if the permissions are okay to use the functionality.
             *
             * @param [requestsStatus][List] is a list of [RequestStatus].
             *
             * @return [Boolean] if it's true, all permissions are okay, if it's false, one or more permissions can't be used.
             *
             */
            fun arePermissionsOK(requestsStatus: List<RequestStatus>): Boolean {
                return requestsStatus.filter { requestStatus -> isPermissionOK(requestStatus) }.size == requestsStatus.size
            }
        }
    }
}