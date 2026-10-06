package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class TapLockAccessibilityService : AccessibilityService() {

    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        initVibrator()
    }

    private fun initVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator = vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {}
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
        isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Zero event processing = 0% CPU & battery
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        isServiceRunning = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isServiceRunning = false
        vibrator = null
    }

    fun executeLockScreenInstant(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locked = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            if (locked) {
                // Instant hardware vibration (0ms delay)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(25)
                    }
                } catch (_: Exception) {}
            }
            return locked
        }
        return false
    }

    companion object {
        private const val TAG = "TapLockAccService"
        var instance: WeakReference<TapLockAccessibilityService>? = null
        var isServiceRunning: Boolean = false

        fun lockScreen(context: Context): Boolean {
            val service = instance?.get()
            return if (service != null) {
                service.executeLockScreenInstant()
            } else {
                Log.w(TAG, "Accessibility service not active")
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
