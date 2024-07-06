package com.apisap.demopersistentservice

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiStatesEnum
import com.apisap.demopersistentservice.ui.theme.DemoPersistentServiceTheme
import com.apisap.demopersistentservice.viewmodels.DemoPersistentServiceViewModel
import com.apisap.persistentservice.activities.PersistentServiceActivity
import com.apisap.persistentservice.permissions.BasePermissions
import com.apisap.persistentservice.permissions.BasePermissions.RequestStatus
import com.apisap.persistentservice.services.PersistentServiceConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private var onRequireUserExplanationCallback: ((permission: String, continueRequest: () -> Unit) -> Unit) =
        { permission, continueRequest ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    viewModel.showDemoPersistentServicePostNotificationDialog(
                        confirmActionName = getString(
                            R.string.text_request_again
                        ), onConfirmation = continueRequest
                    )
                }
            }
        }

    private var onPermissionsChangedStatusCallback: ((permission: String, newRequestStatus: RequestStatus) -> Unit) =
        { permission, newRequestStatus ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (newRequestStatus == BasePermissions.RequestStatus.DENIED) {
                        viewModel.showDemoPersistentServicePostNotificationDialog(
                            confirmActionName = getString(
                                R.string.text_open_settings
                            )
                        ) {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                data = Uri.parse("package:$packageName")
                                addCategory(Intent.CATEGORY_DEFAULT)
                            }.let {
                                startActivity(it)
                            }
                        }
                    }
                }
            }
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
                                val (onConfirmation, onDismissRequest, confirmActionName) = demoPersistentServicePostNotificationDialogState
                                ExplainPostNotificationPermissionDialog(
                                    onConfirmation = onConfirmation,
                                    onDismissRequest = onDismissRequest,
                                    confirmActionName = confirmActionName,
                                )
                            }
                        }
                    }
                }
            }
        }
        persistentServerPermissions.setRequireUserExplanationCallback(
            onRequireUserExplanationCallback = onRequireUserExplanationCallback
        )
        persistentServerPermissions.setPermissionsChangedStatusCallback(
            onPermissionsChangedStatusCallback = onPermissionsChangedStatusCallback
        )
    }
}