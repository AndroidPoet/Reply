package com.androidpoet.reply.designsystem.component

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
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated

val BottomAppBarFabOverhang: Dp = ReplyDimens.fabSize / 2

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

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars),
        )
    }
}

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

@Composable
fun ReplyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
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
        Box(Modifier.size(24.dp)) { icon() }
    }
}

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

