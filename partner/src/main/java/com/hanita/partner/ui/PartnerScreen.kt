package com.hanita.partner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.partner.PartnerStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** For married users: the link with Hanita, her day, phase-aware advice and ready messages. */
@Composable
internal fun PartnerScreen(padding: PaddingValues, store: PartnerStore, version: Int, actions: AppActions) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val card = store.card
    if (store.pairing == null || card == null) {
        ScreenList(padding) {
            item { Wordmark("همسر") }
            item {
                Panel {
                    Label("اتصال به Hanita")
                    Step("۱", "همسرت در اپ Hanita تب «همراه» را باز کند و دکمه QR را بزند.")
                    Step("۲", "اینجا «اسکن QR» را بزن و کد روی گوشی او را اسکن کن.")
                    Step("۳", "هر روز ببین در کدام دوره است، چطور همراهش باشی و چه پیامی بفرستی.")
                    Button(
                        onClick = actions.scan,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("اسکن QR اتصال", fontWeight = FontWeight.Bold)
                    }
                    MutedText(
                        "بدون اینترنت و بدون سرور. فقط تاریخ‌های کلی چرخه، رمزنگاری‌شده و با اجازه خود او می‌رسد. " +
                            "علائم و یادداشت‌های شخصی او هرگز اینجا نمی‌آید و او هر وقت بخواهد می‌تواند اتصال را قطع کند."
                    )
                }
            }
            item { CycleBasics() }
        }
        return
    }

    val name = card.ownerName
    val day = PartnerDay.today(card, today)
    val info = PartnerContent.phaseInfo(day.phase, card.shareFertile)
    val guide = GentlemanGuide.phaseGuide(day.phase)

    ScreenList(padding) {
        item { Wordmark(if (name.isBlank()) "همسرم" else name) }

        item(key = "hero-$version") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(phaseIcon(day.phase))
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
                if (card.hasCycleData && day.cycleDay > 0) CycleBar(card, day.cycleDay)
            }
        }

        item(key = "guide-${day.phase}") {
            Panel {
                Label("رابطه در این روزها · ${guide.focus}")
                Text("انجام بده", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                guide.doThis.forEach { Bullet(it) }
                Spacer(Modifier.height(2.dp))
                Text("پرهیز کن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                guide.avoid.forEach { Bullet(it, MaterialTheme.colorScheme.error) }
                Column(
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Label("اگر اختلاف پیش آمد", MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(guide.conflict, fontSize = 14.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        if (card.note.isNotBlank() && card.hasCycleData) {
            item {
                Panel {
                    Label("یادداشت ${name.ifBlank { "همسرت" }}")
                    Text(card.note, fontSize = 15.sp, lineHeight = 24.sp)
                }
            }
        }

        item(key = "tasks-$today-${day.phase}") { TodayTasks(info.tips.take(3)) }

        item(key = "messages-$version-${day.phase}") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("پیام‌های پیشنهادی") }
                    TextButton(onClick = actions.addMessage) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("پیام من")
                    }
                }
                MutedText("با دکمه ارسال، پیام در پیامک یا پیام‌رسان باز می‌شود و پیش از فرستادن می‌توانی ویرایشش کنی.")
                info.messages.forEach { MessageRow(PartnerContent.personalize(it, name), { m -> share(context, m) }, null) }
                store.customMessages.forEach { own ->
                    MessageRow(own, { m -> share(context, m) }) { m ->
                        store.customMessages = store.customMessages - m
                        actions.onChanged()
                    }
                }
            }
        }

        item(key = "upcoming-$version") { Upcoming(card, today, store.daysBeforePeriod) }

        item(key = "update-$version") {
            Panel {
                Label("به‌روزرسانی از Hanita")
                MutedText(
                    if (store.receivedAt > 0) {
                        val date = Instant.ofEpochMilli(store.receivedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                        "آخرین دریافت: ${PersianDates.long(date)}"
                    } else "هنوز به‌روزرسانی‌ای نرسیده است."
                )
                UpdateButtons(actions.scan, actions.paste)
            }
        }
    }
}

/** General guidance that helps even before pairing. */
@Composable
private fun CycleBasics() {
    val pillar = GentlemanGuide.pillar("cycle") ?: return
    Panel {
        Label(pillar.title)
        pillar.principles.take(4).forEach { p ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(p.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(p.text, fontSize = 13.5.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Three things to do today, ticked off for the day (not stored). */
@Composable
private fun TodayTasks(tasks: List<String>) {
    val done = remember { mutableStateOf(setOf<Int>()) }
    Panel {
        Label("۳ کار امروز برای او")
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
private fun Upcoming(card: PartnerCard, today: LocalDate, daysBefore: Int) {
    val events = buildSet {
        add(PartnerDay.Event.PERIOD_START)
        add(PartnerDay.Event.PMS_START)
        if (card.shareFertile) add(PartnerDay.Event.FERTILE_START)
    }
    val upcoming = PartnerDay.upcoming(card, today, events, daysBefore)
    Panel {
        Label("روزهای پیش رو")
        if (upcoming.isEmpty()) {
            MutedText("فعلاً پیش‌بینی‌ای نیست. با به‌روزرسانی تازه از Hanita اینجا پر می‌شود.")
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
        Text("بر اساس میانگین چرخه؛ ممکن است چند روز جابه‌جا شود.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
