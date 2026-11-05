package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CycleInfo
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekPrimary

@Composable
fun SmartInsightsCard(
    cycleInfo: CycleInfo?,
    modifier: Modifier = Modifier
) {
    if (cycleInfo == null) return

    // Determine advice based on current phase
    val (title, icon, advice, bgColor, contentColor) = when {
        cycleInfo.isPeriod -> {
            arrayOf(
                "مراقبت‌های دوران قاعدگی",
                Icons.Default.Spa,
                "در این فاز، استراحت کافی داشته باشید. مصرف چای زنجبیل و دارچین به کاهش دردهای شکمی (کرامپ) کمک می‌کند. استفاده از کیسه آب گرم روی شکم و کمر توصیه می‌شود.",
                Color(0xFFFEE2E2), // Red 100
                Color(0xFFDC2626)  // Red 600
            )
        }
        cycleInfo.isFertile -> {
            arrayOf(
                "فاز باروری و تخمک‌گذاری",
                Icons.Default.Lightbulb,
                "سطح استروژن در اوج است! شما در بالاترین سطح انرژی و جذابیت هستید. زمان مناسبی برای ورزش‌های سنگین، کارهای چالشی و تعاملات اجتماعی است.",
                Color(0xFFFEF3C7), // Amber 100
                Color(0xFFD97706)  // Amber 600
            )
        }
        cycleInfo.isPms -> {
            arrayOf(
                "مدیریت علائم PMS",
                Icons.Default.LocalDrink,
                "به دلیل افت پروژسترون، ممکن است احساس خستگی یا حساسیت داشته باشید. مصرف آب کافی، شکلات تلخ و مکمل منیزیم می‌تواند نوسانات خلقی و نفخ را کاهش دهد.",
                Color(0xFFF3E8FF), // Purple 100
                Color(0xFF9333EA)  // Purple 600
            )
        }
        cycleInfo.isLate -> {
            arrayOf(
                "تأخیر در قاعدگی",
                Icons.Default.Lightbulb,
                "تأخیر چندروزه معمولاً به استرس، کم‌خوابی، تغییر وزن یا بیماری برمی‌گردد. اگر احتمال بارداری وجود دارد تست بدهید و اگر تأخیر بیش از دو هفته شد یا درد شدید دارید، با پزشک مشورت کنید.",
                Color(0xFFFEE2E2), // Red 100
                Color(0xFFDC2626)  // Red 600
            )
        }
        cycleInfo.phase == com.example.domain.CyclePredictor.Phase.LUTEAL -> {
            arrayOf(
                "فاز لوتئال",
                Icons.Default.Spa,
                "پروژسترون در حال افزایش است و ممکن است اشتهای شما بیشتر شود. خواب منظم، ورزش ملایم مثل پیاده‌روی و یوگا، و غذاهای سرشار از فیبر و منیزیم به حفظ تعادل کمک می‌کند.",
                Color(0xFFEDE9FE), // Violet 100
                Color(0xFF7C3AED)  // Violet 600
            )
        }
        else -> {
            arrayOf(
                "فاز فولیکولی",
                Icons.Default.Lightbulb,
                "بدن در حال بازسازی است. انرژی شما به تدریج افزایش می‌یابد. تغذیه سالم با پروتئین کافی و سبزیجات به بهبود خلق و خو کمک می‌کند.",
                Color(0xFFE0F2FE), // Sky 100
                Color(0xFF0284C7)  // Sky 600
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(bgColor as Color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                        contentDescription = null,
                        tint = contentColor as Color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "پیشنهاد هوشمند برای امروز",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = title as String,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = (bgColor as Color).copy(alpha = 0.3f)
            ) {
                Text(
                    text = advice as String,
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}
