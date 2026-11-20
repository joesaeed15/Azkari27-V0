package com.azkari2027.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "azkari_2027_settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val LANGUAGE_ID = intPreferencesKey("language_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_ADVANCE = booleanPreferencesKey("auto_advance")
        val WIDGET_TASBEEH_ID = intPreferencesKey("widget_tasbeeh_id")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val reminders = ReminderKey.entries.map { key ->
            ReminderConfig(
                key = key,
                enabled = prefs[booleanPreferencesKey("${key.prefKey}_enabled")] ?: false,
                hour = prefs[intPreferencesKey("${key.prefKey}_hour")] ?: key.defaultHour,
                minute = prefs[intPreferencesKey("${key.prefKey}_minute")] ?: key.defaultMinute
            )
        }
        AppSettings(
            languageId = prefs[Keys.LANGUAGE_ID] ?: 1,
            themeMode = runCatching { AppThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: AppThemeMode.AMOLED_DARK.name) }
                .getOrDefault(AppThemeMode.AMOLED_DARK),
            fontFamily = runCatching { AzkarFontFamily.valueOf(prefs[Keys.FONT_FAMILY] ?: AzkarFontFamily.DROID_NASKH.name) }
                .getOrDefault(AzkarFontFamily.DROID_NASKH),
            fontSizeSp = prefs[Keys.FONT_SIZE_SP] ?: 23f,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            autoAdvanceOnComplete = prefs[Keys.AUTO_ADVANCE] ?: true,
            reminders = reminders
        )
    }

    val widgetTasbeehIdFlow: Flow<Int> = context.dataStore.data.map { it[Keys.WIDGET_TASBEEH_ID] ?: 93 }

    suspend fun setLanguage(languageId: Int) { context.dataStore.edit { it[Keys.LANGUAGE_ID] = languageId } }
    suspend fun setThemeMode(mode: AppThemeMode) { context.dataStore.edit { it[Keys.THEME_MODE] = mode.name } }
    suspend fun setFontFamily(family: AzkarFontFamily) { context.dataStore.edit { it[Keys.FONT_FAMILY] = family.name } }
    suspend fun setFontSize(sizeSp: Float) { context.dataStore.edit { it[Keys.FONT_SIZE_SP] = sizeSp.coerceIn(16f, 36f) } }
    suspend fun setSoundEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled } }
    suspend fun setVibrationEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled } }
    suspend fun setAutoAdvance(enabled: Boolean) { context.dataStore.edit { it[Keys.AUTO_ADVANCE] = enabled } }
    suspend fun setWidgetTasbeehId(catId: Int) { context.dataStore.edit { it[Keys.WIDGET_TASBEEH_ID] = catId } }

    suspend fun updateReminder(key: ReminderKey, enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("${key.prefKey}_enabled")] = enabled
            prefs[intPreferencesKey("${key.prefKey}_hour")] = hour
            prefs[intPreferencesKey("${key.prefKey}_minute")] = minute
        }
    }
}
