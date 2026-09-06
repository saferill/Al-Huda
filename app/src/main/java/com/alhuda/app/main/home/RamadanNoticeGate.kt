package com.alhuda.app.main.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.alhuda.app.R

@Composable
fun RamadanNoticeGate(
    shouldShow: suspend () -> Boolean,
    onRemindNextYear: () -> Unit,
    onDontShowAgain: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = shouldShow() }

    if (visible) {
        RamadanNoticeDialog(
            onRemindNextYear = {
                visible = false
                onRemindNextYear()
            },
            onDontShowAgain = {
                visible = false
                onDontShowAgain()
            },
            onDismiss = { visible = false },
        )
    }
}

@Composable
private fun RamadanNoticeDialog(
    onRemindNextYear: () -> Unit,
    onDontShowAgain: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ramadan_notice_title), style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = stringResource(R.string.lunar_calendar_warning)) },
        confirmButton = {
            TextButton(onClick = onRemindNextYear) { Text(stringResource(R.string.ramadan_remind_next_year)) }
        },
        dismissButton = {
            TextButton(onClick = onDontShowAgain) {
                Text(
                    stringResource(R.string.ramadan_dont_show_again),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
