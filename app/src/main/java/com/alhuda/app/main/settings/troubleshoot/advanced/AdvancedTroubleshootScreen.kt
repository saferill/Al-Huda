package com.alhuda.app.main.settings.troubleshoot.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alhuda.app.R
import com.alhuda.app.core.presentation.AlHudaThemePreview
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.InformationRow
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.components.SettingHeader
import com.alhuda.app.core.presentation.components.SettingSwitch
import com.alhuda.app.core.presentation.dialog.PermissionStep
import com.alhuda.app.core.presentation.dialog.SchedulingPermission
import com.alhuda.app.core.presentation.dialog.rememberSchedulingPermissionRequest
import com.alhuda.app.core.presentation.navigation.NavigationController

@Composable
fun AdvancedTroubleshootScreen(
    uiState: AdvancedTroubleshootUiState,
    onAction: (AdvancedTroubleshootUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {

    val requestNotifications = rememberSchedulingPermissionRequest(
        isDontAskAgain = { false },
        onDontAskAgain = {},
        onComplete = { results ->
            if (!results.requiredAllGranted()) onAction(AdvancedTroubleshootUiAction.OnAdaptiveChargingToggle(false))
        },
    )
    ScreenScaffold(
        title = stringResource(R.string.advanced_title),
        onBackClick = { NavigationController.navigateBack() },
        modifier = modifier,
    ) {
        ACard { cardPadding ->
            Column(
                Modifier.padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            ) {
                SettingHeader(stringResource(R.string.adaptive_charging), stringResource(R.string.adaptive_charging_help))

                InformationRow(
                    Modifier.fillMaxWidth(),
                    iconDescription = null,
                ) {
                    Text(stringResource(R.string.adaptive_charging_caution))
                }
                SettingSwitch(
                    title = stringResource(R.string.different_alarm_enable),
                    subtitle = null,
                    checked = uiState.useDifferentAlarmType,
                    onCheckedChange = { enabled ->
                        onAction(AdvancedTroubleshootUiAction.OnAdaptiveChargingToggle(enabled))
                        if (enabled) {
                            requestNotifications(
                                listOf(
                                    PermissionStep(
                                        SchedulingPermission.Notification,
                                        R.string.different_alarm_notification_rationale,
                                        R.string.adhan_notification_permission_denied_text,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdvancedTroubleshootScreenPreview() {
    AlHudaThemePreview {
        AdvancedTroubleshootScreen(uiState = AdvancedTroubleshootUiState(), onAction = {})
    }
}
