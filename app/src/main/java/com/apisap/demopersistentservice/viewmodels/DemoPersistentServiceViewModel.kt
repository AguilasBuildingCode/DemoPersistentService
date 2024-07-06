package com.apisap.demopersistentservice.viewmodels

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apisap.demopersistentservice.ui.states.DemoPersistentServicePostNotificationDialogState
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiState
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiStatesEnum
import com.apisap.persistentservice.services.PersistentService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoPersistentServiceViewModel @Inject constructor() : ViewModel(), DefaultLifecycleObserver {

    private val _demoPersistentServiceUiState: MutableStateFlow<DemoPersistentServiceUiState> =
        MutableStateFlow(DemoPersistentServiceUiState(if (PersistentService.isServiceRunning) DemoPersistentServiceUiStatesEnum.STOP else DemoPersistentServiceUiStatesEnum.START))
    val demoPersistentServiceUiState: StateFlow<DemoPersistentServiceUiState> =
        _demoPersistentServiceUiState.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        _demoPersistentServiceUiState.update { currentStatus ->
            currentStatus.copy(
                uiState = if (PersistentService.isServiceRunning) DemoPersistentServiceUiStatesEnum.STOP else DemoPersistentServiceUiStatesEnum.START,
                log = null
            )
        }
    }

    val dismissPostNotificationDialogState: () -> Unit = {
        viewModelScope.launch {
            _demoPersistentServiceUiState.update { currentState ->
                currentState.copy(
                    log = null,
                    showPostNotificationExplainUserDialog = false,
                    demoPersistentServicePostNotificationDialogState = null
                )
            }
        }
    }

    fun showDemoPersistentServicePostNotificationDialog(
        confirmActionName: String,
        onConfirmation: () -> Unit
    ) {
        _demoPersistentServiceUiState.update { currentState ->
            currentState.copy(
                log = null,
                showPostNotificationExplainUserDialog = true,
                demoPersistentServicePostNotificationDialogState = DemoPersistentServicePostNotificationDialogState(
                    onConfirmation = {
                        onConfirmation()
                        dismissPostNotificationDialogState()
                    },
                    onDismissRequest = dismissPostNotificationDialogState,
                    confirmActionName = confirmActionName
                )
            )
        }
    }

    fun startTransitionBtnStartStopState(): Deferred<Unit> {
        return viewModelScope.async {
            _demoPersistentServiceUiState.update { currentState ->
                currentState.copy(btnStartStopEnabled = false, log = null)
            }
        }
    }

    fun stopTransitionBtnStartStopState(): Deferred<Unit> {
        return viewModelScope.async {
            _demoPersistentServiceUiState.update { currentState ->
                when (currentState.uiState) {
                    DemoPersistentServiceUiStatesEnum.START -> {
                        currentState.copy(
                            uiState = DemoPersistentServiceUiStatesEnum.STOP,
                            btnStartStopEnabled = true,
                            log = null
                        )
                    }

                    DemoPersistentServiceUiStatesEnum.STOP -> {
                        currentState.copy(log = null)
                    }
                }
            }
        }
    }

    fun addNewLog(log: String) {
        viewModelScope.launch {
            _demoPersistentServiceUiState.update { currentState ->
                currentState.copy(log = log)
            }
        }
    }

    fun stoppedDemoPersistentService() {
        viewModelScope.launch {
            _demoPersistentServiceUiState.update { currentState ->
                currentState.copy(
                    uiState = DemoPersistentServiceUiStatesEnum.START,
                    btnStartStopEnabled = true,
                    log = null
                )
            }
        }
    }
}