package com.apisap.persistentservice.services

/**
 * This enum class [PersistentServiceActions] define the possible actions to do in Persistent Service.
 *
 * * [OFF] to stop the service.
 * * [ON] to run the service.
 * * [ON_FOREGROUND] to run as foreground service.
 *
 */
enum class PersistentServiceActions {
    /**
     * [OFF] used to stop the service.
     */
    OFF,
    /**
     * [ON] used to run the service.
     */
    ON,
    /**
     * [ON_FOREGROUND] used to run as foreground service.
     */
    ON_FOREGROUND,
}