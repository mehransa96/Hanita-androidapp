package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val VazirmatnFontFamily = FontFamily(
    Font(
        resId = R.font.vazirmatn_regular,
        weight = FontWeight.Normal,
        loadingStrategy = FontLoadingStrategy.OptionalLocal
    ),
    Font(
        resId = R.font.vazirmatn_bold,
        weight = FontWeight.Bold,
        loadingStrategy = FontLoadingStrategy.OptionalLocal
    )
)

val defaultTextStyle = TextStyle(
    fontFamily = VazirmatnFontFamily,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.5.sp
)

fun getTypographyForFont(fontCode: String): Typography {
    val family = when (fontCode) {
        "CURSIVE" -> FontFamily.Cursive
        "SERIF" -> FontFamily.Serif
        "SANS_SERIF" -> FontFamily.SansSerif
        else -> VazirmatnFontFamily
    }
    val style = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )
    return Typography(
        displayLarge = style.copy(fontSize = 32.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
        displayMedium = style.copy(fontSize = 28.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
        displaySmall = style.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
        headlineLarge = style.copy(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
        headlineMedium = style.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        headlineSmall = style.copy(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleLarge = style.copy(fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = 0.1.sp, fontWeight = FontWeight.Bold),
        titleMedium = style.copy(fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Bold),
        titleSmall = style.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp, fontWeight = FontWeight.Bold),
        bodyLarge = style.copy(fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp, fontWeight = FontWeight.Normal),
        bodyMedium = style.copy(fontSize = 13.sp, lineHeight = 21.sp, letterSpacing = 0.25.sp, fontWeight = FontWeight.Normal),
        bodySmall = style.copy(fontSize = 11.sp, lineHeight = 17.sp, letterSpacing = 0.4.sp, fontWeight = FontWeight.Light),
        labelLarge = style.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp, fontWeight = FontWeight.Bold),
        labelMedium = style.copy(fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp, fontWeight = FontWeight.Bold),
        labelSmall = style.copy(fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp, fontWeight = FontWeight.Bold)
    )
}

val Typography = getTypographyForFont("CLASSIC")
