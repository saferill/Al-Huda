package com.alhuda.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.alhuda.app.MainActivity
import com.alhuda.app.R
import com.alhuda.app.core.data.locale.withAppLocale
import com.alhuda.app.core.domain.model.widget.CustomWidgetData
import com.alhuda.app.core.domain.model.widget.CustomWidgetPrayerCell
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.widget.CustomWidgetRenderer.buildEmptyHint
import java.util.Locale

object CustomWidgetRenderer {

    const val ACTION_PAGE = "com.alhuda.app.action.CUSTOM_WIDGET_PAGE"
    const val EXTRA_DELTA = "com.alhuda.app.extra.CUSTOM_WIDGET_PAGE_DELTA"

    fun build(
        context: Context,
        data: CustomWidgetData,
        pageIndex: Int = 0,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    ): RemoteViews {
        val colors = Colors(data.bgColor, data.textColor, data.highlightColor, data.countdownColor)
        return if (data.pages.size > 1) {
            buildPaged(context, data, colors, pageIndex, appWidgetId)
        } else {
            buildInline(context, data, colors)
        }
    }

    private class Colors(
        @param:ColorInt val bg: Int?,
        @param:ColorInt val text: Int?,
        @param:ColorInt val highlight: Int?,
        @param:ColorInt val countdown: Int?,
    )

    private fun RemoteViews.applyBgColor(
        viewId: Int,
        @ColorInt color: Int?,
    ) {
        if (color != null) setInt(viewId, "setBackgroundColor", color)
    }

    private fun RemoteViews.applyTextColor(
        viewId: Int,
        @ColorInt color: Int?,
    ) {
        if (color != null) setTextColor(viewId, color)
    }

    private fun buildInline(
        context: Context,
        data: CustomWidgetData,
        colors: Colors,
    ): RemoteViews {
        val localized = context.withAppLocale(data.locale)
        val views = RemoteViews(context.packageName, R.layout.custom_widget)
        views.applyBgColor(R.id.custom_widget_root, colors.bg)

        val direction = layoutDirection(data.locale)
        views.setInt(R.id.custom_widget_header, "setLayoutDirection", direction)
        views.setInt(R.id.custom_widget_content, "setLayoutDirection", direction)

        addHeaderCells(
            context,
            views,
            R.id.custom_widget_header,
            data.topStartText,
            data.topEndText,
            colors.text,
            BASE_TEXT_SP * data.headerFontScale,
        )
        addPrayerRows(
            context,
            views,
            R.id.custom_widget_content,
            data.prayerRows,
            colors,
            localized,
            BASE_TEXT_SP * data.prayerFontScale,
        )
        applyCountdown(views, data, colors, localized, BASE_TEXT_SP * data.countdownFontScale)

        val hasHeader = data.topStartText != null || data.topEndText != null
        views.setViewVisibility(R.id.custom_widget_header, if (hasHeader) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.custom_widget_content, if (data.prayerRows.isNotEmpty()) View.VISIBLE else View.GONE)

        val empty = data.prayerRows.isEmpty() &&
            data.topStartText == null && data.topEndText == null &&
            !(data.showCountdown && data.countdown != null)
        if (empty) {
            views.setViewVisibility(R.id.cw_empty_hint, View.VISIBLE)
            views.setTextViewText(R.id.cw_empty_hint, localized.getString(R.string.custom_widget_empty_hint))
            views.applyTextColor(R.id.cw_empty_hint, colors.text)
        } else {
            views.setViewVisibility(R.id.cw_empty_hint, View.GONE)
        }

        views.setOnClickPendingIntent(
            R.id.custom_widget_root,
            if (empty) builderPendingIntent(context) else launchPendingIntent(context),
        )
        return views
    }

    fun buildEmptyHint(context: Context): RemoteViews =
        buildHint(context, context.getString(R.string.custom_widget_empty_hint), builderPendingIntent(context))

    fun buildConfigHint(
        context: Context,
        @StringRes messageRes: Int,
    ): RemoteViews = buildHint(context, context.getString(messageRes), launchPendingIntent(context))

    private fun buildHint(
        context: Context,
        message: String,
        onClick: PendingIntent,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.custom_widget)
        views.removeAllViews(R.id.custom_widget_header)
        views.removeAllViews(R.id.custom_widget_content)

        views.setViewVisibility(R.id.custom_widget_header, View.GONE)
        views.setViewVisibility(R.id.custom_widget_content, View.GONE)
        views.setViewVisibility(R.id.custom_widget_countdown_row, View.GONE)

        views.setViewVisibility(R.id.cw_empty_hint, View.VISIBLE)
        views.setTextViewText(R.id.cw_empty_hint, message)
        views.setOnClickPendingIntent(R.id.custom_widget_root, onClick)
        return views
    }

    private fun buildPaged(
        context: Context,
        data: CustomWidgetData,
        colors: Colors,
        pageIndex: Int,
        appWidgetId: Int,
    ): RemoteViews {
        val localized = context.withAppLocale(data.locale)
        val pageCount = data.pages.size
        val index = ((pageIndex % pageCount) + pageCount) % pageCount
        val page = data.pages[index]

        val views = RemoteViews(context.packageName, R.layout.custom_widget_paged)
        views.applyBgColor(R.id.cw_paged_root, colors.bg)
        val direction = layoutDirection(data.locale)
        views.setInt(R.id.cw_paged_header, "setLayoutDirection", direction)
        views.setInt(R.id.cw_paged_content, "setLayoutDirection", direction)
        addHeaderCells(
            context,
            views,
            R.id.cw_paged_header,
            page.topStartText,
            page.topEndText,
            colors.text,
            BASE_TEXT_SP * data.headerFontScale,
        )
        addPrayerRows(
            context,
            views,
            R.id.cw_paged_content,
            page.prayerRows,
            colors,
            localized,
            BASE_TEXT_SP * data.prayerFontScale,
        )

        applyCountdown(views, data, colors, localized, BASE_TEXT_SP * data.countdownFontScale)

        val hasHeader = page.topStartText != null || page.topEndText != null
        views.setViewVisibility(R.id.cw_paged_header, if (hasHeader) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.cw_paged_content, if (page.prayerRows.isNotEmpty()) View.VISIBLE else View.GONE)

        val dotLit = colors.highlight ?: ContextCompat.getColor(context, R.color.custom_widget_highlight)
        val dotUnlit = withAlpha(colors.text ?: ContextCompat.getColor(context, R.color.custom_widget_text), 0x66)
        views.removeAllViews(R.id.cw_paged_dots)
        repeat(pageCount) { dotIndex ->
            val dot = RemoteViews(context.packageName, R.layout.custom_widget_dot)
            dot.setTextColor(R.id.cw_dot, if (dotIndex == index) dotLit else dotUnlit)
            dot.setViewPadding(R.id.cw_dot, DOT_GAP_PX, 0, DOT_GAP_PX, 0)
            views.addView(R.id.cw_paged_dots, dot)
        }

        views.applyTextColor(R.id.cw_paged_prev, colors.text)
        views.applyTextColor(R.id.cw_paged_next, colors.text)
        views.setOnClickPendingIntent(R.id.cw_paged_prev, pageIntent(context, appWidgetId, -1))
        views.setOnClickPendingIntent(R.id.cw_paged_next, pageIntent(context, appWidgetId, +1))

        views.setOnClickPendingIntent(R.id.cw_paged_root, launchPendingIntent(context))
        return views
    }

    private fun addHeaderCells(
        context: Context,
        target: RemoteViews,
        containerId: Int,
        topStartText: String?,
        topEndText: String?,
        @ColorInt text: Int?,
        sizeSp: Float,
    ) {

        target.removeAllViews(containerId)
        listOfNotNull(topStartText, topEndText).forEach { cellText ->
            val cell = RemoteViews(context.packageName, R.layout.custom_widget_header_cell)
            cell.setTextViewText(R.id.cw_header_text, cellText)
            cell.applyTextColor(R.id.cw_header_text, text)
            cell.setTextViewTextSize(R.id.cw_header_text, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            target.addView(containerId, cell)
        }
    }

    private fun addPrayerRows(
        context: Context,
        target: RemoteViews,
        containerId: Int,
        rows: List<List<CustomWidgetPrayerCell>>,
        colors: Colors,
        localized: Context,
        sizeSp: Float,
    ) {
        target.removeAllViews(containerId)
        rows.forEach { row ->
            val rowViews = RemoteViews(context.packageName, R.layout.custom_widget_prayer_row)
            row.forEach { cell ->

                val color = if (cell.isActive) colors.highlight else colors.text
                val layout = if (cell.isActive && colors.highlight == null) {
                    R.layout.custom_widget_prayer_col_active
                } else {
                    R.layout.custom_widget_prayer_col
                }
                val col = RemoteViews(context.packageName, layout)
                col.setTextViewText(R.id.cw_col_name, localized.getString(cell.prayer.stringRes))
                col.setTextViewText(R.id.cw_col_time, cell.timeText)
                col.applyTextColor(R.id.cw_col_name, color)
                col.applyTextColor(R.id.cw_col_time, color)
                col.setTextViewTextSize(R.id.cw_col_name, TypedValue.COMPLEX_UNIT_SP, sizeSp)
                col.setTextViewTextSize(R.id.cw_col_time, TypedValue.COMPLEX_UNIT_SP, sizeSp)
                rowViews.addView(R.id.cw_prayer_row, col)
            }
            target.addView(containerId, rowViews)
        }
    }

    private fun applyCountdown(
        views: RemoteViews,
        data: CustomWidgetData,
        colors: Colors,
        localized: Context,
        sizeSp: Float,
    ) {
        if (data.showCountdown && data.countdown != null) {
            views.setViewVisibility(R.id.custom_widget_countdown_row, View.VISIBLE)
            views.setTextViewText(
                R.id.cw_countdown_label,
                "${localized.getString(data.countdown.prayer.stringRes)}: ",
            )

            views.applyTextColor(R.id.cw_countdown_label, colors.text)
            views.applyTextColor(R.id.cw_countdown, colors.countdown)
            views.setTextViewTextSize(R.id.cw_countdown_label, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            views.setTextViewTextSize(R.id.cw_countdown, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            val base = SystemClock.elapsedRealtime() + (data.countdown.baseMillis - System.currentTimeMillis())
            views.setChronometer(R.id.cw_countdown, base, null, true)
        } else {
            views.setViewVisibility(R.id.custom_widget_countdown_row, View.GONE)
        }
    }

    @ColorInt
    private fun withAlpha(
        @ColorInt color: Int,
        alpha: Int,
    ): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    private fun layoutDirection(locale: String): Int =
        if (TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(locale)) == View.LAYOUT_DIRECTION_RTL) {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }

    private fun pageIntent(
        context: Context,
        appWidgetId: Int,
        delta: Int,
    ): PendingIntent {
        val intent = Intent(context, CustomWidget::class.java).apply {
            action = ACTION_PAGE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(EXTRA_DELTA, delta)

            data = Uri.parse("alhuda://custom-widget/$appWidgetId/$delta")
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun launchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun builderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Route.Main.Settings.WidgetSettings.CustomBuilder.toUriString().toUri()
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val DOT_GAP_PX = 4

    private const val BASE_TEXT_SP = 13f
}
