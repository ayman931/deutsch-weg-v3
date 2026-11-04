package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Espresso & Linen Design System Colors
val WarmBackground = Color(0xFFFBF9F5)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceElevated = Color(0xFFF3EFEA)
val SurfaceContainerLow = Color(0xFFF7F3ED)
val SurfaceContainer = Color(0xFFF1EDE7)
val SurfaceContainerHigh = Color(0xFFEBE8E2)
val SurfaceContainerHighest = Color(0xFFE6DFD5)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceBright = Color(0xFFFDF9F3)

val BorderSubtle = Color(0xFFE6DFD5)
val BorderActive = Color(0xFFD1C4BA)

val PrimaryEspresso = Color(0xFF291C0E)
val PrimaryContainerWarm = Color(0xFF291C0E)
val OnPrimaryWarm = Color(0xFFFDF9F3)
val OnPrimaryContainerWarm = Color(0xFFFDF9F3)

val TextPrimary = Color(0xFF241B14)
val TextSecondary = Color(0xFF6E6156)
val TextMuted = Color(0xFF8B7E74)

val AccentAmber = Color(0xFFD97706)
val AccentAmberSubtle = Color(0xFFFEF3C7)
val AccentSand = Color(0xFF291C0E)

val FeedbackCorrect = Color(0xFF0F9D8A)
val FeedbackCorrectSubtle = Color(0xFFE6F4F1)
val FeedbackIncorrect = Color(0xFFD63346)
val FeedbackIncorrectSubtle = Color(0xFFFDE8EA)

// Grammatical Article Colors
val ArticleDer = Color(0xFF1D65D4) // Blue
val ArticleDerSubtle = Color(0xFFE8F0FE)

val ArticleDie = Color(0xFFD63346) // Red
val ArticleDieSubtle = Color(0xFFFDE8EA)

val ArticleDas = Color(0xFF1B806A) // Teal
val ArticleDasSubtle = Color(0xFFE6F4F1)

// Legacy aliases for backward compatibility if referenced
val PrimaryMint = PrimaryEspresso
val PrimaryContainer = PrimaryContainerWarm
val OnPrimary = OnPrimaryWarm
val OnPrimaryContainer = OnPrimaryContainerWarm
val SecondaryCyan = ArticleDer
val SecondaryContainer = SurfaceContainerHigh
val OnSecondary = TextPrimary
val TertiaryRose = ArticleDie
val TertiaryContainer = AccentAmberSubtle
val OnTertiaryContainer = AccentAmber
val ErrorRed = FeedbackIncorrect
val ErrorContainer = FeedbackIncorrectSubtle
val OnErrorContainer = FeedbackIncorrect
val SurfaceDark = WarmBackground
val BackgroundDark = WarmBackground
val OnSurfaceDark = TextPrimary
val OnSurfaceVariant = TextSecondary
val OutlineColor = BorderActive
val OutlineVariant = BorderSubtle
