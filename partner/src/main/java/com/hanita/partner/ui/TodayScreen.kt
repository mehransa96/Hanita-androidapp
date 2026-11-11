package com.hanita.partner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.ImportantDate
import com.hanita.core.gentleman.Progress
import com.hanita.core.gentleman.Status
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.partner.PartnerStore
import java.time.LocalDate

@Composable
internal fun TodayScreen(padding: PaddingValues, store: PartnerStore, version: Int, status: Status, actions: AppActions) {
    val today = LocalDate.now()
    val name = store.userName
    val done = store.doneDays
    val doneToday = today.toEpochDay() in done
    val (pillar, principle) = GentlemanGuide.principleOfDay(status, today)
    val (challengePillar, challenge) = GentlemanGuide.challengeOfDay(status, today)
    val upcoming = ImportantDate.upcoming(store.dates, today).filter { it.daysUntil(today) <= 45 }.take(3)

    ScreenList(padding) {
        item(key = "head-$version") {
            Column {
                Wordmark(if (name.isBlank()) "سلام" else "سلام $name")
                Text(PersianDates.long(today), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item(key = "stats-$version") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(PersianNumbers.toPersianDigits(Progress.streak(done, today)), "روز پیاپی", Modifier.weight(1f))
                Stat(PersianNumbers.toPersianDigits(done.size), "چالش انجام‌شده", Modifier.weight(1f))
                Stat(
                    if (status == Status.MARRIED) "متاهل" else "مجرد", "مسیر تو", Modifier.weight(1f)
                )
            }
        }
        item(key = "principle-$today") {
            Panel {
                Label("اصل امروز · ${pillar.title}")
                Text(principle.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(principle.text, fontSize = 15.sp, lineHeight = 25.sp)
                TextButton(onClick = { actions.openPillar(pillar.id) }) { Text("بیشتر درباره ${pillar.title}") }
            }
        }
        item(key = "challenge-$today-$version") {
            Panel {
                Label("چالش امروز · ${challengePillar.title}")
                Text(challenge, fontSize = 15.sp, lineHeight = 25.sp)
                if (doneToday) {
                    OutlinedButton(
                        onClick = {
                            store.doneDays = done - today.toEpochDay()
                            actions.onChanged()
                        },
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
                            store.doneDays = done + today.toEpochDay()
                            actions.onChanged()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("انجامش دادم", fontWeight = FontWeight.Bold) }
                }
            }
        }
        if (status == Status.MARRIED) {
            item(key = "partner-$version") { PartnerSummary(store, today, actions) }
        }
        item(key = "dates-$version") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("تاریخ‌های پیش رو") }
                    TextButton(onClick = { actions.openTab(Tab.DATES) }) { Text(if (upcoming.isEmpty()) "افزودن" else "همه") }
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
