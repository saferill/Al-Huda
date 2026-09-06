package com.alhuda.app.widget

import com.alhuda.app.core.domain.model.widget.WidgetData

object WidgetRenderCache {
    @Volatile
    var lastData: WidgetData? = null
}
