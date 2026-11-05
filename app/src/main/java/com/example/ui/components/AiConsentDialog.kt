package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.viewmodel.MahitaViewModel

/**
 * Explicit, informed consent before any cycle data is sent to the AI service.
 * Shown once; the choice can be changed later.
 */
@Composable
fun AiConsentDialog(viewModel: MahitaViewModel) {
    val visible by viewModel.aiConsentRequest.collectAsState()
    if (!visible) return
    AlertDialog(
        onDismissRequest = { viewModel.answerAiConsent(false) },
        title = { Text("اجازه ارسال اطلاعات برای تحلیل هوشمند", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "برای این تحلیل، تاریخ‌های پریود، علائم و حالات ثبت‌شده شما (بدون نام، ایمیل و یادداشت‌های شخصی) " +
                    "به‌صورت رمزنگاری‌شده برای سرویس هوش مصنوعی Google Gemini ارسال می‌شود.\n\n" +
                    "این تحلیل جنبه آموزشی دارد و جایگزین مشاوره پزشک نیست. هر زمان بخواهید می‌توانید این اجازه را لغو کنید.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = { viewModel.answerAiConsent(true) }) { Text("موافقم") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.answerAiConsent(false) }) { Text("خیر") }
        }
    )
}
