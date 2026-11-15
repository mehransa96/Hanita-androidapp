package com.hanita.partner.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Status
import com.hanita.core.gentleman.Toolkit
import kotlinx.coroutines.delay

/** The toolbox, grouped by the moment it helps with. */
@Composable
internal fun ToolsScreen(padding: PaddingValues, status: Status, actions: AppActions) {
    ScreenList(padding) {
        item { Wordmark("جعبه‌ابزار") }
        item { MutedText("ابزارهای کوچک برای لحظه‌های واقعی: پیش از جلسه، وسط عصبانیت، در گفت‌وگوی سخت یا سر ماه.") }
        item {
            Panel {
                Label("آرامش و تمرکز")
                Toolkit.BREATHING.forEach { b ->
                    Tile(if (b.id == "box") Icons.Filled.Spa else Icons.Filled.Air, b.title, b.subtitle) { actions.open(Routes.breath(b.id)) }
                }
            }
        }
        item {
            Panel {
                Label("گفت‌وگو و رابطه")
                Tile(Icons.Filled.Forum, "شروع نرم یک گفت‌وگوی سخت", "گفتن مشکل بدون سرزنش") { actions.open(Routes.SOFT) }
                Tile(Icons.Filled.VolunteerActivism, "عذرخواهی مؤثر", "بر پایه پژوهش درباره اجزای یک عذرخواهی خوب") { actions.open(Routes.APOLOGY) }
                Tile(
                    Icons.Filled.Sms,
                    if (status == Status.MARRIED) "پرسش‌های گفت‌وگوی دونفره" else "پرسش‌های آشنایی",
                    "پرسش‌هایی که صمیمیت می‌سازند"
                ) { actions.open(Routes.QUESTIONS) }
            }
        }
        item {
            Panel {
                Label("آمادگی")
                Toolkit.CHECKLISTS.forEach { c ->
                    if (c.id != "date" || status == Status.SINGLE) {
                        Tile(Icons.Filled.Description, c.title, "${PersianNumbers.toPersianDigits(c.items.size)} قدم") { actions.open(Routes.check(c.id)) }
                    }
                }
            }
        }
        item {
            Panel {
                Label("پول و زندگی")
                Tile(Icons.Filled.AccountBalanceWallet, "بودجه ۵۰/۳۰/۲۰ و صندوق اضطراری", "درآمدت را تقسیم کن و هدف پس‌انداز بگذار") { actions.open(Routes.BUDGET) }
                Tile(Icons.Filled.Event, "تاریخ‌های مهم", "تولدها، سالگردها و مناسبت‌ها") { actions.open(Routes.DATES) }
            }
        }
        item {
            Panel {
                Label("برنامه")
                Tile(Icons.AutoMirrored.Filled.MenuBook, "منابع علمی", "کتاب‌ها و پژوهش‌هایی که محتوا بر پایه آن‌هاست") { actions.open(Routes.SOURCES) }
                Tile(Icons.Filled.Settings, "تنظیمات", "پروفایل، یادآوری‌ها، اتصال و حریم خصوصی") { actions.open(Routes.SETTINGS) }
            }
        }
    }
}

// ---- Breathing ----

@Composable
internal fun BreathingScreen(padding: PaddingValues, id: String, actions: AppActions) {
    val pattern = Toolkit.BREATHING.firstOrNull { it.id == id } ?: Toolkit.BREATHING.first()
    val haptics = LocalHapticFeedback.current
    var minutes by rememberSaveable { mutableIntStateOf(3) }
    var running by rememberSaveable { mutableStateOf(false) }
    var elapsed by rememberSaveable { mutableIntStateOf(0) }
    val total = minutes * 60
    val (phase, left) = pattern.phaseAt(elapsed)

    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            val before = pattern.phaseAt(elapsed).first
            elapsed++
            if (elapsed >= total) {
                running = false
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            } else if (pattern.phaseAt(elapsed).first !== before) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
    // Keep the screen awake while breathing.
    val view = LocalView.current
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    val target = when (phase.kind) {
        Toolkit.Phase.Kind.INHALE -> 1f
        Toolkit.Phase.Kind.EXHALE -> 0.45f
        Toolkit.Phase.Kind.HOLD -> if (pattern.phases.indexOf(phase) > 0 && pattern.phases[pattern.phases.indexOf(phase) - 1].kind == Toolkit.Phase.Kind.INHALE) 1f else 0.45f
    }
    val scale by animateFloatAsState(
        targetValue = if (running) target else 0.6f,
        animationSpec = tween(durationMillis = if (running) phase.seconds * 1000 else 400, easing = FastOutSlowInEasing),
        label = "breath"
    )
    val ring = MaterialTheme.colorScheme.primary
    val fill = MaterialTheme.colorScheme.primaryContainer

    ScreenList(padding) {
        item { Wordmark(pattern.title, onBack = actions.back) }
        item { MutedText(pattern.subtitle) }
        item {
            Panel {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(220.dp).semantics { contentDescription = if (running) "${phase.label}، ${left} ثانیه" else "آماده" }) {
                        val r = size.minDimension / 2
                        drawCircle(fill, radius = r * scale)
                        drawCircle(ring, radius = r * scale, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (running) phase.label else "آماده‌ای؟", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        if (running) Text(PersianNumbers.toPersianDigits(left), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Meter(if (total == 0) 0 else elapsed * 100 / total)
                val remaining = (total - elapsed).coerceAtLeast(0)
                Text(
                    "${PersianNumbers.toPersianDigits(remaining / 60)}:${PersianNumbers.toPersianDigits((remaining % 60).toString().padStart(2, '0'))} مانده",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!running) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 5).forEach { m ->
                            FilterChip(
                                selected = minutes == m,
                                onClick = { minutes = m; elapsed = 0 },
                                label = { Text("${PersianNumbers.toPersianDigits(m)} دقیقه") }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (!running && elapsed >= total) elapsed = 0
                            running = !running
                        },
                        modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium
                    ) { Text(if (running) "مکث" else if (elapsed in 1 until total) "ادامه" else "شروع", fontWeight = FontWeight.Bold) }
                    if (elapsed > 0) {
                        OutlinedButton(onClick = { running = false; elapsed = 0 }, shape = MaterialTheme.shapes.medium) { Text("از نو") }
                    }
                }
            }
        }
        item {
            Panel {
                Label("چطور؟")
                pattern.phases.forEach { Bullet("${it.label}: ${PersianNumbers.toPersianDigits(it.seconds)} ثانیه") }
                MutedText("آرام و بدون فشار نفس بکش. اگر سرگیجه گرفتی، تمرین را متوقف کن و عادی نفس بکش.")
            }
        }
        item { SourcesPanel(listOf(Sources.get(pattern.sourceId))) }
    }
}

// ---- Budget ----

@Composable
internal fun BudgetScreen(padding: PaddingValues, actions: AppActions) {
    var income by rememberSaveable { mutableStateOf("") }
    var essentials by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf("") }
    val incomeValue = parseAmount(income)
    val split = Toolkit.budget(incomeValue)
    val essentialsValue = parseAmount(essentials).takeIf { it > 0 } ?: split.needs
    val (fund3, fund6) = Toolkit.emergencyFund(essentialsValue)
    val months = Toolkit.monthsToGoal(fund3, parseAmount(saved), split.savings)

    ScreenList(padding) {
        item { Wordmark("بودجه ماهانه", onBack = actions.back) }
        item {
            MutedText("قاعده ۵۰/۳۰/۲۰ یک نقطه شروع ساده است: نیمی برای نیازها، ۳۰٪ برای خواسته‌ها و ۲۰٪ برای پس‌انداز و بازپرداخت بدهی. با شرایط خودت تنظیمش کن.")
        }
        item {
            Panel {
                AmountField(income, { income = it }, "درآمد ماهانه (تومان)")
                if (incomeValue > 0) {
                    BudgetLine("نیازها (اجاره، خورد و خوراک، قبض‌ها، رفت‌وآمد)", split.needs, 50)
                    BudgetLine("خواسته‌ها (تفریح، بیرون رفتن، خریدهای غیرضروری)", split.wants, 30)
                    BudgetLine("پس‌انداز و بازپرداخت بدهی", split.savings, 20)
                }
            }
        }
        item {
            Panel {
                Label("صندوق روزهای سخت")
                MutedText("برای هزینه‌های پیش‌بینی‌نشده (بیکاری، بیماری، تعمیر) هزینه ضروری سه تا شش ماه را کنار بگذار.")
                AmountField(essentials, { essentials = it }, "هزینه ضروری ماهانه (اگر خالی بماند، ۵۰٪ درآمد)")
                AmountField(saved, { saved = it }, "مبلغی که الان پس‌انداز داری")
                if (essentialsValue > 0) {
                    Text("هدف سه‌ماهه: ${Toolkit.formatAmount(fund3)} تومان", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("هدف شش‌ماهه: ${Toolkit.formatAmount(fund6)} تومان", fontSize = 14.sp)
                    when (months) {
                        null -> MutedText("برای رسیدن به هدف، اول سهم پس‌انداز ماهانه را مشخص کن.")
                        0 -> Text("به هدف سه‌ماهه رسیده‌ای ✓", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        else -> Text(
                            "با پس‌انداز ماهانه ${Toolkit.formatAmount(split.savings)} تومان، حدود ${PersianNumbers.toPersianDigits(months)} ماه تا هدف سه‌ماهه.",
                            fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item { SourcesPanel(listOf(Sources.get("warren"), Sources.get("cfpb"))) }
    }
}

private fun parseAmount(text: String): Long =
    PersianNumbers.normalize(text).filter { it.isDigit() }.take(15).toLongOrNull() ?: 0L

@Composable
private fun AmountField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c in "٬," }.take(18)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            val amount = parseAmount(value)
            if (amount > 0) Text("${Toolkit.formatAmount(amount)} تومان")
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BudgetLine(title: String, amount: Long, percent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("${Toolkit.formatAmount(amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Meter(percent)
    }
}

// ---- Words for hard moments ----

@Composable
internal fun ApologyScreen(padding: PaddingValues, actions: AppActions) {
    var what by rememberSaveable { mutableStateOf("") }
    var impact by rememberSaveable { mutableStateOf("") }
    var repair by rememberSaveable { mutableStateOf("") }
    var next by rememberSaveable { mutableStateOf("") }
    val text = Toolkit.apology(what, impact, repair, next)
    ScreenList(padding) {
        item { Wordmark("عذرخواهی مؤثر", onBack = actions.back) }
        item {
            MutedText("پژوهش‌ها نشان می‌دهند مهم‌ترین بخش عذرخواهی، پذیرفتن مسئولیت و پیشنهاد جبران است؛ توجیه طولانی اثرش را کم می‌کند. جاهای خالی را پر کن.")
        }
        item {
            Panel {
                Field(what, { what = it }, "چه کاری کردی؟", "مثلاً: جلوی جمع حرفت را قطع کردم")
                Field(impact, { impact = it }, "چه اثری روی او گذاشت؟", "مثلاً: احساس کنی نظرت مهم نیست")
                Field(repair, { repair = it }, "چطور جبران می‌کنی؟", "مثلاً: امشب با حوصله حرفت را بشنوم")
                Field(next, { next = it }, "از این به بعد چه می‌کنی؟", "مثلاً: صبر می‌کنم حرفت تمام شود")
            }
        }
        item { ResultPanel(text) }
        item { SourcesPanel(listOf(Sources.get("lewicki"))) }
    }
}

@Composable
internal fun SoftStartScreen(padding: PaddingValues, actions: AppActions) {
    var feeling by rememberSaveable { mutableStateOf("") }
    var situation by rememberSaveable { mutableStateOf("") }
    var request by rememberSaveable { mutableStateOf("") }
    val text = Toolkit.softStartup(feeling, situation, request)
    val harsh = Toolkit.harshWords(feeling + " " + situation + " " + request)
    ScreenList(padding) {
        item { Wordmark("شروع نرم", onBack = actions.back) }
        item {
            MutedText("طبق پژوهش‌های گاتمن، شیوه شروع یک بحث تا حد زیادی پایانش را تعیین می‌کند. به جای سرزنش، احساست، موقعیت مشخص و یک درخواست مثبت را بگو.")
        }
        item {
            Panel {
                Field(feeling, { feeling = it }, "چه احساسی داری؟", "مثلاً: نگران، تنها، خسته")
                Field(situation, { situation = it }, "در چه موقعیتی؟ (رفتار، نه شخصیت)", "مثلاً: دیر خبر می‌دهی که دیر می‌آیی")
                Field(request, { request = it }, "چه چیزی کمک می‌کند؟ (مثبت و مشخص)", "مثلاً: وقتی دیر می‌شود یک پیام کوتاه بدهی")
                if (harsh.isNotEmpty()) {
                    Text(
                        "کلمه‌هایی مثل «${harsh.joinToString("»، «")}» حالت سرزنش دارند؛ درباره همین یک موقعیت حرف بزن.",
                        fontSize = 12.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        item { ResultPanel(text) }
        item {
            Panel {
                Label("اگر بحث تند شد")
                Bullet("بگو «بیا ۲۰ دقیقه استراحت کنیم» و حتماً برگرد. ذهن برانگیخته راه‌حل نمی‌بیند.")
                Bullet("از تحقیر، طعنه، حالت دفاعی و قهر سرد دوری کن؛ این چهار رفتار پیش‌بینی‌کننده جدایی‌اند.")
                Bullet("پیش از جواب، حرفش را خلاصه کن: «یعنی می‌گویی…»")
            }
        }
        item { SourcesPanel(listOf(Sources.get("gottman"), Sources.get("gottman-levenson"))) }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(140)) },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3
    )
}

/** The finished text with copy and share; sharing opens the chooser, nothing is sent by itself. */
@Composable
private fun ResultPanel(text: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Panel {
        Label("متن پیشنهادی")
        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium).padding(12.dp)) {
            Text(text, fontSize = 15.sp, lineHeight = 26.sp)
        }
        MutedText("بهتر است رودررو و با لحن آرام بگویی؛ این متن فقط برای آماده شدن است.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(text))
                    toast(context, "کپی شد")
                },
                modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("کپی")
            }
            Button(onClick = { share(context, text) }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) { Text("ارسال") }
        }
    }
}

// ---- Conversation questions and checklists ----

@Composable
internal fun QuestionsScreen(padding: PaddingValues, status: Status, actions: AppActions) {
    val sets = Toolkit.questionsFor(status)
    var index by rememberSaveable { mutableIntStateOf(0) }
    ScreenList(padding) {
        item { Wordmark("پرسش‌های گفت‌وگو", onBack = actions.back) }
        item {
            MutedText("پرسش‌هایی که به‌تدریج عمیق‌تر می‌شوند، آدم‌ها را به هم نزدیک‌تر می‌کنند. یکی را انتخاب کن، بپرس و با دقت گوش کن؛ بعد خودت هم جواب بده.")
        }
        sets.forEach { set ->
            item(key = set.id) {
                Panel {
                    Label(set.title)
                    set.questions.forEachIndexed { i, q ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(q, fontSize = 14.5.sp, lineHeight = 24.sp)
                    }
                }
            }
        }
        item {
            val all = sets.flatMap { it.questions }
            if (all.isNotEmpty()) {
                Panel {
                    Label("یک پرسش تصادفی")
                    Text(all[Math.floorMod(index, all.size)], fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { index = (index + 1 + (all.size / 3)) }, shape = MaterialTheme.shapes.medium) { Text("پرسش بعدی") }
                }
            }
        }
        item { SourcesPanel(listOf(Sources.get("aron"))) }
    }
}

@Composable
internal fun ChecklistScreen(padding: PaddingValues, id: String, actions: AppActions) {
    val list = Toolkit.CHECKLISTS.firstOrNull { it.id == id }
    if (list == null) {
        LaunchedEffect(id) { actions.back() }
        return
    }
    var checked by rememberSaveable(id) { mutableStateOf("") }
    val done = checked.split(',').mapNotNull { it.toIntOrNull() }.toSet()
    ScreenList(padding) {
        item { Wordmark(list.title, onBack = actions.back) }
        item {
            Panel {
                Text(
                    "${PersianNumbers.toPersianDigits(done.size)} از ${PersianNumbers.toPersianDigits(list.items.size)} آماده",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Meter(done.size * 100 / list.items.size)
                list.items.forEachIndexed { i, item ->
                    val on = i in done
                    Row(
                        Modifier.fillMaxWidth().toggleable(value = on, role = Role.Checkbox) {
                            checked = (if (it) done + i else done - i).sorted().joinToString(",")
                        }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (on) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(item, fontSize = 14.5.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
        item {
            val sourceIds = when (list.id) {
                "meeting" -> listOf("carnegie", "balban")
                "date" -> listOf("vanepp", "post")
                else -> listOf("gottman")
            }
            SourcesPanel(sourceIds.map(Sources::get))
        }
    }
}
