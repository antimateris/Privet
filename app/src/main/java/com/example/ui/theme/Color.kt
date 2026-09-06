package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// "Steam & Shine" palette — grounded in the subject: a motorcycle wash & cash
// register app. Petrol-teal (water, steam, trust) pairs with citrus lime
// (freshly-cleaned shine, positive/active states) instead of a generic
// blue-on-slate SaaS look. Neutrals carry a faint warm-teal tint rather than
// cold grey-blue, so the whole palette reads as one deliberate family.
// ============================================================================

// --- Neutrals (warm paper background, teal-tinted ink) ---
val PaperBackground = Color(0xFFFAF8F3)     // Warm soft paper, easy on the eyes, not stark white
val CardSurface = Color(0xFFFFFFFF)         // Crisp white cards popping against the warm paper
val SubtleSurfaceVariant = Color(0xFFF0EEE6) // Warm neutral tint for chips & inner containers

val InkOnSurface = Color(0xFF162321)        // Deep teal-black — cohesive with the primary hue
val MutedOnVariant = Color(0xFF5B6B67)      // Muted teal-grey for captions & subtitles
val SoftBorderOutline = Color(0xFFE6E1D6)   // Warm hairline border for cards
val SoftDivider = Color(0xFFEFEBE1)

// --- Brand: Petrol (primary) ---
val PetrolPrimary = Color(0xFF0E4F4A)
val PetrolPrimaryContainer = Color(0xFFD3EDE7)
val OnPetrolPrimaryContainer = Color(0xFF07332F)

// --- Brand: Citrus Lime (secondary — shine, positive/active energy) ---
val CitrusSecondary = Color(0xFF5C8A02)
val CitrusSecondaryContainer = Color(0xFFE3F5C4)
val OnCitrusSecondaryContainer = Color(0xFF2F4A00)

// --- Brand: Amber (tertiary — money, bagi hasil) ---
val AmberTertiary = Color(0xFFB4590A)
val AmberTertiaryContainer = Color(0xFFFEF0DB)
val OnAmberTertiaryContainer = Color(0xFF7A3B00)

// --- Status colors ---
val SuccessGreen = Color(0xFF3F7D20)
val SuccessGreenContainer = Color(0xFFE3F5C4)
val OnSuccessGreen = Color(0xFF1F3D0F)

val MoneyBlue = PetrolPrimary
val MoneyBlueContainer = PetrolPrimaryContainer

val AmberShare = AmberTertiary
val AmberShareContainer = AmberTertiaryContainer

val ErrorRed = Color(0xFFBA1B1B)
val ErrorRedContainer = Color(0xFFFFDAD4)

// --- Dark theme variants (kept in step with the light identity) ---
val PetrolPrimaryDark = Color(0xFF8BD3C8)
val CitrusSecondaryDark = Color(0xFFC1E88B)
val AmberTertiaryDark = Color(0xFFFFB77C)
val DarkBackground = Color(0xFF0E1513)
val DarkSurface = Color(0xFF162321)
val DarkSurfaceVariant = Color(0xFF2A3634)
