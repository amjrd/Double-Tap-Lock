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
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class TapLockOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null
    private lateinit var windowManager: WindowManager
    private lateinit var preferencesManager: PreferencesManager

    private var touchZoneView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var lastTapTimestamp: Long = 0L
    private val doubleTapIntervalMs: Long = 320L

    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var touchDownRawX = 0f
    private var touchDownRawY = 0f

    private var isGuideVisible = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(applicationContext)

        startForeground(TapLockApplication.NOTIFICATION_ID_OVERLAY, buildNotification())
        observePreferences()
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
            .setContentText("منطقة النقر المزدوج الحرة نشطة (انقر مرتين للقفل)")
            .setSmallIcon(R.drawable.ic_screen_off)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun observePreferences() {
        job = serviceScope.launch {
            launch {
                val initialX = preferencesManager.overlayX.first()
                val initialY = preferencesManager.overlayY.first()
                setupFreeTouchZone(initialX, initialY)
            }
            launch {
                preferencesManager.isServiceEnabled.collectLatest { enabled ->
                    if (!enabled) {
                        stopSelf()
                    }
                }
            }
            launch {
                preferencesManager.isVisibleGuide.collectLatest { visible ->
                    isGuideVisible = visible
                    updateAppearance()
                }
            }
        }
    }

    private fun getWindowLayoutType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFreeTouchZone(savedX: Int, savedY: Int) {
        if (touchZoneView != null) return

        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            54f,
            resources.displayMetrics
        ).toInt()

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            getWindowLayoutType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            // Y is positioned away from the status bar so it NEVER blocks notification pull-down
            y = if (savedY <= 80) 350 else savedY
        }
        layoutParams = params

        val view = ImageView(this)
        view.setOnTouchListener { _, event ->
            handleTouch(event)
        }

        touchZoneView = view
        updateAppearance()

        try {
            windowManager.addView(touchZoneView, layoutParams)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun updateAppearance() {
        val view = touchZoneView ?: return

        if (isGuideVisible) {
            // Visible mode for dragging & repositioning freely
            view.setImageResource(R.drawable.ic_screen_off)
            val pad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
            view.setPadding(pad, pad, pad, pad)
            view.setColorFilter(Color.parseColor("#10B981"))
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1E293B"))
                setStroke(3, Color.parseColor("#10B981"))
            }
            view.alpha = 0.9f
        } else {
            // Secret invisible mode (no icon on screen, zero visual clutter)
            view.setImageDrawable(null)
            view.setBackgroundColor(Color.TRANSPARENT)
            view.alpha = 0.01f
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val params = layoutParams ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                touchDownRawX = event.rawX
                touchDownRawY = event.rawY
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchDownRawX
                val dy = event.rawY - touchDownRawY

                if (abs(dx) > 12 || abs(dy) > 12) {
                    isDragging = true
                    params.x = (initialX + dx).toInt()
                    params.y = (initialY + dy).toInt()
                    touchZoneView?.let { windowManager.updateViewLayout(it, params) }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    // Save new free position
                    serviceScope.launch {
                        preferencesManager.setOverlayPosition(params.x, params.y)
                    }
                } else {
                    // Crisp tap registered -> Check for Double Tap!
                    val now = SystemClock.uptimeMillis()
                    if (now - lastTapTimestamp in 1..doubleTapIntervalMs) {
                        lastTapTimestamp = 0L
                        TapLockAccessibilityService.lockScreen(applicationContext)
                    } else {
                        lastTapTimestamp = now
                    }
                }
                return true
            }
        }
        return false
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
