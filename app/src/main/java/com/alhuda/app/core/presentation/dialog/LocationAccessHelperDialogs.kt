package com.alhuda.app.core.presentation.dialog

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.presentation.components.CtaDialog
import com.alhuda.app.core.util.android.LocationUtils
import kotlinx.coroutines.launch

@Composable
fun rememberLocationAccessHelperDialogs(

    requireBackground: Boolean = false,
    onLocation: ((CalculationLocationDetail) -> Unit)? = null,

    onPermissionGranted: ((forceFresh: Boolean) -> Unit)? = null,
): (Boolean) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showLocationDisabledDialog = remember { mutableStateOf(false) }
    val showPermissionDeniedDialog = remember { mutableStateOf(false) }

    val showBackgroundDisclosureDialog = remember { mutableStateOf(false) }
    val showBackgroundDeniedDialog = remember { mutableStateOf(false) }

    val showDisclosureDialog = remember { mutableStateOf(false) }

    val showFeedbackForPendingRequest = remember { mutableStateOf(true) }

    val onAccessGranted = {
        showPermissionDeniedDialog.value = false
        showBackgroundDeniedDialog.value = false
        onPermissionGranted?.invoke(showFeedbackForPendingRequest.value)
        if (onLocation != null) {
            scope.launch {
                val result = LocationUtils.requestCurrentLocation(context).getOrNull()
                if (result != null) {
                    onLocation(
                        CalculationLocationDetail(
                            lat = result.latitude,
                            long = result.longitude,
                        ),
                    )
                }
            }
        }
    }

    val backgroundGranted = {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val bgPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            onAccessGranted()
        } else if (showFeedbackForPendingRequest.value) {
            showBackgroundDeniedDialog.value = true
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (requireBackground && !backgroundGranted()) {
                if (showFeedbackForPendingRequest.value) {
                    showBackgroundDisclosureDialog.value = true
                } else {
                    bgPermLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                onAccessGranted()
            }
        } else if (showFeedbackForPendingRequest.value) {
            showPermissionDeniedDialog.value = true
        }
    }

    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (LocationUtils.isLocationEnabled(context)) {
            showLocationDisabledDialog.value = false
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val proceedToRequest = remember {
        {
            if (!LocationUtils.isLocationEnabled(context)) {
                if (showFeedbackForPendingRequest.value) showLocationDisabledDialog.value = true
            } else {
                permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val triggerLocation = remember {
        { showFeedback: Boolean ->
            showFeedbackForPendingRequest.value = showFeedback

            val foregroundGranted =
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (foregroundGranted) {
                proceedToRequest()
            } else {
                showDisclosureDialog.value = true
            }
        }
    }

    if (showDisclosureDialog.value) {
        CtaDialog(
            title = stringResource(R.string.location_disclosure_title),
            text = stringResource(R.string.location_disclosure_text),
            confirmLabel = stringResource(R.string.continue_label),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                showDisclosureDialog.value = false
                proceedToRequest()
            },
            onDismissRequest = {
                showDisclosureDialog.value = false
            },
        )
    }

    if (showLocationDisabledDialog.value) {
        CtaDialog(
            title = stringResource(R.string.location_service_required_title),
            text = stringResource(R.string.location_service_required_text),
            confirmLabel = stringResource(R.string.open_settings_label),
            dismissLabel = stringResource(R.string.okay),
            onConfirm = {
                try {
                    activityLauncher.launch(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        },
                    )
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        R.string.open_settings_failed,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            onDismissRequest = {
                showLocationDisabledDialog.value = false
            },
        )
    }

    if (showPermissionDeniedDialog.value) {
        CtaDialog(
            title = stringResource(R.string.permission_denied),
            text = stringResource(R.string.location_permission_denied_text),
            confirmLabel = stringResource(R.string.open_settings_label),
            dismissLabel = stringResource(R.string.okay),
            onConfirm = {
                try {
                    activityLauncher.launch(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        },
                    )
                    Toast.makeText(
                        context,
                        R.string.open_permissions_guidance,
                        Toast.LENGTH_LONG,
                    ).show()
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        R.string.open_settings_failed,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            onDismissRequest = {
                showPermissionDeniedDialog.value = false
            },
        )
    }

    if (showBackgroundDisclosureDialog.value) {
        CtaDialog(
            title = stringResource(R.string.background_location_required_title),
            text = stringResource(R.string.background_location_required_text),
            confirmLabel = stringResource(R.string.continue_label),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                showBackgroundDisclosureDialog.value = false
                bgPermLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            },
            onDismissRequest = {
                showBackgroundDisclosureDialog.value = false
            },
        )
    }

    if (showBackgroundDeniedDialog.value) {
        CtaDialog(
            title = stringResource(R.string.background_location_required_title),
            text = stringResource(R.string.background_location_required_text),
            confirmLabel = stringResource(R.string.open_settings_label),
            dismissLabel = stringResource(R.string.okay),
            onConfirm = {
                try {
                    activityLauncher.launch(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        },
                    )
                    Toast.makeText(
                        context,
                        R.string.open_permissions_guidance,
                        Toast.LENGTH_LONG,
                    ).show()
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        R.string.open_settings_failed,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            onDismissRequest = {
                showBackgroundDeniedDialog.value = false
            },
        )
    }

    return triggerLocation
}
