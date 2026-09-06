package com.alhuda.app.main.settings.calculation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.settings.ThemeColor
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.components.ACard

@Composable
fun ParamAdjustBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Preview
@Composable
private fun ParamAdjustBoxPreview() {
    Column(Modifier.padding(10.dp)) {
        AlHudaTheme(ThemeColor.Light) {
            ACard { cardPadding ->
                ParamAdjustBox(stringResource(R.string.fajr_angle), "16", Modifier.padding(cardPadding))
            }
        }
        AlHudaTheme(ThemeColor.Dark) {
            ACard { cardPadding ->
                ParamAdjustBox(stringResource(R.string.fajr_angle), "16", Modifier.padding(cardPadding))
            }
        }
    }
}
