package com.hanita.partner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.JalaliCalendar
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.ImportantDate
import com.hanita.core.gentleman.Status
import com.hanita.partner.PartnerStore
import java.time.LocalDate
import java.util.UUID

private val MONTHS = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")

/** A date being added or edited. */
private class Draft(
    val id: String?,
    val title: String,
    val kind: ImportantDate.Kind,
    val month: Int,
    val day: Int?,
    val year: Int?,
    val before: Int
)

@Composable
internal fun DatesScreen(padding: PaddingValues, store: PartnerStore, version: Int, status: Status, actions: AppActions) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dates = ImportantDate.upcoming(store.dates, today)
    var draft by remember { mutableStateOf<Draft?>(null) }
    var deleting by remember { mutableStateOf<ImportantDate?>(null) }
    val thisMonth = JalaliCalendar.gregorianToJalali(today).month

    fun save(list: List<ImportantDate>) {
        store.dates = list
        actions.onChanged()
    }

    val presets: List<Pair<String, ImportantDate.Kind>> = if (status == Status.MARRIED) listOf(
        "تولد همسرم" to ImportantDate.Kind.BIRTHDAY,
        "سالگرد ازدواج" to ImportantDate.Kind.ANNIVERSARY,
        "سالگرد آشنایی" to ImportantDate.Kind.ANNIVERSARY,
        "سالگرد عقد" to ImportantDate.Kind.ANNIVERSARY,
        "تولد مادر همسرم" to ImportantDate.Kind.BIRTHDAY,
        "تولد پدر همسرم" to ImportantDate.Kind.BIRTHDAY
    ) else listOf(
        "تولد مادرم" to ImportantDate.Kind.BIRTHDAY,
        "تولد پدرم" to ImportantDate.Kind.BIRTHDAY,
        "تولد دوستم" to ImportantDate.Kind.BIRTHDAY,
        "تولد خودم" to ImportantDate.Kind.BIRTHDAY
    )

    ScreenList(padding) {
        item {
            Wordmark("تاریخ‌های مهم", onBack = actions.back) {
                IconButton(onClick = { draft = Draft(null, "", ImportantDate.Kind.BIRTHDAY, thisMonth, null, null, 1) }) {
                    Icon(Icons.Filled.Add, contentDescription = "افزودن تاریخ", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            MutedText("جنتلمن تاریخ‌های مهم را فراموش نمی‌کند. هر تاریخ هر سال در همان روز شمسی تکرار می‌شود و روز موعد و چند روز قبلش یادآوری می‌گیری.")
        }
        item(key = "list-$version") {
            Panel {
                Label("به ترتیب نزدیکی")
                if (dates.isEmpty()) MutedText("هنوز تاریخی اضافه نکرده‌ای. از پیشنهادهای زیر شروع کن.")
                dates.forEachIndexed { i, date ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(
                        Modifier.fillMaxWidth().clickable(onClickLabel = "ویرایش") {
                            draft = Draft(date.id, date.title, date.kind, date.month, date.day, date.year, date.remindDaysBefore)
                        }
                    ) {
                        DateRow(date, today) {
                            IconButton(onClick = { deleting = date }) {
                                Icon(
                                    Icons.Filled.Delete, contentDescription = "حذف ${date.title}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item(key = "presets-$version") {
            Panel {
                Label("پیشنهادها")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.filter { (title, _) -> dates.none { it.title == title } }.forEach { (title, kind) ->
                        AssistChip(
                            onClick = { draft = Draft(null, title, kind, thisMonth, null, null, 3) },
                            label = { Text(title) }
                        )
                    }
                }
                Label("مناسبت‌ها")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImportantDate.SUGGESTED_OCCASIONS.filter { (title, _, _) -> dates.none { it.title == title } }
                        .forEach { (title, month, day) ->
                            AssistChip(
                                onClick = {
                                    save(
                                        store.dates + ImportantDate.create(
                                            UUID.randomUUID().toString(), title, ImportantDate.Kind.OCCASION, month, day, null, 3
                                        )
                                    )
                                    toast(context, "«$title» اضافه شد")
                                },
                                label = { Text("$title · ${PersianNumbers.toPersianDigits(day)} ${MONTHS[month - 1]}") }
                            )
                        }
                }
            }
        }
    }

    deleting?.let { date ->
        ConfirmDelete("«${date.title}» حذف شود؟", "یادآوری این تاریخ هم دیگر نمی‌آید.", onDismiss = { deleting = null }) {
            deleting = null
            save(store.dates.filter { it.id != date.id })
        }
    }

    draft?.let { d ->
        DateDialog(
            draft = d,
            onDismiss = { draft = null },
            onSave = { saved ->
                val others = store.dates.filter { it.id != saved.id }
                if (others.size >= ImportantDate.MAX_DATES) {
                    toast(context, "حداکثر ${ImportantDate.MAX_DATES} تاریخ می‌توانی داشته باشی.")
                } else {
                    save(others + saved)
                    draft = null
                }
            }
        )
    }
}

@Composable
private fun DateDialog(draft: Draft, onDismiss: () -> Unit, onSave: (ImportantDate) -> Unit) {
    var title by remember { mutableStateOf(draft.title) }
    var kind by remember { mutableStateOf(draft.kind) }
    var month by remember { mutableStateOf(draft.month) }
    var dayText by remember { mutableStateOf(draft.day?.toString().orEmpty()) }
    var yearText by remember { mutableStateOf(draft.year?.toString().orEmpty()) }
    var before by remember { mutableStateOf(draft.before) }
    var monthMenu by remember { mutableStateOf(false) }

    val maxDay = if (month <= 6) 31 else 30
    val day = PersianNumbers.parseInt(dayText, 1..maxDay)
    val yearOk = yearText.isBlank() || PersianNumbers.parseInt(yearText, 1300..1500) != null
    val valid = title.isNotBlank() && day != null && yearOk

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.id == null) "تاریخ جدید" else "ویرایش تاریخ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(ImportantDate.MAX_TITLE) },
                    label = { Text("عنوان") },
                    placeholder = { Text("مثلاً تولد همسرم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImportantDate.Kind.values().forEach { k ->
                        FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(k.label) })
                    }
                }
                Label("تاریخ شمسی")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it.take(2) },
                        label = { Text("روز") },
                        singleLine = true,
                        isError = dayText.isNotBlank() && day == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Box(Modifier.weight(1.4f)) {
                        OutlinedButton(onClick = { monthMenu = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                            Text(MONTHS[month - 1], modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }) {
                            MONTHS.forEachIndexed { i, name ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { month = i + 1; monthMenu = false })
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.take(4) },
                    label = { Text("سال (اختیاری، مثلاً ۱۳۹۸)") },
                    supportingText = { Text("برای نشان دادن چندمین سالگرد یا سن") },
                    singleLine = true,
                    isError = !yearOk,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Label("یادآوری")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImportantDate.REMIND_CHOICES.forEach { n ->
                        FilterChip(
                            selected = before == n,
                            onClick = { before = n },
                            label = { Text(if (n == 0) "فقط همان روز" else "${PersianNumbers.toPersianDigits(n)} روز قبل") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ImportantDate.create(
                            draft.id ?: UUID.randomUUID().toString(), title, kind, month, day ?: 1,
                            yearText.takeIf { it.isNotBlank() }?.let { PersianNumbers.parseInt(it, 1300..1500) }, before
                        )
                    )
                },
                enabled = valid
            ) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
