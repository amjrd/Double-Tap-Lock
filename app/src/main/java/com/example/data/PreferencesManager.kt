package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taplock_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val KEY_OVERLAY_X = intPreferencesKey("overlay_x")
        val KEY_OVERLAY_Y = intPreferencesKey("overlay_y")
        val KEY_VISIBLE_GUIDE = booleanPreferencesKey("visible_guide")
    }

    val isServiceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVICE_ENABLED] ?: false
    }

    val overlayX: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_OVERLAY_X] ?: 30
    }

    val overlayY: Flow<Int> = context.dataStore.data.map { prefs ->
        // Default Y is 350px below the top, avoiding status bar / notch / notification shade completely!
        prefs[KEY_OVERLAY_Y] ?: 450
    }

    val isVisibleGuide: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VISIBLE_GUIDE] ?: false
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVICE_ENABLED] = enabled
        }
    }

    suspend fun setOverlayPosition(x: Int, y: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OVERLAY_X] = x
            prefs[KEY_OVERLAY_Y] = y
        }
    }

    suspend fun setVisibleGuide(visible: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VISIBLE_GUIDE] = visible
        }
    }
}
