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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.partner.PartnerStore
import com.hanita.partner.Reminders
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A message that needs the partner's attention after a scan or a received link. */
private class Notice(val title: String, val text: String)

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

    var notice by remember { mutableStateOf<Notice?>(null) }
    var showSettings by remember { mutableStateOf(false) }
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
                notice = Notice(
                    "اتصال برقرار شد ✅",
                    "کد تأیید: ${PersianNumbers.toPersianDigits(result.code)}\n" +
                        "این کد باید با کدی که روی گوشی ${result.ownerName.ifBlank { "همسرت" }} دیده می‌شود یکی باشد. اگر فرق دارد، دوباره اسکن کن."
                )
                askForNotifications()
            }
            is PartnerStore.Result.Updated -> toast(context, "اطلاعات به‌روز شد 💙")
            PartnerStore.Result.Disconnected -> notice = Notice(
                "اتصال قطع شد",
                "همسرت اتصال را قطع کرد و همه اطلاعات از این گوشی پاک شد. هر وقت دوباره خواست، با یک QR تازه وصل شوید."
            )
            PartnerStore.Result.Older -> toast(context, "این به‌روزرسانی از اطلاعات فعلی قدیمی‌تر است.")
            PartnerStore.Result.PairingNeedsScan -> notice = Notice(
                "کد اتصال را اسکن کن",
                "کد اتصال فقط با «اسکن QR» از روی گوشی همسرت پذیرفته می‌شود، چون کلید رمز داخل آن است و نباید از راه پیام‌رسان جابه‌جا شود."
            )
            PartnerStore.Result.WrongPair -> notice = Notice(
                "مال این اتصال نیست",
                "این به‌روزرسانی برای اتصال فعلی نیست. اگر همسرت در هانیتا «اتصال دوباره» را زده، QR جدید را اسکن کن."
            )
            PartnerStore.Result.NotPaired -> notice = Notice(
                "هنوز وصل نیستی",
                "اول در هانیتا (تب «همراه»، دکمه QR) کد اتصال را بساز و با همین برنامه اسکنش کن."
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Header(onSettings = { showSettings = true }) }
            if (!paired) {
                item { WelcomeCard(onScan = ::scan) }
            } else {
                val day = PartnerDay.today(card, today)
                val info = PartnerContent.phaseInfo(day.phase, card?.shareFertile == true && day.fertile)
                val name = card?.ownerName.orEmpty()
                item { TodayCard(card, day, info, receivedAt) }
                item { TipsCard(info) }
                item {
                    MessagesCard(
                        suggested = info.messages.map { PartnerContent.personalize(it, name) },
                        own = remember(version) { store.customMessages },
                        onSend = { share(context, it) },
                        onAdd = { showAddMessage = true },
                        onDelete = { message ->
                            store.customMessages = store.customMessages - message
                            version++
                        }
                    )
                }
                item { UpcomingCard(card, today, store.daysBeforePeriod) }
                item { UpdateCard(onScan = ::scan, onPaste = ::paste) }
            }
        }
    }

    notice?.let { n ->
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(n.title) },
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

    if (showSettings) {
        SettingsDialog(
            store = store,
            fertileShared = card?.shareFertile == true,
            paired = paired,
            onEnableReminders = ::askForNotifications,
            onDismiss = {
                showSettings = false
                version++
                Reminders.reschedule(context)
            },
            onDisconnect = {
                store.clearAll()
                showSettings = false
                version++
                Reminders.reschedule(context)
                toast(context, "اتصال قطع شد و اطلاعات پاک شد.")
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

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Header(onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("همراه هانیتا 💙", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("کنار همسرت، در همه روزهای ماه", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WelcomeCard(onScan: () -> Unit) {
    SectionCard {
        Text("👋 خوش آمدی", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(
            "این برنامه به تو کمک می‌کند روزهای مختلف ماه همسرت را بشناسی، به‌موقع یادآوری بگیری و با پیام‌ها و کارهای کوچک همراهش باشی.",
            fontSize = 14.sp, lineHeight = 23.sp
        )
        Text(
            "برای شروع:\n۱. همسرت در هانیتا تب «همراه» را باز کند و دکمه QR را بزند.\n۲. اینجا «اسکن QR» را بزن و کد روی گوشی او را اسکن کن.",
            fontSize = 13.5.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("اسکن QR اتصال", fontWeight = FontWeight.Bold)
        }
        Text(
            "🔒 این برنامه به اینترنت وصل نمی‌شود. فقط تاریخ‌های کلی چرخه به‌صورت رمزنگاری‌شده از هانیتا می‌رسد؛ علائم و یادداشت‌های همسرت هرگز اینجا نمی‌آید.",
            fontSize = 12.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodayCard(card: PartnerCard?, day: PartnerDay.Today, info: PartnerContent.PhaseInfo, receivedAt: Long) {
    val name = card?.ownerName?.ifBlank { null } ?: "همسرت"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("امروز برای $name", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(info.emoji, fontSize = 26.sp) }
                Spacer(Modifier.width(12.dp))
                Text(info.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(info.summary, fontSize = 14.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            val details = buildList {
                if (day.cycleDay > 0) add("روز ${PersianNumbers.toPersianDigits(day.cycleDay)} چرخه")
                day.daysUntilPeriod?.let { if (it > 0) add("${PersianNumbers.toPersianDigits(it)} روز تا روزهای پریود") }
            }
            if (details.isNotEmpty()) {
                Text(details.joinToString(" • "), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (day.fertile) {
                Text("🌸 از روزهای باروری", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
            val note = card?.note.orEmpty()
            if (note.isNotBlank() && card?.hasCycleData == true) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("💌 یادداشت $name: $note", fontSize = 13.5.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            if (receivedAt > 0) {
                val date = Instant.ofEpochMilli(receivedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                Text(
                    "آخرین به‌روزرسانی: ${PersianDates.long(date)}",
                    fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TipsCard(info: PartnerContent.PhaseInfo) {
    SectionCard {
        SectionTitle("چطور همراهش باشم؟")
        info.tips.forEach { tip ->
            Row {
                Text("•  ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(tip, fontSize = 14.sp, lineHeight = 23.sp)
            }
        }
    }
}

@Composable
private fun MessagesCard(
    suggested: List<String>,
    own: List<String>,
    onSend: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    SectionCard {
        SectionTitle("پیام‌های پیشنهادی امروز")
        Text(
            "روی دکمه ارسال بزن تا پیام در پیامک یا پیام‌رسان باز شود؛ قبل از ارسال می‌توانی ویرایشش کنی.",
            fontSize = 12.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        suggested.forEach { MessageRow(it, onSend, null) }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("پیام‌های من", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("پیام جدید")
            }
        }
        if (own.isEmpty()) {
            Text(
                "پیام‌هایی که زیاد می‌فرستی را اینجا ذخیره کن تا همیشه دم دستت باشد.",
                fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            own.forEach { MessageRow(it, onSend, onDelete) }
        }
    }
}

@Composable
private fun MessageRow(text: String, onSend: (String) -> Unit, onDelete: ((String) -> Unit)?) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f).padding(vertical = 6.dp))
        if (onDelete != null) {
            IconButton(onClick = { onDelete(text) }) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف پیام", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = { onSend(text) }) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال پیام", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun UpcomingCard(card: PartnerCard?, today: LocalDate, daysBefore: Int) {
    val events = buildSet {
        add(PartnerDay.Event.PERIOD_START)
        add(PartnerDay.Event.PMS_START)
        if (card?.shareFertile == true) add(PartnerDay.Event.FERTILE_START)
    }
    val upcoming = PartnerDay.upcoming(card, today, events, daysBefore)
    SectionCard {
        SectionTitle("روزهای پیش رو")
        if (upcoming.isEmpty()) {
            Text(
                "فعلاً پیش‌بینی‌ای نیست. وقتی به‌روزرسانی تازه از هانیتا برسد، اینجا نمایش داده می‌شود.",
                fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        upcoming.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(PartnerContent.eventEmoji(item.event), fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(PartnerContent.eventTitle(item.event), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(
                    PersianDates.short(item.date) + if (item.daysFromToday == 0) " (امروز)"
                    else " (${PersianNumbers.toPersianDigits(item.daysFromToday)} روز دیگر)",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            "پیش‌بینی بر اساس میانگین چرخه است و ممکن است چند روز جابه‌جا شود.",
            fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdateCard(onScan: () -> Unit, onPaste: () -> Unit) {
    SectionCard {
        SectionTitle("دریافت به‌روزرسانی")
        Text(
            "هر وقت همسرت در هانیتا «QR به‌روزرسانی» یا «ارسال با پیام‌رسان» را زد، اینجا اسکنش کن یا پیامش را کپی کن و «جای‌گذاری» را بزن. لینک پیام هم مستقیم همین برنامه را باز می‌کند.",
            fontSize = 12.5.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onScan, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("اسکن QR")
            }
            OutlinedButton(onClick = onPaste, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("جای‌گذاری")
            }
        }
    }
}

@Composable
private fun AddMessageDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیام جدید") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(PartnerStore.MAX_MESSAGE_LENGTH) },
                placeholder = { Text("مثلاً: امشب شام با من 🍲") },
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
private fun SettingsDialog(
    store: PartnerStore,
    fertileShared: Boolean,
    paired: Boolean,
    onEnableReminders: () -> Unit,
    onDismiss: () -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(store.remindersEnabled) }
    var events by remember { mutableStateOf(store.reminderEvents) }
    var daysBefore by remember { mutableFloatStateOf(store.daysBeforePeriod.toFloat()) }
    var dailyTip by remember { mutableStateOf(store.dailyTip) }
    var minuteOfDay by remember { mutableIntStateOf(store.reminderMinuteOfDay) }
    var confirmDisconnect by remember { mutableStateOf(false) }

    fun save() {
        store.remindersEnabled = enabled
        store.reminderEvents = events
        store.daysBeforePeriod = daysBefore.toInt()
        store.dailyTip = dailyTip
        store.reminderMinuteOfDay = minuteOfDay
    }

    AlertDialog(
        onDismissRequest = { save(); onDismiss() },
        title = { Text("تنظیمات یادآوری") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).widthIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SwitchRow("یادآوری‌ها روشن باشد", enabled) {
                    enabled = it
                    if (it) onEnableReminders()
                }
                if (enabled) {
                    val choices = buildList {
                        add(PartnerDay.Event.PERIOD_SOON)
                        add(PartnerDay.Event.PERIOD_START)
                        add(PartnerDay.Event.PMS_START)
                        if (fertileShared) add(PartnerDay.Event.FERTILE_START)
                    }
                    choices.forEach { event ->
                        Row(
                            Modifier.fillMaxWidth().toggleable(
                                value = event in events,
                                role = Role.Checkbox,
                                onValueChange = { on -> events = if (on) events + event else events - event }
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = event in events, onCheckedChange = null)
                            Spacer(Modifier.width(6.dp))
                            Text("${PartnerContent.eventEmoji(event)} ${PartnerContent.eventTitle(event)}", fontSize = 14.sp)
                        }
                    }
                    if (PartnerDay.Event.PERIOD_SOON in events) {
                        Text(
                            "یادآوری ${PersianNumbers.toPersianDigits(daysBefore.toInt())} روز قبل از پریود",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        Slider(value = daysBefore, onValueChange = { daysBefore = it }, valueRange = 1f..5f, steps = 3)
                    }
                    SwitchRow("یک توصیه کوتاه هر روز", dailyTip) { dailyTip = it }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> minuteOfDay = h * 60 + m },
                                minuteOfDay / 60, minuteOfDay % 60, true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ساعت یادآوری: " + PersianNumbers.formatTime(minuteOfDay / 60, minuteOfDay % 60))
                    }
                    Text(
                        "متن اعلان‌ها محتاطانه است و روی صفحه قفل فقط «یک یادآوری تازه داری» دیده می‌شود.",
                        fontSize = 11.5.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (paired) {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    TextButton(onClick = { confirmDisconnect = true }) {
                        Text("قطع اتصال و پاک کردن اطلاعات", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { save(); onDismiss() }) { Text("ذخیره") } }
    )

    if (confirmDisconnect) {
        AlertDialog(
            onDismissRequest = { confirmDisconnect = false },
            title = { Text("قطع اتصال؟") },
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
