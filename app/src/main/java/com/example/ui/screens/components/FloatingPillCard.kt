package com.example.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import kotlin.math.roundToInt

@Composable
fun FloatingPillCard(
    isPillEnabled: Boolean,
    canDrawOverlays: Boolean,
    pillOpacity: Float,
    doubleTapSpeedMs: Int,
    hapticEnabled: Boolean,
    onPillToggle: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onOpacityChange: (Float) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_pill_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "الزر العائم",
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "الزر العائم (نقر مزدوج على الشاشة)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "نقطة لمس عائمة قابلة للتحريك فوق أي تطبيق",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isPillEnabled,
                    onCheckedChange = { checked ->
                        if (checked && !canDrawOverlays) {
                            onRequestOverlayPermission()
                        } else {
                            onPillToggle(checked)
                        }
                    },
                    modifier = Modifier.testTag("floating_pill_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanPrimary
                    )
                )
            }

            if (!canDrawOverlays) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRequestOverlayPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("request_overlay_permission_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SlateSurfaceVariant,
                        contentColor = CyanPrimary
                    )
                ) {
                    Text(
                        text = "منح إذن الظهور فوق التطبيقات (Overlay)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Double tap speed slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurfaceVariant)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "سرعة النقر",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مهلة النقر المزدوج (Double Tap Threshold)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "$doubleTapSpeedMs مللي ثانية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanPrimary
                    )
                }

                Slider(
                    value = doubleTapSpeedMs.toFloat(),
                    onValueChange = { onSpeedChange(it.roundToInt()) },
                    valueRange = 200f..500f,
                    steps = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("double_tap_speed_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = SlateBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opacity slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurfaceVariant)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "الشفافية",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "شفافية الزر العائم (Opacity)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "${(pillOpacity * 100).roundToInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanPrimary
                    )
                }

                Slider(
                    value = pillOpacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.2f..1.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pill_opacity_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = SlateBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Haptic toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "الاهتزاز اللمسي",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الاهتزاز اللمسي (Haptic Feedback)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "نبضة اهتزاز فورية دقيقة عند نجاح القفل",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggle,
                    modifier = Modifier.testTag("haptic_feedback_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanPrimary
                    )
                )
            }
        }
    }
}
