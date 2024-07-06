package com.apisap.demopersistentservice

import android.Manifest
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.apisap.demopersistentservice.services.DemoPersistentService
import com.apisap.demopersistentservice.ui.DemoPersistentServiceUI
import com.apisap.demopersistentservice.ui.ExplainPostNotificationPermissionDialog
import com.apisap.demopersistentservice.ui.states.DemoPersistentServicePostNotificationDialogState
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiStatesEnum
import com.apisap.demopersistentservice.ui.theme.DemoPersistentServiceTheme
import com.apisap.demopersistentservice.viewmodels.DemoPersistentServiceViewModel
import com.apisap.persistentservice.activities.PersistentServiceActivity
import com.apisap.persistentservice.permissions.BasePermissions
import com.apisap.persistentservice.services.PersistentServiceConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DemoPersistentServiceActivity :
    PersistentServiceActivity<DemoPersistentService>(clazz = DemoPersistentService::class.java) {

    private val viewModel by viewModels<DemoPersistentServiceViewModel>()
    override val persistentServiceConnection: PersistentServiceConnection<DemoPersistentService> =
        object :
            PersistentServiceConnection<DemoPersistentService>() {
            override fun onPersistentServiceConnected(
                name: ComponentName?,
                persistentService: DemoPersistentService?
            ) {
                persistentService?.onNewLog { log ->
                    viewModel.addNewLog(log)
                }

                persistentService?.onStoppedService {
                    viewModel.stoppedDemoPersistentService()
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {}
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.demoPersistentServiceUiState.collectLatest { (uiStatus, showPostNotificationExplainUserDialog, demoPersistentServicePostNotificationDialogState, btnEnabled, log) ->
                    setContent {
                        DemoPersistentServiceTheme {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                Modifier.padding(innerPadding).DemoPersistentServiceUI(
                                    uiStatus = uiStatus,
                                    onClickBtnStartStop = {
                                        lifecycleScope.launch {
                                            when (viewModel.demoPersistentServiceUiState.value.uiState) {
                                                DemoPersistentServiceUiStatesEnum.START -> {
                                                    viewModel.startTransitionBtnStartStopState()
                                                        .await()
                                                    startPersistentServiceAndBind()
                                                    viewModel.stopTransitionBtnStartStopState()
                                                        .await()
                                                }

                                                DemoPersistentServiceUiStatesEnum.STOP -> {
                                                    viewModel.startTransitionBtnStartStopState()
                                                        .await()
                                                    stopPersistentServiceAndUnbind()
                                                    viewModel.stopTransitionBtnStartStopState()
                                                        .await()
                                                }
                                            }
                                        }
                                    },
                                    btnEnabled = btnEnabled,
                                    log = log
                                )
                            }
                            if (showPostNotificationExplainUserDialog && demoPersistentServicePostNotificationDialogState != null) {
                                ExplainPostNotificationPermissionDialog(
                                    demoPersistentServicePostNotificationDialogState.onConfirmation,
                                    demoPersistentServicePostNotificationDialogState.onDismissRequest
                                )
                            }
                        }
                    }
                }
            }
        }
        persistentServerPermissions.setRequireUserExplanationCallback { permission, continueRequest ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    viewModel.showDemoPersistentServicePostNotificationDialog(onConfirmation = continueRequest)
                }
            }
        }
        persistentServerPermissions.setPermissionsChangedStatusCallback { permission, newRequestStatus ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (newRequestStatus == BasePermissions.RequestStatus.DENIED) {
                        viewModel.showDemoPersistentServicePostNotificationDialog {
                            persistentServerPermissions.requestPermissions(this, permission)
                        }
                    }
                }
            }
        }
    }
}