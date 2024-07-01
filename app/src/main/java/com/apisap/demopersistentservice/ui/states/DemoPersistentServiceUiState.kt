package com.apisap.demopersistentservice.ui.states

data class DemoPersistentServiceUiState(val uiState: DemoPersistentServiceUiStatesEnum, val btnStartStopEnabled: Boolean = true, val log: String? = null)