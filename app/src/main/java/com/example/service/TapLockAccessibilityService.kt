package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class TapLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
        isServiceRunning = true
        Log.d(TAG, "TapLockAccessibilityService connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event listening required - keeps battery consumption at 0%
    }

    override fun onInterrupt() {
        Log.d(TAG, "TapLockAccessibilityService interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        isServiceRunning = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isServiceRunning = false
    }

    fun executeLockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val result = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            if (result) {
                triggerHapticFeedback()
                serviceScope.launch {
                    preferencesManager.incrementLockCount()
                }
            }
            result
        } else {
            false
        }
    }

    private fun triggerHapticFeedback() {
        serviceScope.launch {
            val isHapticEnabled = preferencesManager.hapticFeedbackEnabled.first()
            if (!isHapticEnabled) return@launch

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vibratorManager?.defaultVibrator
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(35)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to vibrate", e)
            }
        }
    }

    companion object {
        private const val TAG = "TapLockAccService"
        var instance: WeakReference<TapLockAccessibilityService>? = null
        var isServiceRunning: Boolean = false

        fun lockScreen(context: Context): Boolean {
            val service = instance?.get()
            return if (service != null) {
                service.executeLockScreen()
            } else {
                Log.w(TAG, "Accessibility service is not active")
                false
            }
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (isServiceRunning && instance?.get() != null) return true

            val expectedServiceName = "${context.packageName}/${TapLockAccessibilityService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}
