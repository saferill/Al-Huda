package com.alhuda.app.core.presentation.mapping

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alhuda.app.core.domain.model.TextResource

@Composable
fun TextResource.asString() =
    when (this) {
        is TextResource.Literal -> value
        is TextResource.StringResId -> stringResource(id)
        is TextResource.StringResIdWithArgs -> stringResource(id, *formatArgs)
    }
