package com.apisap.demopersistentservice.ui.states

data class DemoPersistentServicePostNotificationDialogState(
    val onConfirmation: () -> Unit,
    val onDismissRequest: () -> Unit,
)
