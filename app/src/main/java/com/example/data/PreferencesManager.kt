package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taplock_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
    }

    val isServiceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVICE_ENABLED] ?: false
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVICE_ENABLED] = enabled
        }
    }
}
