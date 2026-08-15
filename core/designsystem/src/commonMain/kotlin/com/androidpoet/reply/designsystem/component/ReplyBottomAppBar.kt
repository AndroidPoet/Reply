package com.androidpoet.reply.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.designsystem.shape.CutoutTopEdgeShape
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyMotion
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated

/** Total height the bar assembly reserves above the bottom edge (bar + FAB overhang). */
val BottomAppBarFabOverhang: Dp = ReplyDimens.fabSize / 2

/**
 * `Widget.MaterialComponents.BottomAppBar.PrimarySurface` with a centred FAB cradle
 * (`fabCradleMargin` 8dp, `fabCradleRoundedCornerRadius` 32dp) — the Reply bottom bar.
 *
 * [navigation] is placed at the start (the chevron / logo / title cluster) and [actions] at the end.
 * The bar itself is 56dp tall plus the navigation-bar inset; call sites layer [ReplyFab] over it
 * with its centre on the bar's top edge (see [BottomAppBarWithFab]).
 */
@Composable
fun ReplyBottomAppBar(
    modifier: Modifier = Modifier,
    fabCradle: Boolean = true,
    navigation: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    val colors = ReplyTheme.colors
    val shape = remember(fabCradle) {
        CutoutTopEdgeShape(
            cutoutMargin = ReplyDimens.bottomAppBarFabCradleMargin,
            cutoutRoundedCornerRadius = ReplyDimens.bottomAppBarFabCradleCornerRadius,
            cutoutVerticalOffset = 0.dp,
            cutoutDiameter = if (fabCradle) ReplyDimens.fabSize else 0.dp,
            topCornerRadius = 0.dp,
        )
    }
    val container = colors.elevated(colors.primarySurface, ReplyDimens.plane08)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(ReplyDimens.plane08, shape, clip = false)
            .background(container, shape),
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onPrimarySurface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ReplyDimens.bottomAppBarHeight)
                    .padding(horizontal = ReplyDimens.grid0_5),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigation()
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                actions()
            }
        }
        // Reserve room for the system navigation bar under the bar content.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars),
        )
    }
}

/**
 * Lays [bar] at the bottom and [fab] centred on its top edge (cradleVerticalOffset = 0), so the
 * FAB's centre sits exactly on the bar's top line, matching `app:layout_anchor` on MDC's FAB.
 */
@Composable
fun BottomAppBarWithFab(
    modifier: Modifier = Modifier,
    fab: @Composable BoxScope.() -> Unit,
    bar: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(top = BottomAppBarFabOverhang),
        ) {
            bar()
        }
        Box(Modifier.align(Alignment.TopCenter)) {
            fab()
        }
    }
}

/**
 * The 56dp secondary-coloured FAB. [icon] is swapped with the `edit ⇄ reply` morph
 * (`asl_edit_reply`): fade + scale + rotate, 175ms.
 */
@Composable
fun ReplyFab(
    icon: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconKey: Any = icon,
) {
    val colors = ReplyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(ReplyDimens.fabSize)
            .shadow(ReplyDimens.plane06, CircleShape, clip = false)
            .background(colors.secondary, CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = colors.onSecondary),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = iconKey to icon,
            transitionSpec = {
                (fadeIn(tween(ReplyMotion.DURATION_SMALL, easing = ReplyMotion.Incoming)) +
                    scaleIn(tween(ReplyMotion.DURATION_SMALL, easing = ReplyMotion.Incoming), initialScale = 0.5f))
                    .togetherWith(
                        fadeOut(tween(ReplyMotion.DURATION_SMALL / 2, easing = ReplyMotion.Outgoing)) +
                            scaleOut(tween(ReplyMotion.DURATION_SMALL / 2, easing = ReplyMotion.Outgoing), targetScale = 0.5f),
                    )
            },
            contentKey = { it.first },
            label = "fabIcon",
        ) { (_, painter) ->
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = colors.onSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** A 48dp `?attr/actionBarItemBackground` style icon button (12dp padding, unbounded ripple). */
@Composable
fun ReplyIconButton(
    icon: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    enabled: Boolean = true,
    size: Dp = ReplyDimens.minTouchTarget,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = false, radius = size / 2),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** Fills the navigation-bar inset with [color]; used under sheets that scroll behind the system bar. */
@Composable
fun NavigationBarSpacer(color: Color = Color.Transparent, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(color)
            .windowInsetsPadding(WindowInsets.navigationBars),
    )
}
