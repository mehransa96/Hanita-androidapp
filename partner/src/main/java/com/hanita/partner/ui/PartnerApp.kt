package com.hanita.partner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.Status
import com.hanita.partner.PartnerStore
import com.hanita.partner.Reminders
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** A message that needs the user's attention after a scan or a received link. */
private class Notice(val title: String, val text: String)

internal enum class Tab(val title: String, val icon: ImageVector) {
    TODAY("امروز", Icons.Filled.Today),
    PARTNER("همسر", Icons.Filled.FavoriteBorder),
    PATH("مسیر", Icons.Filled.Explore),
    DATES("تاریخ‌ها", Icons.Filled.Event),
    SETTINGS("تنظیمات", Icons.Filled.Tune);

    companion object {
        fun forStatus(status: Status): List<Tab> =
            if (status == Status.MARRIED) values().toList() else values().filter { it != PARTNER }
    }
}

/** Callbacks the screens share. */
internal class AppActions(
    val onChanged: () -> Unit,
    val openTab: (Tab) -> Unit,
    val openPillar: (String) -> Unit,
    val scan: () -> Unit,
    val paste: () -> Unit,
    val askForNotifications: () -> Unit,
    val addMessage: () -> Unit
)

/** The scrolling body every screen uses: edge-to-edge padding plus the screen's own items. */
@Composable
internal fun ScreenList(padding: PaddingValues, content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
    val store = remember { PartnerStore(context) }
    var version by remember { mutableIntStateOf(0) }
    val status = remember(version) { store.status }
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    var pillarId by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<Notice?>(null) }
    var showAddMessage by remember { mutableStateOf(false) }

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
                tab = Tab.PARTNER
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
                "همسرت اتصال با Hanita را قطع کرد و اطلاعات او از این گوشی پاک شد. تاریخ‌ها و تنظیمات خودت سر جایشان هستند."
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
            tab = Tab.TODAY
            askForNotifications()
            changed()
        }
    } else {
        val tabs = Tab.forStatus(status)
        val current = if (tab in tabs) tab else Tab.TODAY
        val actions = AppActions(
            onChanged = ::changed,
            openTab = { tab = it },
            openPillar = { pillarId = it; tab = Tab.PATH },
            scan = ::scan,
            paste = ::paste,
            askForNotifications = ::askForNotifications,
            addMessage = { showAddMessage = true }
        )
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = current == t,
                            onClick = {
                                if (t == Tab.PATH && current == Tab.PATH) pillarId = null
                                tab = t
                            },
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
            when (current) {
                Tab.TODAY -> TodayScreen(padding, store, version, status, actions)
                Tab.PARTNER -> PartnerScreen(padding, store, version, actions)
                Tab.PATH -> PathScreen(padding, status, pillarId, onOpen = { pillarId = it }, onBack = { pillarId = null })
                Tab.DATES -> DatesScreen(padding, store, version, status, actions)
                Tab.SETTINGS -> SettingsScreen(
                    padding, store, version, status, actions,
                    onUnlink = {
                        store.clearLink()
                        changed()
                        toast(context, "اتصال با Hanita قطع شد و اطلاعات همسرت پاک شد.")
                    },
                    onResetAll = {
                        store.clearAll()
                        tab = Tab.TODAY
                        pillarId = null
                        changed()
                    }
                )
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
