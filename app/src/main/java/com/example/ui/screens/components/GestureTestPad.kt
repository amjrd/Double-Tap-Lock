package com.example.ui.screens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch

@Composable
fun GestureTestPad(
    statusText: String,
    isSuccess: Boolean,
    lastIntervalMs: Long?,
    tapsCount: Int,
    onPadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val rippleAlpha = remember { Animatable(0f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gesture_test_pad_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "اختبار النقر",
                        tint = CyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "منطقة تدريب ومعايرة النقر المزدوج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "جرب سرعة نقراتك ولاحظ استجابة النظام بالمللي ثانية",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Pad
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleAnim.value)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isSuccess) listOf(
                                AccentEmerald.copy(alpha = 0.35f),
                                SlateSurfaceVariant
                            ) else listOf(
                                CyanPrimary.copy(alpha = 0.25f),
                                SlateSurfaceVariant
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSuccess) AccentEmerald else CyanPrimary,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            scaleAnim.animateTo(0.92f, tween(50))
                            scaleAnim.animateTo(1f, tween(90))
                        }
                        onPadClick()
                    }
                    .testTag("interactive_double_tap_pad"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Double Tap Target",
                        tint = if (isSuccess) AccentEmerald else CyanPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "انقر مرتين",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    if (lastIntervalMs != null) {
                        Text(
                            text = "$lastIntervalMs ms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSuccess) AccentEmerald else CyanPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSuccess) AccentEmerald.copy(alpha = 0.15f) else SlateSurfaceVariant
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isSuccess) AccentEmerald else Color.White
                )
            }
        }
    }
}
