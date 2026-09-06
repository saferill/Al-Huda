package com.alhuda.app.adhan

object AdhanContract {

    const val ACTION_ADHAN = "com.alhuda.app.action.ADHAN_ALARM"

    const val ACTION_PRE_ADHAN = "com.alhuda.app.action.PRE_ADHAN_ALARM"

    const val ACTION_CANCEL_ADHAN = "com.alhuda.app.action.CANCEL_ADHAN"

    const val ACTION_UNSILENCE = "com.alhuda.app.action.UNSILENCE"

    const val ACTION_ADHAN_REMIND = "com.alhuda.app.action.ADHAN_REMIND"

    const val ADHAN_ALARM_ID = "adhan_alarm"
    const val PRE_ADHAN_ALARM_ID = "pre_adhan_alarm"
    const val REMIND_ALARM_ID = "adhan_remind_alarm"
    const val UNSILENCE_ALARM_ID = "adhan_unsilence_alarm"
    const val DEV_TEST_ALARM_ID = "adhan_dev_test_alarm"

    const val ADHAN_NOTIFICATION_ID = "adhan_notification"
    const val PRE_ADHAN_NOTIFICATION_ID = "pre_adhan_notification"
    const val REMIND_NOTIFICATION_ID = "adhan_remind_notification"

    const val DND_REVOKED_NOTIFICATION_ID = "adhan_dnd_revoked_notification"

    const val DND_ACTIVE_NOTIFICATION_ID = "adhan_dnd_active_notification"

    const val EXTRA_PLAY_SOUND = "adhan_play_sound"
    const val EXTRA_TIMESTAMP = "adhan_timestamp"

    const val EXTRA_PRAYER = "prayer"

    const val EXTRA_INTRUSIVE = "adhan_intrusive"
    const val EXTRA_REMIND_MINUTES = "adhan_remind_minutes"

    const val SHORT_REMIND_MINUTES = 15
    const val LONG_REMIND_MINUTES = 30

    const val DISMISS_SILENT_MINUTES = 30
}
