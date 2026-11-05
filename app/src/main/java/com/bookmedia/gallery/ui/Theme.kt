package com.bookmedia.gallery.ui

import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BackgroundLight = Color(0xFFFBF8F2)
val SurfaceLight = Color(0xFFFFFDF9)
val SurfaceElevated = Color(0xFFF2E9DA)
val BorderSubtle = Color(0xFFC9BBAA)
val TextPrimary = Color(0xFF2A201C)
val TextSecondary = Color(0xFF6E6057)
val Accent = Color(0xFF7A1F2B)

val GalleryColorScheme = lightColorScheme(
    primary = Accent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceElevated,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderSubtle
)

val GalleryTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = TextSecondary
    )
)
