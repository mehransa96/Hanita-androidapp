package com.hanita.partner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.Status
import com.hanita.partner.PartnerStore
import com.hanita.partner.Reminders
import com.hanita.partner.data.GrowthRepository
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.time.LocalDate

/** What the content area shows: a tab, or the page [depth] levels above it. */
private data class Shown(val tab: Tab, val route: String?, val depth: Int)

/** A message that needs the user's attention after a scan or a received link. */
private class Notice(val title: String, val text: String)

internal enum class Tab(val title: String, val icon: ImageVector) {
    TODAY("امروز", Icons.Filled.Today),
    PARTNER("همسر", Icons.Filled.FavoriteBorder),
    PATH("مسیر", Icons.Filled.Explore),
    GROWTH("رشد", Icons.AutoMirrored.Filled.TrendingUp),
    TOOLS("ابزار", Icons.Filled.TipsAndUpdates);

    companion object {
        fun forStatus(status: Status): List<Tab> =
            if (status == Status.MARRIED) values().toList() else values().filter { it != PARTNER }
    }
}

/**
 * Pages opened on top of a tab (with a back arrow), kept as plain strings so they survive
 * rotation: "settings", "dates", "sources", "pillar/<id>", "assess/<code>", "journal/<epochDay>",
 * "breath/<id>", "budget", "apology", "soft", "questions", "check/<id>", "healthcheck", "body".
 */
internal object Routes {
    const val SETTINGS = "settings"
    const val DATES = "dates"
    const val SOURCES = "sources"
    const val BUDGET = "budget"
    const val APOLOGY = "apology"
    const val SOFT = "soft"
    const val QUESTIONS = "questions"
    const val HEALTH_CHECK = "healthcheck"
    const val BODY = "body"
    fun pillar(id: String) = "pillar/$id"
    fun assess(code: String) = "assess/$code"
    fun journal(epochDay: Long) = "journal/$epochDay"
    fun breath(id: String) = "breath/$id"
    fun check(id: String) = "check/$id"
}

/** Callbacks the screens share. */
internal class AppActions(
    val onChanged: () -> Unit,
    val openTab: (Tab) -> Unit,
    val open: (String) -> Unit,
    val back: () -> Unit,
    val scan: () -> Unit,
    val paste: () -> Unit,
    val askForNotifications: () -> Unit,
    val addMessage: () -> Unit
) {
    fun openPillar(id: String) = open(Routes.pillar(id))
}

/**
 * The scrolling body every screen uses: edge-to-edge padding plus the screen's own items. The
 * list also shrinks above the keyboard, so a text field near the bottom is never hidden behind it.
 */
@Composable
internal fun ScreenList(padding: PaddingValues, content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(padding).imePadding(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun PartnerApp(incomingText: String?, onIncomingHandled: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val store = remember { PartnerStore(context) }
    val repo = remember { GrowthRepository.get(context) }
    var version by remember { mutableIntStateOf(0) }
    val status = remember(version) { store.status }
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    // Pages opened on top of the tab, newest last, joined by '|' so the stack survives rotation
    // and process death. Back returns to the page underneath (for example source -> pillar).
    var stackText by rememberSaveable { mutableStateOf("") }
    val stack = if (stackText.isEmpty()) emptyList() else stackText.split('|')
    val route = stack.lastOrNull()
    // Keeps each tab's scroll position (and each open page's state) while the user moves around.
    val saveable = rememberSaveableStateHolder()
    fun stateKey(depth: Int, r: String) = "page$depth:$r"
    fun screenKey(s: Shown) = if (s.route == null) "tab:${s.tab.name}" else stateKey(s.depth, s.route)
    fun setStack(next: List<String>) {
        // Forget the state of pages that were closed, so reopening one starts fresh.
        stack.forEachIndexed { i, r -> if (next.getOrNull(i) != r) saveable.removeState(stateKey(i + 1, r)) }
        stackText = next.joinToString("|")
    }
    fun openRoute(r: String) { if (route != r) setStack(stack + r) }
    fun closeRoute() { setStack(stack.dropLast(1)) }
    fun showTab(t: Tab) { tab = t; setStack(emptyList()) }
    var notice by remember { mutableStateOf<Notice?>(null) }
    var showAddMessage by remember { mutableStateOf(false) }
    // "Today" moves on when the app comes back after midnight.
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleResumeEffect(Unit) {
        val now = LocalDate.now()
        if (now != today) today = now
        onPauseOrDispose { }
    }

    LaunchedEffect(status) { repo.prepare(status) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun changed() {
        version++
        Reminders.reschedule(context)
    }

    fun handle(text: String?, fromScan: Boolean) {
        val result = store.receive(text, fromScan)
        when (result) {
            is PartnerStore.Result.Paired -> {
                // Pairing with Hanita means a married user.
                store.status = Status.MARRIED
                showTab(Tab.PARTNER)
                notice = Notice(
                    "وصل شدی ✓",
                    "کد تأیید: ${PersianNumbers.toPersianDigits(result.code)}\n" +
                        "این کد باید با کد روی گوشی ${result.ownerName.ifBlank { "همسرت" }} یکی باشد. اگر فرق دارد، دوباره اسکن کن."
                )
                askForNotifications()
            }
            is PartnerStore.Result.Updated -> toast(context, "اطلاعات همسرت به‌روز شد")
            PartnerStore.Result.Disconnected -> notice = Notice(
                "اتصال قطع شد",
                "همسرت اتصال با Hanita را قطع کرد و اطلاعات او از این گوشی پاک شد. تاریخ‌ها، پیشرفت و تنظیمات خودت سر جایشان هستند."
            )
            PartnerStore.Result.Older -> toast(context, "این به‌روزرسانی از اطلاعات فعلی قدیمی‌تر است.")
            PartnerStore.Result.PairingNeedsScan -> notice = Notice(
                "کد اتصال را اسکن کن",
                "کد اتصال فقط با «اسکن QR» از روی گوشی همسرت پذیرفته می‌شود، چون کلید رمز داخل آن است و نباید از پیام‌رسان رد شود."
            )
            PartnerStore.Result.WrongPair -> notice = Notice(
                "مال این اتصال نیست",
                "این به‌روزرسانی برای اتصال فعلی نیست. اگر همسرت در Hanita «اتصال دوباره» را زده، QR جدید را اسکن کن."
            )
            PartnerStore.Result.NotPaired -> notice = Notice(
                "هنوز به Hanita وصل نیستی",
                "اول همسرت در Hanita (تب «همراه»، دکمه QR) کد اتصال بسازد و تو در بخش «همسر» آن را اسکن کن."
            )
            PartnerStore.Result.Invalid -> toast(context, "کد معتبر نبود یا ناقص کپی شده است.")
        }
        changed()
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

    LaunchedEffect(incomingText) {
        if (incomingText != null) {
            handle(incomingText, fromScan = false)
            onIncomingHandled()
        }
    }

    if (status == null) {
        WelcomeScreen { name, chosen, dailyTip ->
            store.userName = name
            store.status = chosen
            store.dailyTip = dailyTip
            showTab(Tab.TODAY)
            askForNotifications()
            changed()
        }
    } else {
        val tabs = Tab.forStatus(status)
        val current = if (tab in tabs) tab else Tab.TODAY
        val actions = AppActions(
            onChanged = ::changed,
            openTab = { showTab(it) },
            open = { openRoute(it) },
            back = { closeRoute() },
            scan = ::scan,
            paste = ::paste,
            askForNotifications = ::askForNotifications,
            addMessage = { showAddMessage = true }
        )
        if (route != null) BackHandler { closeRoute() }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = current == t && route == null,
                            onClick = { showTab(t) },
                            icon = { Icon(t.icon, contentDescription = null) },
                            label = { Text(t.title, fontSize = 11.sp, maxLines = 1) },
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
        ) { padding ->
            AnimatedContent(
                targetState = Shown(current, route, stack.size),
                transitionSpec = {
                    val deeper = targetState.depth > initialState.depth
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { w -> if (deeper) -w / 12 else 0 })
                        .togetherWith(fadeOut(tween(120)))
                },
                // One key per screen, shared with its saved state, so a screen is never shown twice.
                contentKey = { screenKey(it) },
                label = "screen"
            ) { shown ->
                val shownTab = shown.tab
                val shownRoute = shown.route
                saveable.SaveableStateProvider(screenKey(shown)) {
                if (shownRoute != null) {
                    RoutePage(
                        shownRoute, padding, store, repo, version, status, today, actions,
                        onUnlink = {
                            store.clearLink()
                            changed()
                            toast(context, "اتصال با Hanita قطع شد و اطلاعات همسرت پاک شد.")
                        },
                        onResetAll = {
                            scope.launch {
                                repo.clearAll()
                                store.clearAll()
                                showTab(Tab.TODAY)
                                changed()
                            }
                        }
                    )
                } else {
                    when (shownTab) {
                        Tab.TODAY -> TodayScreen(padding, store, repo, version, status, today, actions)
                        Tab.PARTNER -> PartnerScreen(padding, store, version, actions)
                        Tab.PATH -> PathScreen(padding, status, actions)
                        Tab.GROWTH -> GrowthScreen(padding, repo, status, today, actions)
                        Tab.TOOLS -> ToolsScreen(padding, status, actions)
                    }
                }
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
                changed()
            }
        )
    }
}

/** Shows the page a route names; unknown or no-longer-valid routes fall back to nothing. */
@Composable
private fun RoutePage(
    route: String,
    padding: PaddingValues,
    store: PartnerStore,
    repo: GrowthRepository,
    version: Int,
    status: Status,
    today: LocalDate,
    actions: AppActions,
    onUnlink: () -> Unit,
    onResetAll: () -> Unit
) {
    val arg = route.substringAfter('/', "")
    when {
        route == Routes.SETTINGS -> SettingsScreen(padding, store, version, status, actions, onUnlink, onResetAll)
        route == Routes.DATES -> DatesScreen(padding, store, version, status, actions)
        route == Routes.SOURCES -> SourcesScreen(padding, actions)
        route == Routes.BUDGET -> BudgetScreen(padding, actions)
        route == Routes.APOLOGY -> ApologyScreen(padding, actions)
        route == Routes.SOFT -> SoftStartScreen(padding, actions)
        route == Routes.QUESTIONS -> QuestionsScreen(padding, status, actions)
        route == Routes.HEALTH_CHECK -> HealthCheckScreen(padding, actions)
        route == Routes.BODY -> BodyScreen(padding, actions)
        route.startsWith("pillar/") -> PillarScreen(padding, status, arg, actions)
        route.startsWith("assess/") -> AssessmentScreen(padding, repo, arg, actions)
        route.startsWith("journal/") -> JournalScreen(padding, repo, arg.toLongOrNull() ?: today.toEpochDay(), today, actions)
        route.startsWith("breath/") -> BreathingScreen(padding, arg, actions)
        route.startsWith("check/") -> ChecklistScreen(padding, arg, actions)
        else -> LaunchedEffect(route) { actions.back() }
    }
}
