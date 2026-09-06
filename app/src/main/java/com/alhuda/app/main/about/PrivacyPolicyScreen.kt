package com.alhuda.app.main.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alhuda.app.R
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.navigation.NavigationController

@Composable
fun PrivacyPolicyScreen() {
    ScreenScaffold(
        title = stringResource(R.string.privacy_policy_title),
        onBackClick = { NavigationController.navigateBack() },
    ) {
        ACard { cardPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Privacy & Security Policy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Al-Huda is an open-source Islamic companion application developed by saferill. This app is designed with a strict privacy-first principle: your personal data belongs solely to you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                )

                HorizontalDivider()

                Text(
                    text = "1. Zero Data Collection & No Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "• Al-Huda does not collect, store, track, or share any personal data.\n• The application contains no advertisements, no third-party tracking libraries, and no hidden analytics services.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )

                HorizontalDivider()

                Text(
                    text = "2. Device Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "• Location (GPS / Network): Used strictly for local prayer time calculations and Qibla compass alignment. Coordinates are processed 100% on-device and never transmitted to any external server.\n• Exact Alarms & Notifications: Used to play the Adhan and deliver prayer reminders accurately on time.\n• Internet Access: Used exclusively for streaming Qur'an recitation audio and downloading selected translations. No user identity is attached to network requests.\n• Local Storage: Used only to save your preferences (prayer calculation methods, themes, and Qur'an bookmarks) locally on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )

                HorizontalDivider()

                Text(
                    text = "3. Open Source & Auditability",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "As Free and Open Source Software (FOSS), the complete source code of Al-Huda is open for public review and security auditing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )

                HorizontalDivider()

                Text(
                    text = "4. Developer Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "For inquiries, suggestions, or security audits, contact saferill via the official Al-Huda repository.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}
