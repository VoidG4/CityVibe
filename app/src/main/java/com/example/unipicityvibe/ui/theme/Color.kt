package com.example.unipicityvibe.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Card Backgrounds
val CardDark = Color(0xFF020617)   // main bg (ήδη έχεις)
val CardLight = Color(0xFF0F172A)  // λίγο πιο ανοιχτό για cards

// Text on Cards
val CardTitle = Color(0xFFFFFFFF)
val CardText = Color(0xFFCBD5E1)

// Light mode cards (αν θες)
val CardLightBg = Color(0xFFFFFFFF)
val CardLightTitle = Color(0xFF020617)
val CardLightText = Color(0xFF475569)


// --- ΤΑ ΧΡΩΜΑΤΑ ΤΗΣ ΕΦΑΡΜΟΓΗΣ ΜΑΣ (UPDATED) ---
object ShadcnColors {
    // ΑΛΛΑΓΗ: Έγινε σχεδόν μαύρο (Pure Dark)
    val Primary = Color(0xFF020617)

    // ΑΛΛΑΓΗ: Έγινε πολύ πιο σκούρο γκρι για να διαβάζεται εύκολα
    val MutedForeground = Color(0xFF475569)

    val Brand = Color(0xFF4F46E5)
    val Border = Color(0xFFE2E8F0)

    val BrandGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4F46E5), // Indigo
            Color(0xFF9333EA)  // Purple
        )
    )
}