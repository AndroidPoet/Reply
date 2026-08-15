package com.androidpoet.reply.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Raw Reply palette — mirrors `res/values/color.xml` of the Material Study. */
object ReplyPalette {
    val White50 = Color(0xFFFFFFFF)
    val Black800 = Color(0xFF121212)
    val Black900 = Color(0xFF000000)
    val Blue50 = Color(0xFFEEF0F2)
    val Blue100 = Color(0xFFD2DBE0)
    val Blue200 = Color(0xFFADBBC4)
    val Blue300 = Color(0xFF8CA2AE)
    val Blue600 = Color(0xFF4A6572)
    val Blue700 = Color(0xFF344955)
    val Blue800 = Color(0xFF232F34)
    val Orange300 = Color(0xFFFBD790)
    val Orange400 = Color(0xFFF9BE64)
    val Orange500 = Color(0xFFF9AA33)
    val Red200 = Color(0xFFCF7779)
    val Red400 = Color(0xFFFF4C5D)
    val White50Alpha060 = Color(0x99FFFFFF)
    val Blue50Alpha060 = Color(0x99EEF0F2)
    val Black900Alpha020 = Color(0x33000000)
    val Black900Alpha087 = Color(0xDE000000)
    val Black900Alpha060 = Color(0x99000000)
}

/** Text/icon emphasis alphas from `Theme.Reply`. */
object Emphasis {
    const val HIGH = 0.87f
    const val MEDIUM = 0.60f
    const val DISABLED = 0.38f
    const val DIVIDER = 0.10f
}

/**
 * The Material 2 style colour roles used by Reply. Compose Material 3 has no
 * primaryVariant / primarySurface roles, so the theme carries its own scheme and maps a subset
 * onto [androidx.compose.material3.ColorScheme] for the stock components it still uses.
 */
@Immutable
data class ReplyColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    /** `colorPrimarySurface`: primary in light, surface in dark. Bottom app bar + drawer. */
    val primarySurface: Color,
    /** `colorPrimarySurfaceVariant`: primaryVariant in light, surface in dark. Drawer backdrop. */
    val primarySurfaceVariant: Color,
    /** `colorOnPrimarySurface`. */
    val onPrimarySurface: Color,
    val scrim: Color,
    val statusBar: Color,
    val isDark: Boolean,
) {
    val onSurfaceHigh: Color get() = onSurface.copy(alpha = Emphasis.HIGH)
    val onSurfaceMedium: Color get() = onSurface.copy(alpha = Emphasis.MEDIUM)
    val onSurfaceDisabled: Color get() = onSurface.copy(alpha = Emphasis.DISABLED)
    val onSurfaceDivider: Color get() = onSurface.copy(alpha = Emphasis.DIVIDER)
    val onSurfaceStroke: Color get() = onSurface.copy(alpha = 0.12f)

    val onPrimarySurfaceHigh: Color get() = onPrimarySurface.copy(alpha = Emphasis.HIGH)
    val onPrimarySurfaceMedium: Color get() = onPrimarySurface.copy(alpha = Emphasis.MEDIUM)
    val onPrimarySurfaceDisabled: Color get() = onPrimarySurface.copy(alpha = Emphasis.DISABLED)
    val onPrimarySurfaceDivider: Color get() = onPrimarySurface.copy(alpha = Emphasis.DIVIDER)
}

val LightReplyColors = ReplyColors(
    primary = ReplyPalette.Blue700,
    primaryVariant = ReplyPalette.Blue800,
    secondary = ReplyPalette.Orange500,
    secondaryVariant = ReplyPalette.Orange400,
    background = ReplyPalette.Blue50,
    surface = ReplyPalette.White50,
    error = ReplyPalette.Red400,
    onPrimary = ReplyPalette.White50,
    onSecondary = ReplyPalette.Black900,
    onBackground = ReplyPalette.Black900,
    onSurface = ReplyPalette.Black900,
    onError = ReplyPalette.Black900,
    primarySurface = ReplyPalette.Blue700,
    primarySurfaceVariant = ReplyPalette.Blue800,
    onPrimarySurface = ReplyPalette.White50,
    scrim = ReplyPalette.White50Alpha060,
    statusBar = ReplyPalette.Blue50Alpha060,
    isDark = false,
)

val DarkReplyColors = ReplyColors(
    primary = ReplyPalette.Blue200,
    primaryVariant = ReplyPalette.Blue300,
    secondary = ReplyPalette.Orange300,
    secondaryVariant = ReplyPalette.Orange300,
    background = ReplyPalette.Black900,
    surface = ReplyPalette.Black800,
    error = ReplyPalette.Red200,
    onPrimary = ReplyPalette.Black900,
    onSecondary = ReplyPalette.Black900,
    onBackground = ReplyPalette.White50,
    onSurface = ReplyPalette.White50,
    onError = ReplyPalette.Black900,
    primarySurface = ReplyPalette.Black800,
    primarySurfaceVariant = ReplyPalette.Black800,
    onPrimarySurface = ReplyPalette.White50,
    scrim = ReplyPalette.Black900Alpha087,
    statusBar = ReplyPalette.Black900Alpha060,
    isDark = true,
)

val LocalReplyColors = staticCompositionLocalOf { LightReplyColors }
