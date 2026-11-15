package com.hanita.partner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.ImportantDate
import com.hanita.core.gentleman.Progress
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Status
import com.hanita.core.gentleman.Toolkit
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.partner.PartnerStore
import com.hanita.partner.data.GrowthRepository
import com.hanita.partner.data.HabitEntity
import com.hanita.partner.data.goods
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
internal fun TodayScreen(
    padding: PaddingValues,
    store: PartnerStore,
    repo: GrowthRepository,
    version: Int,
    status: Status,
    today: LocalDate,
    actions: AppActions
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val name = store.userName
    val challengeDays by repo.challengeDays.collectAsState(initial = emptySet())
    val habits by repo.habits.collectAsState(initial = emptyList())
    val habitDaysFlow = remember(today) { repo.habitDays(today) }
    val habitDays by habitDaysFlow.collectAsState(initial = emptyMap())
    val journal by repo.journal.collectAsState(initial = emptyList())
    val todayEntry = journal.firstOrNull { it.epochDay == today.toEpochDay() }
    val doneToday = today.toEpochDay() in challengeDays
    val (pillar, principle) = GentlemanGuide.principleOfDay(status, today)
    val (challengePillar, challenge) = GentlemanGuide.challengeOfDay(status, today)
    val upcoming = ImportantDate.upcoming(store.dates, today).filter { it.daysUntil(today) <= 45 }.take(3)
    val habitsDone = habits.count { today.toEpochDay() in habitDays[it.id].orEmpty() }

    ScreenList(padding) {
        item(key = "head") {
            Wordmark(if (name.isBlank()) "سلام" else "سلام $name") {
                IconButton(onClick = { actions.open(Routes.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item(key = "date") {
            Text(PersianDates.long(today), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item(key = "stats") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(PersianNumbers.toPersianDigits(Progress.streak(challengeDays, today)), "روز پیاپی چالش", Modifier.weight(1f))
                Stat(
                    "${PersianNumbers.toPersianDigits(habitsDone)}/${PersianNumbers.toPersianDigits(habits.size)}",
                    "عادت امروز", Modifier.weight(1f)
                )
                Stat(
                    todayEntry?.mood?.takeIf { it > 0 }?.let { Toolkit.moodLabel(it) } ?: "—",
                    "حال امروز", Modifier.weight(1f)
                )
            }
        }
        item(key = "mood") {
            MoodPanel(todayEntry?.mood ?: 0) { mood ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch { repo.setMood(today, mood, todayEntry) }
            }
        }
        item(key = "principle-$today") {
            Panel {
                Label("اصل امروز · ${pillar.title}")
                Text(principle.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(principle.text, fontSize = 15.sp, lineHeight = 25.sp)
                Sources.forPillar(pillar.id).firstOrNull()?.let {
                    Text("بر پایه: ${it.author}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.secondary)
                }
                TextButton(onClick = { actions.openPillar(pillar.id) }) { Text("بیشتر درباره ${pillar.title}") }
            }
        }
        item(key = "challenge-$today") {
            Panel {
                Label("چالش امروز · ${challengePillar.title}")
                Text(challenge, fontSize = 15.sp, lineHeight = 25.sp)
                if (doneToday) {
                    OutlinedButton(
                        onClick = { scope.launch { repo.setChallengeDone(today, false, challengePillar.id) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("انجام شد")
                    }
                } else {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { repo.setChallengeDone(today, true, challengePillar.id) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("انجامش دادم", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item(key = "habits") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("عادت‌های امروز") }
                    TextButton(onClick = { actions.openTab(Tab.GROWTH) }) { Text(if (habits.isEmpty()) "افزودن" else "مدیریت") }
                }
                if (habits.isEmpty()) MutedText("چند عادت کوچک انتخاب کن؛ پیشرفت واقعی از تکرار روزانه می‌آید.")
                habits.forEach { habit ->
                    HabitCheckRow(habit, today.toEpochDay() in habitDays[habit.id].orEmpty()) { done ->
                        if (done) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { repo.setHabitDone(habit.id, today, done) }
                    }
                }
            }
        }
        item(key = "quick") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(Icons.Filled.Edit, if (todayEntry?.goods.isNullOrEmpty()) "سه اتفاق خوب" else "دفترچه امروز", Modifier.weight(1f)) {
                    actions.open(Routes.journal(today.toEpochDay()))
                }
                QuickAction(Icons.Filled.Air, "تنفس آرام", Modifier.weight(1f)) { actions.open(Routes.breath("sigh")) }
            }
        }
        if (status == Status.MARRIED) {
            item(key = "partner-$version") { PartnerSummary(store, today, actions) }
        }
        item(key = "dates-$version") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("تاریخ‌های پیش رو") }
                    TextButton(onClick = { actions.open(Routes.DATES) }) { Text(if (upcoming.isEmpty()) "افزودن" else "همه") }
                }
                if (upcoming.isEmpty()) {
                    MutedText(
                        if (status == Status.MARRIED) "تولد همسر، سالگرد ازدواج و مناسبت‌ها را اضافه کن تا هیچ‌وقت فراموش نشوند."
                        else "تولد خانواده و دوستان و مناسبت‌های مهم را اضافه کن تا به‌موقع یادت بیاید."
                    )
                }
                upcoming.forEachIndexed { i, date ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DateRow(date, today)
                }
            }
        }
    }
}

@Composable
private fun MoodPanel(selected: Int, onSelect: (Int) -> Unit) {
    Panel {
        Label("امروز حالت چطور است؟")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Toolkit.MOODS.forEachIndexed { i, label ->
                val value = i + 1
                val on = value == selected
                Column(
                    Modifier.weight(1f).selectable(selected = on, role = Role.RadioButton) { onSelect(value) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MoodDot(value, on)
                    Spacer(Modifier.size(4.dp))
                    Text(
                        label, fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.Center,
                        color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/** A filled circle whose fill grows with the mood (1..5); no emoji, in keeping with the look. */
@Composable
private fun MoodDot(value: Int, selected: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    androidx.compose.foundation.Canvas(Modifier.size(34.dp)) {
        drawCircle(if (selected) accent.copy(alpha = 0.18f) else track)
        drawCircle(accent.copy(alpha = if (selected) 1f else 0.45f), radius = size.minDimension / 2 * (0.2f + 0.14f * value))
    }
}

@Composable
internal fun HabitCheckRow(habit: HabitEntity, done: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(value = done, role = Role.Checkbox, onValueChange = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(10.dp))
        Text(
            habit.title, fontSize = 14.5.sp,
            color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    Panel(modifier.clickable(role = Role.Button, onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun PartnerSummary(store: PartnerStore, today: LocalDate, actions: AppActions) {
    val card = store.card
    Panel {
        if (store.pairing == null || card == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Filled.LinkOff)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("اتصال به Hanita همسرت", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    MutedText("روزهای خاص او را بدان و برای هر دوره توصیه بگیر.")
                }
            }
            Button(onClick = { actions.openTab(Tab.PARTNER) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Text("اتصال")
            }
        } else {
            val day = PartnerDay.today(card, today)
            val info = PartnerContent.phaseInfo(day.phase, card.shareFertile)
            val guide = GentlemanGuide.phaseGuide(day.phase)
            Label("${card.ownerName.ifBlank { "همسرت" }} امروز")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(phaseIcon(day.phase))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plain(info.title), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("تمرکز: ${guide.focus}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            guide.doThis.firstOrNull()?.let { Bullet(it) }
            TextButton(onClick = { actions.openTab(Tab.PARTNER) }) { Text("راهنمای کامل امروز") }
        }
    }
}

@Composable
internal fun DateRow(date: ImportantDate, today: LocalDate, trailing: @Composable () -> Unit = {}) {
    val left = date.daysUntil(today)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(date.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            val years = date.yearsOnNext(today)?.let { " · ${PersianNumbers.toPersianDigits(it)} سال" }.orEmpty()
            Text(
                "${date.kind.label} · ${PersianDates.short(date.nextOccurrence(today))}$years",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            when (left) {
                0 -> "امروز"
                1 -> "فردا"
                else -> "${PersianNumbers.toPersianDigits(left)} روز"
            },
            fontSize = 13.sp, fontWeight = FontWeight.Bold,
            color = if (left <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        trailing()
    }
}
