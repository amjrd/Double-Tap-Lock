package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.service.TapLockAccessibilityService
import com.example.service.TapLockOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TapLockUiState(
    val isAccessibilityEnabled: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val floatingPillEnabled: Boolean = false,
    val doubleTapSpeedMs: Int = 320,
    val hapticFeedbackEnabled: Boolean = true,
    val pillOpacity: Float = 0.65f,
    val lockCount: Int = 0,
    val testPadTapsCount: Int = 0,
    val testPadLastIntervalMs: Long? = null,
    val testPadStatusText: String = "انقر نقراً مزدوجاً هنا لتجربة سرعة الاستجابة",
    val testPadIsSuccess: Boolean = false
)

private data class SystemPermissions(
    val isAccessibilityEnabled: Boolean,
    val canDrawOverlays: Boolean
)

private data class TestPadState(
    val tapsCount: Int = 0,
    val lastIntervalMs: Long? = null,
    val message: String = "انقر نقراً مزدوجاً هنا لتجربة سرعة الاستجابة",
    val isSuccess: Boolean = false
)

private data class UserPreferences(
    val floatingPillEnabled: Boolean = false,
    val doubleTapSpeedMs: Int = 320,
    val hapticFeedbackEnabled: Boolean = true,
    val pillOpacity: Float = 0.65f,
    val lockCount: Int = 0
)

class TapLockViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val preferencesManager = PreferencesManager(context)

    private val _systemPermissions = MutableStateFlow(
        SystemPermissions(
            isAccessibilityEnabled = TapLockAccessibilityService.isAccessibilityServiceEnabled(context),
            canDrawOverlays = Settings.canDrawOverlays(context)
        )
    )

    private val _testPadState = MutableStateFlow(TestPadState())

    private var lastTestTapTimestamp = 0L

    private val _userPreferencesFlow = combine(
        preferencesManager.floatingPillEnabled,
        preferencesManager.doubleTapSpeedMs,
        preferencesManager.hapticFeedbackEnabled,
        preferencesManager.pillOpacity,
        preferencesManager.lockCount
    ) { pillEnabled, speed, haptic, opacity, locks ->
        UserPreferences(
            floatingPillEnabled = pillEnabled,
            doubleTapSpeedMs = speed,
            hapticFeedbackEnabled = haptic,
            pillOpacity = opacity,
            lockCount = locks
        )
    }

    val uiState: StateFlow<TapLockUiState> = combine(
        _systemPermissions,
        _testPadState,
        _userPreferencesFlow
    ) { perms, testState, prefs ->
        TapLockUiState(
            isAccessibilityEnabled = perms.isAccessibilityEnabled,
            canDrawOverlays = perms.canDrawOverlays,
            floatingPillEnabled = prefs.floatingPillEnabled,
            doubleTapSpeedMs = prefs.doubleTapSpeedMs,
            hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
            pillOpacity = prefs.pillOpacity,
            lockCount = prefs.lockCount,
            testPadTapsCount = testState.tapsCount,
            testPadLastIntervalMs = testState.lastIntervalMs,
            testPadStatusText = testState.message,
            testPadIsSuccess = testState.isSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TapLockUiState()
    )

    fun refreshPermissions() {
        _systemPermissions.update {
            SystemPermissions(
                isAccessibilityEnabled = TapLockAccessibilityService.isAccessibilityServiceEnabled(context),
                canDrawOverlays = Settings.canDrawOverlays(context)
            )
        }
    }

    fun setFloatingPill(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !Settings.canDrawOverlays(context)) {
                return@launch
            }
            preferencesManager.setFloatingPillEnabled(enabled)
            if (enabled) {
                TapLockOverlayService.start(context)
            } else {
                TapLockOverlayService.stop(context)
            }
        }
    }

    fun setDoubleTapSpeed(speedMs: Int) {
        viewModelScope.launch {
            preferencesManager.setDoubleTapSpeedMs(speedMs)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setPillOpacity(opacity: Float) {
        viewModelScope.launch {
            preferencesManager.setPillOpacity(opacity)
        }
    }

    fun onTestPadTapped() {
        val now = System.currentTimeMillis()
        val interval = now - lastTestTapTimestamp
        val currentSpeedLimit = uiState.value.doubleTapSpeedMs

        if (interval in 1..currentSpeedLimit) {
            // Double tap success!
            lastTestTapTimestamp = 0L
            performHapticClick()
            _testPadState.update { current ->
                current.copy(
                    tapsCount = current.tapsCount + 1,
                    lastIntervalMs = interval,
                    message = "✨ نقر مزدوج ناجح ومثالي! ($interval مللي ثانية)",
                    isSuccess = true
                )
            }
        } else {
            // First tap registered
            lastTestTapTimestamp = now
            _testPadState.update { current ->
                current.copy(
                    lastIntervalMs = null,
                    message = "👆 النقرة الأولى مسجلة.. انقر مجدداً بسرعة!",
                    isSuccess = false
                )
            }
        }
    }

    private fun performHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            }
        } catch (_: Exception) { }
    }

    fun instantLockScreen(): Boolean {
        return TapLockAccessibilityService.lockScreen(context)
    }
}
