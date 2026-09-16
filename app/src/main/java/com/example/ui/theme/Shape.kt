package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Theme-aware semantic colors — brighter variants are used automatically in dark mode. */
@Composable
fun incomeColor(): Color = if (isSystemInDarkTheme()) IncomeColorDark else IncomeColor

@Composable
fun expenseColor(): Color = if (isSystemInDarkTheme()) ExpenseColorDark else ExpenseColor

// A single, consistent corner-radius scale used app-wide instead of
// hand-picked RoundedCornerShape(16.dp) / (24.dp) values scattered per screen.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Extra radii for components outside the default M3 scale (hero cards, sheets)
object AppRadius {
    val Hero = RoundedCornerShape(24.dp)
    val Chip = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(50)
    val SheetTop = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
