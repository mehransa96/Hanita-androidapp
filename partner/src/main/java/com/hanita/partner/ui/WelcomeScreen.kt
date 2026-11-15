package com.hanita.partner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanita.core.gentleman.Status

/** First run: a short introduction, an optional name and single or married. */
@Composable
internal fun WelcomeScreen(onDone: (name: String, status: Status, dailyTip: Boolean) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf<Status?>(null) }
    var dailyTip by rememberSaveable { mutableStateOf(true) }
    ScreenList(WindowInsets.safeDrawing.asPaddingValues()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(Modifier.height(16.dp))
                Text(
                    APP_NAME.uppercase(),
                    fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "هر روز، کمی جنتلمن‌تر.",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
                )
                MutedText("برنامه خودسازی مردانه بر پایه منابع علمی: عزت نفس، اعتمادبه‌نفس، منش و ارتباط، سلامت، کار و پول؛ با عادت‌ها، دفترچه روزانه، آزمون‌های خودشناسی و ابزارهای کاربردی. برای متاهل‌ها همسرداری و همراهی با همسر در روزهای حساس.")
            }
        }
        item {
            Panel {
                Label("اسمت (اختیاری)")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    placeholder = { Text("مثلاً علی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Panel {
                Label("وضعیتت")
                StatusOption(
                    Icons.Filled.Person, "مجرد",
                    "عزت نفس، منش، آشنایی محترمانه، انتخاب آگاهانه همسر و زندگی مستقل",
                    status == Status.SINGLE
                ) { status = Status.SINGLE }
                StatusOption(
                    Icons.Filled.Favorite, "متاهل",
                    "همسرداری، مدیریت اختلاف، پدری و اتصال اختیاری به Hanita همسرت",
                    status == Status.MARRIED
                ) { status = Status.MARRIED }
                MutedText("بعداً هم از تنظیمات قابل تغییر است.")
            }
        }
        item {
            Panel {
                SwitchRow("اصل روزانه را یادآوری کن", dailyTip, "یک اعلان کوتاه در روز") { dailyTip = it }
            }
        }
        item {
            Button(
                onClick = { status?.let { onDone(name.trim(), it, dailyTip) } },
                enabled = status != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("شروع کن", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
        item {
            MutedText("$APP_NAME به اینترنت وصل نمی‌شود و حساب کاربری ندارد. همه چیز فقط روی همین گوشی می‌ماند.")
        }
    }
}

@Composable
private fun StatusOption(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}
