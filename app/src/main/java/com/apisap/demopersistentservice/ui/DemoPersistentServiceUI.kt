package com.apisap.demopersistentservice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.apisap.demopersistentservice.R
import com.apisap.demopersistentservice.ui.states.DemoPersistentServiceUiStatesEnum
import com.apisap.demopersistentservice.ui.theme.DemoPersistentServiceTheme

private var currentBtnState = DemoPersistentServiceUiStatesEnum.START

@Composable
fun Modifier.DemoPersistentServiceUI(
    uiStatus: DemoPersistentServiceUiStatesEnum,
    onClickBtnStartStop: () -> Unit,
    btnEnabled: Boolean = true,
    log: String? = null,
) {

    val logs = remember {
        mutableListOf<String>()
    }

    log?.let {
        logs.add(it)
    }

    Column(
        modifier = fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = padding(16.dp)) {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 4.em,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.app_name)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Button(modifier = Modifier.fillMaxWidth(), enabled = btnEnabled, onClick = onClickBtnStartStop) {
                Text(text = stringResource(id = if (uiStatus == DemoPersistentServiceUiStatesEnum.START) R.string.text_start else R.string.text_stop))
            }
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxSize(),
                shape = ShapeDefaults.Small
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight(Alignment.Bottom)
                        .verticalScroll(rememberScrollState(), reverseScrolling = true)
                        .padding(8.dp),
                    text = logs.joinToString(separator = "\n"),
                    style = TextStyle(
                        color = Color.Green,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        letterSpacing = TextUnit(0.5F, TextUnitType.Sp)
                    )
                )
            }
        }
    }
}

@Composable
fun ExplainPostNotificationPermissionDialog(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.post_notification_permission_explain_title_user))
        },
        text = {
            Text(text = stringResource(R.string.post_notification_permission_explain_text_user))
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.text_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.text_dismiss))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DemoPersistentServicePreview() {
    DemoPersistentServiceTheme {
        Modifier.padding(
            start = 0.0.dp, top = 23.466667.dp, end = 0.0.dp, bottom = 14.933333.dp
        ).DemoPersistentServiceUI(uiStatus = currentBtnState, onClickBtnStartStop = {
            val newCurrentState = when(currentBtnState) {
                DemoPersistentServiceUiStatesEnum.START -> DemoPersistentServiceUiStatesEnum.STOP
                DemoPersistentServiceUiStatesEnum.STOP -> DemoPersistentServiceUiStatesEnum.START
            }
            currentBtnState = newCurrentState
        })
    }
}