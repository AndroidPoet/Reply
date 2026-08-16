package com.androidpoet.reply.feature.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.Account
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyCard
import com.androidpoet.reply.designsystem.component.ReplyDivider
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.motion.ContainerTransform
import com.androidpoet.reply.designsystem.motion.ContainerTransformSpec
import com.androidpoet.reply.designsystem.motion.Corners
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import com.androidpoet.reply.designsystem.motion.ProgressThresholds
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_down
import com.androidpoet.reply.designsystem.resources.ic_close
import com.androidpoet.reply.designsystem.resources.ic_close_small
import com.androidpoet.reply.designsystem.resources.ic_twotone_add_circle_outline
import com.androidpoet.reply.designsystem.resources.ic_twotone_send
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import org.jetbrains.compose.resources.painterResource

private val RECIPIENT_CARD_WIDTH = 360.dp
private val RECIPIENT_CARD_CORNER = 4.dp

private enum class RecipientCardPhase { Expanding, Open, Collapsing }

@Composable
fun ComposeScreen(
    draft: ComposeDraft,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,

    cardTranslationY: () -> Float = { 0f },
) {
    val colors = ReplyTheme.colors
    val density = LocalDensity.current
    val statusBars = WindowInsets.statusBars.asPaddingValues()

    var screenOrigin by remember { mutableStateOf(Offset.Zero) }
    var cardBounds by remember { mutableStateOf(Rect.Zero) }
    var expandedRecipient by remember { mutableStateOf<Account?>(null) }
    var chipBounds by remember { mutableStateOf(Rect.Zero) }
    var phase by remember { mutableStateOf(RecipientCardPhase.Open) }
    val transformProgress = remember { Animatable(0f) }

    LaunchedEffect(expandedRecipient, phase) {
        when (phase) {
            RecipientCardPhase.Expanding -> {
                transformProgress.snapTo(0f)
                transformProgress.animateTo(1f, tween(Durations.LARGE, easing = Interpolators.FastOutSlowIn))
                phase = RecipientCardPhase.Open
            }
            RecipientCardPhase.Collapsing -> {
                transformProgress.snapTo(0f)
                transformProgress.animateTo(1f, tween(Durations.LARGE, easing = Interpolators.FastOutSlowIn))
                expandedRecipient = null
                phase = RecipientCardPhase.Open
            }
            RecipientCardPhase.Open -> Unit
        }
    }
    fun collapse() {
        if (expandedRecipient != null && phase == RecipientCardPhase.Open) phase = RecipientCardPhase.Collapsing
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .onGloballyPositioned { screenOrigin = it.positionInRoot() },
    ) {
        val topPadding = statusBars.calculateTopPadding() + ReplyDimens.grid1

        val minCardHeight = maxHeight - topPadding
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = ReplyDimens.grid0_5, end = ReplyDimens.grid0_5, top = topPadding),
        ) {
            ComposeCard(
                draft = draft,
                onClose = onClose,
                minHeight = minCardHeight,
                modifier = Modifier.graphicsLayer { translationY = cardTranslationY() },
                hiddenRecipient = expandedRecipient,
                onRecipientClick = { account, bounds ->
                    if (expandedRecipient == null) {
                        chipBounds = bounds.translate(-screenOrigin)
                        expandedRecipient = account
                        phase = RecipientCardPhase.Expanding
                    }
                },
                onCardBounds = { cardBounds = it.translate(-screenOrigin) },
            )
        }

        val recipient = expandedRecipient
        if (recipient != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { collapse() },
            )

            val cardWidthPx = with(density) { minOf(RECIPIENT_CARD_WIDTH, maxWidth - ReplyDimens.grid4 - ReplyDimens.grid2).toPx() }
            val cardHeightPx = with(density) { (64.dp * 2).toPx() }
            val cardRect = Rect(
                offset = Offset(
                    cardBounds.left + with(density) { (ReplyDimens.grid1 + ReplyDimens.grid2).toPx() },
                    cardBounds.top + with(density) { (ReplyDimens.grid2 + ReplyDimens.grid2).toPx() },
                ),
                size = Size(cardWidthPx, cardHeightPx),
            )
            val chipCorners = Corners.all(with(density) { 16.dp.toPx() })
            val cardCorners = Corners.all(with(density) { RECIPIENT_CARD_CORNER.toPx() })
            val chipColor = colors.onSurface.copy(alpha = 0.10f).compositeOver(colors.elevated(colors.surface, ReplyDimens.plane01))
            val cardColor = colors.elevated(colors.surface, ReplyDimens.plane06)
            val chip: @Composable () -> Unit = { RecipientChip(account = recipient, onClick = {}) }
            val card: @Composable () -> Unit = {
                RecipientCard(
                    account = recipient,
                    onClose = { collapse() },
                    modifier = Modifier.width(with(density) { cardWidthPx.toDp() }),
                    elevated = false,
                )
            }
            val spec = if (phase == RecipientCardPhase.Collapsing) {
                ContainerTransformSpec(
                    startBounds = cardRect, endBounds = chipBounds,
                    startCorners = cardCorners, endCorners = chipCorners,
                    startColor = cardColor, endColor = chipColor,
                    thresholds = ProgressThresholds.Return,
                    startElevation = ReplyDimens.plane06, endElevation = 0.dp,
                    startContent = card, endContent = chip,
                )
            } else {
                ContainerTransformSpec(
                    startBounds = chipBounds, endBounds = cardRect,
                    startCorners = chipCorners, endCorners = cardCorners,
                    startColor = chipColor, endColor = cardColor,
                    thresholds = ProgressThresholds.Enter,
                    startElevation = 0.dp, endElevation = ReplyDimens.plane06,
                    startContent = chip, endContent = card,
                )
            }
            ContainerTransform(
                spec = spec,
                progress = if (phase == RecipientCardPhase.Open) 1f else transformProgress.value,
            )
        }
    }
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(
        red = red * a + background.red * (1 - a),
        green = green * a + background.green * (1 - a),
        blue = blue * a + background.blue * (1 - a),
        alpha = 1f,
    )
}

@Composable
fun ComposeCard(
    draft: ComposeDraft,
    onClose: () -> Unit,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    hiddenRecipient: Account? = null,
    onRecipientClick: (Account, Rect) -> Unit = { _, _ -> },
    onCardBounds: (Rect) -> Unit = {},
) {
    val colors = ReplyTheme.colors
    val typography = ReplyTheme.typography
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    ReplyCard(
        elevation = ReplyDimens.plane01,
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .onGloballyPositioned { onCardBounds(it.boundsInRoot()) },
    ) {
        Column(Modifier.padding(top = ReplyDimens.grid2)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                ReplyIconButton(
                    icon = painterResource(Res.drawable.ic_close),
                    contentDescription = "Close editing email",
                    onClick = onClose,
                    tint = colors.onSurfaceDisabled,
                    modifier = Modifier.padding(start = ReplyDimens.grid1),
                )
                ReplyTextField(
                    value = draft.subject,
                    onValueChange = { draft.subject = it },
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

            SenderSpinner(
                selected = draft.sender,
                options = draft.senderOptions,
                onSelected = { draft.sender = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ReplyDimens.grid0_5, end = ReplyDimens.grid1),
            )
            ReplyDivider(Modifier.padding(top = ReplyDimens.grid0_5, start = ReplyDimens.grid2, end = ReplyDimens.grid2))

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
                    draft.recipients.forEachIndexed { index, account ->
                        if (index > 0) Spacer(Modifier.width(ReplyDimens.grid1))
                        var bounds by remember { mutableStateOf(Rect.Zero) }
                        RecipientChip(
                            account = account,
                            onClick = { onRecipientClick(account, bounds) },
                            modifier = Modifier
                                .onGloballyPositioned { bounds = it.boundsInRoot() }

                                .alpha(if (account == hiddenRecipient) 0f else 1f),
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

            ReplyTextField(
                value = draft.body,
                onValueChange = { draft.body = it },
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

@Composable
private fun RecipientCard(
    account: Account,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    elevated: Boolean = true,
) {
    val colors = ReplyTheme.colors
    val shape = RoundedCornerShape(RECIPIENT_CARD_CORNER)
    Column(
        modifier
            .width(RECIPIENT_CARD_WIDTH)
            .then(
                if (elevated) {
                    Modifier
                        .shadow(ReplyDimens.plane06, shape)
                        .clip(shape)
                        .background(colors.elevated(colors.surface, ReplyDimens.plane06))
                } else {
                    Modifier
                },
            )
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
