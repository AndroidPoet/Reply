package com.androidpoet.reply.feature.email

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailAttachment
import com.androidpoet.reply.designsystem.SharedKeys
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyCard
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_down
import com.androidpoet.reply.designsystem.sharedCardBounds
import com.androidpoet.reply.designsystem.theme.Emphasis
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import org.jetbrains.compose.resources.painterResource

private const val MAX_GRID_SPANS = 3

/**
 * `fragment_email.xml`: the opened email inside a full-width card, with a masonry grid of
 * attachments. The card is the shared element the home card morphs into.
 */
@Composable
fun EmailScreen(
    viewModel: EmailViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val colors = ReplyTheme.colors
    val statusBars = WindowInsets.statusBars.asPaddingValues()
    val navBars = WindowInsets.navigationBars.asPaddingValues()

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val topPadding = statusBars.calculateTopPadding() + ReplyDimens.grid1
        // NestedScrollView fillViewport="true": the card is at least as tall as the viewport.
        val minCardHeight = maxHeight - topPadding
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = ReplyDimens.grid0_5, end = ReplyDimens.grid0_5, top = topPadding),
        ) {
        val current = email ?: return@Column
        ReplyCard(
            elevation = ReplyDimens.plane01,
            modifier = Modifier
                .defaultMinSize(minHeight = minCardHeight)
                .sharedCardBounds(SharedKeys.emailCard(current.id), ReplyTheme.shapes.medium),
        ) {
            EmailContent(
                email = current,
                onNavigateUp = onNavigateUp,
                bottomPadding = ReplyDimens.bottomAppBarHeight + navBars.calculateBottomPadding(),
            )
        }
        }
    }
}

@Composable
private fun EmailContent(
    email: Email,
    onNavigateUp: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    val colors = ReplyTheme.colors
    val typography = ReplyTheme.typography
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = ReplyDimens.grid2, end = ReplyDimens.grid2, top = ReplyDimens.grid3, bottom = bottomPadding),
    ) {
        Row(Modifier.fillMaxWidth()) {
            ReplyText(
                text = email.subject,
                style = typography.headline3,
                color = colors.onSurfaceHigh,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = ReplyDimens.grid1, end = ReplyDimens.grid1),
            )
            // Centred over the sender avatar column (42dp) — the 56dp touch target overflows it.
            Box(
                Modifier
                    .width(ReplyDimens.emailSenderProfileImageSize)
                    .padding(top = ReplyDimens.grid1),
                contentAlignment = Alignment.TopCenter,
            ) {
                ReplyIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_down),
                    contentDescription = "Navigate back",
                    onClick = onNavigateUp,
                    tint = colors.onSurface,
                    size = 56.dp,
                    modifier = Modifier
                        .requiredSize(56.dp)
                        .alpha(Emphasis.MEDIUM),
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = ReplyDimens.grid1)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = ReplyDimens.grid1),
            ) {
                ReplyText(
                    text = email.senderPreview,
                    style = typography.body2,
                    color = colors.onSurfaceHigh,
                )
                ReplyText(
                    text = "To ${email.recipientsPreview}",
                    style = typography.caption,
                    color = colors.onSurfaceMedium,
                    modifier = Modifier.padding(top = ReplyDimens.grid0_25),
                )
            }
            Avatar(
                image = email.sender.avatar,
                contentDescription = "Profile image of sender",
                size = ReplyDimens.emailSenderProfileImageSize,
            )
        }
        ReplyText(
            text = email.body,
            style = typography.body1,
            color = colors.onSurfaceHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ReplyDimens.grid3),
        )
        if (email.hasAttachments) {
            AttachmentGrid(
                attachments = email.attachments,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ReplyDimens.grid3),
            )
        }
    }
}

/**
 * `EmailAttachmentGridAdapter`: a 3-span grid where each item takes 1–3 spans. The original
 * randomises spans per bind; here the sequence is fixed so the layout is stable across
 * recompositions (and matches the reference: one full-width shot above three portraits).
 */
private val SPAN_PATTERN = listOf(3, 1, 1, 1, 2, 1, 1, 2, 3)

@Composable
private fun AttachmentGrid(
    attachments: List<EmailAttachment>,
    modifier: Modifier = Modifier,
) {
    val rows = mutableListOf<List<Pair<EmailAttachment, Int>>>()
    var row = mutableListOf<Pair<EmailAttachment, Int>>()
    var occupied = 0
    attachments.forEachIndexed { index, attachment ->
        var span = SPAN_PATTERN[index % SPAN_PATTERN.size]
        if (span > MAX_GRID_SPANS - occupied) span = MAX_GRID_SPANS - occupied
        row += attachment to span
        occupied += span
        if (occupied >= MAX_GRID_SPANS) {
            rows += row
            row = mutableListOf()
            occupied = 0
        }
    }
    if (row.isNotEmpty()) rows += row

    Column(modifier) {
        rows.forEach { items ->
            Row(Modifier.fillMaxWidth()) {
                items.forEach { (attachment, span) ->
                    Image(
                        painter = painterResource(attachment.image),
                        contentDescription = attachment.contentDesc,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(span.toFloat())
                            .padding(ReplyDimens.grid0_25)
                            .height(200.dp - ReplyDimens.grid0_25 * 2),
                    )
                }
                // Keep partial rows left-aligned at their span width.
                val used = items.sumOf { it.second }
                if (used < MAX_GRID_SPANS) {
                    Box(Modifier.weight((MAX_GRID_SPANS - used).toFloat()))
                }
            }
        }
    }
}
