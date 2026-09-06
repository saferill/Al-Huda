package com.alhuda.app.widget

import java.util.concurrent.ConcurrentHashMap

object CustomWidgetPageState {
    private val indexByWidgetId = ConcurrentHashMap<Int, Int>()

    fun get(appWidgetId: Int): Int = indexByWidgetId[appWidgetId] ?: 0

    fun set(
        appWidgetId: Int,
        index: Int,
    ) {
        indexByWidgetId[appWidgetId] = index
    }
}
