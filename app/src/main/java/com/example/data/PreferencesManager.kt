package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taplock_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_FLOATING_PILL = booleanPreferencesKey("floating_pill_enabled")
        val KEY_DOUBLE_TAP_SPEED = intPreferencesKey("double_tap_speed_ms")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        val KEY_PILL_OPACITY = floatPreferencesKey("pill_opacity")
        val KEY_LOCK_COUNT = intPreferencesKey("lock_count")
        val KEY_SINGLE_TAP_LOCK = booleanPreferencesKey("single_tap_lock")
    }

    val floatingPillEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FLOATING_PILL] ?: false
    }

    val doubleTapSpeedMs: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DOUBLE_TAP_SPEED] ?: 320
    }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_FEEDBACK] ?: true
    }

    val pillOpacity: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_PILL_OPACITY] ?: 0.65f
    }

    val lockCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCK_COUNT] ?: 0
    }

    val singleTapLock: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SINGLE_TAP_LOCK] ?: false
    }

    suspend fun setFloatingPillEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FLOATING_PILL] = enabled
        }
    }

    suspend fun setDoubleTapSpeedMs(speedMs: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DOUBLE_TAP_SPEED] = speedMs
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setPillOpacity(opacity: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PILL_OPACITY] = opacity
        }
    }

    suspend fun setSingleTapLock(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SINGLE_TAP_LOCK] = enabled
        }
    }

    suspend fun incrementLockCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_LOCK_COUNT] ?: 0
            prefs[KEY_LOCK_COUNT] = current + 1
        }
    }
}
