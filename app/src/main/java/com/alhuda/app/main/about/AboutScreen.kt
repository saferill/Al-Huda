package com.alhuda.app.main.about

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alhuda.app.BuildConfig
import com.alhuda.app.R
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.components.SettingHeader
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.presentation.navigation.Route

private const val DEV_UNLOCK_TAPS = 5
private const val GITHUB_REPO_URL = "https://github.com/saferill/Al-Huda"
private const val GITHUB_ISSUES_URL = "https://github.com/saferill/Al-Huda/issues"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onAction: (AboutUiAction) -> Unit = {}) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val tapCount = remember { intArrayOf(0) }

    ScreenScaffold(
        title = stringResource(R.string.about),
        onBackClick = { NavigationController.navigateBack() },
    ) {
        ACard { cardPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_alhuda),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            tapCount[0]++
                            if (tapCount[0] == DEV_UNLOCK_TAPS) {
                                tapCount[0] = 0
                                onAction(AboutUiAction.OnUnlockDeveloper)
                                Toast.makeText(context, R.string.dev_unlocked_toast, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
                HorizontalDivider()
                SettingHeader(stringResource(R.string.developer_name_title), stringResource(R.string.developer_name))
                HorizontalDivider()
                Text(
                    stringResource(R.string.app_about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { NavigationController.navigateTo(Route.Main.PrivacyPolicy) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.privacy_policy_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider()
                SettingHeader(stringResource(R.string.license), stringResource(R.string.license_type))
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(GITHUB_REPO_URL) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingHeader(
                        stringResource(R.string.source_code),
                        "github.com/saferill/Al-Huda",
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(GITHUB_ISSUES_URL) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingHeader(
                        stringResource(R.string.report_issue),
                        "github.com/saferill/Al-Huda/issues",
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider()
                Text(
                    stringResource(R.string.about_credits),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
