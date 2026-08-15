package com.androidpoet.reply.feature.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.Account
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyCard
import com.androidpoet.reply.designsystem.component.ReplyDivider
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_down
import com.androidpoet.reply.designsystem.resources.ic_close
import com.androidpoet.reply.designsystem.resources.ic_close_small
import com.androidpoet.reply.designsystem.resources.ic_twotone_add_circle_outline
import com.androidpoet.reply.designsystem.resources.ic_twotone_send
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyMotion
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import org.jetbrains.compose.resources.painterResource

/**
 * `fragment_compose.xml`: subject / from / to / body inside a surface card. Send and close both
 * simply leave the screen, as in the sample.
 */
@Composable
fun ComposeScreen(
    viewModel: ComposeViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val typography = ReplyTheme.typography
    val statusBars = WindowInsets.statusBars.asPaddingValues()
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    var expandedRecipient by remember { mutableStateOf<Account?>(null) }

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
                .imePadding()
                .padding(start = ReplyDimens.grid0_5, end = ReplyDimens.grid0_5, top = topPadding),
        ) {
            ReplyCard(elevation = ReplyDimens.plane01, modifier = Modifier.defaultMinSize(minHeight = minCardHeight)) {
                Column(Modifier.padding(top = ReplyDimens.grid2)) {
                    // Close · Subject · Send
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        ReplyIconButton(
                            icon = painterResource(Res.drawable.ic_close),
                            contentDescription = "Close editing email",
                            onClick = onClose,
                            tint = colors.onSurfaceDisabled,
                            modifier = Modifier.padding(start = ReplyDimens.grid1),
                        )
                        ReplyTextField(
                            value = viewModel.subject,
                            onValueChange = { viewModel.subject = it },
                            hint = "Subject",
                            textStyle = typography.headline5.copy(color = colors.onSurfaceHigh),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = ReplyDimens.grid2)
                                .defaultMinSize(minHeight = ReplyDimens.minTouchTarget)
                                .padding(vertical = ReplyDimens.grid1),
                        )
                        ReplyIconButton(
                            icon = painterResource(Res.drawable.ic_twotone_send),
                            contentDescription = "Send email",
                            onClick = onClose,
                            tint = colors.primary,
                            modifier = Modifier.padding(end = ReplyDimens.grid1),
                        )
                    }
                    ReplyDivider(Modifier.padding(top = ReplyDimens.grid1, start = ReplyDimens.grid2, end = ReplyDimens.grid2))

                    // From (spinner)
                    SenderSpinner(
                        selected = viewModel.sender,
                        options = viewModel.senderOptions,
                        onSelected = { viewModel.sender = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = ReplyDimens.grid0_5, end = ReplyDimens.grid1),
                    )
                    ReplyDivider(Modifier.padding(top = ReplyDimens.grid0_5, start = ReplyDimens.grid2, end = ReplyDimens.grid2))

                    // To (chips)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = ReplyDimens.grid1),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            Modifier
                                .weight(1f)
                                .padding(end = ReplyDimens.grid2)
                                .heightIn(min = ReplyDimens.minTouchTarget)
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = ReplyDimens.grid0_25),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(Modifier.width(ReplyDimens.grid2))
                            viewModel.recipients.forEachIndexed { index, account ->
                                if (index > 0) Spacer(Modifier.width(ReplyDimens.grid1))
                                RecipientChip(
                                    account = account,
                                    onClick = { expandedRecipient = account },
                                )
                            }
                        }
                        ReplyIconButton(
                            icon = painterResource(Res.drawable.ic_twotone_add_circle_outline),
                            contentDescription = "Add recipient",
                            onClick = {},
                            tint = colors.onSurfaceDisabled,
                            modifier = Modifier.padding(end = ReplyDimens.grid1),
                        )
                    }
                    ReplyDivider(Modifier.padding(top = ReplyDimens.grid1, start = ReplyDimens.grid2, end = ReplyDimens.grid2))

                    // Body
                    ReplyTextField(
                        value = viewModel.body,
                        onValueChange = { viewModel.body = it },
                        hint = "New message…",
                        textStyle = typography.body1.copy(color = colors.onSurfaceHigh),
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = ReplyDimens.grid2, start = ReplyDimens.grid2, end = ReplyDimens.grid2)
                            .defaultMinSize(minHeight = 250.dp)
                            .padding(bottom = ReplyDimens.grid4 + navBars.calculateBottomPadding()),
                    )
                }
            }
        }

        // Expanded recipient card (MaterialContainerTransform chip → card) over a tap-to-close scrim.
        val expanded = expandedRecipient
        if (expanded != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expandedRecipient = null },
            )
        }
        AnimatedVisibility(
            visible = expanded != null,
            enter = fadeIn(tween(ReplyMotion.DURATION_MEDIUM)) +
                expandIn(tween(ReplyMotion.DURATION_MEDIUM), expandFrom = Alignment.TopStart),
            exit = fadeOut(tween(ReplyMotion.DURATION_MEDIUM)) +
                shrinkOut(tween(ReplyMotion.DURATION_MEDIUM), shrinkTowards = Alignment.TopStart),
            modifier = Modifier
                .padding(top = statusBars.calculateTopPadding() + ReplyDimens.grid1 + ReplyDimens.grid2 + ReplyDimens.grid2)
                .padding(start = ReplyDimens.grid0_5 + ReplyDimens.grid1 + ReplyDimens.grid2, end = ReplyDimens.grid2),
        ) {
            var shown by remember { mutableStateOf(expanded) }
            if (expanded != null) shown = expanded
            shown?.let { RecipientCard(account = it, onClose = { expandedRecipient = null }) }
        }
    }
}

/** A hint-bearing borderless field (`EditText` with transparent background). */
@Composable
private fun ReplyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    textStyle: TextStyle,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = singleLine,
        cursorBrush = SolidColor(colors.secondary),
        modifier = modifier,
        decorationBox = { inner ->
            Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                if (value.isEmpty()) {
                    ReplyText(text = hint, style = textStyle, color = colors.onSurfaceMedium)
                }
                inner()
            }
        },
    )
}

/** `Spinner` with `spinner_item_layout`: body1 text, 16dp padding, drop-down arrow at the end. */
@Composable
private fun SenderSpinner(
    selected: Account,
    options: List<Account>,
    onSelected: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { open = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReplyText(
                text = selected.email,
                style = ReplyTheme.typography.body1,
                color = colors.onSurfaceHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(ReplyDimens.grid2),
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = null,
                tint = colors.onSurfaceMedium,
                modifier = Modifier
                    .padding(end = ReplyDimens.grid2)
                    .size(24.dp),
            )
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            offset = DpOffset(0.dp, 0.dp),
            containerColor = colors.elevated(colors.surface, ReplyDimens.plane16),
        ) {
            options.forEach { account ->
                DropdownMenuItem(
                    text = {
                        ReplyText(
                            text = account.email,
                            style = ReplyTheme.typography.body1,
                            color = colors.onSurfaceHigh,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onSelected(account)
                        open = false
                    },
                )
            }
        }
    }
}

/**
 * `Widget.Reply.Chip.Entry`: 32dp tall, avatar flush-left as the chip icon, no close icon,
 * `SmallComponent` (fully rounded) shape.
 */
@Composable
private fun RecipientChip(
    account: Account,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    Row(
        modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.onSurface.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(image = account.avatar, contentDescription = null, size = ReplyDimens.chipIconDiameter)
        ReplyText(
            text = account.fullName,
            style = ReplyTheme.typography.body2,
            color = colors.onSurfaceHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = ReplyDimens.grid1),
        )
    }
}

/** The 360dp popup card a chip expands into: primary address (highlighted) + alternate address. */
@Composable
private fun RecipientCard(
    account: Account,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val shape = RoundedCornerShape(4.dp)
    Column(
        modifier
            .width(360.dp)
            .shadow(ReplyDimens.plane06, shape)
            .clip(shape)
            .background(colors.elevated(colors.surface, ReplyDimens.plane06))
            .clickable(onClick = onClose),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.onSecondary)
                .heightIn(min = 64.dp)
                .padding(horizontal = ReplyDimens.grid2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(image = account.avatar, contentDescription = account.email)
            ReplyText(
                text = account.email,
                style = ReplyTheme.typography.body1,
                color = colors.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ReplyDimens.grid2),
            )
            Box(
                Modifier
                    .size(24.dp)
                    .background(colors.secondary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close_small),
                    contentDescription = null,
                    tint = colors.onSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = ReplyDimens.grid2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(image = account.avatar, contentDescription = account.altEmail)
            ReplyText(
                text = account.altEmail,
                style = ReplyTheme.typography.body1,
                color = colors.onSurfaceHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ReplyDimens.grid2),
            )
        }
    }
}
