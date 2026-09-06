package com.alhuda.app.widget

import com.alhuda.app.core.domain.model.widget.CustomWidgetData

object CustomWidgetRenderCache {
    @Volatile
    var lastData: CustomWidgetData? = null
}
