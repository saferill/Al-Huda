package com.alhuda.app.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.alhuda.app.R

@Composable
fun AlHudaThemePreview(content: @Composable () -> Unit) {
    AlHudaTheme {
        Surface(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            content()
        }
    }
}

@Composable
fun AlHudaThemePaddedPreview(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlHudaTheme {
        Surface {
            Column(
                modifier = modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(dimensionResource(R.dimen.page_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
                content = content,
            )
        }
    }
}

const val LOREM_IMPSUM_SHORT =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut ac purus dolor. Curabitur efficitur hendrerit suscipit. Phasellus auctor blandit lacus, et sollicitudin sem consequat at. Suspendisse accumsan eros diam, eu laoreet est efficitur dignissim."
