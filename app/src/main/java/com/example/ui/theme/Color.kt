package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Brand palette
// A premium "ledger" identity: deep forest green (trust, money, growth)
// paired with a warm bronze/gold accent (premium, currency, achievement).
// Every Material3 color role is defined explicitly below so no screen ever
// silently falls back to Compose's default purple.
// ---------------------------------------------------------------------------

// ---- Light scheme ----
val GreenPrimary = Color(0xFF0B6E4F)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFB5F1D1)
val OnPrimaryContainerLight = Color(0xFF002114)

val GoldSecondary = Color(0xFF8A6D1E)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFF6E3B4)
val OnSecondaryContainerLight = Color(0xFF2A1F00)

val GreenTertiary = Color(0xFF2B627B)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC6E7FF)
val OnTertiaryContainerLight = Color(0xFF001E2E)

val BackgroundLight = Color(0xFFF6FBF5)
val OnBackgroundLight = Color(0xFF181D19)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF181D19)
val SurfaceVariantLight = Color(0xFFE3EBE3)
val OnSurfaceVariantLight = Color(0xFF454943)
val SurfaceContainerLowLight = Color(0xFFF0F5EE)
val SurfaceContainerLight = Color(0xFFEAF1E8)
val SurfaceContainerHighLight = Color(0xFFE4EBE2)

val OutlineLight = Color(0xFF757971)
val OutlineVariantLight = Color(0xFFC4C9BF)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// ---- Dark scheme ----
val GreenPrimaryDark = Color(0xFF6FDBA6)
val OnPrimaryDark = Color(0xFF00391F)
val PrimaryContainerDark = Color(0xFF00522F)
val OnPrimaryContainerDark = Color(0xFF8FF8C4)

val GoldSecondaryDark = Color(0xFFDCC382)
val OnSecondaryDark = Color(0xFF3C2E00)
val SecondaryContainerDark = Color(0xFF574300)
val OnSecondaryContainerDark = Color(0xFFF6E3B4)

val GreenTertiaryDark = Color(0xFFA0CDE8)
val OnTertiaryDark = Color(0xFF00344A)
val TertiaryContainerDark = Color(0xFF124A69)
val OnTertiaryContainerDark = Color(0xFFC6E7FF)

val BackgroundDark = Color(0xFF10140F)
val OnBackgroundDark = Color(0xFFE1E3DD)
val SurfaceDark = Color(0xFF10140F)
val OnSurfaceDark = Color(0xFFE1E3DD)
val SurfaceVariantDark = Color(0xFF414942)
val OnSurfaceVariantDark = Color(0xFFC4C9BF)
val SurfaceContainerLowDark = Color(0xFF181D18)
val SurfaceContainerDark = Color(0xFF1C211C)
val SurfaceContainerHighDark = Color(0xFF272C26)

val OutlineDark = Color(0xFF8E9389)
val OutlineVariantDark = Color(0xFF414942)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ---------------------------------------------------------------------------
// Semantic / financial colors — used consistently for money direction and
// status across every screen (cards, charts, chips, buttons).
// ---------------------------------------------------------------------------
val IncomeColor = Color(0xFF10A567) // Emerald — money in
val IncomeColorDark = Color(0xFF6FE3AB)
val ExpenseColor = Color(0xFFDC4B4B) // Coral red — money out
val ExpenseColorDark = Color(0xFFFF8B8B)
val WarningColor = Color(0xFFC98A15) // Amber
val InfoColor = Color(0xFF2F79C9) // Info blue

// Brand accent used for premium highlights (avatar rings, hero card, badges)
val AccentGold = Color(0xFFD4AF37)

// Hero / balance-card gradient (kept as named tokens instead of scattered hex)
val HeroGradientStart = Color(0xFF0E4A32)
val HeroGradientEnd = Color(0xFF07281A)

// Accent colors tuned for legibility on top of the dark hero gradient card
// (brighter/lighter than the standard semantic colors, which are tuned for
// light surfaces).
val HeroPositiveBalance = Color(0xFF80E8B0)
val HeroNegativeBalance = Color(0xFFFF9E9E)
val HeroIncomeAccent = Color(0xFF69F0AE)
val HeroIncomeBadgeBg = Color(0xFF2E7D32)
val HeroExpenseAccent = Color(0xFFFF8A80)
val HeroExpenseBadgeBg = Color(0xFFC62828)
