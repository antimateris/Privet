package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// iOS 18 "Cupertino Modern" Theme Palette
// Inspired by the latest Apple iPhone iOS design language:
// Signature vibrant iOS system tints, soft grouped background (#F2F2F7),
// clean white squircle surfaces with subtle borders, and high-contrast typography.
// ============================================================================

// --- iOS Neutrals (Light: Grouped Background & Elevated Card Surfaces) ---
val AppleGroupedBackground = Color(0xFFF2F2F7)    // iOS 18 System Grouped Background
val PaperBackground = AppleGroupedBackground
val AppleCardSurface = Color(0xFFFFFFFF)          // Crisp white iOS card surface
val CardSurface = AppleCardSurface
val AppleSubtleFill = Color(0xFFE5E5EA)           // iOS secondary fill for chips & inner containers
val SubtleSurfaceVariant = AppleSubtleFill

val AppleLabel = Color(0xFF1C1C1E)                // iOS primary label
val InkOnSurface = AppleLabel
val AppleSecondaryLabel = Color(0xFF8E8E93)       // iOS secondary label
val MutedOnVariant = AppleSecondaryLabel
val AppleHairlineBorder = Color(0xFFE5E5EA)       // iOS delicate hairline separator
val SoftBorderOutline = AppleHairlineBorder
val SoftDivider = Color(0xFFE5E5EA)

// --- Apple iOS System Accents ---
// iOS System Blue (Signature Apple Primary Accent)
val AppleSystemBlue = Color(0xFF007AFF)
val PetrolPrimary = AppleSystemBlue
val PetrolPrimaryContainer = Color(0xFFE8F2FF)
val OnPetrolPrimaryContainer = Color(0xFF004085)

// iOS System Cyan / Mint (Fresh Steam & Clean Motorcycles)
val AppleSystemCyan = Color(0xFF00C7BE)
val CitrusSecondary = AppleSystemCyan
val CitrusSecondaryContainer = Color(0xFFE0F9F8)
val OnCitrusSecondaryContainer = Color(0xFF004D4A)

// iOS System Orange / Amber (Finances & Bagi Hasil)
val AppleSystemOrange = Color(0xFFFF9500)
val AmberTertiary = AppleSystemOrange
val AmberTertiaryContainer = Color(0xFFFFF4E5)
val OnAmberTertiaryContainer = Color(0xFF804B00)

// --- iOS System Status Colors ---
val AppleSystemGreen = Color(0xFF34C759)
val SuccessGreen = AppleSystemGreen
val SuccessGreenContainer = Color(0xFFE8F8ED)
val OnSuccessGreen = Color(0xFF1B6B32)

val MoneyBlue = AppleSystemBlue
val MoneyBlueContainer = PetrolPrimaryContainer

val AmberShare = AppleSystemOrange
val AmberShareContainer = AmberTertiaryContainer

val AppleSystemRed = Color(0xFFFF3B30)
val ErrorRed = AppleSystemRed
val ErrorRedContainer = Color(0xFFFFEBEA)

// --- iOS 18 Dark Theme / OLED System Colors ---
val AppleDarkOledBackground = Color(0xFF000000)     // iOS pure OLED Black
val AppleDarkCardSurface = Color(0xFF1C1C1E)        // iOS secondary grouped background
val AppleDarkSurfaceVariant = Color(0xFF2C2C2E)     // iOS tertiary system fill
val AppleDarkBlue = Color(0xFF0A84FF)               // iOS 18 Dark Blue
val AppleDarkCyan = Color(0xFF64D2FF)               // iOS 18 Dark Cyan
val AppleDarkOrange = Color(0xFFFF9F0A)             // iOS 18 Dark Orange

val PetrolPrimaryDark = AppleDarkBlue
val CitrusSecondaryDark = AppleDarkCyan
val AmberTertiaryDark = AppleDarkOrange
val DarkBackground = AppleDarkOledBackground
val DarkSurface = AppleDarkCardSurface
val DarkSurfaceVariant = AppleDarkSurfaceVariant
