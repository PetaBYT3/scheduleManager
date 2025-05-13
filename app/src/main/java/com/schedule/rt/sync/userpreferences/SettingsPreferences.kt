package com.schedule.rt.sync.userpreferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsPreferences(
    private val context: Context,
) {

    companion object {
        val FOREGROUND_SERVICE = booleanPreferencesKey("foreground_service")
    }

    suspend fun setForegroundService(value: Boolean?) {
        context.dataStore.edit {
            it[FOREGROUND_SERVICE] = value ?: true
        }
    }

    val foregroundServiceStatus: Flow<Boolean> = context.dataStore.data.map {
        it[FOREGROUND_SERVICE] ?: true
    }
}