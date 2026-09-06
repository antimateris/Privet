package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Two-family pairing: a bold geometric sans carries headlines and the big rupiah
// figures (this is fundamentally a cash-ledger app, so numbers should feel
// substantial and confident), while the familiar Roboto default handles dense
// body copy and captions for easy reading at small sizes.
private val HeadlineFamily = FontFamily.SansSerif
private val BodyFamily = FontFamily.Default

val Typography =
  Typography(
    displaySmall = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 34.sp,
      lineHeight = 40.sp,
      letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 28.sp,
      lineHeight = 34.sp,
      letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      lineHeight = 30.sp,
      letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
      fontFamily = HeadlineFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.4.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
      fontFamily = BodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      letterSpacing = 0.2.sp
    )
  )
