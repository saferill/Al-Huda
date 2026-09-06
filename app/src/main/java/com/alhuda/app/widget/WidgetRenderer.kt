package com.alhuda.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.StringRes
import com.alhuda.app.MainActivity
import com.alhuda.app.R
import com.alhuda.app.core.data.locale.withAppLocale
import com.alhuda.app.core.domain.model.widget.NextPrayerWidgetData
import com.alhuda.app.core.domain.model.widget.WidgetData

object WidgetRenderer {

    private const val MAX_SLOTS = 6

    private val slotContainerIds = intArrayOf(
        R.id.prayer1,
        R.id.prayer1active,
        R.id.prayer2,
        R.id.prayer2active,
        R.id.prayer3,
        R.id.prayer3active,
        R.id.prayer4,
        R.id.prayer4active,
        R.id.prayer5,
        R.id.prayer5active,
        R.id.prayer6,
        R.id.prayer6active,
    )

    private val slotNameIds = intArrayOf(
        R.id.prayer1_name,
        R.id.prayer1active_name,
        R.id.prayer2_name,
        R.id.prayer2active_name,
        R.id.prayer3_name,
        R.id.prayer3active_name,
        R.id.prayer4_name,
        R.id.prayer4active_name,
        R.id.prayer5_name,
        R.id.prayer5active_name,
        R.id.prayer6_name,
        R.id.prayer6active_name,
    )

    private val slotTimeIds = intArrayOf(
        R.id.prayer1_time,
        R.id.prayer1active_time,
        R.id.prayer2_time,
        R.id.prayer2active_time,
        R.id.prayer3_time,
        R.id.prayer3active_time,
        R.id.prayer4_time,
        R.id.prayer4active_time,
        R.id.prayer5_time,
        R.id.prayer5active_time,
        R.id.prayer6_time,
        R.id.prayer6active_time,
    )

    fun build(
        context: Context,
        layoutResId: Int,
        data: WidgetData,
    ): RemoteViews {

        val localized = context.withAppLocale(data.locale)
        val views = RemoteViews(context.packageName, layoutResId)

        val direction = if (data.swapLayoutDirection) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
        views.setInt(R.id.widget_header, "setLayoutDirection", direction)
        views.setInt(R.id.widget_content, "setLayoutDirection", direction)

        views.setTextViewText(R.id.top_start_text, data.topStartText)
        views.setTextViewText(R.id.top_end_text, data.topEndText)

        slotContainerIds.forEach { views.setViewVisibility(it, View.GONE) }

        data.rows.take(MAX_SLOTS).forEachIndexed { index, row ->
            val variant = if (row.isActive) index * 2 + 1 else index * 2
            views.setViewVisibility(slotContainerIds[variant], View.VISIBLE)
            views.setTextViewText(slotNameIds[variant], localized.getString(row.prayer.stringRes))
            views.setTextViewText(slotTimeIds[variant], row.timeText)
        }

        if (data.showCountdown && data.countdown != null) {
            views.setTextViewText(
                R.id.countdown_label,
                "${localized.getString(data.countdown.prayer.stringRes)}: ",
            )
            val base = SystemClock.elapsedRealtime() + (data.countdown.baseMillis - System.currentTimeMillis())
            views.setChronometer(R.id.countdown, base, null, true)
        }

        views.setOnClickPendingIntent(R.id.screen_widget_layout, launchPendingIntent(context))
        return views
    }

    fun buildScreenWidget(
        context: Context,
        data: WidgetData,
    ): RemoteViews = build(context, screenWidgetLayout(data.adaptiveTheme, data.showCountdown), data)

    fun screenWidgetLayout(
        adaptive: Boolean,
        countdown: Boolean,
    ): Int =
        when {
            countdown && adaptive -> R.layout.screen_widget_countdown_adaptive
            countdown -> R.layout.screen_widget_countdown
            adaptive -> R.layout.screen_widget_adaptive
            else -> R.layout.screen_widget
        }

    private enum class NextPrayerSize(
        val nameSp: Float,
        val timeSp: Float,
    ) {
        Small(14f, 14f),
        Medium(22f, 20f),
        Large(30f, 28f),
    }

    private const val MIN_WIDTH_DP = 40f
    private const val MEDIUM_MIN_WIDTH_DP = 110f
    private const val LARGE_MIN_WIDTH_DP = 170f

    fun buildNextPrayerWidget(
        context: Context,
        data: NextPrayerWidgetData,
    ): RemoteViews {
        val layout = if (data.adaptiveTheme) R.layout.next_prayer_widget_adaptive else R.layout.next_prayer_widget
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return renderNextPrayer(context, data, NextPrayerSize.Small, layout)

        return RemoteViews(
            mapOf(
                SizeF(MIN_WIDTH_DP, MIN_WIDTH_DP) to renderNextPrayer(context, data, NextPrayerSize.Small, layout),
                SizeF(MEDIUM_MIN_WIDTH_DP, MIN_WIDTH_DP) to renderNextPrayer(context, data, NextPrayerSize.Medium, layout),
                SizeF(LARGE_MIN_WIDTH_DP, MIN_WIDTH_DP) to renderNextPrayer(context, data, NextPrayerSize.Large, layout),
            ),
        )
    }

    fun buildNextPrayerNotification(
        context: Context,
        data: NextPrayerWidgetData,
        expanded: Boolean,
    ): RemoteViews {
        val layout = if (data.adaptiveTheme) R.layout.notif_next_prayer_adaptive else R.layout.notif_next_prayer
        return renderNextPrayer(context, data, if (expanded) NextPrayerSize.Medium else NextPrayerSize.Small, layout)
    }

    private fun renderNextPrayer(
        context: Context,
        data: NextPrayerWidgetData,
        size: NextPrayerSize,
        layout: Int,
    ): RemoteViews {
        val localized = context.withAppLocale(data.locale)
        val base = SystemClock.elapsedRealtime() + (data.countdownBaseMillis - System.currentTimeMillis())
        return RemoteViews(context.packageName, layout).apply {
            setTextViewText(R.id.next_prayer_name, localized.getString(data.prayer.stringRes))
            setChronometer(R.id.next_prayer_countdown, base, null, true)
            setTextViewTextSize(R.id.next_prayer_name, TypedValue.COMPLEX_UNIT_SP, size.nameSp)
            setTextViewTextSize(R.id.next_prayer_countdown, TypedValue.COMPLEX_UNIT_SP, size.timeSp)
            setOnClickPendingIntent(R.id.next_prayer_widget_layout, launchPendingIntent(context))
        }
    }

    fun buildHint(
        context: Context,
        @StringRes messageRes: Int,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_hint)
        views.setTextViewText(R.id.widget_hint_text, context.getString(messageRes))
        views.setOnClickPendingIntent(R.id.widget_hint_layout, launchPendingIntent(context))
        return views
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
}
