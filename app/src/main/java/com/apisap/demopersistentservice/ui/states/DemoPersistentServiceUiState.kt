package com.apisap.demopersistentservice.ui.states

data class DemoPersistentServiceUiState(
    val uiState: DemoPersistentServiceUiStatesEnum,
    val showPostNotificationExplainUserDialog: Boolean = false,
    val demoPersistentServicePostNotificationDialogState: DemoPersistentServicePostNotificationDialogState? = null,
    val btnStartStopEnabled: Boolean = true,
    val log: String? = null
)