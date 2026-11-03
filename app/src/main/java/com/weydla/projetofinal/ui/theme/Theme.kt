package com.weydla.projetofinal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de Cores Personalizada (Módulo 3 - Personalização + IHC Conforto Visual e Contraste WCAG)
val TerrorBlack = Color(0xFF0E0E12)
val TerrorSurface = Color(0xFF1A1A22)
val TerrorCard = Color(0xFF242430)
val CrimsonPrimary = Color(0xFFE53935)
val CrimsonDark = Color(0xFFB71C1C)
val AmberStar = Color(0xFFFFB300)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFFB0B0C0)
val WatchedGreen = Color(0xFF43A047)

private val CineTerrorColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = AmberStar,
    onSecondary = Color.Black,
    background = TerrorBlack,
    onBackground = TextPrimary,
    surface = TerrorSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerrorCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CineTerrorWeydlaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CineTerrorColorScheme,
        content = content
    )
}
