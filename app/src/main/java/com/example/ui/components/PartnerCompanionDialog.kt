package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tools.CompanionLinkStore
import com.example.tools.PartnerReminders
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerLink
import com.hanita.core.partner.QrCodes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val Rose = Color(0xFFE11D48)

/** What the dialog shows: the settings, or a QR code to scan on the partner's phone. */
private sealed class CompanionScreen {
    object Overview : CompanionScreen()
    class Qr(val text: String, val title: String, val hint: String, val verificationCode: String?) : CompanionScreen()
}

/**
 * Pairs Hanita with the partner app "GentleMan" and sends it encrypted cycle cards.
 *
 * No server is involved: the first pairing is a QR code scanned face to face (it carries the
 * secret key), later updates are a QR code again or an encrypted text sent through any
 * messenger. Only the last period start, the average lengths and the owner's choices are
 * shared; symptoms, moods and notes never leave this phone.
 */
@Composable
fun PartnerCompanionDialog(
    userName: String,
    lastPeriodStart: LocalDate?,
    cycleLength: Int,
    periodLength: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val store = remember { CompanionLinkStore(context) }
    var version by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf<CompanionScreen>(CompanionScreen.Overview) }
    var confirmDisconnect by remember { mutableStateOf(false) }
    var confirmRepair by remember { mutableStateOf(false) }

    val paired = remember(version) { store.isPaired }
    val shareFertile = remember(version) { store.shareFertile }
    val paused = remember(version) { store.paused }
    var note by remember { mutableStateOf(store.note) }
    val lastSentAt = remember(version) { store.lastSentAt }

    fun currentCard(revoked: Boolean = false): PartnerCard {
        store.note = note
        return store.card(userName, lastPeriodStart, cycleLength, periodLength, revoked)
    }
    val unsent = remember(version, note, lastPeriodStart, cycleLength, periodLength, userName) {
        paired && store.hasUnsentChanges(store.card(userName, lastPeriodStart, cycleLength, periodLength, note = note))
    }

    fun startPairing() {
        val pairing = store.newPairing()
        val card = currentCard()
        store.markSent(card)
        version++
        screen = CompanionScreen.Qr(
            text = PartnerLink.pairingText(pairing, card),
            title = "QR اتصال",
            hint = "در گوشی همسرت «GentleMan» را باز کن، «اسکن QR» را بزن و این کد را اسکن کن. " +
                "این QR کلید رمز را دارد؛ فقط حضوری نشانش بده و از آن عکس نگیر یا برای کسی نفرست.",
            verificationCode = pairing.verificationCode()
        )
    }

    fun showUpdateQr() {
        val pairing = store.pairing ?: return
        val card = currentCard()
        store.markSent(card)
        version++
        screen = CompanionScreen.Qr(
            text = PartnerLink.updateText(pairing, card),
            title = "QR به‌روزرسانی",
            hint = "در «GentleMan» گزینه «دریافت به‌روزرسانی» و سپس «اسکن QR» را بزن.",
            verificationCode = null
        )
    }

    fun shareUpdate() {
        val pairing = store.pairing ?: return
        val card = currentCard()
        val text = PartnerLink.updateText(pairing, card)
        PartnerReminders.launch(context, PartnerReminders.shareIntent(updateMessage(text)))
        store.markSent(card)
        version++
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).widthIn(max = 560.dp).padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Rose.copy(alpha = 0.2f))
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            when (val s = screen) {
                                is CompanionScreen.Qr -> s.title
                                CompanionScreen.Overview -> "اپ همراه همسر 💙"
                            },
                            fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "«GentleMan»: یادآور و راهنمای روزانه برای همسرت",
                            fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "بستن") }
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (val s = screen) {
                        is CompanionScreen.Qr -> QrContent(s, onBack = { screen = CompanionScreen.Overview })
                        CompanionScreen.Overview -> {
                            PrivacyBox()

                            ToggleRow(
                                title = "نمایش روزهای باروری",
                                subtitle = "پیش‌فرض خاموش؛ فقط اگر بخواهی همسرت این روزها را می‌بیند.",
                                checked = shareFertile,
                                onChange = { store.shareFertile = it; version++ }
                            )
                            ToggleRow(
                                title = "توقف موقت اشتراک",
                                subtitle = "بعد از ارسال، همسرت هیچ اطلاعات چرخه‌ای نمی‌بیند تا دوباره روشنش کنی.",
                                checked = paused,
                                onChange = { store.paused = it; version++ }
                            )
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it.take(PartnerCard.MAX_NOTE_LENGTH) },
                                label = { Text("یادداشت کوتاه برای همسر (اختیاری)") },
                                placeholder = { Text("مثلاً: این هفته شکلات تلخ دوست دارم 🍫") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            if (!paired) {
                                Text(
                                    "۱. «GentleMan» را روی گوشی همسرت نصب کن.\n۲. دکمه زیر را بزن و QR را با گوشی او اسکن کن.",
                                    fontSize = 12.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { startPairing() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose)
                                ) {
                                    Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("ساخت QR اتصال", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    buildString {
                                        append("✅ متصل")
                                        if (lastSentAt > 0) {
                                            val day = Instant.ofEpochMilli(lastSentAt).atZone(ZoneId.systemDefault()).toLocalDate()
                                            append(" • آخرین ارسال: ").append(PersianDates.long(day))
                                        }
                                    },
                                    fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                                )
                                if (unsent) {
                                    Text(
                                        "🔔 اطلاعات یا تنظیمات تغییر کرده است؛ برای اینکه همسرت هم ببیند، به‌روزرسانی بفرست.",
                                        fontSize = 12.sp, lineHeight = 19.sp, color = Rose
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { showUpdateQr() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Rose)
                                    ) {
                                        Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("QR به‌روزرسانی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { shareUpdate() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("ارسال با پیام‌رسان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    "متن ارسالی رمزنگاری شده است و فقط «GentleMan» روی گوشی همسرت می‌تواند آن را باز کند.",
                                    fontSize = 11.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { confirmRepair = true }, modifier = Modifier.weight(1f)) {
                                        Text("اتصال دوباره", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { confirmDisconnect = true }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = Rose)
                                        Spacer(Modifier.width(4.dp))
                                        Text("قطع اتصال", fontSize = 12.sp, color = Rose)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmRepair) {
        AlertDialog(
            onDismissRequest = { confirmRepair = false },
            title = { Text("اتصال دوباره؟") },
            text = { Text("یک کلید تازه ساخته می‌شود و اتصال قبلی دیگر کار نمی‌کند. برای ادامه، QR جدید را با گوشی همسرت اسکن کن.") },
            confirmButton = { TextButton(onClick = { confirmRepair = false; startPairing() }) { Text("ساخت QR جدید") } },
            dismissButton = { TextButton(onClick = { confirmRepair = false }) { Text("انصراف") } }
        )
    }

    if (confirmDisconnect) {
        AlertDialog(
            onDismissRequest = { confirmDisconnect = false },
            title = { Text("قطع اتصال با GentleMan؟") },
            text = {
                Text(
                    "کلید اتصال از این گوشی پاک می‌شود و دیگر هیچ به‌روزرسانی‌ای ساخته نمی‌شود. " +
                        "یک پیام «قطع اتصال» هم آماده می‌شود؛ اگر آن را اسکن یا برای همسرت ارسال کنی، اطلاعات از گوشی او هم پاک می‌شود."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDisconnect = false
                    val pairing = store.pairing
                    val revokeText = pairing?.let { PartnerLink.updateText(it, currentCard(revoked = true)) }
                    store.clear()
                    note = ""
                    version++
                    if (revokeText != null) {
                        screen = CompanionScreen.Qr(
                            text = revokeText,
                            title = "QR قطع اتصال",
                            hint = "این کد را در «GentleMan» اسکن کن یا با دکمه زیر بفرست تا اطلاعات از گوشی همسرت هم پاک شود.",
                            verificationCode = null
                        )
                    }
                    Toast.makeText(context, "اتصال قطع شد.", Toast.LENGTH_SHORT).show()
                }) { Text("قطع اتصال", color = Rose) }
            },
            dismissButton = { TextButton(onClick = { confirmDisconnect = false }) { Text("انصراف") } }
        )
    }
}

/** The text sent through a messenger: a short explanation and the encrypted link. */
private fun updateMessage(updateText: String): String =
    "💌 به‌روزرسانی برای «GentleMan»\n" +
        "اگر لینک باز نشد، همین پیام را کپی کن و در GentleMan «جای‌گذاری» را بزن.\n" +
        PartnerLink.updateLink(updateText)

@Composable
private fun QrContent(screen: CompanionScreen.Qr, onBack: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(screen.text) { QrCodes.bitmap(screen.text).asImageBitmap() }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp)
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "کد QR برای اسکن با GentleMan",
                modifier = Modifier.size(260.dp),
                filterQuality = FilterQuality.None
            )
        }
    }
    screen.verificationCode?.let { code ->
        Text(
            "کد تأیید: ${PersianNumbers.toPersianDigits(code)}\nبعد از اسکن، همین کد باید روی گوشی همسرت هم دیده شود.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, color = Rose
        )
    }
    Text(screen.hint, fontSize = 12.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (screen.verificationCode == null) {
        OutlinedButton(
            onClick = { PartnerReminders.launch(context, PartnerReminders.shareIntent(updateMessage(screen.text))) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("ارسال با پیام‌رسان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
    Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Text("انجام شد")
    }
}

@Composable
private fun PrivacyBox() {
    Column(
        Modifier.fillMaxWidth()
            .background(Rose.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🔒 چه چیزی با همسرت به اشتراک گذاشته می‌شود؟", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        Text(
            "فقط تاریخ شروع آخرین پریود، میانگین طول چرخه و پریود، نام و یادداشت کوتاه تو. " +
                "علائم، حال روحی، یادداشت‌ها و سابقه هرگز فرستاده نمی‌شوند.\n" +
                "هیچ سروری در کار نیست: اطلاعات رمزنگاری‌شده فقط با QR یا پیام‌رسانی که خودت انتخاب می‌کنی منتقل می‌شود.",
            fontSize = 11.5.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
