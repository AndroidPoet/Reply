@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.androidpoet.reply.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.androidpoet.reply.ThemeMode
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.LocalSharedTransitionScope
import com.androidpoet.reply.designsystem.component.BottomAppBarFabOverhang
import com.androidpoet.reply.designsystem.component.BottomAppBarWithFab
import com.androidpoet.reply.designsystem.component.MenuBottomSheet
import com.androidpoet.reply.designsystem.component.MenuSheetItem
import com.androidpoet.reply.designsystem.component.ReplyBottomAppBar
import com.androidpoet.reply.designsystem.component.ReplyFab
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.rememberViewModel
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_drop_up
import com.androidpoet.reply.designsystem.resources.ic_edit
import com.androidpoet.reply.designsystem.resources.ic_more_vert
import com.androidpoet.reply.designsystem.resources.ic_reply_all
import com.androidpoet.reply.designsystem.resources.ic_reply_logo
import com.androidpoet.reply.designsystem.resources.ic_search
import com.androidpoet.reply.designsystem.resources.ic_settings
import com.androidpoet.reply.designsystem.resources.ic_twotone_delete
import com.androidpoet.reply.designsystem.resources.ic_twotone_forward
import com.androidpoet.reply.designsystem.resources.ic_twotone_star
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyMotion
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.feature.compose.ComposeScreen
import com.androidpoet.reply.feature.email.EmailScreen
import com.androidpoet.reply.feature.home.HomeScreen
import com.androidpoet.reply.feature.nav.BottomNavDrawer
import com.androidpoet.reply.feature.nav.NavigationModel
import com.androidpoet.reply.feature.nav.rememberBottomNavDrawerState
import com.androidpoet.reply.feature.search.SearchScreen
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

/** Navigation 3 destination keys — the four fragments of `navigation_graph.xml`. */
@Serializable
data class HomeRoute(val mailbox: Mailbox = Mailbox.INBOX) : NavKey

@Serializable
data class EmailRoute(val emailId: Long) : NavKey

@Serializable
data class ComposeRoute(val replyToId: Long = -1L) : NavKey

@Serializable
data object SearchRoute : NavKey

/**
 * `MainActivity` + `activity_main.xml`: the nav host, the bottom navigation drawer above it, and
 * the bottom app bar + FAB on top of everything, reconfigured per destination.
 */
@Composable
fun ReplyApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val graph = LocalAppGraph.current
    val colors = ReplyTheme.colors
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute()) }
    val current = backStack.lastOrNull() ?: HomeRoute()
    val currentMailbox = (backStack.firstOrNull { it is HomeRoute } as? HomeRoute)?.mailbox ?: Mailbox.INBOX
    val accounts by graph.accountStore.userAccounts.collectAsStateWithLifecycle()

    val drawer = rememberBottomNavDrawerState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val fabOverhangPx = with(density) { BottomAppBarFabOverhang.toPx() }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showEmailOverflow by remember { mutableStateOf(false) }

    // hideOnScroll: the bar (and its FAB) slide away when the list scrolls down, back when it scrolls up.
    var barHiddenByScroll by remember { mutableStateOf(false) }
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y < -2f) barHiddenByScroll = true
                if (consumed.y > 2f) barHiddenByScroll = false
                return Offset.Zero
            }
        }
    }

    val barVisible = when (current) {
        is HomeRoute, is EmailRoute -> !(barHiddenByScroll && current is HomeRoute)
        else -> false
    }
    val fabVisible = barVisible && !drawer.isOpen
    var barHeightPx by remember { mutableStateOf(0f) }
    val barTranslation by animateFloatAsState(
        targetValue = if (barVisible) 0f else barHeightPx,
        animationSpec = tween(
            if (barVisible) ReplyMotion.DURATION_MEDIUM else ReplyMotion.DURATION_SMALL,
            easing = if (barVisible) ReplyMotion.Incoming else ReplyMotion.Outgoing,
        ),
        label = "barTranslation",
    )
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = tween(ReplyMotion.DURATION_SMALL, easing = ReplyMotion.Persistent),
        label = "fabScale",
    )

    fun navigateToHome(mailbox: Mailbox) {
        drawer.close()
        backStack.clear()
        backStack.add(HomeRoute(mailbox))
    }

    fun navigateToCompose(replyToId: Long) {
        barHiddenByScroll = false
        backStack.add(ComposeRoute(replyToId))
    }

    Box(Modifier.fillMaxSize()) {
        SharedTransitionLayout(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    transitionSpec = {
                        // MaterialElevationScale / FadeThrough approximation.
                        (fadeIn(tween(ReplyMotion.DURATION_LARGE)) + scaleIn(tween(ReplyMotion.DURATION_LARGE), initialScale = 0.92f))
                            .togetherWith(fadeOut(tween(ReplyMotion.DURATION_LARGE)) + scaleOut(tween(ReplyMotion.DURATION_LARGE), targetScale = 1.0f))
                    },
                    popTransitionSpec = {
                        (fadeIn(tween(ReplyMotion.DURATION_LARGE)) + scaleIn(tween(ReplyMotion.DURATION_LARGE), initialScale = 1.0f))
                            .togetherWith(fadeOut(tween(ReplyMotion.DURATION_LARGE)) + scaleOut(tween(ReplyMotion.DURATION_LARGE), targetScale = 0.92f))
                    },
                    entryProvider = entryProvider {
                        entry<HomeRoute> { key ->
                            HomeScreen(
                                viewModel = rememberViewModel(key = "home_${key.mailbox}") {
                                    graph.homeViewModelFactory.create(key.mailbox)
                                },
                                onEmailClick = { backStack.add(EmailRoute(it.id)) },
                                onReply = { navigateToCompose(it.id) },
                                scrollConnection = scrollConnection,
                            )
                        }
                        entry<EmailRoute> { key ->
                            EmailScreen(
                                viewModel = rememberViewModel(key = "email_${key.emailId}") {
                                    graph.emailViewModelFactory.create(key.emailId)
                                },
                                onNavigateUp = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<ComposeRoute> { key ->
                            ComposeScreen(
                                viewModel = rememberViewModel(key = "compose_${key.replyToId}") {
                                    graph.composeViewModelFactory.create(key.replyToId)
                                },
                                onClose = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<SearchRoute> {
                            SearchScreen(onBack = { backStack.removeLastOrNull() })
                        }
                    },
                )
            }
        }

        // Bottom navigation drawer (under the bar).
        BottomNavDrawer(
            state = drawer,
            currentMailbox = currentMailbox,
            accounts = accounts,
            folders = graph.emailStore.getAllFolders(),
            onMenuItemClick = { navigateToHome(it.mailbox) },
            onAccountClick = { graph.accountStore.setCurrentUserAccount(it.id) },
        )

        // Bottom app bar + FAB.
        val chevronRotation = 180f * drawer.openFraction - 180f * drawer.sandwichProgress
        val currentEmailId = (current as? EmailRoute)?.emailId ?: -1L
        BottomAppBarWithFab(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged {
                    barHeightPx = it.height.toFloat()
                    drawer.bottomBarHeight = it.height.toFloat() - fabOverhangPx
                }
                .graphicsLayer { translationY = barTranslation },
            fab = {
                val isEmail = current is EmailRoute
                ReplyFab(
                    icon = painterResource(if (isEmail) Res.drawable.ic_reply_all else Res.drawable.ic_edit),
                    iconKey = isEmail,
                    contentDescription = if (isEmail) "Reply to email" else "Compose new email",
                    onClick = { navigateToCompose(currentEmailId) },
                    modifier = Modifier.graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                        alpha = fabScale
                    },
                )
            },
            bar = {
                ReplyBottomAppBar(
                    navigation = {
                        // Chevron · logo · title — one 48dp-tall tappable cluster with a pill ripple.
                        Row(
                            Modifier
                                .padding(vertical = ReplyDimens.grid0_5)
                                .height(ReplyDimens.minTouchTarget)
                                .clip(RoundedCornerShape(ReplyDimens.grid3))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = colors.onPrimarySurface),
                                ) { drawer.toggle() }
                                .padding(horizontal = ReplyDimens.grid0_5),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_drop_up),
                                contentDescription = "Toggle navigation drawer",
                                tint = colors.onPrimarySurface,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { rotationZ = chevronRotation },
                            )
                            Icon(
                                painter = painterResource(Res.drawable.ic_reply_logo),
                                contentDescription = "Reply logo",
                                tint = colors.onPrimarySurface,
                                modifier = Modifier
                                    .padding(start = ReplyDimens.grid1)
                                    .size(ReplyDimens.bottomAppBarLogoSize),
                            )
                            if (current is HomeRoute) {
                                ReplyText(
                                    text = NavigationModel.titleFor(currentMailbox),
                                    style = ReplyTheme.typography.body1,
                                    color = colors.onPrimarySurface,
                                    modifier = Modifier
                                        .padding(horizontal = ReplyDimens.grid1)
                                        .graphicsLayer { alpha = 1f - drawer.openFraction },
                                )
                            }
                        }
                    },
                    actions = {
                        when {
                            drawer.isOpen -> ReplyIconButton(
                                icon = painterResource(Res.drawable.ic_settings),
                                contentDescription = "Settings",
                                onClick = {
                                    drawer.close()
                                    showThemeMenu = true
                                },
                            )
                            current is EmailRoute -> {
                                ReplyIconButton(
                                    icon = painterResource(Res.drawable.ic_twotone_star),
                                    contentDescription = "Star",
                                    onClick = { graph.emailStore.toggleStar(currentEmailId) },
                                )
                                ReplyIconButton(
                                    icon = painterResource(Res.drawable.ic_twotone_delete),
                                    contentDescription = "Delete",
                                    onClick = {
                                        graph.emailStore.delete(currentEmailId)
                                        backStack.removeLastOrNull()
                                    },
                                )
                                ReplyIconButton(
                                    icon = painterResource(Res.drawable.ic_more_vert),
                                    contentDescription = "More options",
                                    onClick = { showEmailOverflow = true },
                                )
                            }
                            else -> ReplyIconButton(
                                icon = painterResource(Res.drawable.ic_search),
                                contentDescription = "Search",
                                onClick = {
                                    barHiddenByScroll = false
                                    backStack.add(SearchRoute)
                                },
                            )
                        }
                        Spacer(Modifier.width(0.dp))
                    },
                )
            },
        )
    }

    if (showThemeMenu) {
        MenuBottomSheet(
            items = listOf(
                MenuSheetItem("light", "Light"),
                MenuSheetItem("dark", "Dark"),
                MenuSheetItem("system", "System default"),
            ),
            onDismiss = { showThemeMenu = false },
            onItemClick = { item ->
                when (item.id) {
                    "light" -> onThemeModeChange(ThemeMode.LIGHT)
                    "dark" -> onThemeModeChange(ThemeMode.DARK)
                    "system" -> onThemeModeChange(ThemeMode.SYSTEM)
                }
                showThemeMenu = false
            },
        )
    }

    if (showEmailOverflow) {
        MenuBottomSheet(
            items = listOf(MenuSheetItem("forward", "Forward", Res.drawable.ic_twotone_forward)),
            onDismiss = { showEmailOverflow = false },
            onItemClick = {
                showEmailOverflow = false
                navigateToCompose((current as? EmailRoute)?.emailId ?: -1L)
            },
        )
    }
}
