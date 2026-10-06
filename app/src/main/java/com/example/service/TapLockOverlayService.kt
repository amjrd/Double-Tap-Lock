package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.TapLockApplication
import com.example.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TapLockOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null
    private lateinit var windowManager: WindowManager
    private lateinit var preferencesManager: PreferencesManager

    private var touchZoneView: View? = null
    private var lastTapTimestamp: Long = 0L
    private val doubleTapIntervalMs: Long = 320L

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(applicationContext)

        startForeground(TapLockApplication.NOTIFICATION_ID_OVERLAY, buildNotification())
        setupUltraFastTouchZone()

        job = serviceScope.launch {
            preferencesManager.isServiceEnabled.collectLatest { enabled ->
                if (!enabled) {
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TapLockApplication.CHANNEL_ID_OVERLAY)
            .setContentTitle("TapLock نشط")
            .setContentText("انقر نقراً مزدوجاً في أعلى الشاشة لإطفائها")
            .setSmallIcon(R.drawable.ic_screen_off)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUltraFastTouchZone() {
        if (touchZoneView != null) return

        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            resources.displayMetrics
        ).toInt()

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            heightPx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
        }

        val view = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            alpha = 0.01f
        }

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val now = SystemClock.uptimeMillis()
                if (now - lastTapTimestamp in 1..doubleTapIntervalMs) {
                    lastTapTimestamp = 0L
                    TapLockAccessibilityService.lockScreen(applicationContext)
                } else {
                    lastTapTimestamp = now
                }
            }
            false
        }

        try {
            windowManager.addView(view, params)
            touchZoneView = view
        } catch (_: Exception) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        touchZoneView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        touchZoneView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TapLockOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TapLockOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
