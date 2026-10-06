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

    private var overlayView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var lastTapTime: Long = 0L
    private var doubleTapSpeedMs: Int = 320
    private var pillOpacity: Float = 0.65f
    private var isSecretMode: Boolean = true
    private var zonePosition: String = "STATUS_BAR"
    private var zoneHeightDp: Int = 52

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var isSwipeDown = false
    private var isDragging = false
    private var initialX = 0
    private var initialY = 0

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(applicationContext)

        startForeground(TapLockApplication.NOTIFICATION_ID_OVERLAY, createNotification())
        observePreferences()
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
            .setContentTitle("TapLock • قفل سري بالنقر المزدوج")
            .setContentText("منطقة النقر المزدوج الموسعة نشطة عبر كامل الشاشة")
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
                    updateAppearance()
                }
            }
            launch {
                preferencesManager.secretModeEnabled.collectLatest { secret ->
                    isSecretMode = secret
                    updateAppearance()
                    reapplyLayout()
                }
            }
            launch {
                preferencesManager.secretPosition.collectLatest { pos ->
                    zonePosition = pos
                    reapplyLayout()
                }
            }
            launch {
                preferencesManager.zoneHeightDp.collectLatest { height ->
                    zoneHeightDp = height
                    reapplyLayout()
                }
            }
            launch {
                preferencesManager.floatingPillEnabled.collectLatest { enabled ->
                    if (!enabled) {
                        stopSelf()
                    } else if (overlayView == null) {
                        setupOverlayView()
                    }
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
    private fun setupOverlayView() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            dpToPx(zoneHeightDp.toFloat()),
            getWindowLayoutType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        applyGravityAndPosition(params)
        layoutParams = params

        val view = ImageView(this)
        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        overlayView = view
        updateAppearance()

        try {
            windowManager.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay view", e)
            stopSelf()
        }
    }

    private fun applyGravityAndPosition(params: WindowManager.LayoutParams) {
        if (isSecretMode) {
            // Full width strip - Tap anywhere!
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = dpToPx(zoneHeightDp.toFloat())
            params.x = 0

            if (zonePosition == "BOTTOM_NAV") {
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.y = 0
            } else {
                // STATUS_BAR (default)
                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                params.y = 0
            }
        } else {
            // Visible draggable pill
            params.gravity = Gravity.TOP or Gravity.START
            params.width = dpToPx(50f)
            params.height = dpToPx(50f)
            if (params.x == 0 && params.y == 0) {
                params.x = 40
                params.y = 300
            }
        }
    }

    private fun reapplyLayout() {
        val view = overlayView ?: return
        val params = layoutParams ?: return

        applyGravityAndPosition(params)
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update overlay layout", e)
        }
    }

    private fun updateAppearance() {
        val view = overlayView ?: return

        if (isSecretMode) {
            // 100% invisible ghost zone - completely secret
            view.setImageDrawable(null)
            view.setBackgroundColor(Color.TRANSPARENT)
            view.alpha = 0.01f
        } else {
            // Visible pill
            view.setImageResource(R.drawable.ic_screen_off)
            val padding = dpToPx(10f)
            view.setPadding(padding, padding, padding, padding)
            view.setColorFilter(Color.parseColor("#38BDF8"))
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1.5f), Color.parseColor("#38BDF8"))
            }
            view.alpha = pillOpacity
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val params = layoutParams ?: return false

        if (isSecretMode) {
            // Smart Anti-Interference handling:
            // 1. Swipe down -> Open notification shade (no blocked status bar!)
            // 2. Double tap anywhere -> Lock screen immediately!
            // 3. Single tap -> Ignored, no accidental locking!
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.rawX
                    touchDownY = event.rawY
                    isSwipeDown = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - touchDownY
                    val dx = abs(event.rawX - touchDownX)
                    // If user slides finger down on top bar by > 32px
                    if (dy > dpToPx(28f) && dy > dx && zonePosition != "BOTTOM_NAV") {
                        if (!isSwipeDown) {
                            isSwipeDown = true
                            // Anti-interference: expand notification shade seamlessly!
                            TapLockAccessibilityService.openNotificationShade()
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isSwipeDown) {
                        val dx = abs(event.rawX - touchDownX)
                        val dy = abs(event.rawY - touchDownY)
                        // If it is a crisp tap (not a drag)
                        if (dx < dpToPx(20f) && dy < dpToPx(20f)) {
                            onZoneTapped()
                        }
                    }
                    return true
                }
            }
            return true
        } else {
            // Draggable visible pill mode
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchDownX = event.rawX
                    touchDownY = event.rawY
                    isDragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - touchDownX
                    val dy = event.rawY - touchDownY
                    if (abs(dx) > 12 || abs(dy) > 12) {
                        isDragging = true
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        overlayView?.let { windowManager.updateViewLayout(it, params) }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        onZoneTapped()
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun onZoneTapped() {
        val currentTime = System.currentTimeMillis()
        val interval = currentTime - lastTapTime

        if (interval in 1..doubleTapSpeedMs) {
            // Double tap recognized! Lock the screen!
            lastTapTime = 0L
            val locked = TapLockAccessibilityService.lockScreen(this)
            if (!locked) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_ACCESSIBILITY_GUIDE", true)
                }
                startActivity(intent)
            }
        } else {
            // First tap registered
            lastTapTime = currentTime
            if (!isSecretMode) {
                overlayView?.animate()?.scaleX(1.2f)?.scaleY(1.2f)?.setDuration(80)?.withEndAction {
                    overlayView?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(80)?.start()
                }?.start()
            }
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
