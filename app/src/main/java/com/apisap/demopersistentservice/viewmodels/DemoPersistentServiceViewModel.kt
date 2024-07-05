package com.apisap.demopersistentservice.viewmodels

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.apisap.demopersistentservice.services.DemoPersistentService
import com.apisap.demopersistentservice.ui.states.DemoPersistentServicePostNotificationDialogState
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiState
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiStatesEnum
import com.apisap.persistentservice.permissions.BasePermissions.RequestStatus
import com.apisap.persistentservice.services.PersistentService
import com.apisap.persistentservice.services.PersistentServiceConnection
import com.apisap.persistentservice.viewmodels.PersistentServiceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoPersistentServiceViewModel @Inject constructor() :
    PersistentServiceViewModel<DemoPersistentService>() {

    private val _demoPersistentServiceUiState: MutableStateFlow<DemoPersistentServiceUiState> =
        MutableStateFlow(DemoPersistentServiceUiState(if (PersistentService.isServiceRunning) DemoPersistentServiceUiStatesEnum.STOP else DemoPersistentServiceUiStatesEnum.START))
    val demoPersistentServiceUiState: StateFlow<DemoPersistentServiceUiState> =
        _demoPersistentServiceUiState.asStateFlow()

    private val dismissPostNotificationDialogState: () -> Unit = {
        _demoPersistentServiceUiState.update { currentState ->
            currentState.copy(
                showPostNotificationExplainUserDialog = false,
                demoPersistentServicePostNotificationDialogState = null
            )
        }
    }

    private val onRequireUserExplanationCallback =
        { permission: String, continueRequest: () -> Unit ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    _demoPersistentServiceUiState.update { currentState ->
                        currentState.copy(
                            showPostNotificationExplainUserDialog = true,
                            demoPersistentServicePostNotificationDialogState = DemoPersistentServicePostNotificationDialogState(
                                onConfirmation = {
                                    continueRequest()
                                    dismissPostNotificationDialogState()
                                },
                                onDismissRequest = dismissPostNotificationDialogState
                            )
                        )
                    }
                }
            }
        }

    override val persistentServiceConnection: PersistentServiceConnection<DemoPersistentService> =
        object :
            PersistentServiceConnection<DemoPersistentService>() {
            override fun onPersistentServiceConnected(
                name: ComponentName?,
                persistentService: DemoPersistentService?
            ) {
                persistentService?.onNewLog { log ->
                    _demoPersistentServiceUiState.update { currentState ->
                        currentState.copy(log = log)
                    }
                }
                persistentService?.onStoppedService {
                    _demoPersistentServiceUiState.update { currentState ->
                        currentState.copy(btnStartStopEnabled = false)
                    }
                    _demoPersistentServiceUiState.update { currentState ->
                        currentState.copy(
                            uiState = DemoPersistentServiceUiStatesEnum.START,
                            btnStartStopEnabled = true,
                            log = null
                        )
                    }
                }
                Log.i("DemoService", "onPersistentServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.i("DemoService", "onServiceDisconnected")
            }

        }

    fun onBtnStartStopClick(activity: Activity) {
        viewModelScope.launch {
            _demoPersistentServiceUiState.update { currentState ->
                currentState.copy(btnStartStopEnabled = false)
            }
            _demoPersistentServiceUiState.update { currentState ->
                when (currentState.uiState) {
                    DemoPersistentServiceUiStatesEnum.START -> {
                        startPersistentService<DemoPersistentService>(activity)
                        currentState.copy(
                            uiState = DemoPersistentServiceUiStatesEnum.STOP,
                            btnStartStopEnabled = true,
                            log = null
                        )
                    }

                    DemoPersistentServiceUiStatesEnum.STOP -> {
                        stopPersistentService<DemoPersistentService>(activity)
                        currentState
                    }
                }
            }
        }
    }

    fun getRequireUserExplanationCallback(): (String, () -> Unit) -> Unit {
        return onRequireUserExplanationCallback
    }

    fun bindDemoPersistentService(activity: Activity) {
        bindPersistentService<DemoPersistentService>(activity)
    }

    fun unBindDemoPersistentService(activity: Activity) {
        unBindPersistentService<DemoPersistentService>(activity)
    }
}