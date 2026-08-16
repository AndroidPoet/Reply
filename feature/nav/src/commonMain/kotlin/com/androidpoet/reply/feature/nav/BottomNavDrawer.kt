@file:OptIn(ExperimentalComposeUiApi::class)

package com.androidpoet.reply.feature.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.Account
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_done
import com.androidpoet.reply.designsystem.resources.ic_twotone_folder
import com.androidpoet.reply.designsystem.shape.CutoutTopEdgeShape
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomNavDrawer(
    state: BottomNavDrawerState,
    currentMailbox: Mailbox,
    accounts: List<Account>,
    folders: List<String>,
    onMenuItemClick: (NavigationItem.Menu) -> Unit,
    onAccountClick: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BackHandler(enabled = state.isOpen) { state.close() }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val heightPx = with(density) { maxHeight.toPx() }
        val widthPx = with(density) { maxWidth.toPx() }
        LaunchedEffect(heightPx, widthPx, density) {
            state.containerHeight = heightPx
            state.containerWidth = widthPx
            state.density = density.density
        }

        val foregroundInterpolation = 1f - (state.expandFraction / 0.25f).coerceIn(0f, 1f)
        val topInsetProgress = (state.expandFraction / 0.9f).coerceIn(0f, 1f)

        val open = state.openFraction
        if (open > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.scrim.copy(alpha = colors.scrim.alpha * open))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { state.close() },
                    ),
            )
        }

        val dragState = rememberDraggableState { delta -> state.dragBy(delta) }
        val bgShape = RoundedCornerShape(topStart = ReplyDimens.plane16 - 4.dp, topEnd = ReplyDimens.plane16 - 4.dp)

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = state.position * heightPx + state.sandwichTranslation }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    enabled = state.isOpen,
                    onDragStarted = { state.onDragStart() },
                    onDragStopped = { velocity -> state.settle(velocity) },
                ),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .shadow(ReplyDimens.plane08, bgShape, clip = false)
                    .background(colors.elevated(colors.primarySurfaceVariant, ReplyDimens.plane08), bgShape),
            ) {
                val sandwichOpen = state.sandwichState == SandwichState.OPEN
                Column(
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { state.accountListHeight = it.height.toFloat() }
                        .graphicsLayer { alpha = state.accountProgress }
                        .padding(top = ReplyDimens.grid3, bottom = ReplyDimens.grid3),
                ) {
                    accounts.forEach { account ->
                        AccountRow(
                            account = account,
                            enabled = sandwichOpen,
                            onClick = {
                                onAccountClick(account)
                                state.toggleSandwich()
                            },
                        )
                    }
                }
            }

            if (state.sandwichState != SandwichState.OPEN) {
                val shapeInterpolation = (1f - state.navProgress) * foregroundInterpolation
                val fgShape = remember(shapeInterpolation) {
                    CutoutTopEdgeShape(
                        cutoutMargin = ReplyDimens.grid1,
                        cutoutRoundedCornerRadius = ReplyDimens.grid3,
                        cutoutVerticalOffset = 0.dp,
                        cutoutDiameter = ReplyDimens.navigationDrawerProfileImageSizePadded,
                        topCornerRadius = ReplyDimens.plane16 - 4.dp,
                        interpolation = shapeInterpolation,
                    )
                }
                val fgColor = colors.elevated(colors.primarySurface, ReplyDimens.plane16)
                val navItems = remember(folders) { NavigationModel.items(folders) }
                val listState = rememberLazyListState()
                LaunchedEffect(state.currentValue) {
                    if (state.currentValue == DrawerValue.Hidden) listState.scrollToItem(0)
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = ReplyDimens.grid3)
                        .graphicsLayer {
                            translationY = size.height * 0.15f * state.navProgress -
                                (1f - foregroundInterpolation) * ReplyDimens.grid3.toPx()
                            alpha = 1f - state.navProgress
                        }
                        .shadow(ReplyDimens.plane16, fgShape, clip = false)
                        .background(fgColor, fgShape)
                        .clip(fgShape)
                        .padding(top = ReplyDimens.grid3 + statusBarTop * topInsetProgress, bottom = ReplyDimens.grid4),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(state.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            top = ReplyDimens.grid3,
                            bottom = with(density) { state.bottomBarHeight.toDp() },
                        ),
                    ) {
                        items(navItems) { item ->
                            when (item) {
                                is NavigationItem.Menu -> NavMenuRow(
                                    item = item,
                                    checked = item.mailbox == currentMailbox,
                                    onClick = { onMenuItemClick(item) },
                                )
                                is NavigationItem.Divider -> NavDividerRow(item.title)
                                is NavigationItem.Folder -> NavFolderRow(item.name)
                            }
                        }
                    }
                }
            }

            val imageVisibility = (1f - state.navProgress) * foregroundInterpolation
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .size(ReplyDimens.navigationDrawerProfileImageSize)
                    .graphicsLayer {
                        scaleX = imageVisibility
                        scaleY = imageVisibility
                        alpha = imageVisibility
                    }
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .clickable(
                        enabled = state.sandwichState != SandwichState.OPEN,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = { state.toggleSandwich() },
                    )
                    .padding(ReplyDimens.grid1),
                contentAlignment = Alignment.Center,
            ) {
                val current = accounts.firstOrNull { it.isCurrentAccount } ?: accounts.firstOrNull()
                if (current != null) {
                    Avatar(
                        image = current.avatar,
                        contentDescription = "profile avatar image",
                        size = ReplyDimens.navigationDrawerProfileImageSizePadded,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavMenuRow(
    item: NavigationItem.Menu,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val colors = ReplyTheme.colors
    val tint = if (checked) colors.secondary else colors.onPrimarySurfaceMedium
    Row(
        Modifier
            .fillMaxWidth()
            .height(ReplyDimens.navigationDrawerMenuItemHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = ReplyDimens.grid4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(item.icon), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(ReplyDimens.grid4))
        ReplyText(text = item.title, style = ReplyTheme.typography.subtitle1, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NavFolderRow(name: String) {
    val colors = ReplyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(ReplyDimens.navigationDrawerMenuItemHeight)
            .clickable { }
            .padding(horizontal = ReplyDimens.grid4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_twotone_folder),
            contentDescription = null,
            tint = colors.onPrimarySurfaceMedium,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(ReplyDimens.grid4))
        ReplyText(text = name, style = ReplyTheme.typography.subtitle1, color = colors.onPrimarySurfaceMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NavDividerRow(title: String) {
    val colors = ReplyTheme.colors
    Column(Modifier.padding(horizontal = ReplyDimens.grid4, vertical = ReplyDimens.grid2)) {
        Box(
            Modifier
                .width(200.dp)
                .height(1.dp)
                .background(colors.onPrimarySurfaceDivider),
        )
        Spacer(Modifier.height(ReplyDimens.grid4))
        ReplyText(
            text = title.uppercase(),
            style = ReplyTheme.typography.overline,
            color = colors.onPrimarySurfaceMedium,
        )
    }
}

@Composable
private fun AccountRow(
    account: Account,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = ReplyTheme.colors
    val tint = if (account.isCurrentAccount) colors.secondary else colors.onPrimarySurfaceMedium
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = ReplyDimens.grid2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ReplyDimens.navigationDrawerProfileImageSize).padding(ReplyDimens.grid1)) {
            Avatar(image = account.avatar, contentDescription = account.email, size = ReplyDimens.navigationDrawerProfileImageSizePadded)
        }
        ReplyText(
            text = account.email,
            style = ReplyTheme.typography.body1,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = ReplyDimens.grid2),
        )
        if (account.isCurrentAccount) {
            Icon(
                painter = painterResource(Res.drawable.ic_done),
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .padding(end = ReplyDimens.grid2)
                    .size(24.dp),
            )
        }
    }
}
