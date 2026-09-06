package com.alhuda.app.core.data.mapping

import android.content.res.Resources
import com.alhuda.app.core.domain.model.TextResource

fun TextResource.asString(resources: Resources): String =
    when (this) {
        is TextResource.Literal -> value

        is TextResource.StringResId -> resources.getString(id)

        is TextResource.StringResIdWithArgs ->
            resources.getString(
                id,

                *formatArgs.map { if (it is TextResource) it.asString(resources) else it }.toTypedArray(),
            )
    }
