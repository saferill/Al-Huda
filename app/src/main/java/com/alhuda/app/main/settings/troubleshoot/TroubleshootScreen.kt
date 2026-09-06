package com.alhuda.app.main.settings.troubleshoot

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.alhuda.app.R
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.InformationCard
import com.alhuda.app.core.presentation.components.InformationRow
import com.alhuda.app.core.presentation.components.PrimaryButton
import com.alhuda.app.core.presentation.components.SettingLabel
import com.alhuda.app.core.presentation.components.SettingLinkButton
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.core.presentation.util.annotatedStringResource
import com.alhuda.app.core.util.device.PowerManagerUtils

@SuppressLint("BatteryLife")
@Composable
fun TroubleshootScreen(
    uiState: TroubleshootUiState,
    onAction: (TroubleshootUiAction) -> Unit,
    modifier: Modifier = Modifier,
    advancedRoute: Route = Route.Main.Settings.Troubleshoot.AdvancedTroubleshoot,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val ignoreBatteryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { _ ->
            onAction(TroubleshootUiAction.OnLifecycleChanged)
        },
    )
    val dndAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { _ ->
            onAction(TroubleshootUiAction.OnLifecycleChanged)
        },
    )

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            onAction(TroubleshootUiAction.OnLifecycleChanged)
        }
    }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
    ) {

        ACard { cardPadding ->
            Column(
                Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            ) {
                SettingLabel(stringResource(R.string.battery_problem_title))
                Text(stringResource(R.string.battery_problem_body), style = MaterialTheme.typography.bodyMedium)

                BatterSaverCTA(isAllowed = uiState.appIsAllowedToKeepRunning) {
                    when {
                        uiState.isTelevision -> safeLaunch(context) {
                            ignoreBatteryLauncher.launch(PowerManagerUtils.tvEnergySettingsIntent(context))
                        }

                        uiState.appIsAllowedToKeepRunning ->
                            onAction(TroubleshootUiAction.OnAppIsAllowedToKeepRunningClick(activity))

                        else -> safeLaunch(context) {
                            ignoreBatteryLauncher.launch(
                                Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    ("package:" + context.packageName).toUri(),
                                ),
                            )
                        }
                    }
                }
            }
        }

        uiState.powerManagerInfo?.let {
            val samsungHintRes = if (it.manufacturer.equals("samsung", ignoreCase = true)) {
                samsungHintRes()
            } else {
                null
            }
            PowerManagerCard(samsungHintRes = samsungHintRes) {
                onAction(TroubleshootUiAction.OnOpenPowerManagerSettingsClick(activity))
            }
        }

        if (!uiState.isTelevision) {
            DndAccessCard(granted = uiState.dndAccessGranted) {
                safeLaunch(context) {
                    dndAccessLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
        }

        if (uiState.autostartAvailable) {
            AutostartCard {
                onAction(TroubleshootUiAction.OnOpenAutostartSettingsClick(activity))
            }
        }

        SettingLinkButton(stringResource(R.string.advanced)) {
            onAction(TroubleshootUiAction.OnAdvancedSettingsClick(advancedRoute))
        }

        if (!uiState.isTelevision) {
            InformationCard(Modifier.fillMaxWidth()) {
                Text(annotatedStringResource(R.string.battery_problem_final_hint, "https://dontkillmyapp.com", "dontkillmyapp.com"))
            }
        }
    }
}

private fun safeLaunch(
    context: Context,
    launch: () -> Unit,
) {
    try {
        launch()
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun BatterSaverCTA(
    isAllowed: Boolean,
    onButtonClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Column {
            if (isAllowed) {
                OutlinedButton(onClick = onButtonClick) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_padding)),
                    ) {
                        Text(stringResource(R.string.app_is_allowed_to_keep_running))
                        Icon(painterResource(R.drawable.baseline_check_24), contentDescription = null)
                    }
                }
            } else {
                PrimaryButton(onClick = onButtonClick) {
                    Text(stringResource(R.string.allow_app_to_keep_running))
                }
            }
        }
    }
}

@Composable
fun DndAccessCard(
    granted: Boolean,
    onButtonClick: () -> Unit,
) {
    ACard { cardPadding ->
        Column(
            Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingLabel(stringResource(R.string.dnd_access_card_title))
                DndStatusBadge(granted)
            }
            Text(stringResource(R.string.dnd_access_card_body), style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (granted) {
                    OutlinedButton(onClick = onButtonClick) {
                        Text(stringResource(R.string.dnd_access_card_button))
                    }
                } else {
                    PrimaryButton(onButtonClick) {
                        Text(stringResource(R.string.dnd_access_card_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun DndStatusBadge(granted: Boolean) {
    val color = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_padding)),
    ) {
        Icon(
            painterResource(if (granted) R.drawable.baseline_check_24 else R.drawable.baseline_close_24),
            contentDescription = null,
            tint = color,
        )
        Text(
            stringResource(if (granted) R.string.dnd_access_status_granted else R.string.dnd_access_status_missing),
            color = color,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun AutostartCard(onButtonClick: () -> Unit) {
    ACard { cardPadding ->
        Column(
            Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            SettingLabel(stringResource(R.string.autostart_card_title))
            Text(stringResource(R.string.autostart_card_body), style = MaterialTheme.typography.bodyMedium)

            ACard(tonalElevation = 2.dp) { innerCardPadding ->
                InformationRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(innerCardPadding),
                    iconDescription = null,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.autostart_card_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PrimaryButton(onButtonClick) {
                    Text(stringResource(R.string.open_autostart_settings))
                }
            }
        }
    }
}

@Composable
fun PowerManagerCard(
    @StringRes samsungHintRes: Int? = null,
    onButtonClick: () -> Unit,
) {
    ACard { cardPadding ->
        Column(
            Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            SettingLabel(stringResource(R.string.power_manager_card_title))
            Text(stringResource(R.string.power_manager_card_body), style = MaterialTheme.typography.bodyMedium)

            if (samsungHintRes != null) {
                Text(stringResource(samsungHintRes), style = MaterialTheme.typography.bodyMedium)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PrimaryButton(onButtonClick) {
                    Text(stringResource(R.string.open_power_manager_settings))
                }
            }
        }
    }
}

@StringRes
private fun samsungHintRes(): Int =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> R.string.power_manager_samsung_hint_oneui6
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> R.string.power_manager_samsung_hint_oneui4
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> R.string.power_manager_samsung_hint_oneui1
        else -> R.string.power_manager_samsung_hint_oreo
    }

@Preview
@Composable
private fun BatterSaverCTAPreview() {
    AlHudaTheme {
        ACard { cardPadding ->
            Column(Modifier.padding(cardPadding)) {
                BatterSaverCTA(true) {}
                BatterSaverCTA(false) {}
            }
        }
    }
}

@Preview(name = "Autostart")
@Preview(name = "Autostart (fa)", locale = "fa")
@Composable
private fun AutostartCardPreview() {
    AlHudaTheme {
        AutostartCard {}
    }
}

@Preview(name = "PowerManager - generic")
@Composable
private fun PowerManagerCardPreview() {
    AlHudaTheme {
        PowerManagerCard {}
    }
}

@Preview(name = "PowerManager - Samsung Oreo")
@Preview(name = "PowerManager - Samsung Oreo (fa)", locale = "fa")
@Composable
private fun PowerManagerCardSamsungOreoPreview() {
    AlHudaTheme {
        PowerManagerCard(samsungHintRes = R.string.power_manager_samsung_hint_oreo) {}
    }
}

@Preview(name = "PowerManager - Samsung One UI 1-3")
@Preview(name = "PowerManager - Samsung One UI 1-3 (fa)", locale = "fa")
@Composable
private fun PowerManagerCardSamsungOneUi1Preview() {
    AlHudaTheme {
        PowerManagerCard(samsungHintRes = R.string.power_manager_samsung_hint_oneui1) {}
    }
}

@Preview(name = "PowerManager - Samsung One UI 4-5")
@Preview(name = "PowerManager - Samsung One UI 4-5 (fa)", locale = "fa")
@Composable
private fun PowerManagerCardSamsungOneUi4Preview() {
    AlHudaTheme {
        PowerManagerCard(samsungHintRes = R.string.power_manager_samsung_hint_oneui4) {}
    }
}

@Preview(name = "PowerManager - Samsung One UI 6+")
@Preview(name = "PowerManager - Samsung One UI 6+ (fa)", locale = "fa")
@Composable
private fun PowerManagerCardSamsungOneUi6Preview() {
    AlHudaTheme {
        PowerManagerCard(samsungHintRes = R.string.power_manager_samsung_hint_oneui6) {}
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF00585A,
)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF00585A,
    device = Devices.TABLET,
)
@Composable
private fun TroubleshootPreview() {
    AlHudaTheme {
        TroubleshootScreen(
            uiState = TroubleshootUiState(true, PowerManagerUtils.PowerManagerInfo("SAMSUNG", "foo", "foo", "foo")),
            onAction = {},
        )
    }
}
