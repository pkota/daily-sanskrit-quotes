package com.dailysanskritquotes.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-registers the daily quote alarm after device reboot,
 * since all AlarmManager alarms are cleared on reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("quote_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("notifications_enabled", true)
        if (!enabled) return

        val hour = prefs.getInt("notification_hour", QuoteNotificationManager.DEFAULT_HOUR)
        val minute = prefs.getInt("notification_minute", QuoteNotificationManager.DEFAULT_MINUTE)

        val manager = QuoteNotificationManager(context)
        manager.scheduleDailyNotification(hour, minute)
    }
}
