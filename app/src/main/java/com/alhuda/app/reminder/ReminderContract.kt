package com.alhuda.app.reminder

object ReminderContract {

    const val ACTION_REMINDER = "com.alhuda.app.action.REMINDER_ALARM"

    const val ACTION_PRE_REMINDER = "com.alhuda.app.action.PRE_REMINDER_ALARM"

    const val ACTION_CANCEL_REMINDER = "com.alhuda.app.action.CANCEL_REMINDER"

    const val ALARM_ID_PREFIX = "reminder_alarm_"
    const val PRE_ALARM_ID_PREFIX = "reminder_prealarm_"
    const val NOTIFICATION_ID_PREFIX = "reminder_notification_"
    const val PRE_NOTIFICATION_ID_PREFIX = "reminder_pre_notification_"

    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_TIMESTAMP = "reminder_timestamp"

    const val EXTRA_INTRUSIVE = "reminder_intrusive"

    fun alarmId(reminderId: String) = "$ALARM_ID_PREFIX$reminderId"

    fun preAlarmId(reminderId: String) = "$PRE_ALARM_ID_PREFIX$reminderId"

    fun notificationId(reminderId: String) = "$NOTIFICATION_ID_PREFIX$reminderId"

    fun preNotificationId(reminderId: String) = "$PRE_NOTIFICATION_ID_PREFIX$reminderId"
}
