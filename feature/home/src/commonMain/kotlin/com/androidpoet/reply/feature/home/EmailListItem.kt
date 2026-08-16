@file:OptIn(ExperimentalFoundationApi::class)

package com.androidpoet.reply.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyCard
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_twotone_star_on_background
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import org.jetbrains.compose.resources.painterResource

private const val ICON_MAX_SCALE_ADDITION = 0.5f

@Composable
fun EmailListItem(
    email: Email,
    onClick: (email: Email, cardBoundsInRoot: Rect, topLeftCornerPx: Float) -> Unit,
    onLongClick: (Email) -> Unit,
    onStarChanged: (Email, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
) {
    val colors = ReplyTheme.colors
    val swipe = rememberReboundingSwipeState()
    val starredCornerPx = ReplyDimens.grid3

    val activated = if (swipe.hasMetThresholdOnce) !email.isStarred else email.isStarred
    val reveal = remember { Animatable(if (email.isStarred) 1f else 0f) }
    LaunchedEffect(activated) {
        val target = if (activated) 1f else 0f
        val distance = abs(target - reveal.value)
        reveal.animateTo(
            target,
            tween((distance * Durations.MEDIUM).toInt(), easing = Interpolators.FastOutSlowIn),
        )
    }

    val cornerInterpolation = if (swipe.rawDx > 0f && !swipe.hasMetThresholdOnce) {
        val interpolation = (swipe.swipePercentage / TRUE_SWIPE_THRESHOLD).coerceIn(0f, 1f)
        abs((if (email.isStarred) 1f else 0f) - interpolation)
    } else if (swipe.hasMetThresholdOnce) {
        if (email.isStarred) 0f else 1f
    } else {
        if (email.isStarred) 1f else 0f
    }
    val topLeftCorner = starredCornerPx * cornerInterpolation

    val star: Painter = painterResource(Res.drawable.ic_twotone_star_on_background)
    var cardBounds by remember { mutableStateOf(Rect.Zero) }
    val topLeftCornerPx = with(LocalDensity.current) { topLeftCorner.toPx() }
    val iconTint = colors.onBackground
    val iconTintActive = colors.onSecondary
    val circleColor = colors.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ReplyDimens.grid0_5, vertical = ReplyDimens.grid0_25)
            .onSizeChanged { swipe.width = it.width.toFloat() }
            .clipToBounds()
            .drawBehind {
                val progress = reveal.value
                val iconMargin = ReplyDimens.grid4.toPx()
                val iconSize = 24.dp.toPx()
                val cx = iconMargin + iconSize / 2f
                val cy = size.height / 2f
                val cr = hypot(size.width - (iconMargin + iconSize / 2f), size.height / 2f)
                drawCircle(color = circleColor, radius = cr * progress, center = Offset(cx, cy))

                val range = (PI * progress).toFloat()
                val additive = (sin(range.toDouble()) * ICON_MAX_SCALE_ADDITION).coerceIn(0.0, 1.0)
                val scaleFactor = (1 + additive).toFloat()
                val tint = lerp(iconTint, iconTintActive, (progress / 0.15f).coerceIn(0f, 1f))
                translate(left = cx - iconSize / 2f, top = cy - iconSize / 2f) {
                    scale(scaleFactor, pivot = Offset(iconSize / 2f, iconSize / 2f)) {
                        with(star) {
                            draw(
                                size = androidx.compose.ui.geometry.Size(iconSize, iconSize),
                                colorFilter = ColorFilter.tint(tint),
                            )
                        }
                    }
                }
            },
    ) {
        ReplyCard(
            topLeftCorner = topLeftCorner,
            modifier = Modifier
                .graphicsLayer { translationX = swipe.translationX }
                .onGloballyPositioned { cardBounds = it.boundsInRoot() }
                .reboundingSwipe(swipe, enabled = swipeEnabled) {
                    onStarChanged(email, !email.isStarred)
                }
                .combinedClickable(
                    onClick = { onClick(email, cardBounds, topLeftCornerPx) },
                    onLongClick = { onLongClick(email) },
                ),
        ) {
            EmailCardBody(email)
        }
    }
}

@Composable
fun EmailCardBody(email: Email) {
    val colors = ReplyTheme.colors
    val typography = ReplyTheme.typography
    Column(Modifier.padding(top = ReplyDimens.grid2, bottom = ReplyDimens.grid2)) {
        Row(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = ReplyDimens.grid2, end = ReplyDimens.grid2, top = ReplyDimens.grid1),
            ) {
                ReplyText(
                    text = email.senderPreview,
                    style = typography.body2,
                    color = colors.onSurfaceHigh,
                    maxLines = 1,
                )
                ReplyText(
                    text = email.subject,
                    style = if (email.isImportant) typography.headline4 else typography.headline5,
                    color = colors.onSurfaceHigh,
                    maxLines = 1,
                    modifier = Modifier.padding(top = ReplyDimens.grid1),
                )
            }
            Avatar(
                image = email.sender.avatar,
                contentDescription = "Profile image of sender",
                modifier = Modifier.padding(top = ReplyDimens.grid1, end = ReplyDimens.grid2),
                size = ReplyDimens.emailSenderProfileImageSize,
            )
        }
        if (email.hasBody) {
            ReplyText(
                text = email.body,
                style = typography.body1,
                color = colors.onSurfaceHigh,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ReplyDimens.grid1, start = ReplyDimens.grid2, end = ReplyDimens.grid2),
            )
        }
        if (email.hasAttachments) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ReplyDimens.grid2)
                    .height(96.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                userScrollEnabled = true,
            ) {
                items(email.attachments) { attachment ->
                    Image(
                        painter = painterResource(attachment.image),
                        contentDescription = attachment.contentDesc,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(ReplyDimens.grid0_25)
                            .width(150.dp)
                            .height(92.dp),
                    )
                }
            }
        }
    }
}
