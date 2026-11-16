package com.hanita.partner.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.hanita.core.gentleman.ThemePresets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.Status
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.partner.PartnerStore

/** Settings are saved as soon as they change. */
@Composable
internal fun SettingsScreen(
    padding: PaddingValues,
    store: PartnerStore,
    version: Int,
    status: Status,
    actions: AppActions,
    onUnlink: () -> Unit,
    onResetAll: () -> Unit
) {
    val context = LocalContext.current
    val themes = LocalThemeController.current
    var name by remember { mutableStateOf(store.userName) }
    var enabled by remember { mutableStateOf(store.remindersEnabled) }
    var dailyTip by remember { mutableStateOf(store.dailyTip) }
    var dateReminders by remember { mutableStateOf(store.dateReminders) }
    var events by remember { mutableStateOf(store.reminderEvents) }
    var daysBefore by remember { mutableFloatStateOf(store.daysBeforePeriod.toFloat()) }
    var minuteOfDay by remember { mutableIntStateOf(store.reminderMinuteOfDay) }
    var confirmUnlink by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val paired = store.pairing != null
    val card = store.card

    ScreenList(padding) {
        item { Wordmark("تنظیمات", onBack = actions.back) }

        item {
            Panel {
                Label("پروفایل")
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(30)
                        store.userName = name
                    },
                    label = { Text("اسمت") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == Status.SINGLE,
                        onClick = { store.status = Status.SINGLE; actions.onChanged() },
                        label = { Text("مجرد") }
                    )
                    FilterChip(
                        selected = status == Status.MARRIED,
                        onClick = { store.status = Status.MARRIED; actions.onChanged() },
                        label = { Text("متاهل") }
                    )
                }
                MutedText("مسیر، چالش‌ها و بخش «همسر» بر اساس وضعیتت تنظیم می‌شوند.")
            }
        }

        if (themes != null) {
            item(key = "appearance") { AppearancePanel(themes) }
        }

        item(key = "reminders-$version") {
            Panel {
                Label("یادآوری")
                SwitchRow("یادآوری‌ها روشن باشد", enabled) {
                    enabled = it
                    store.remindersEnabled = it
                    if (it) actions.askForNotifications()
                    actions.onChanged()
                }
                if (enabled) {
                    SwitchRow("تاریخ‌های مهم", dateReminders, "روز موعد و چند روز قبلش") {
                        dateReminders = it
                        store.dateReminders = it
                        actions.onChanged()
                    }
                    SwitchRow("اصل روزانه", dailyTip, "وقتی یادآوری دیگری نباشد") {
                        dailyTip = it
                        store.dailyTip = it
                        actions.onChanged()
                    }
                    if (status == Status.MARRIED && paired) {
                        Text("روزهای خاص همسر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        val choices = buildList {
                            add(PartnerDay.Event.PERIOD_SOON)
                            add(PartnerDay.Event.PERIOD_START)
                            add(PartnerDay.Event.PMS_START)
                            if (card?.shareFertile == true) add(PartnerDay.Event.FERTILE_START)
                        }
                        choices.forEach { event ->
                            val on = event in events
                            Row(
                                Modifier.fillMaxWidth().toggleable(
                                    value = on, role = Role.Checkbox,
                                    onValueChange = { v ->
                                        events = if (v) events + event else events - event
                                        store.reminderEvents = events
                                        actions.onChanged()
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
                                onValueChangeFinished = { store.daysBeforePeriod = daysBefore.toInt(); actions.onChanged() },
                                valueRange = 1f..5f, steps = 3
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    minuteOfDay = h * 60 + m
                                    store.reminderMinuteOfDay = minuteOfDay
                                    actions.onChanged()
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
                    MutedText("روی صفحه قفل فقط «یک یادآوری تازه داری» دیده می‌شود.")
                }
            }
        }

        if (status == Status.MARRIED) {
            item(key = "hanita-$version") {
                Panel {
                    Label("اتصال به Hanita")
                    if (paired) {
                        MutedText(
                            "وصل هستی${card?.ownerName?.takeIf { it.isNotBlank() }?.let { " به $it" }.orEmpty()}. " +
                                "وقتی همسرت در Hanita «QR به‌روزرسانی» یا «ارسال با پیام‌رسان» را زد، اسکنش کن یا پیامش را کپی کن و «جای‌گذاری» را بزن."
                        )
                        UpdateButtons(actions.scan, actions.paste)
                        TextButton(onClick = { confirmUnlink = true }) {
                            Text("قطع اتصال و پاک کردن اطلاعات همسر", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        MutedText("هنوز وصل نیستی. همسرت در Hanita تب «همراه» و دکمه QR را بزند و تو اینجا اسکن کن.")
                        UpdateButtons(actions.scan, actions.paste)
                    }
                }
            }
        }

        item {
            Panel {
                Label("حریم خصوصی")
                MutedText(
                    "$APP_NAME به اینترنت وصل نمی‌شود و حساب کاربری ندارد. پروفایل، تاریخ‌ها، عادت‌ها، دفترچه، نتیجه آزمون‌ها و هدف‌هایت فقط در پایگاه داده خصوصی همین گوشی است و در پشتیبان‌گیری ابری اندروید هم نمی‌رود. " +
                        "اطلاعات همسر فقط با اجازه او و رمزنگاری‌شده با AES-256 از Hanita می‌رسد."
                )
                TextButton(onClick = { confirmReset = true }) {
                    Text("پاک کردن همه اطلاعات برنامه", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmUnlink) {
        AlertDialog(
            onDismissRequest = { confirmUnlink = false },
            title = { Text("قطع اتصال با Hanita؟", fontWeight = FontWeight.Bold) },
            text = { Text("اطلاعات چرخه همسرت از این گوشی پاک می‌شود. تاریخ‌ها و تنظیمات خودت می‌مانند. برای اتصال دوباره باید QR تازه اسکن کنی.") },
            confirmButton = {
                TextButton(onClick = { confirmUnlink = false; onUnlink() }) {
                    Text("قطع اتصال", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmUnlink = false }) { Text("انصراف") } }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("همه چیز پاک شود؟", fontWeight = FontWeight.Bold) },
            text = { Text("پروفایل، تاریخ‌ها، عادت‌ها، دفترچه، آزمون‌ها، هدف‌ها، پیام‌ها و اتصال با Hanita پاک می‌شوند. این کار برگشت‌پذیر نیست.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetAll() }) {
                    Text("پاک کن", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("انصراف") } }
        )
    }
}

/** Ready-made themes as swatches, plus automatic / light / dark. Applied at once. */
@Composable
private fun AppearancePanel(themes: ThemeController) {
    val dark = themes.isDark()
    Panel {
        Label("ظاهر و تم")
        MutedText("یکی از تم‌های آماده را انتخاب کن؛ همه برای خوانایی در نور کم و زیاد تنظیم شده‌اند.")
        ThemePresets.ALL.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { preset ->
                    ThemeSwatch(preset, if (dark) preset.dark else preset.light, preset.id == themes.id, Modifier.weight(1f)) {
                        themes.choose(preset.id)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text("حالت نمایش", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemePresets.Mode.values().forEach { mode ->
                FilterChip(
                    selected = themes.mode == mode,
                    onClick = { themes.choose(mode) },
                    label = { Text(mode.title) },
                    leadingIcon = {
                        Icon(
                            when (mode) {
                                ThemePresets.Mode.SYSTEM -> Icons.Filled.BrightnessAuto
                                ThemePresets.Mode.LIGHT -> Icons.Filled.LightMode
                                ThemePresets.Mode.DARK -> Icons.Filled.DarkMode
                            },
                            contentDescription = null, modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSwatch(
    preset: ThemePresets.Preset,
    palette: ThemePresets.Palette,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier
            .clip(shape)
            .background(Color(palette.background))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(palette.primary, palette.secondary, palette.surfaceVariant).forEach { c ->
                Box(Modifier.size(16.dp).background(Color(c), CircleShape).border(1.dp, Color(palette.outline), CircleShape))
            }
            Spacer(Modifier.weight(1f))
            if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = "انتخاب‌شده", tint = Color(palette.primary), modifier = Modifier.size(18.dp))
        }
        Text(preset.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(palette.onSurface), maxLines = 1)
        Text(preset.subtitle, fontSize = 11.sp, lineHeight = 16.sp, color = Color(palette.onSurfaceVariant), maxLines = 2)
    }
}
