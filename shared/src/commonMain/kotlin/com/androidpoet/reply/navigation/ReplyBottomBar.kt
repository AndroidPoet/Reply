package com.androidpoet.reply.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.designsystem.component.BottomAppBarWithFab
import com.androidpoet.reply.designsystem.component.EditReplyIcon
import com.androidpoet.reply.designsystem.component.ReplyBottomAppBar
import com.androidpoet.reply.designsystem.component.ReplyFab
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_drop_up
import com.androidpoet.reply.designsystem.resources.ic_more_vert
import com.androidpoet.reply.designsystem.resources.ic_reply_logo
import com.androidpoet.reply.designsystem.resources.ic_search
import com.androidpoet.reply.designsystem.resources.ic_settings
import com.androidpoet.reply.designsystem.resources.ic_twotone_delete
import com.androidpoet.reply.designsystem.resources.ic_twotone_star
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import org.jetbrains.compose.resources.painterResource

@Immutable
internal enum class BarActions { Home, Email, DrawerOpen }

@Composable
internal fun ReplyBottomBar(
    title: String?,
    actions: BarActions,
    replyMode: Boolean,
    fabVisibleFraction: () -> Float,
    fabHidden: Boolean,
    chevronRotation: () -> Float,
    titleAlpha: () -> Float,
    onFabClick: () -> Unit,
    onFabPositioned: (Rect) -> Unit,
    onToggleDrawer: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    BottomAppBarWithFab(
        modifier = modifier,
        fab = {
            ReplyFab(
                onClick = onFabClick,
                modifier = Modifier
                    .semantics { contentDescription = if (replyMode) "Reply to email" else "Compose new email" }
                    .onGloballyPositioned { onFabPositioned(it.boundsInRoot()) }
                    .graphicsLayer {
                        val fraction = if (fabHidden) 0f else fabVisibleFraction()
                        scaleX = fraction
                        scaleY = fraction
                        alpha = fraction
                    },
            ) {
                EditReplyIcon(activated = replyMode, tint = colors.onSecondary, modifier = Modifier.size(24.dp))
            }
        },
        bar = {
            ReplyBottomAppBar(
                navigation = {
                    Row(
                        Modifier
                            .padding(vertical = ReplyDimens.grid0_5)
                            .height(ReplyDimens.minTouchTarget)
                            .clip(RoundedCornerShape(ReplyDimens.grid3))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.onPrimarySurface),
                                onClick = onToggleDrawer,
                            )
                            .padding(horizontal = ReplyDimens.grid0_5),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_drop_up),
                            contentDescription = "Toggle navigation drawer",
                            tint = colors.onPrimarySurface,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = chevronRotation() },
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_reply_logo),
                            contentDescription = "Reply logo",
                            tint = colors.onPrimarySurface,
                            modifier = Modifier
                                .padding(start = ReplyDimens.grid1)
                                .size(ReplyDimens.bottomAppBarLogoSize),
                        )
                        if (title != null) {
                            ReplyText(
                                text = title,
                                style = ReplyTheme.typography.body1,
                                color = colors.onPrimarySurface,
                                modifier = Modifier
                                    .padding(horizontal = ReplyDimens.grid1)
                                    .graphicsLayer { alpha = titleAlpha() },
                            )
                        }
                    }
                },
                actions = {
                    when (actions) {
                        BarActions.DrawerOpen -> ReplyIconButton(
                            icon = painterResource(Res.drawable.ic_settings),
                            contentDescription = "Settings",
                            onClick = onSettings,
                        )
                        BarActions.Email -> {
                            ReplyIconButton(
                                icon = painterResource(Res.drawable.ic_twotone_star),
                                contentDescription = "Star",
                                onClick = onStar,
                            )
                            ReplyIconButton(
                                icon = painterResource(Res.drawable.ic_twotone_delete),
                                contentDescription = "Delete",
                                onClick = onDelete,
                            )
                            ReplyIconButton(
                                icon = painterResource(Res.drawable.ic_more_vert),
                                contentDescription = "More options",
                                onClick = onMore,
                            )
                        }
                        BarActions.Home -> ReplyIconButton(
                            icon = painterResource(Res.drawable.ic_search),
                            contentDescription = "Search",
                            onClick = onSearch,
                        )
                    }
                },
            )
        },
    )
}
