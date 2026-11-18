package com.hanita.partner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.MensHealth
import com.hanita.core.gentleman.Sources

/** Reads a whole or decimal number typed with Persian or Latin digits ("۷۵٫۵", "75.5"). */
internal fun parseNumber(text: String): Double? =
    PersianNumbers.normalize(text).replace('٫', '.').replace('/', '.').replace(',', '.')
        .filter { it.isDigit() || it == '.' }.toDoubleOrNull()

private fun formatDecimal(v: Double, digits: Int): String =
    PersianNumbers.toPersianDigits(String.format(java.util.Locale.US, "%.${digits}f", v)).replace('.', '٫')

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, suffix: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c in ".٫/," }.take(6)) },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Which screenings fit the user's age and risk, after the USPSTF A and B recommendations. */
@Composable
internal fun HealthCheckScreen(padding: PaddingValues, actions: AppActions) {
    var ageText by rememberSaveable { mutableStateOf("") }
    var smoker by rememberSaveable { mutableStateOf(false) }
    var overweight by rememberSaveable { mutableStateOf(false) }
    val age = parseNumber(ageText)?.toInt()
    val valid = age != null && age in MensHealth.MIN_AGE..MensHealth.MAX_AGE
    val list = if (valid) MensHealth.screeningsFor(age!!, smoker, overweight) else emptyList()

    ScreenList(padding) {
        item { Wordmark("چکاپ مردانه", onBack = actions.back) }
        item { MutedText("سنت را وارد کن تا ببینی کدام آزمایش‌ها و غربالگری‌ها برای تو توصیه می‌شوند. اطلاعاتت جایی ذخیره نمی‌شود.") }
        item(key = "form") {
            Panel {
                NumberField(ageText, { ageText = it }, "سن", "سال")
                if (ageText.isNotBlank() && !valid) {
                    Text(
                        "سن را بین ${PersianNumbers.toPersianDigits(MensHealth.MIN_AGE)} و ${PersianNumbers.toPersianDigits(MensHealth.MAX_AGE)} وارد کن.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.error
                    )
                }
                SwitchRow("سیگاری هستم یا کمتر از ۱۵ سال است ترک کرده‌ام", smoker, "با سابقه طولانی مصرف") { smoker = it }
                SwitchRow("اضافه‌وزن دارم (BMI ۲۵ یا بیشتر)", overweight, "از ابزار «BMI و دور کمر» می‌توانی حساب کنی") { overweight = it }
            }
        }
        if (valid) {
            item(key = "list") {
                Panel {
                    Label("برای تو در ${PersianNumbers.toPersianDigits(age!!)} سالگی")
                    list.forEachIndexed { i, s ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(s.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                (if (s.discuss) "تصمیم با پزشک · " else "") + s.schedule,
                                fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary
                            )
                            Text(s.why, fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (s.how.isNotBlank()) {
                                Text("چطور: " + s.how, fontSize = 12.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            item(key = "always") {
                Panel {
                    Label("برای همه سن‌ها")
                    Bullet("دندانپزشکی هر ۶ تا ۱۲ ماه.")
                    Bullet("اگر توده تازه، درد قفسه سینه، تنگی نفس یا خون در ادرار و مدفوع دیدی، منتظر نوبت چکاپ نمان.")
                    Bullet("واکسن‌ها را با پزشک مرور کن (مثلاً یادآور کزاز هر ۱۰ سال).")
                    Bullet("سابقه خانوادگی (سکته یا حمله قلبی زودرس، سرطان روده یا پروستات در پدر یا برادر، دیابت) را به پزشک بگو؛ ممکن است غربالگری را زودتر یا منظم‌تر کند.")
                    Bullet("فهرست داروها و مکمل‌هایت را همراه ببر و نتیجه آزمایش‌های قبلی را نگه دار تا روند دیده شود.")
                }
            }
        }
        item { MutedText(MensHealth.DISCLAIMER) }
        item { SourcesPanel(listOf(Sources.get("uspstf"), Sources.get("who-bp"))) }
    }
}

/** BMI (WHO classes) and waist-to-height ratio (NICE), with the healthy weight range. */
@Composable
internal fun BodyScreen(padding: PaddingValues, actions: AppActions) {
    var heightText by rememberSaveable { mutableStateOf("") }
    var weightText by rememberSaveable { mutableStateOf("") }
    var waistText by rememberSaveable { mutableStateOf("") }
    val height = parseNumber(heightText)
    val weight = parseNumber(weightText)
    val waist = parseNumber(waistText)
    val result = if (height != null && weight != null) MensHealth.body(weight, height, waist) else null

    ScreenList(padding) {
        item { Wordmark("BMI و دور کمر", onBack = actions.back) }
        item {
            MutedText("شاخص توده بدنی (BMI) تصویر کلی وزن است و نسبت دور کمر به قد، چربی شکمی را بهتر نشان می‌دهد. دور کمر را در وسط فاصله پایین‌ترین دنده و بالای لگن، پس از یک بازدم معمولی اندازه بگیر.")
        }
        item(key = "form") {
            Panel {
                NumberField(heightText, { heightText = it }, "قد", "سانتی‌متر")
                NumberField(weightText, { weightText = it }, "وزن", "کیلوگرم")
                NumberField(waistText, { waistText = it }, "دور کمر (اختیاری)", "سانتی‌متر")
            }
        }
        if (result != null) {
            item(key = "bmi") {
                Panel {
                    Label("شاخص توده بدنی")
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatDecimal(result.bmi, 1), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("  ${result.bmiClass.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Meter((((result.bmi - 15.0) / 25.0) * 100).toInt().coerceIn(0, 100))
                    Text(result.bmiClass.advice, fontSize = 14.sp, lineHeight = 23.sp)
                    val (low, high) = MensHealth.healthyWeight(height!!)
                    MutedText("وزن سالم برای قد تو: ${PersianNumbers.toPersianDigits(low)} تا ${PersianNumbers.toPersianDigits(high)} کیلوگرم. برای ورزشکاران عضلانی، BMI ممکن است وزن را بیش از واقع نشان دهد.")
                    when {
                        result.kgToNormal > 0 -> {
                            val first = (weight!! * 0.05).toInt().coerceAtLeast(1)
                            Text(
                                "تا مرز وزن طبیعی حدود ${PersianNumbers.toPersianDigits(result.kgToNormal)} کیلوگرم فاصله داری. هدف اول واقع‌بینانه: ${PersianNumbers.toPersianDigits(first)} کیلوگرم (۵٪ وزن) در ۳ تا ۶ ماه؛ همین مقدار هم فشار خون و قند را بهتر می‌کند.",
                                fontSize = 13.5.sp, lineHeight = 22.sp
                            )
                        }
                        result.kgToNormal < 0 -> Text(
                            "تا مرز وزن طبیعی حدود ${PersianNumbers.toPersianDigits(-result.kgToNormal)} کیلوگرم کم داری.",
                            fontSize = 13.5.sp, lineHeight = 22.sp
                        )
                    }
                }
            }
            val ratio = result.waistRatio
            val risk = result.waistRisk
            if (ratio != null && risk != null) {
                item(key = "waist") {
                    Panel {
                        Label("نسبت دور کمر به قد")
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(formatDecimal(ratio, 2), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("  ${risk.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Text(risk.advice, fontSize = 14.sp, lineHeight = 23.sp)
                        MutedText("هدف ساده: دور کمرت کمتر از نصف قدت باشد.")
                        result.waistSize?.let { size ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text("دور کمر: ${size.title}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(size.advice, fontSize = 13.5.sp, lineHeight = 22.sp)
                        }
                    }
                }
            }
        } else if (heightText.isNotBlank() && weightText.isNotBlank()) {
            item {
                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium).padding(12.dp)) {
                    Text("عددها را بررسی کن: قد بین ۱۲۰ و ۲۳۰ سانتی‌متر، وزن بین ۳۰ و ۳۰۰ کیلوگرم.", fontSize = 13.sp)
                }
            }
        }
        item { SourcesPanel(listOf(Sources.get("who-bmi"), Sources.get("nice-whtr"), Sources.get("who-waist"))) }
    }
}
