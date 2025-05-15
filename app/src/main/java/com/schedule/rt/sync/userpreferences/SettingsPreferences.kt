package com.schedule.rt.sync.userpreferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsPreferences(
    private val context: Context,
) {

    companion object {
        val FOREGROUND_SERVICE = booleanPreferencesKey("foreground_service")
        val ALARM_DELAY = intPreferencesKey("alarm_delay")
    }

    suspend fun setForegroundService(value: Boolean?) {
        context.dataStore.edit {
            it[FOREGROUND_SERVICE] = value ?: true
            Log.d("SettingsPreferences", "Current DataStore contents: $value")
        }
    }

    suspend fun setAlarmDelay(value: Int?) {
        context.dataStore.edit {
            it[ALARM_DELAY] = value ?: 60
            Log.d("SettingsPreferences", "Current DataStore contents: $value")
        }
    }

    val getForegroundServiceStatus: Flow<Boolean> = context.dataStore.data.map {
        it[FOREGROUND_SERVICE] ?: true
    }

    val getAlarmDelayMinutes: Flow<Int> = context.dataStore.data.map {
        it[ALARM_DELAY] ?: 60
    }
}