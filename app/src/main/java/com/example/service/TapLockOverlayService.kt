package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
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
import kotlin.math.abs

class TapLockOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var collectJob: Job? = null
    private lateinit var windowManager: WindowManager
    private lateinit var preferencesManager: PreferencesManager

    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var lastTapTime: Long = 0L
    private var doubleTapSpeedMs: Int = 320
    private var pillOpacity: Float = 0.65f
    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(applicationContext)

        startForeground(TapLockApplication.NOTIFICATION_ID_OVERLAY, createNotification())
        observePreferences()
        setupOverlayView()
    }

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TapLockApplication.CHANNEL_ID_OVERLAY)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.drawable.ic_screen_off)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun observePreferences() {
        collectJob = serviceScope.launch {
            launch {
                preferencesManager.doubleTapSpeedMs.collectLatest { speed ->
                    doubleTapSpeedMs = speed
                }
            }
            launch {
                preferencesManager.pillOpacity.collectLatest { opacity ->
                    pillOpacity = opacity
                    overlayView?.alpha = opacity
                }
            }
            launch {
                preferencesManager.floatingPillEnabled.collectLatest { enabled ->
                    if (!enabled) {
                        stopSelf()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayView() {
        if (overlayView != null) return

        val dpSize = dpToPx(48f)
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            dpSize,
            dpSize,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }
        layoutParams = params

        // Create pill UI view
        val pill = ImageView(this).apply {
            setImageResource(R.drawable.ic_screen_off)
            val padding = dpToPx(10f)
            setPadding(padding, padding, padding, padding)
            setColorFilter(Color.parseColor("#38BDF8"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1.5f), Color.parseColor("#38BDF8"))
            }
            alpha = pillOpacity
        }

        pill.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        overlayView = pill
        try {
            windowManager.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay view", e)
            stopSelf()
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val params = layoutParams ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY

                if (abs(dx) > 10 || abs(dy) > 10) {
                    isDragging = true
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    overlayView?.let { windowManager.updateViewLayout(it, params) }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    onPillTapped()
                }
                return true
            }
        }
        return false
    }

    private fun onPillTapped() {
        val currentTime = System.currentTimeMillis()
        val interval = currentTime - lastTapTime

        if (interval <= doubleTapSpeedMs) {
            // Double tap recognized! Lock the screen!
            lastTapTime = 0L
            val locked = TapLockAccessibilityService.lockScreen(this)
            if (!locked) {
                // If accessibility service is not active, open guide
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_ACCESSIBILITY_GUIDE", true)
                }
                startActivity(intent)
            }
        } else {
            // First tap registered
            lastTapTime = currentTime
            // Quick subtle pulse animation
            overlayView?.animate()?.scaleX(1.2f)?.scaleY(1.2f)?.setDuration(80)?.withEndAction {
                overlayView?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(80)?.start()
            }?.start()
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        collectJob?.cancel()
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view", e)
            }
        }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TapLockOverlayService"

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
