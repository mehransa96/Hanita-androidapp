package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.CycleInfo

@Composable
fun PartnerSyncDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    userName: String,
    cycleInfo: CycleInfo
) {
    if (!show) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Determine current phase details
    val (phaseTitle, phaseEmoji, phaseTone, supportTips, messagePreset) = when {
        cycleInfo.isPeriod -> Quintuple(
            "فاز قاعدگی (Menstruation)",
            "🩸",
            "نیاز به آرامش، استراحت و محبت بیشتر",
            listOf(
                "یک فنجان چای یا دمنوش گرم (دارچین، زنجبیل، نبات) آماده کن.",
                "کیسه آب گرم یا ماساژ ملایم کمر می‌تواند معجزه کند.",
                "در کارهای سنگین خانه یا خرید پیش‌قدم شو.",
                "صبوری در برابر کم‌حوصلگی جسمانی، بهترین ابراز عشق است."
            ),
            "عزیزم سلام ❤️ امروز در روزهای قاعدگی هستم و کمی خستگی جسمی دارم. وجود پرمهر و صبوریت به من بی‌نهایت آرامش می‌ده. ممنون که هوامو داری 🌸"
        )
        cycleInfo.isFertile -> Quintuple(
            "فاز تخمک‌گذاری (Ovulation)",
            "✨",
            "انرژی بالا، نشاط و صمیمیت عاطفی مضاعف",
            listOf(
                "یک برنامه شام دو نفره یا پیاده‌روی عاشقانه ترتیب دهید.",
                "بهترین زمان برای گفتگوهای عمیق و برنامه‌ریزی آینده است.",
                "احساس جذابیت و درخشش او را با کلمات ستایش کن.",
                "در صورت تمایل به بارداری، اکنون بالاترین شانس باروری است."
            ),
            "سلام مهربانم ✨ امروز حالم عالیه، پر از انرژی و شادابی‌ام! دوست دارم وقت بیشتری رو کنار هم با آرامش و شادی بگذرونیم 💕"
        )
        cycleInfo.isPms -> Quintuple(
            "فاز لوتئال و سندروم پیش‌قاعدگی (PMS)",
            "🍂",
            "احتمال نوسان خلق‌وخو، خستگی و نیاز به همدلی",
            listOf(
                "سعی کن حرف‌های تند یا حساسیت‌ها را به دل نگیری؛ این اثر تغییرات هورمونی است.",
                "شکلات تلخ یا تنقلات موردعلاقه‌اش را فراهم کن.",
                "به او یادآوری کن چقدر دوستش داری و درکش می‌کنی.",
                "از جروبحث‌های بی‌مورد در این چند روز پرهیز کنید."
            ),
            "سلام عشق من 🌸 این روزها در فاز PMS هستم و هورمون‌ها بدنم رو کمی حساس‌تر و خسته‌تر کردن. اگر گاهی بی‌حوصله‌ام بدون از ته دلم دوستت دارم و همراهی صبورانه‌ت برام یک دنیا ارزش داره ❤️"
        )
        else -> Quintuple(
            "فاز فولیکولار (Follicular)",
            "🌱",
            "تجدید قوا، تمرکز و برنامه‌ریزی مثبت",
            listOf(
                "از ایده‌های جدید و پروژه‌های مشترک استقبال کنید.",
                "ورزش دو نفره یا کارهای خلاقانه عالی هستند.",
                "او در حال بازیابی کامل انرژی و انگیزه است."
            ),
            "سلام عزیزم 🌱 امروز حس شادابی و انگیزه خوبی دارم! خوشحالم که تو مسیر زندگی در کنارتم و پرانرژی به سمت هدف‌هامون می‌ریم 🌟"
        )
    }

    val shareMessage = {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, messagePreset)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "ارسال پیام به همسر")
        context.startActivity(shareIntent)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFFFF1F2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(phaseEmoji, fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "حالت همراه و اشتراک با همسر 💕",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "همدلی آگاهانه و درک چرخه‌های هورمونی",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "وضعیت امروز شما: $phaseTitle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFFE11D48)
                            )
                            Text(
                                text = phaseTone,
                                fontSize = 11.5.sp,
                                color = Color(0xFF9F1239),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // How your partner can support you
                    Text("راهنمای حمایت و توجه برای همراه شما:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        supportTips.forEach { tip ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💡", fontSize = 13.sp)
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Message Preset to Send
                    Text("متن پیشنهادی برای ارسال پیام محبت‌آمیز:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = messagePreset,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(messagePreset))
                            Toast.makeText(context, "متن پیام در حافظه کپی شد.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کپی پیام", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = shareMessage,
                        modifier = Modifier.weight(1.3f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ارسال به همسر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
