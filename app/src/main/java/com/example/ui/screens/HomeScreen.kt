package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.components.AccessibilityStatusCard
import com.example.ui.screens.components.BatteryStatsCard
import com.example.ui.screens.components.FloatingPillCard
import com.example.ui.screens.components.GestureTestPad
import com.example.ui.screens.components.HeroSection
import com.example.ui.screens.components.PixelGuideSection
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateSurface
import com.example.ui.viewmodel.TapLockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TapLockViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(SlateBackground),
            containerColor = SlateBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "TapLock",
                                    tint = SlateBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TapLock • إطفاء الشاشة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.refreshPermissions()
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم تحديث حالة الأذونات والخدمة")
                                }
                            },
                            modifier = Modifier.testTag("refresh_permissions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث الحالة",
                                tint = CyanPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SlateSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                HeroSection(
                    isServiceActive = uiState.isAccessibilityEnabled,
                    onInstantLockClick = {
                        if (uiState.isAccessibilityEnabled) {
                            val locked = viewModel.instantLockScreen()
                            if (!locked) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("تعذر قفل الشاشة، تأكد من تشغيل الخدمة")
                                }
                            }
                        } else {
                            openAccessibilitySettings(context)
                        }
                    }
                )

                // Accessibility Status & Permissions Card
                AccessibilityStatusCard(
                    isServiceActive = uiState.isAccessibilityEnabled,
                    onOpenAccessibilitySettings = {
                        openAccessibilitySettings(context)
                    }
                )

                // Floating Pill Double Tap Customization Card
                FloatingPillCard(
                    isPillEnabled = uiState.floatingPillEnabled,
                    canDrawOverlays = uiState.canDrawOverlays,
                    pillOpacity = uiState.pillOpacity,
                    doubleTapSpeedMs = uiState.doubleTapSpeedMs,
                    hapticEnabled = uiState.hapticFeedbackEnabled,
                    onPillToggle = { enabled ->
                        viewModel.setFloatingPill(enabled)
                    },
                    onRequestOverlayPermission = {
                        openOverlayPermissionSettings(context)
                    },
                    onOpacityChange = { opacity ->
                        viewModel.setPillOpacity(opacity)
                    },
                    onSpeedChange = { speed ->
                        viewModel.setDoubleTapSpeed(speed)
                    },
                    onHapticToggle = { haptic ->
                        viewModel.setHapticFeedback(haptic)
                    }
                )

                // Interactive Double Tap Test Pad
                GestureTestPad(
                    statusText = uiState.testPadStatusText,
                    isSuccess = uiState.testPadIsSuccess,
                    lastIntervalMs = uiState.testPadLastIntervalMs,
                    tapsCount = uiState.testPadTapsCount,
                    onPadClick = {
                        viewModel.onTestPadTapped()
                    }
                )

                // Battery and Device Health Stats Card
                BatteryStatsCard(lockCount = uiState.lockCount)

                // Pixel 8 Guide & Quick Settings
                PixelGuideSection()

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

private fun openOverlayPermissionSettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}
