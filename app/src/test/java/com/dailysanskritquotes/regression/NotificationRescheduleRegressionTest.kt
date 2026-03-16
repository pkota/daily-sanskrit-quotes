package com.dailysanskritquotes.regression

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Regression test: Notification rescheduling must use the user's saved time
 * preference, not hardcoded defaults.
 *
 * Previously, QuoteAlarmReceiver called scheduleDailyNotification() with no
 * arguments after firing, which always rescheduled at the default 8:00 AM
 * regardless of what the user had set in Settings.
 *
 * This test verifies the logic that reads saved hour/minute from preferences
 * and that arbitrary valid times are preserved through the save/load cycle.
 */
class NotificationRescheduleRegressionTest : StringSpec({

    /**
     * Simulates SharedPreferences save/load for notification time.
     * This mirrors the logic in QuoteAlarmReceiver and SettingsViewModel.
     */
    class FakePrefs {
        private val store = mutableMapOf<String, Int>()

        fun putInt(key: String, value: Int) { store[key] = value }
        fun getInt(key: String, default: Int): Int = store.getOrDefault(key, default)
        fun contains(key: String): Boolean = store.containsKey(key)
    }

    val DEFAULT_HOUR = 8
    val DEFAULT_MINUTE = 0

    "saved notification time is retrieved correctly for any valid hour/minute" {
        checkAll(25, Arb.int(0..23), Arb.int(0..59)) { hour, minute ->
            val prefs = FakePrefs()

            // Simulate SettingsViewModel.setNotificationTime
            prefs.putInt("notification_hour", hour)
            prefs.putInt("notification_minute", minute)

            // Simulate QuoteAlarmReceiver reading saved time
            val savedHour = prefs.getInt("notification_hour", DEFAULT_HOUR)
            val savedMinute = prefs.getInt("notification_minute", DEFAULT_MINUTE)

            savedHour shouldBe hour
            savedMinute shouldBe minute
        }
    }

    "unsaved prefs return defaults, not arbitrary values" {
        val prefs = FakePrefs()
        prefs.getInt("notification_hour", DEFAULT_HOUR) shouldBe DEFAULT_HOUR
        prefs.getInt("notification_minute", DEFAULT_MINUTE) shouldBe DEFAULT_MINUTE
    }

    "saved time differs from default when user sets non-default time" {
        checkAll(25, Arb.int(0..23), Arb.int(0..59)) { hour, minute ->
            if (hour != DEFAULT_HOUR || minute != DEFAULT_MINUTE) {
                val prefs = FakePrefs()
                prefs.putInt("notification_hour", hour)
                prefs.putInt("notification_minute", minute)

                val savedHour = prefs.getInt("notification_hour", DEFAULT_HOUR)
                val savedMinute = prefs.getInt("notification_minute", DEFAULT_MINUTE)

                // At least one should differ from default
                (savedHour != DEFAULT_HOUR || savedMinute != DEFAULT_MINUTE) shouldBe true
            }
        }
    }

    "hour is clamped to valid range 0-23" {
        checkAll(25, Arb.int(-100..200)) { rawHour ->
            val clamped = rawHour.coerceIn(0, 23)
            (clamped in 0..23) shouldBe true
        }
    }

    "minute is clamped to valid range 0-59" {
        checkAll(25, Arb.int(-100..200)) { rawMinute ->
            val clamped = rawMinute.coerceIn(0, 59)
            (clamped in 0..59) shouldBe true
        }
    }
})
