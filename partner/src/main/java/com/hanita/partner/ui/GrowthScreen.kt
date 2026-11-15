package com.hanita.partner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.Progress
import com.hanita.core.gentleman.Status
import com.hanita.core.gentleman.Toolkit
import com.hanita.partner.data.AssessmentEntity
import com.hanita.partner.data.GoalEntity
import com.hanita.partner.data.GrowthRepository
import com.hanita.partner.data.HabitEntity
import com.hanita.partner.data.JournalEntity
import com.hanita.partner.data.goods
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

/** Habits, journal, self-checks and goals: everything that shows the user's progress. */
@Composable
internal fun GrowthScreen(padding: PaddingValues, repo: GrowthRepository, status: Status, today: LocalDate, actions: AppActions) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habits by repo.habits.collectAsState(initial = emptyList())
    val habitDaysFlow = remember(today) { repo.habitDays(today) }
    val habitDays by habitDaysFlow.collectAsState(initial = emptyMap())
    val journal by repo.journal.collectAsState(initial = emptyList())
    val assessments by repo.assessments.collectAsState(initial = emptyList())
    val goals by repo.goals.collectAsState(initial = emptyList())
    var addHabit by remember { mutableStateOf(false) }
    var addGoal by remember { mutableStateOf(false) }
    var deleteHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var deleteGoal by remember { mutableStateOf<GoalEntity?>(null) }

    ScreenList(padding) {
        item { Wordmark("رشد من") }
        item {
            MutedText("عادت‌ها، دفترچه، آزمون‌ها و هدف‌هایت اینجاست. همه چیز فقط روی همین گوشی ذخیره می‌شود.")
        }

        // ---- Habits ----
        item(key = "habits") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("عادت‌ها") }
                    if (habits.size < Habits.MAX_HABITS) {
                        TextButton(onClick = { addHabit = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Text("افزودن")
                        }
                    }
                }
                if (habits.isEmpty()) MutedText("هنوز عادتی نداری. از پیشنهادها شروع کن؛ بهتر است با دو سه عادت کوچک شروع کنی.")
                habits.forEachIndexed { i, habit ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    HabitRow(
                        habit, habitDays[habit.id].orEmpty(), today,
                        onToggle = { done -> scope.launch { repo.setHabitDone(habit.id, today, done) } },
                        onDelete = { deleteHabit = habit }
                    )
                }
                if (habits.isNotEmpty()) {
                    MutedText("ساخته شدن یک عادت معمولاً چند هفته طول می‌کشد و یک روز جا انداختن مهم نیست؛ فقط دو روز پشت‌سرهم نشود.")
                }
            }
        }

        // ---- Journal ----
        item(key = "journal") {
            val todayEntry = journal.firstOrNull { it.epochDay == today.toEpochDay() }
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("دفترچه روزانه") }
                    Text(
                        "${PersianNumbers.toPersianDigits(journal.size)} روز نوشته‌شده",
                        fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MutedText("هر شب سه اتفاق خوب روزت و دلیلش را بنویس. این تمرین ساده در پژوهش‌ها شادی را بالا برده است.")
                Button(
                    onClick = { actions.open(Routes.journal(today.toEpochDay())) },
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (todayEntry?.goods.isNullOrEmpty()) "نوشتن دفترچه امروز" else "ویرایش دفترچه امروز")
                }
                journal.filter { it.epochDay != today.toEpochDay() }.take(5).forEach { entry ->
                    JournalRow(entry) { actions.open(Routes.journal(entry.epochDay)) }
                }
            }
        }

        // ---- Self-checks ----
        item(key = "assess") {
            Panel {
                Label("آزمون‌های معتبر خودشناسی")
                MutedText("هر یک تا دو ماه تکرارشان کن تا تغییرت را ببینی. همه آزمون‌ها ابزار علمی منتشرشده‌اند و نتیجه فقط برای خودشناسی است، نه تشخیص.")
                val kinds = Assessments.Kind.forStatus(status)
                Assessments.Group.values().forEach { group ->
                    val inGroup = kinds.filter { it.group == group }
                    if (inGroup.isNotEmpty()) {
                        Column(Modifier.padding(top = 6.dp)) {
                            Text(group.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(group.subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        inGroup.forEachIndexed { i, kind ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            AssessmentRow(kind, assessments.filter { it.kind == kind.code }) { actions.open(Routes.assess(kind.code)) }
                        }
                    }
                }
            }
        }

        // ---- Goals ----
        item(key = "goals") {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Label("هدف‌ها") }
                    TextButton(onClick = { addGoal = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("هدف تازه")
                    }
                }
                if (goals.isEmpty()) {
                    MutedText("یک هدف بنویس و برایش برنامه «اگر… آنگاه…» بگذار؛ مثلاً «اگر ساعت ۷ صبح شد، آنگاه ۲۰ دقیقه انگلیسی می‌خوانم».")
                }
                goals.forEachIndexed { i, goal ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GoalRow(
                        goal,
                        onToggle = { done -> scope.launch { repo.setGoalDone(goal, done) } },
                        onDelete = { deleteGoal = goal }
                    )
                }
            }
        }
    }

    if (addHabit) {
        AddHabitDialog(
            status = status,
            existing = habits.mapNotNull { it.presetId }.toSet(),
            onDismiss = { addHabit = false },
            onPreset = { preset ->
                addHabit = false
                scope.launch { if (!repo.addPreset(preset)) toast(context, "حداکثر ${PersianNumbers.toPersianDigits(Habits.MAX_HABITS)} عادت.") }
            },
            onCustom = { title ->
                addHabit = false
                scope.launch { if (!repo.addHabit(title)) toast(context, "حداکثر ${PersianNumbers.toPersianDigits(Habits.MAX_HABITS)} عادت.") }
            }
        )
    }
    if (addGoal) {
        AddGoalDialog(
            onDismiss = { addGoal = false },
            onSave = { title, plan ->
                addGoal = false
                scope.launch { repo.addGoal(title, plan, null) }
            }
        )
    }
    deleteHabit?.let { habit ->
        ConfirmDelete("عادت «${habit.title}» حذف شود؟", "سابقه این عادت هم پاک می‌شود.", onDismiss = { deleteHabit = null }) {
            deleteHabit = null
            scope.launch { repo.deleteHabit(habit.id) }
        }
    }
    deleteGoal?.let { goal ->
        ConfirmDelete("هدف «${goal.title}» حذف شود؟", null, onDismiss = { deleteGoal = null }) {
            deleteGoal = null
            scope.launch { repo.deleteGoal(goal.id) }
        }
    }
}

@Composable
private fun HabitRow(habit: HabitEntity, days: Set<Long>, today: LocalDate, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val done = today.toEpochDay() in days
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).toggleable(value = done, role = Role.Checkbox, onValueChange = onToggle).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(habit.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${PersianNumbers.toPersianDigits(Progress.streak(days, today))} روز پیاپی · " +
                        "بهترین ${PersianNumbers.toPersianDigits(Habits.bestStreak(days))} · " +
                        "هفته ${PersianNumbers.toPersianDigits(Habits.completionPercent(days, today))}٪",
                    fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WeekStrip(Habits.lastDays(days, today))
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "حذف عادت", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun JournalRow(entry: JournalEntity, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = 6.dp)
    ) {
        val mood = if (entry.mood > 0) " · ${Toolkit.moodLabel(entry.mood)}" else ""
        Text("${PersianDates.short(LocalDate.ofEpochDay(entry.epochDay))}$mood", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        val preview = (entry.goods + entry.note).filter { it.isNotBlank() }.joinToString(" · ")
        if (preview.isNotBlank()) Text(preview, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 2)
    }
}

internal fun assessmentIcon(kind: Assessments.Kind): ImageVector = when (kind) {
    Assessments.Kind.WELL_BEING -> Icons.Filled.Insights
    Assessments.Kind.DEPRESSION -> Icons.Filled.Psychology
    Assessments.Kind.ANXIETY -> Icons.Filled.Air
    Assessments.Kind.STRESS -> Icons.Filled.MonitorHeart
    Assessments.Kind.SELF_ESTEEM -> Icons.Filled.HealthAndSafety
    Assessments.Kind.PERSONALITY -> Icons.Filled.Face
    Assessments.Kind.RESILIENCE -> Icons.Filled.Bolt
    Assessments.Kind.SELF_EFFICACY -> Icons.Filled.EmojiEvents
    Assessments.Kind.LIFE_SATISFACTION -> Icons.Filled.AutoAwesome
    Assessments.Kind.LONELINESS -> Icons.Filled.People
    Assessments.Kind.RELATIONSHIP -> Icons.Filled.Favorite
}

@Composable
private fun AssessmentRow(kind: Assessments.Kind, history: List<AssessmentEntity>, onStart: () -> Unit) {
    val last = history.firstOrNull()
    val scale = Assessments.scale(kind)
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(assessmentIcon(kind), size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(kind.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                "${kind.subtitle} · ${PersianNumbers.toPersianDigits(kind.minutes)} دقیقه",
                fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (last != null) {
                Text(
                    "آخرین: ${historyLine(kind, last.score, last.answers)} · ${PersianDates.short(dateOf(last.takenAt))}",
                    fontSize = 11.5.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.primary
                )
            }
        }
        OutlinedButton(onClick = onStart, shape = MaterialTheme.shapes.medium) { Text(if (last == null) "شروع" else "تکرار") }
    }
    if (!scale.isProfile && history.size >= 2) {
        ScoreTrend(history.take(8).reversed().map { it.score - scale.minScore }, scale.maxScore - scale.minScore)
    }
}

@Composable
private fun GoalRow(goal: GoalEntity, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val done = goal.doneAt != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).toggleable(value = done, role = Role.Checkbox, onValueChange = onToggle).padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    goal.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (goal.plan.isNotBlank()) Text(goal.plan, fontSize = 12.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (done) Text("رسیدی ✓ ${PersianDates.short(dateOf(goal.doneAt!!))}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "حذف هدف", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddHabitDialog(
    status: Status,
    existing: Set<String>,
    onDismiss: () -> Unit,
    onPreset: (Habits.Preset) -> Unit,
    onCustom: (String) -> Unit
) {
    var custom by rememberSaveable { mutableStateOf("") }
    val presets = Habits.presetsFor(status).filter { it.id !in existing }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عادت تازه", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.take(Habits.MAX_TITLE) },
                    label = { Text("عادت خودت") },
                    placeholder = { Text("مثلاً: ۱۰ دقیقه زبان انگلیسی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (presets.isNotEmpty()) Label("یا از پیشنهادها")
                presets.forEach { preset ->
                    Column(
                        Modifier.fillMaxWidth().clickable(role = Role.Button) { onPreset(preset) }.padding(vertical = 6.dp)
                    ) {
                        Text(preset.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(preset.why, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCustom(custom) }, enabled = custom.isNotBlank()) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var plan by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("هدف تازه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it.take(GrowthRepository.MAX_LINE) },
                    label = { Text("هدف") }, placeholder = { Text("مثلاً: تا پایان سال ۱۰ کیلو کم کنم") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plan, onValueChange = { plan = it.take(GrowthRepository.MAX_LINE) },
                    label = { Text("برنامه اگر… آنگاه…") },
                    placeholder = { Text("اگر ساعت ۷ عصر شد، آنگاه ۳۰ دقیقه پیاده‌روی می‌کنم.") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
                MutedText("برنامه دقیق «کی، کجا، چه کاری» احتمال انجام هدف را بسیار بیشتر می‌کند (Gollwitzer, 1999).")
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, plan) }, enabled = title.isNotBlank()) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
internal fun ConfirmDelete(title: String, text: String?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { if (text != null) Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
