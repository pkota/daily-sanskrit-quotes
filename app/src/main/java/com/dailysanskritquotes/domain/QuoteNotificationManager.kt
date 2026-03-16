package com.dailysanskritquotes.domain

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Manages scheduling and cancellation of daily quote notifications
 * using AlarmManager for reliable delivery.
 */
class QuoteNotificationManager(
    private val context: Context
) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    /**
     * Checks if the app can schedule exact alarms (required on Android 12+).
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedules a daily notification at the given hour and minute in the user's local time zone.
     * Defaults to 8:00 AM.
     * 
     * Uses setAlarmClock() for maximum reliability — this is treated as a user-visible alarm
     * by the system and is not subject to Doze/battery optimization delays.
     */
    fun scheduleDailyNotification(hour: Int = DEFAULT_HOUR, minute: Int = DEFAULT_MINUTE) {
        val clampedHour = hour.coerceIn(0, 23)
        val clampedMinute = minute.coerceIn(0, 59)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, clampedHour)
            set(Calendar.MINUTE, clampedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pendingIntent = createAlarmPendingIntent()

        // setAlarmClock is the most reliable for user-visible alarms.
        // It's exempt from Doze mode and battery optimizations.
        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, com.dailysanskritquotes.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, showIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    /**
     * Cancels all scheduled notification alarms.
     */
    fun cancelAllNotifications() {
        val pendingIntent = createAlarmPendingIntent()
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Reschedules the daily notification at a new time.
     * Cancels any existing alarm before scheduling the new one.
     */
    fun updateNotificationTime(hour: Int, minute: Int) {
        cancelAllNotifications()
        scheduleDailyNotification(hour, minute)
    }

    private fun createAlarmPendingIntent(): PendingIntent {
        val intent = Intent(context, QuoteAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_QUOTE_ALARM
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_QUOTE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily Sanskrit quote notifications"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "daily_quote_channel"
        const val CHANNEL_NAME = "Daily Quote"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE_DAILY_QUOTE = 2001
        const val ACTION_DAILY_QUOTE_ALARM = "com.dailysanskritquotes.ACTION_DAILY_QUOTE_ALARM"
        const val DEFAULT_HOUR = 8
        const val DEFAULT_MINUTE = 0
    }
}
