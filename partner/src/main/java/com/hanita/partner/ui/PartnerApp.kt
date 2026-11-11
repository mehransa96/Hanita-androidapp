package com.hanita.partner.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.CyclePredictor
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.core.partner.PartnerDay.Phase
import com.hanita.core.partner.PartnerLink
import com.hanita.partner.PartnerStore
import com.hanita.partner.Reminders
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val APP_NAME = PartnerLink.PARTNER_APP_NAME

/** A message that needs the partner's attention after a scan or a received link. */
private class Notice(val title: String, val text: String)

private enum class Tab(val title: String, val icon: ImageVector) {
    TODAY("امروز", Icons.Filled.Today),
    MESSAGES("پیام‌ها", Icons.Filled.ChatBubbleOutline),
    SETTINGS("تنظیمات", Icons.Filled.Tune)
}

@Composable
fun PartnerApp(incomingText: String?, onIncomingHandled: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val store = remember { PartnerStore(context) }
    var version by remember { mutableIntStateOf(0) }
    val paired = remember(version) { store.pairing != null }
    val card = remember(version) { store.card }
    val receivedAt = remember(version) { store.receivedAt }
    val today = LocalDate.now()
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }

    var notice by remember { mutableStateOf<Notice?>(null) }
    var showAddMessage by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun handle(text: String?, fromScan: Boolean) {
        val result = store.receive(text, fromScan)
        version++
        Reminders.reschedule(context)
        when (result) {
            is PartnerStore.Result.Paired -> {
                tab = Tab.TODAY
                notice = Notice(
                    "وصل شدی ✓",
                    "کد تأیید: ${PersianNumbers.toPersianDigits(result.code)}\n" +
                        "این کد باید با کد روی گوشی ${result.ownerName.ifBlank { "همسرت" }} یکی باشد. اگر فرق دارد، دوباره اسکن کن."
                )
                askForNotifications()
            }
            is PartnerStore.Result.Updated -> toast(context, "اطلاعات به‌روز شد")
            PartnerStore.Result.Disconnected -> notice = Notice(
                "اتصال قطع شد",
                "همسرت اتصال را قطع کرد و همه اطلاعات از این گوشی پاک شد. هر وقت خواست، با یک QR تازه دوباره وصل شوید."
            )
            PartnerStore.Result.Older -> toast(context, "این به‌روزرسانی از اطلاعات فعلی قدیمی‌تر است.")
            PartnerStore.Result.PairingNeedsScan -> notice = Notice(
                "کد اتصال را اسکن کن",
                "کد اتصال فقط با «اسکن QR» از روی گوشی همسرت پذیرفته می‌شود، چون کلید رمز داخل آن است و نباید از پیام‌رسان رد شود."
            )
            PartnerStore.Result.WrongPair -> notice = Notice(
                "مال این اتصال نیست",
                "این به‌روزرسانی برای اتصال فعلی نیست. اگر همسرت در هانیتا «اتصال دوباره» را زده، QR جدید را اسکن کن."
            )
            PartnerStore.Result.NotPaired -> notice = Notice(
                "هنوز وصل نیستی",
                "اول در هانیتا (تب «همراه»، دکمه QR) کد اتصال ساخته شود و با همین برنامه اسکنش کن."
            )
            PartnerStore.Result.Invalid -> toast(context, "کد معتبر نبود یا ناقص کپی شده است.")
        }
    }

    LaunchedEffect(incomingText) {
        if (incomingText != null) {
            handle(incomingText, fromScan = false)
            onIncomingHandled()
        }
    }

    val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { handle(it, fromScan = true) }
    }
    fun scan() {
        scanner.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("کد QR روی گوشی همسرت را داخل کادر بگیر")
                .setBeepEnabled(false)
                .setOrientationLocked(false)
        )
    }
    fun paste() {
        val text = clipboard.getText()?.text
        if (text.isNullOrBlank()) toast(context, "چیزی کپی نشده است. اول پیام به‌روزرسانی را کپی کن.")
        else handle(text, fromScan = false)
    }

    val day = PartnerDay.today(card, today)
    val info = PartnerContent.phaseInfo(day.phase, card?.shareFertile == true)
    val name = card?.ownerName.orEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (paired) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    Tab.values().forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Icon(t.icon, contentDescription = null) },
                            label = { Text(t.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!paired) {
                item { Onboarding(onScan = ::scan) }
            } else when (tab) {
                Tab.TODAY -> todayTab(
                    card = card, day = day, info = info, name = name, today = today, receivedAt = receivedAt,
                    daysBefore = store.daysBeforePeriod,
                    onSend = { share(context, it) },
                    onAllMessages = { tab = Tab.MESSAGES },
                    onScan = ::scan, onPaste = ::paste
                )
                Tab.MESSAGES -> messagesTab(
                    suggested = info.messages.map { PartnerContent.personalize(it, name) },
                    own = store.customMessages,
                    onSend = { share(context, it) },
                    onAdd = { showAddMessage = true },
                    onDelete = { message ->
                        store.customMessages = store.customMessages - message
                        version++
                    }
                )
                Tab.SETTINGS -> item(key = "settings-$version") {
                    SettingsScreen(
                        store = store,
                        fertileShared = card?.shareFertile == true,
                        onEnableReminders = ::askForNotifications,
                        onChanged = { Reminders.reschedule(context) },
                        onScan = ::scan,
                        onPaste = ::paste,
                        onDisconnect = {
                            store.clearAll()
                            version++
                            tab = Tab.TODAY
                            Reminders.reschedule(context)
                            toast(context, "اتصال قطع شد و اطلاعات پاک شد.")
                        }
                    )
                }
            }
        }
    }

    notice?.let { n ->
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(n.title, fontWeight = FontWeight.Bold) },
            text = { Text(n.text, lineHeight = 22.sp) },
            confirmButton = { TextButton(onClick = { notice = null }) { Text("باشه") } }
        )
    }

    if (showAddMessage) {
        AddMessageDialog(
            onDismiss = { showAddMessage = false },
            onSave = { text ->
                store.customMessages = store.customMessages + text
                showAddMessage = false
                version++
            }
        )
    }
}

private fun toast(context: Context, text: String) = Toast.makeText(context, text, Toast.LENGTH_LONG).show()

/** Opens the share sheet (SMS, Telegram, WhatsApp, …): nothing is ever sent automatically. */
private fun share(context: Context, message: String) {
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

private fun phaseIcon(phase: Phase): ImageVector = when (phase) {
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

/** Titles without emoji: the partner app keeps a clean, text-first look. */
private fun plain(title: String): String = title.filter { it.code < 0x2190 || it in '؀'..'ۿ' || it == '‌' }.trim()

// ---------------------------------------------------------------- building blocks

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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

/** Small uppercase-style section label in the accent colour. */
@Composable
private fun Label(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Wordmark(subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                APP_NAME.uppercase(),
                fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(subtitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * The cycle as one slim bar, read right to left: period, (fertile days when shared), PMS, and a
 * marker on today. Purely visual; the numbers are in the stats above it.
 */
@Composable
private fun CycleBar(card: PartnerCard, cycleDay: Int) {
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

// ---------------------------------------------------------------- screens

@Composable
private fun Onboarding(onScan: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(
            APP_NAME.uppercase(),
            fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "همراهی آرام و مطمئن، در همه روزهای ماه.",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
        )
        Panel {
            Step("۱", "همسرت در هانیتا تب «همراه» را باز کند و دکمه QR را بزند.")
            Step("۲", "اینجا «اسکن QR» را بزن و کد روی گوشی او را اسکن کن.")
            Step("۳", "هر روز ببین در کدام دوره است، چه کاری کمک می‌کند و چه پیامی بفرستی.")
        }
        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("اسکن QR اتصال", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text(
            "بدون اینترنت و بدون حساب کاربری. فقط تاریخ‌های کلی چرخه، رمزنگاری‌شده از هانیتا می‌رسد؛ علائم و یادداشت‌های همسرت هرگز اینجا نمی‌آید.",
            fontSize = 12.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Step(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(26.dp).border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(number, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

private fun LazyListScope.todayTab(
    card: PartnerCard?,
    day: PartnerDay.Today,
    info: PartnerContent.PhaseInfo,
    name: String,
    today: LocalDate,
    receivedAt: Long,
    daysBefore: Int,
    onSend: (String) -> Unit,
    onAllMessages: () -> Unit,
    onScan: () -> Unit,
    onPaste: () -> Unit
) {
    item { Wordmark(if (name.isBlank()) "امروز" else "امروزِ $name") }

    item {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(phaseIcon(day.phase), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plain(info.title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (day.fertile) Text("روزهای باروری", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Text(info.summary, fontSize = 14.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (day.cycleDay > 0 || day.daysUntilPeriod != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (day.cycleDay > 0) Stat(PersianNumbers.toPersianDigits(day.cycleDay), "روز چرخه", Modifier.weight(1f))
                    day.daysUntilPeriod?.let {
                        Stat(if (it == 0) "امروز" else PersianNumbers.toPersianDigits(it), "روز تا پریود", Modifier.weight(1f))
                    }
                }
            }
            if (card != null && card.hasCycleData && day.cycleDay > 0) CycleBar(card, day.cycleDay)
        }
    }

    val note = card?.note.orEmpty()
    if (note.isNotBlank() && card?.hasCycleData == true) {
        item {
            Panel {
                Label("یادداشت ${name.ifBlank { "همسرت" }}")
                Text(note, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }
    }

    item(key = "tasks-$today-${day.phase}") { TodayTasks(info.tips.take(3)) }

    item {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Label("پیام پیشنهادی") }
                TextButton(onClick = onAllMessages) { Text("همه پیام‌ها") }
            }
            info.messages.firstOrNull()?.let { MessageRow(PartnerContent.personalize(it, name), onSend, null) }
        }
    }

    item { Upcoming(card, today, daysBefore) }

    item {
        Panel {
            Label("به‌روزرسانی")
            Text(
                if (receivedAt > 0) {
                    val date = Instant.ofEpochMilli(receivedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    "آخرین دریافت: ${PersianDates.long(date)}"
                } else "هنوز به‌روزرسانی‌ای نرسیده است.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            UpdateButtons(onScan, onPaste)
        }
    }
}

/** Three things to do today, ticked off for the day (not stored). */
@Composable
private fun TodayTasks(tasks: List<String>) {
    val done = remember { mutableStateOf(setOf<Int>()) }
    Panel {
        Label("۳ کار امروز")
        tasks.forEachIndexed { i, task ->
            val checked = i in done.value
            Row(
                Modifier.fillMaxWidth().toggleable(
                    value = checked, role = Role.Checkbox,
                    onValueChange = { done.value = if (it) done.value + i else done.value - i }
                ),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    task, fontSize = 14.sp, lineHeight = 22.sp,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun Upcoming(card: PartnerCard?, today: LocalDate, daysBefore: Int) {
    val events = buildSet {
        add(PartnerDay.Event.PERIOD_START)
        add(PartnerDay.Event.PMS_START)
        if (card?.shareFertile == true) add(PartnerDay.Event.FERTILE_START)
    }
    val upcoming = PartnerDay.upcoming(card, today, events, daysBefore)
    Panel {
        Label("پیش رو")
        if (upcoming.isEmpty()) {
            Text(
                "فعلاً پیش‌بینی‌ای نیست. با به‌روزرسانی تازه از هانیتا اینجا پر می‌شود.",
                fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        upcoming.forEachIndexed { i, item ->
            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plain(PartnerContent.eventTitle(item.event)), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(PersianDates.short(item.date), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (item.daysFromToday == 0) "امروز" else "${PersianNumbers.toPersianDigits(item.daysFromToday)} روز دیگر",
                        fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Text(
            "بر اساس میانگین چرخه؛ ممکن است چند روز جابه‌جا شود.",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdateButtons(onScan: () -> Unit, onPaste: () -> Unit) {
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

private fun LazyListScope.messagesTab(
    suggested: List<String>,
    own: List<String>,
    onSend: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    item { Wordmark("پیام‌ها") }
    item {
        Panel {
            Label("پیشنهاد امروز")
            Text(
                "با دکمه ارسال، پیام در پیامک یا پیام‌رسان باز می‌شود و قبل از فرستادن می‌توانی ویرایشش کنی.",
                fontSize = 12.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            suggested.forEach { MessageRow(it, onSend, null) }
        }
    }
    item {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Label("پیام‌های من") }
                TextButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("پیام جدید")
                }
            }
            if (own.isEmpty()) {
                Text(
                    "پیام‌هایی که زیاد می‌فرستی را اینجا نگه دار تا همیشه دم دستت باشد.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                own.forEach { MessageRow(it, onSend, onDelete) }
            }
        }
    }
}

@Composable
private fun MessageRow(text: String, onSend: (String) -> Unit, onDelete: ((String) -> Unit)?) {
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
private fun AddMessageDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
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

/** Settings are saved as soon as they change. */
@Composable
private fun SettingsScreen(
    store: PartnerStore,
    fertileShared: Boolean,
    onEnableReminders: () -> Unit,
    onChanged: () -> Unit,
    onScan: () -> Unit,
    onPaste: () -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(store.remindersEnabled) }
    var events by remember { mutableStateOf(store.reminderEvents) }
    var daysBefore by remember { mutableFloatStateOf(store.daysBeforePeriod.toFloat()) }
    var dailyTip by remember { mutableStateOf(store.dailyTip) }
    var minuteOfDay by remember { mutableIntStateOf(store.reminderMinuteOfDay) }
    var confirmDisconnect by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Wordmark("تنظیمات")
        Panel {
            Label("یادآوری")
            SwitchRow("یادآوری‌ها روشن باشد", enabled) {
                enabled = it
                store.remindersEnabled = it
                if (it) onEnableReminders()
                onChanged()
            }
            if (enabled) {
                val choices = buildList {
                    add(PartnerDay.Event.PERIOD_SOON)
                    add(PartnerDay.Event.PERIOD_START)
                    add(PartnerDay.Event.PMS_START)
                    if (fertileShared) add(PartnerDay.Event.FERTILE_START)
                }
                choices.forEach { event ->
                    val on = event in events
                    Row(
                        Modifier.fillMaxWidth().toggleable(
                            value = on, role = Role.Checkbox,
                            onValueChange = { v ->
                                events = if (v) events + event else events - event
                                store.reminderEvents = events
                                onChanged()
                            }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = on, onCheckedChange = null)
                        Spacer(Modifier.width(6.dp))
                        Text(plain(PartnerContent.eventTitle(event)), fontSize = 14.sp)
                    }
                }
                if (PartnerDay.Event.PERIOD_SOON in events) {
                    Text(
                        "${PersianNumbers.toPersianDigits(daysBefore.toInt())} روز قبل از پریود",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = daysBefore,
                        onValueChange = { daysBefore = it },
                        onValueChangeFinished = { store.daysBeforePeriod = daysBefore.toInt(); onChanged() },
                        valueRange = 1f..5f, steps = 3
                    )
                }
                SwitchRow("یک توصیه کوتاه هر روز", dailyTip) {
                    dailyTip = it
                    store.dailyTip = it
                    onChanged()
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                minuteOfDay = h * 60 + m
                                store.reminderMinuteOfDay = minuteOfDay
                                onChanged()
                            },
                            minuteOfDay / 60, minuteOfDay % 60, true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ساعت یادآوری: " + PersianNumbers.formatTime(minuteOfDay / 60, minuteOfDay % 60))
                }
                Text(
                    "متن اعلان‌ها محتاطانه است و روی صفحه قفل فقط «یک یادآوری تازه داری» دیده می‌شود.",
                    fontSize = 11.5.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Panel {
            Label("دریافت به‌روزرسانی")
            Text(
                "وقتی همسرت در هانیتا «QR به‌روزرسانی» یا «ارسال با پیام‌رسان» را زد، اسکنش کن یا پیامش را کپی کن و «جای‌گذاری» را بزن. لینک پیام هم مستقیم همین برنامه را باز می‌کند.",
                fontSize = 12.5.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            UpdateButtons(onScan, onPaste)
        }
        Panel {
            Label("حریم خصوصی")
            Text(
                "$APP_NAME به اینترنت وصل نمی‌شود و حساب کاربری ندارد. فقط تاریخ‌های کلی چرخه و انتخاب‌های همسرت، رمزنگاری‌شده با AES-256، روی همین گوشی نگه داشته می‌شود.",
                fontSize = 12.5.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { confirmDisconnect = true }) {
                Text("قطع اتصال و پاک کردن اطلاعات", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDisconnect) {
        AlertDialog(
            onDismissRequest = { confirmDisconnect = false },
            title = { Text("قطع اتصال؟", fontWeight = FontWeight.Bold) },
            text = { Text("اتصال با هانیتا و همه اطلاعات این برنامه پاک می‌شود. برای اتصال دوباره باید QR تازه اسکن کنی.") },
            confirmButton = {
                TextButton(onClick = { confirmDisconnect = false; onDisconnect() }) {
                    Text("قطع اتصال", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDisconnect = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
