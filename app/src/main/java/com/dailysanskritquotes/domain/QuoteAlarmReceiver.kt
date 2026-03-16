package com.dailysanskritquotes.domain

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dailysanskritquotes.MainActivity
import com.dailysanskritquotes.R
import com.dailysanskritquotes.data.QuoteRepository
import com.dailysanskritquotes.data.db.QuoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * BroadcastReceiver that handles the daily quote alarm.
 * When triggered, it fetches the daily quote and posts a notification.
 * After posting, it reschedules the alarm for the next day.
 */
class QuoteAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != QuoteNotificationManager.ACTION_DAILY_QUOTE_ALARM) return

        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val quote = fetchDailyQuote(context)
                if (quote != null) {
                    postNotification(context, quote.sanskritText, quote.englishTranslation)
                }
                // Reschedule for the next day using the user's saved time preference
                val prefs = context.getSharedPreferences("quote_prefs", Context.MODE_PRIVATE)
                val savedHour = prefs.getInt("notification_hour", QuoteNotificationManager.DEFAULT_HOUR)
                val savedMinute = prefs.getInt("notification_minute", QuoteNotificationManager.DEFAULT_MINUTE)
                val manager = QuoteNotificationManager(context)
                manager.scheduleDailyNotification(savedHour, savedMinute)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun fetchDailyQuote(
        context: Context
    ): com.dailysanskritquotes.data.db.QuoteEntity? {
        return try {
            val database = QuoteDatabase.getInstance(context)
            val prefs = context.getSharedPreferences("quote_prefs", Context.MODE_PRIVATE)
            val repository = QuoteRepository(database, prefs)
            val selector = DailyQuoteSelector(repository)
            selector.getQuoteForDate(LocalDate.now())
        } catch (e: Exception) {
            null
        }
    }

    private fun postNotification(
        context: Context,
        sanskritText: String,
        englishTranslation: String
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, QuoteNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(sanskritText)
            .setContentText(englishTranslation)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$sanskritText\n\n$englishTranslation")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            QuoteNotificationManager.NOTIFICATION_ID,
            notification
        )
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "from_notification"
    }
}
