package com.dailysanskritquotes.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.dailysanskritquotes.domain.QuoteNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class AppColorTheme(val label: String) {
    DEFAULT("Default"),
    WARM("Warm"),
    COOL("Cool"),
    FOREST("Forest")
}

enum class TextSizeOption(val label: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.3f)
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = QuoteNotificationManager.DEFAULT_HOUR,
    val notificationMinute: Int = QuoteNotificationManager.DEFAULT_MINUTE,
    val darkModeEnabled: Boolean = false,
    val colorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    val textSize: TextSizeOption = TextSizeOption.MEDIUM,
    val orientationLocked: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationManager: QuoteNotificationManager,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadSettings())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun loadSettings(): SettingsUiState {
        return SettingsUiState(
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
            notificationHour = prefs.getInt(KEY_NOTIFICATION_HOUR, QuoteNotificationManager.DEFAULT_HOUR),
            notificationMinute = prefs.getInt(KEY_NOTIFICATION_MINUTE, QuoteNotificationManager.DEFAULT_MINUTE),
            darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false),
            colorTheme = AppColorTheme.entries.getOrElse(
                prefs.getInt(KEY_COLOR_THEME, 0)
            ) { AppColorTheme.DEFAULT },
            textSize = TextSizeOption.entries.getOrElse(
                prefs.getInt(KEY_TEXT_SIZE, 1)
            ) { TextSizeOption.MEDIUM },
            orientationLocked = prefs.getBoolean(KEY_ORIENTATION_LOCKED, false)
        )
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) {
            notificationManager.scheduleDailyNotification(
                _uiState.value.notificationHour,
                _uiState.value.notificationMinute
            )
        } else {
            notificationManager.cancelAllNotifications()
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()
        _uiState.update { it.copy(notificationHour = hour, notificationMinute = minute) }
        if (_uiState.value.notificationsEnabled) {
            notificationManager.updateNotificationTime(hour, minute)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _uiState.update { it.copy(darkModeEnabled = enabled) }
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit().putInt(KEY_COLOR_THEME, theme.ordinal).apply()
        _uiState.update { it.copy(colorTheme = theme) }
    }

    fun setTextSize(size: TextSizeOption) {
        prefs.edit().putInt(KEY_TEXT_SIZE, size.ordinal).apply()
        _uiState.update { it.copy(textSize = size) }
    }

    fun setOrientationLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_ORIENTATION_LOCKED, locked).apply()
        _uiState.update { it.copy(orientationLocked = locked) }
    }

    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_HOUR = "notification_hour"
        const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_COLOR_THEME = "color_theme"
        const val KEY_TEXT_SIZE = "text_size"
        const val KEY_ORIENTATION_LOCKED = "orientation_locked"
    }
}
