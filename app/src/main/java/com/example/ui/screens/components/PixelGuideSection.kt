package com.example.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun PixelGuideSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pixel_guide_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "دليل الاستخدام لهواتف Pixel 8 وأندرويد الحديثة",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Guide Item 1: Quick Settings Tile
            GuideItem(
                icon = Icons.Default.DashboardCustomize,
                iconColor = CyanPrimary,
                title = "مربع الإعدادات السريعة (Quick Settings Tile)",
                description = "اسحب شريط الإشعارات في Pixel 8 للأسفل، اضغط على أيقونة القلم للتعديل، ثم اسحب مربع 'إطفاء الشاشة' للأعلى. بلمسة واحدة ستطفئ الشاشة من أي شاشة."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Guide Item 2: Home Screen Widget
            GuideItem(
                icon = Icons.Default.Widgets,
                iconColor = CyanPrimary,
                title = "ودجت الشاشة الرئيسية (Home Screen Widget)",
                description = "اضغط مطولاً على خلفية شاشة هاتف Pixel 8، اختر 'Widgets'، وابحث عن TapLock لتثبيت أيقونة القفل الفوري بجانب تطبيقاتك المفضلة."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Guide Item 3: Battery & Security
            GuideItem(
                icon = Icons.Default.Security,
                iconColor = AccentEmerald,
                title = "أمان تام وحفاظ 100% على البطارية",
                description = "لا تتطلب الخدمة تشغيل GPS أو كاميرا أو اتصالات بالإنترنت. استهلاك البطارية هو 0.0% أثناء الخمول، مع حماية زر الباور الفيزيائي من التآكل."
            )
        }
    }
}

@Composable
private fun GuideItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateSurfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
