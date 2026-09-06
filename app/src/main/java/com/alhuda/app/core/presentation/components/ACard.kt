package com.alhuda.app.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.settings.ThemeColor
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.LOREM_IMPSUM_SHORT

@Composable
fun ACard(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    compact: Boolean = false,
    cardPadding: PaddingValues = if (compact) {
        PaddingValues(
            vertical = dimensionResource(R.dimen.card_padding_vc),
            horizontal = dimensionResource(R.dimen.card_padding_hc),
        )
    } else {
        PaddingValues(
            vertical = dimensionResource(R.dimen.card_padding_v),
            horizontal = dimensionResource(R.dimen.card_padding_h),
        )
    },
    content: @Composable (cardPadding: PaddingValues) -> Unit,
) {
    Surface(
        modifier,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        shape = MaterialTheme.shapes.medium,
    ) {
        content(cardPadding)
    }
}

@Composable
fun ACard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    compact: Boolean = false,
    cardPadding: PaddingValues = if (compact) {
        PaddingValues(
            vertical = dimensionResource(R.dimen.card_padding_vc),
            horizontal = dimensionResource(R.dimen.card_padding_hc),
        )
    } else {
        PaddingValues(
            vertical = dimensionResource(R.dimen.card_padding_v),
            horizontal = dimensionResource(R.dimen.card_padding_h),
        )
    },
    content: @Composable (cardPadding: PaddingValues) -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        shape = MaterialTheme.shapes.medium,
    ) {
        content(cardPadding)
    }
}

@Preview
@Composable
private fun CardPreview() {
    AlHudaTheme {
        ACard { cardPadding ->
            Column(Modifier.padding(cardPadding)) {
                Text(LOREM_IMPSUM_SHORT)
            }
        }
    }
}

@Preview
@Composable
private fun CardOnBackgroundPreview() {
    AlHudaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp)) {
                ACard { cardPadding ->
                    Column(Modifier.padding(cardPadding)) {
                        Text(LOREM_IMPSUM_SHORT)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CardOnBackgroundClassicLightPreview() {
    AlHudaTheme(themeColor = ThemeColor.ClassicLight) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp)) {
                ACard { cardPadding ->
                    Column(Modifier.padding(cardPadding)) {
                        Text(LOREM_IMPSUM_SHORT)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CardOnBackgroundClassicDarkPreview() {
    AlHudaTheme(themeColor = ThemeColor.ClassicDark) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp)) {
                ACard { cardPadding ->
                    Column(Modifier.padding(cardPadding)) {
                        Text(LOREM_IMPSUM_SHORT)
                    }
                }
            }
        }
    }
}
