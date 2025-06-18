package com.example.weather.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val RAIN_SOUND_KEY = booleanPreferencesKey("rain_sound_enabled")
    private val USE_IMPERIAL_UNITS_KEY = booleanPreferencesKey("use_imperial_units")
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")

    fun getRainSoundFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[RAIN_SOUND_KEY] ?: true }

    suspend fun setRainSoundEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[RAIN_SOUND_KEY] = enabled }
    }

    fun getUnitsFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[USE_IMPERIAL_UNITS_KEY] ?: false }

    suspend fun setUnits(context: Context, useImperial: Boolean) {
        context.dataStore.edit { it[USE_IMPERIAL_UNITS_KEY] = useImperial }
    }

    fun getDarkModeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[IS_DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { it[IS_DARK_MODE_KEY] = isDarkMode }
    }
}