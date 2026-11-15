package com.hanita.partner.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CyclePredictor
import com.example.utils.PersianNumbers
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerDay
import com.hanita.core.partner.PartnerDay.Phase
import com.hanita.core.partner.PartnerLink
import com.hanita.core.gentleman.Sources
import com.hanita.partner.PartnerStore
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

internal val APP_NAME = PartnerLink.PARTNER_APP_NAME

internal fun toast(context: Context, text: String) = Toast.makeText(context, text, Toast.LENGTH_LONG).show()

/** Opens the share sheet (SMS, Telegram, WhatsApp, …): nothing is ever sent automatically. */
internal fun share(context: Context, message: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        context.startActivity(Intent.createChooser(send, "ارسال پیام"))
    } catch (e: ActivityNotFoundException) {
        toast(context, "برنامه‌ای برای ارسال پیام پیدا نشد.")
    }
}

internal fun phaseIcon(phase: Phase): ImageVector = when (phase) {
    Phase.PERIOD -> Icons.Filled.WaterDrop
    Phase.AFTER_PERIOD -> Icons.AutoMirrored.Filled.TrendingUp
    Phase.MID_CYCLE -> Icons.Filled.Bolt
    Phase.LUTEAL -> Icons.Filled.NightsStay
    Phase.PMS -> Icons.Filled.Thermostat
    Phase.PERIOD_DUE -> Icons.Filled.Schedule
    Phase.AWAITING_UPDATE -> Icons.Filled.HourglassEmpty
    Phase.STALE -> Icons.Filled.History
    Phase.PAUSED -> Icons.Filled.PauseCircleOutline
    Phase.NO_DATA -> Icons.Filled.FavoriteBorder
}

/** Titles without emoji: the app keeps a clean, text-first look. */
internal fun plain(title: String): String =
    title.filter { it.code < 0x2190 || it in '؀'..'ۿ' || it == '‌' }.trim()

@Composable
internal fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

/** Small section label in the accent colour. */
@Composable
internal fun Label(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = color)
}

@Composable
internal fun Wordmark(subtitle: String, onBack: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
            }
            Spacer(Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                APP_NAME.uppercase(),
                fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(subtitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        trailing()
    }
}

@Composable
internal fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A round icon tile used in rows and heroes. */
@Composable
internal fun IconTile(icon: ImageVector, size: Int = 44) {
    Box(
        Modifier.size(size.dp).background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
internal fun Bullet(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 9.dp).size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, lineHeight = 22.sp)
    }
}

/**
 * The cycle as one slim bar, read right to left: period, (fertile days when shared), PMS, and a
 * marker on today.
 */
@Composable
internal fun CycleBar(card: PartnerCard, cycleDay: Int) {
    val length = card.cycleLength
    val ovulation = CyclePredictor.ovulationDay(length)
    val track = MaterialTheme.colorScheme.surfaceVariant
    val pms = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val fertile = MaterialTheme.colorScheme.secondary
    val marker = MaterialTheme.colorScheme.onSurface
    val markerRing = MaterialTheme.colorScheme.surface
    val day = cycleDay.coerceIn(1, length)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            Modifier.fillMaxWidth().height(18.dp).semantics {
                contentDescription = "روز ${PersianNumbers.toPersianDigits(day)} از ${PersianNumbers.toPersianDigits(length)} روز چرخه"
            }
        ) {
            val barH = 8.dp.toPx()
            val top = (size.height - barH) / 2
            val unit = size.width / length
            // Day 1 starts on the right (RTL).
            fun x(fromDay: Int) = size.width - (fromDay - 1) * unit
            fun segment(from: Int, to: Int, color: Color) {
                val right = x(from)
                val left = x(to + 1)
                drawRoundRect(color, Offset(left, top), Size(right - left, barH), CornerRadius(barH / 2))
            }
            segment(1, length, track)
            segment(1, card.periodLength.coerceAtMost(length), PeriodRed)
            if (card.shareFertile) segment((ovulation - 5).coerceAtLeast(1), (ovulation + 1).coerceAtMost(length), fertile)
            segment(length - PartnerDay.PMS_DAYS + 1, length, pms)
            val cx = x(day) - unit / 2
            drawCircle(markerRing, radius = size.height / 2, center = Offset(cx, size.height / 2))
            drawCircle(marker, radius = size.height / 2 - 3.dp.toPx(), center = Offset(cx, size.height / 2))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(PeriodRed, "پریود")
            if (card.shareFertile) Legend(fertile, "باروری")
            Legend(pms, "پیش از پریود")
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun Step(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(26.dp).border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(number, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
internal fun UpdateButtons(onScan: () -> Unit, onPaste: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onScan, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("اسکن QR")
        }
        OutlinedButton(onClick = onPaste, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) {
            Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("جای‌گذاری")
        }
    }
}

@Composable
internal fun MessageRow(text: String, onSend: (String) -> Unit, onDelete: ((String) -> Unit)?) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
        if (onDelete != null) {
            IconButton(onClick = { onDelete(text) }) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف پیام", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = { onSend(text) }) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال پیام", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
internal fun AddMessageDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیام جدید", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(PartnerStore.MAX_MESSAGE_LENGTH) },
                placeholder = { Text("مثلاً: امشب شام با من") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.trim()) }, enabled = text.isNotBlank()) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
internal fun SwitchRow(title: String, checked: Boolean, subtitle: String? = null, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            if (subtitle != null) Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
internal fun MutedText(text: String) {
    Text(text, fontSize = 12.5.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Opens a web page in the phone's browser (this app itself never goes online). */
internal fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        toast(context, "مرورگری برای باز کردن لینک پیدا نشد.")
    }
}

/** A tappable row with an icon, a title, an optional subtitle and a chevron. */
@Composable
internal fun Tile(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** "Based on" list under a pillar, test or tool; entries with a link open in the browser. */
@Composable
internal fun SourcesPanel(sources: List<Sources.Source>, title: String = "منابع") {
    if (sources.isEmpty()) return
    val context = LocalContext.current
    Panel {
        Label(title, color = MaterialTheme.colorScheme.secondary)
        sources.forEach { source -> SourceRow(source) { url -> openUrl(context, url) } }
    }
}

@Composable
internal fun SourceRow(source: Sources.Source, onOpen: (String) -> Unit) {
    val url = source.url
    Row(
        Modifier.fillMaxWidth().then(
            if (url != null) Modifier.clickable(role = Role.Button) { onOpen(url) } else Modifier
        ),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.padding(top = 4.dp).width(3.dp).height(36.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(source.author, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(source.title, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(source.note, fontSize = 12.5.sp, lineHeight = 20.sp)
        }
        if (url != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "باز کردن منبع",
                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 6.dp).size(18.dp)
            )
        }
    }
}

/** Seven small squares, oldest on the right (RTL), filled where the habit was done. */
@Composable
internal fun WeekStrip(days: List<Boolean>) {
    val on = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.surfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics {
            contentDescription = "${PersianNumbers.toPersianDigits(days.count { it })} روز از ${PersianNumbers.toPersianDigits(days.size)} روز اخیر"
        }
    ) {
        days.forEach { done -> Box(Modifier.size(12.dp).background(if (done) on else off, MaterialTheme.shapes.extraSmall)) }
    }
}

/** A small line chart of scores (oldest first) on a 0..[max] axis. */
@Composable
internal fun ScoreTrend(scores: List<Int>, max: Int, modifier: Modifier = Modifier) {
    if (scores.size < 2) return
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(64.dp).semantics { contentDescription = "روند امتیازها" }) {
        val stepX = size.width / (scores.size - 1)
        fun y(v: Int) = size.height - (v.coerceIn(0, max).toFloat() / max) * size.height
        drawLine(grid, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
        val path = Path()
        // RTL: the newest point sits on the left end, like the rest of the layout reads.
        scores.forEachIndexed { i, v ->
            val x = size.width - i * stepX
            if (i == 0) path.moveTo(x, y(v)) else path.lineTo(x, y(v))
        }
        drawPath(path, line, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        scores.forEachIndexed { i, v -> drawCircle(line, 3.5.dp.toPx(), Offset(size.width - i * stepX, y(v))) }
    }
}

/** A thin progress bar for 0..100. */
@Composable
internal fun Meter(percent: Int, color: Color = MaterialTheme.colorScheme.primary) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(Modifier.fillMaxWidth().height(6.dp).background(track, CircleShape)) {
        Box(Modifier.fillMaxWidth(percent.coerceIn(0, 100) / 100f).fillMaxHeight().background(color, CircleShape))
    }
}
