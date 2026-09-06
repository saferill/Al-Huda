package com.alhuda.app.widget

import com.alhuda.app.core.domain.model.widget.NextPrayerWidgetData

object NextPrayerRenderCache {
    @Volatile
    var lastData: NextPrayerWidgetData? = null
}
