package com.hanita.partner.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
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
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Toolkit
import com.hanita.partner.data.GrowthRepository
import com.hanita.partner.data.JournalEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/** One question at a time, then the result, the history and the source. */
@Composable
internal fun AssessmentScreen(padding: PaddingValues, repo: GrowthRepository, code: String, actions: AppActions) {
    val kind = Assessments.Kind.fromCode(code)
    if (kind == null) {
        LaunchedEffect(code) { actions.back() }
        return
    }
    val scale = Assessments.scale(kind)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    // Chosen option per question as digits ("" = not started); saved across rotation.
    var answers by rememberSaveable(code) { mutableStateOf("") }
    var savedScore by rememberSaveable(code) { mutableStateOf(-1) }
    var deleteId by rememberSaveable(code) { mutableStateOf(-1L) }
    val all by repo.assessments.collectAsState(initial = emptyList())
    val history = all.filter { it.kind == kind.code }
    val step = answers.length
    val finished = step >= scale.questions.size

    LaunchedEffect(finished) {
        if (finished && savedScore < 0) {
            savedScore = repo.saveAssessment(kind, Assessments.decodeAnswers(answers)) ?: -1
        }
    }

    ScreenList(padding) {
        item { Wordmark(kind.title, onBack = actions.back) }
        if (!finished) {
            item { Text(kind.subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }
            item { MutedText(scale.intro) }
            item(key = "progress") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "سؤال ${PersianNumbers.toPersianDigits(step + 1)} از ${PersianNumbers.toPersianDigits(scale.questions.size)}",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Meter(step * 100 / scale.questions.size)
                }
            }
            item(key = "question") {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { (fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 10 }).togetherWith(fadeOut(tween(100))) },
                    label = "question"
                ) { index ->
                    if (index < scale.questions.size) {
                        Panel {
                            Text(scale.questions[index].text, fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                            scale.options.forEachIndexed { i, option ->
                                OutlinedButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (answers.length == index) answers += i.toString()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                ) { Text(option) }
                            }
                            if (index > 0) {
                                TextButton(onClick = { answers = answers.dropLast(1) }) { Text("سؤال قبلی") }
                            }
                        }
                    }
                }
            }
        } else {
            val score = savedScore.takeIf { it >= 0 } ?: Assessments.score(kind, Assessments.decodeAnswers(answers)) ?: 0
            val result = Assessments.interpret(kind, score)
            item(key = "result") {
                Panel {
                    Label("نتیجه تو")
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(PersianNumbers.toPersianDigits(score), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("از ${PersianNumbers.toPersianDigits(scale.maxScore)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Meter(score * 100 / scale.maxScore)
                    Text(result.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(result.text, fontSize = 14.5.sp, lineHeight = 24.sp)
                    if (result.seekHelp) {
                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium).padding(12.dp)) {
                            Text(Assessments.HELP_TEXT, fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (kind == Assessments.Kind.SELF_ESTEEM) {
                            Button(onClick = { actions.openPillar("esteem") }, shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f)) {
                                Text("محور عزت نفس")
                            }
                        } else {
                            Button(onClick = { actions.openPillar("calm") }, shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f)) {
                                Text("آرامش و استرس")
                            }
                        }
                        OutlinedButton(
                            onClick = { answers = ""; savedScore = -1 },
                            shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f)
                        ) { Text("دوباره") }
                    }
                }
            }
        }
        if (history.isNotEmpty()) {
            item(key = "history") {
                Panel {
                    Label("سابقه")
                    ScoreTrend(history.take(8).reversed().map { it.score }, scale.maxScore)
                    history.take(10).forEachIndexed { i, a ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${PersianNumbers.toPersianDigits(a.score)} · ${Assessments.interpret(kind, a.score).title}",
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                                Text(PersianDates.short(dateOf(a.takenAt)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleteId = a.id }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف نتیجه", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item(key = "src") { SourcesPanel(listOf(Sources.get(kind.sourceId)), "درباره این آزمون") }
        item { MutedText(Assessments.HELP_TEXT) }
    }

    if (deleteId >= 0) {
        ConfirmDelete("این نتیجه حذف شود؟", "از سابقه و نمودار پیشرفتت هم پاک می‌شود.", onDismiss = { deleteId = -1L }) {
            val id = deleteId
            deleteId = -1L
            scope.launch { repo.deleteAssessment(id) }
        }
    }
}

/** One day's journal page: mood, three good things (and why), and a free note. */
@Composable
internal fun JournalScreen(padding: PaddingValues, repo: GrowthRepository, epochDay: Long, today: LocalDate, actions: AppActions) {
    val scope = rememberCoroutineScope()
    var loaded by rememberSaveable(epochDay) { mutableStateOf(false) }
    var mood by rememberSaveable(epochDay) { mutableIntStateOf(0) }
    var g1 by rememberSaveable(epochDay) { mutableStateOf("") }
    var g2 by rememberSaveable(epochDay) { mutableStateOf("") }
    var g3 by rememberSaveable(epochDay) { mutableStateOf("") }
    var note by rememberSaveable(epochDay) { mutableStateOf("") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val day = LocalDate.ofEpochDay(epochDay)

    LaunchedEffect(epochDay) {
        if (!loaded) {
            repo.journalFor(epochDay)?.let { e ->
                mood = e.mood; g1 = e.good1; g2 = e.good2; g3 = e.good3; note = e.note
            }
            loaded = true
        }
    }

    var deleted by remember { mutableStateOf(false) }
    val page = JournalEntity(epochDay, mood, g1, g2, g3, note)
    val latest by rememberUpdatedState(page)

    // Everything is saved as you write (shortly after you stop typing) and again when the page
    // closes, whether by back, a tab or the save button: nothing written is ever lost.
    LaunchedEffect(page, loaded) {
        if (loaded) {
            delay(700)
            if (!deleted) repo.saveJournal(page)
        }
    }
    DisposableEffect(Unit) {
        onDispose { if (loaded && !deleted) repo.saveJournalInBackground(latest) }
    }

    ScreenList(padding) {
        item {
            Wordmark(if (day == today) "دفترچه امروز" else "دفترچه", onBack = actions.back)
        }
        item { Text(PersianDates.long(day), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (loaded) {
            item(key = "mood") {
                Panel {
                    Label("حال کلی")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Toolkit.MOODS.forEachIndexed { i, label ->
                            val on = mood == i + 1
                            Box(
                                Modifier.weight(1f)
                                    .background(
                                        if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.shapes.medium
                                    )
                                    .selectable(selected = on, role = Role.RadioButton) { mood = if (on) 0 else i + 1 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label, fontSize = 11.5.sp, maxLines = 1, textAlign = TextAlign.Center,
                                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item(key = "goods") {
                Panel {
                    Label("سه اتفاق خوب امروز")
                    MutedText("سه چیز خوب، هرچقدر کوچک، که امروز اتفاق افتاد بنویس و اینکه چرا اتفاق افتاد یا نقش تو در آن چه بود.")
                    listOf(
                        Triple(g1, { v: String -> g1 = v }, "اتفاق خوب اول"),
                        Triple(g2, { v: String -> g2 = v }, "اتفاق خوب دوم"),
                        Triple(g3, { v: String -> g3 = v }, "اتفاق خوب سوم")
                    ).forEach { (value, set, label) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { set(it.take(GrowthRepository.MAX_LINE)) },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            }
            item(key = "note") {
                Panel {
                    Label("یادداشت آزاد")
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(GrowthRepository.MAX_NOTE) },
                        placeholder = { Text("چه چیزی یاد گرفتی؟ فردا چه کاری را بهتر انجام می‌دهی؟") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            }
            item(key = "save") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = actions.back, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) {
                        Text("ذخیره", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { confirmDelete = true }, shape = MaterialTheme.shapes.medium) { Text("پاک کردن") }
                }
            }
            item { MutedText("نوشته‌ها خودکار ذخیره می‌شوند.") }
            item { SourcesPanel(listOf(Sources.get("seligman")), "چرا این تمرین؟") }
        }
    }

    if (confirmDelete) {
        ConfirmDelete("دفترچه این روز پاک شود؟", null, onDismiss = { confirmDelete = false }) {
            confirmDelete = false
            deleted = true
            scope.launch {
                repo.deleteJournal(epochDay)
                actions.back()
            }
        }
    }
}
