package com.example.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import kotlin.math.roundToInt

@Composable
fun FloatingPillCard(
    isPillEnabled: Boolean,
    isSecretMode: Boolean,
    zonePosition: String,
    zoneHeightDp: Int,
    canDrawOverlays: Boolean,
    pillOpacity: Float,
    doubleTapSpeedMs: Int,
    hapticEnabled: Boolean,
    onPillToggle: (Boolean) -> Unit,
    onSecretModeToggle: (Boolean) -> Unit,
    onZonePositionChange: (String) -> Unit,
    onZoneHeightChange: (Int) -> Unit,
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
        border = BorderStroke(1.dp, if (isSecretMode && isPillEnabled) AccentEmerald.copy(alpha = 0.5f) else SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Main Service Switch
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
                            .background(if (isSecretMode) AccentEmerald.copy(alpha = 0.15f) else CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSecretMode) Icons.Default.VisibilityOff else Icons.Default.TouchApp,
                            contentDescription = "النقر المزدوج السري",
                            tint = if (isSecretMode) AccentEmerald else CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "النقر المزدوج السري الشامل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isSecretMode) "انقر مرتين في أي مكان بدون أيقونة ظاهرة" else "أيقونة طافية مرئية على الشاشة",
                            fontSize = 12.sp,
                            color = if (isSecretMode) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
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
                        checkedTrackColor = if (isSecretMode) AccentEmerald else CyanPrimary
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
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secret Mode Toggle (Invisible Ghost Zone)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSecretMode) AccentEmerald.copy(alpha = 0.12f) else SlateSurfaceVariant)
                    .border(
                        1.dp,
                        if (isSecretMode) AccentEmerald.copy(alpha = 0.35f) else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSecretMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "الوضع السري",
                        tint = if (isSecretMode) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الوضع السري المخفي (بدون أيقونة) 👻",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isSecretMode) "الشاشة نقية 100% واللمس يعمل في أي مكان" else "الأيقونة ظاهرة على الشاشة",
                            fontSize = 11.sp,
                            color = if (isSecretMode) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isSecretMode,
                    onCheckedChange = onSecretModeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentEmerald
                    )
                )
            }

            // Anti-Interference Info Banner
            if (isSecretMode) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F243A))
                        .border(1.dp, CyanPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "حماية التداخل",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تقنية منع التداخل الذكية (Zero Interference)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• النقر المزدوج في أي مكان يمين، وسط، أو يسار الشاشة = إطفاء فوري.\n• السحب لأسفل (Swipe Down) = تفتح ستارة الإشعارات فوراً دون أي إعاقة أو حجب.\n• النقرة العادية المفردة = يتم تجاهلها لمنع الإغلاق الخاطئ.",
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Position Choice: Full Top Bar or Full Bottom Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SlateSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "موقع شريط النقر السري الواسع:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PositionBarChip(
                            label = "الشريط العلوي كاملاً (شريط الحالة)",
                            subLabel = "انقر مرتين في أي مكان بأعلى الهاتف",
                            isSelected = zonePosition == "STATUS_BAR",
                            onClick = { onZonePositionChange("STATUS_BAR") },
                            modifier = Modifier.weight(1f)
                        )
                        PositionBarChip(
                            label = "الشريط السفلي (أسفل الشاشة)",
                            subLabel = "انقر مرتين في أي مكان بأسفل الهاتف",
                            isSelected = zonePosition == "BOTTOM_NAV",
                            onClick = { onZonePositionChange("BOTTOM_NAV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zone Height (Width expansion) Slider
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
                                imageVector = Icons.Default.Height,
                                contentDescription = "عرض المساحة",
                                tint = AccentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سعة مساحة النقر (ارتفاع الشريط)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${zoneHeightDp}dp (مساحة واسعة)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentEmerald
                        )
                    }

                    Slider(
                        value = zoneHeightDp.toFloat(),
                        onValueChange = { onZoneHeightChange(it.roundToInt()) },
                        valueRange = 36f..84f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentEmerald,
                            activeTrackColor = AccentEmerald,
                            inactiveTrackColor = SlateBorder
                        )
                    )
                }
            } else {
                // If visible mode: Opacity slider
                Spacer(modifier = Modifier.height(12.dp))
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
                        Text(
                            text = "شفافية الأيقونة المرئية",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
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
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = SlateBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            text = "نبضة اهتزاز فورية دقيقة تؤكد إطفاء الشاشة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun PositionBarChip(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentEmerald.copy(alpha = 0.22f) else Color(0xFF1E293B))
            .border(
                1.5.dp,
                if (isSelected) AccentEmerald else SlateBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AccentEmerald else Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
