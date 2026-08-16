package com.androidpoet.reply.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.designsystem.component.EditReplyIcon
import com.androidpoet.reply.designsystem.motion.ContainerTransform
import com.androidpoet.reply.designsystem.motion.ContainerTransformSpec
import com.androidpoet.reply.designsystem.motion.Corners
import com.androidpoet.reply.designsystem.motion.ProgressThresholds
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import com.androidpoet.reply.feature.compose.ComposeCard
import com.androidpoet.reply.feature.compose.ComposeDraft
import com.androidpoet.reply.feature.email.EmailDetailCard
import com.androidpoet.reply.feature.home.EmailCardBody

@Composable
internal fun TransformOverlay(
    transform: Transform,
    progress: Float,
    fullCardRect: Rect,
    fullCardMinHeight: Dp,
    emailStore: EmailStore,
    accountStore: AccountStore,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val density = LocalDensity.current
    val cardColor = colors.elevated(colors.surface, ReplyDimens.plane01)
    val spec = when (transform) {
        is Transform.CardToEmail -> {
            val cardContent: @Composable () -> Unit = { EmailCardBody(transform.email) }
            val detailContent: @Composable () -> Unit = {
                EmailDetailCard(email = transform.email, onNavigateUp = {}, minHeight = fullCardMinHeight)
            }
            if (transform.entering) {
                ContainerTransformSpec(
                    startBounds = transform.cardBounds,
                    endBounds = fullCardRect,
                    startCorners = Corners(topLeft = transform.topLeftCornerPx),
                    endCorners = Corners(),
                    startColor = colors.surface,
                    endColor = cardColor,
                    thresholds = ProgressThresholds.Enter,
                    startElevation = 0.dp,
                    endElevation = ReplyDimens.plane01,
                    startContent = cardContent,
                    endContent = detailContent,
                )
            } else {
                ContainerTransformSpec(
                    startBounds = fullCardRect,
                    endBounds = transform.cardBounds,
                    startCorners = Corners(),
                    endCorners = Corners(topLeft = transform.topLeftCornerPx),
                    startColor = cardColor,
                    endColor = colors.surface,
                    thresholds = ProgressThresholds.Return,
                    startElevation = ReplyDimens.plane01,
                    endElevation = 0.dp,
                    startContent = detailContent,
                    endContent = cardContent,
                )
            }
        }
        is Transform.FabToCompose -> {
            val fabContent: @Composable () -> Unit = {
                Box(Modifier.size(ReplyDimens.fabSize), contentAlignment = Alignment.Center) {
                    EditReplyIcon(
                        activated = transform.replyToId >= 0,
                        tint = colors.onSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            val composeContent: @Composable () -> Unit = {
                val draft = remember(transform.replyToId) {
                    ComposeDraft.create(transform.replyToId, emailStore, accountStore)
                }
                ComposeCard(draft = draft, onClose = {}, minHeight = fullCardMinHeight)
            }
            ContainerTransformSpec(
                startBounds = transform.fabBounds,
                endBounds = fullCardRect,
                startCorners = Corners.all(with(density) { (ReplyDimens.fabSize / 2).toPx() }),
                endCorners = Corners(),
                startColor = colors.secondary,
                endColor = cardColor,
                thresholds = ProgressThresholds.Enter,
                startElevation = ReplyDimens.plane06,
                endElevation = ReplyDimens.plane01,
                startContent = fabContent,
                endContent = composeContent,
            )
        }
    }
    ContainerTransform(spec = spec, progress = progress, modifier = modifier)
}
