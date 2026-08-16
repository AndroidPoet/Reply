package com.androidpoet.reply.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

@Immutable
data class ReplyShapes(
    val small: CornerBasedShape = RoundedCornerShape(24.dp),
    val medium: CornerBasedShape = RoundedCornerShape(0.dp),
    val large: CornerBasedShape = RoundedCornerShape(12.dp),
) {
    val smallCornerRadius: Dp get() = 24.dp
    val mediumCornerRadius: Dp get() = 0.dp
    val largeCornerRadius: Dp get() = 12.dp
}

object ReplyDimens {
    val grid0_25 = 2.dp
    val grid0_5 = 4.dp
    val grid1 = 8.dp
    val grid2 = 16.dp
    val grid3 = 24.dp
    val grid4 = 32.dp

    val minTouchTarget = 48.dp
    val minIconTargetPadding = 12.dp
    val bottomAppBarHeight = 56.dp
    val bottomAppBarFabCradleCornerRadius = 32.dp
    val bottomAppBarFabCradleMargin = 8.dp
    val bottomAppBarLogoSize = 32.dp
    val fabSize = 56.dp
    val navigationDrawerProfileImageSize = 48.dp
    val navigationDrawerProfileImageSizePadded = 32.dp
    val navigationDrawerMenuItemHeight = 56.dp
    val emailSenderProfileImageSize = 42.dp
    val chipIconDiameter = 32.dp

    val plane00 = 0.dp
    val plane01 = 1.dp
    val plane02 = 2.dp
    val plane06 = 6.dp
    val plane08 = 8.dp
    val plane16 = 16.dp
}

val LocalReplyShapes = staticCompositionLocalOf { ReplyShapes() }

object ReplyTheme {
    val colors: ReplyColors
        @Composable @ReadOnlyComposable get() = LocalReplyColors.current
    val typography: ReplyTypography
        @Composable @ReadOnlyComposable get() = LocalReplyTypography.current
    val shapes: ReplyShapes
        @Composable @ReadOnlyComposable get() = LocalReplyShapes.current
}

@Composable
@ReadOnlyComposable
fun ReplyColors.elevated(surface: Color, elevation: Dp): Color {
    if (!isDark || elevation <= 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return onSurface.copy(alpha = alpha).compositeOverColor(surface)
}

private fun Color.compositeOverColor(background: Color): Color {
    val a = alpha
    return Color(
        red = red * a + background.red * (1 - a),
        green = green * a + background.green * (1 - a),
        blue = blue * a + background.blue * (1 - a),
        alpha = 1f,
    )
}

private fun ReplyColors.toMaterialColorScheme(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerLow = surface,
        error = error,
        onError = onError,
        outline = onSurface.copy(alpha = 0.12f),
    )
} else {
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerLow = surface,
        error = error,
        onError = onError,
        outline = onSurface.copy(alpha = 0.12f),
    )
}

@Composable
fun ReplyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkReplyColors else LightReplyColors
    val family = workSansFamily()
    val typography = remember(family) { replyTypography(family) }
    val shapes = ReplyShapes()
    val materialScheme = remember(colors) { colors.toMaterialColorScheme() }

    CompositionLocalProvider(
        LocalReplyColors provides colors,
        LocalReplyTypography provides typography,
        LocalReplyShapes provides shapes,
        LocalContentColor provides colors.onSurfaceHigh,
        LocalTextStyle provides typography.body1,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            shapes = Shapes(
                extraSmall = shapes.small,
                small = shapes.small,
                medium = shapes.medium,
                large = shapes.large,
                extraLarge = shapes.large,
            ),
            content = content,
        )
    }
}
