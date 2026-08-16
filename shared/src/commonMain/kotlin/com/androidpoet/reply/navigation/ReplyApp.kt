@file:OptIn(ExperimentalComposeUiApi::class)

package com.androidpoet.reply.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.androidpoet.reply.ThemeMode
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.component.BottomAppBarFabOverhang
import com.androidpoet.reply.designsystem.component.MenuBottomSheet
import com.androidpoet.reply.designsystem.component.MenuSheetItem
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import com.androidpoet.reply.designsystem.rememberViewModel
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_twotone_forward
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.feature.compose.ComposeScreen
import com.androidpoet.reply.feature.email.EmailScreen
import com.androidpoet.reply.feature.home.HomeScreen
import com.androidpoet.reply.feature.nav.BottomNavDrawer
import com.androidpoet.reply.feature.nav.NavigationModel
import com.androidpoet.reply.feature.nav.rememberBottomNavDrawerState
import com.androidpoet.reply.feature.search.SearchScreen

private val themeMenuItems = listOf(
    MenuSheetItem("light", "Light"),
    MenuSheetItem("dark", "Dark"),
    MenuSheetItem("system", "System default"),
)

@Composable
fun ReplyApp(
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val graph = LocalAppGraph.current
    val colors = ReplyTheme.colors
    val density = LocalDensity.current
    val navigator = rememberReplyNavigator(graph.emailStore)
    val current = navigator.current
    val accounts by graph.accountStore.userAccounts.collectAsStateWithLifecycle()
    val folders by graph.emailStore.folders.collectAsStateWithLifecycle()

    val drawer = rememberBottomNavDrawerState()
    var showThemeMenu by remember { mutableStateOf(false) }
    var showEmailOverflow by remember { mutableStateOf(false) }

    val transformProgress = remember { Animatable(0f) }
    LaunchedEffect(navigator.transform) {
        val running = navigator.transform ?: return@LaunchedEffect
        transformProgress.snapTo(0f)
        transformProgress.animateTo(1f, tween(Durations.LARGE, easing = Interpolators.FastOutSlowIn))
        if (navigator.transform === running) navigator.transform = null
    }
    var fabBounds by remember { mutableStateOf(Rect.Zero) }

    val composeExit = remember { Animatable(0f) }
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

    val isEmail = current is EmailRoute
    val barVisible = isEmail || (current is HomeRoute && !barHiddenByScroll)
    val fabVisible = barVisible && !drawer.isOpen
    var barHeightPx by remember { mutableFloatStateOf(0f) }
    val barTranslation by animateFloatAsState(
        targetValue = if (barVisible) 0f else barHeightPx,
        animationSpec = if (barVisible) {
            tween(Durations.BOTTOM_VIEW_ENTER, easing = Interpolators.LinearOutSlowIn)
        } else {
            tween(Durations.BOTTOM_VIEW_EXIT, easing = Interpolators.FastOutLinearIn)
        },
        label = "barTranslation",
    )
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = tween(Durations.SMALL, easing = Interpolators.FastOutSlowIn),
        label = "fabScale",
    )

    fun navigateToCompose(replyToId: Long) {
        barHiddenByScroll = false
        navigator.navigateToCompose(replyToId, fabBounds)
    }

    BackHandler(enabled = navigator.backStack.size > 1 && !drawer.isOpen) { navigator.goBack() }
    BackHandler(
        enabled = navigator.backStack.size == 1 && navigator.currentMailbox != Mailbox.INBOX && !drawer.isOpen,
    ) { navigator.navigateToHome(Mailbox.INBOX) }

    PrewarmImages()

    BoxWithConstraints(modifier.fillMaxSize().background(colors.background)) {
        val rootWidthPx = with(density) { maxWidth.toPx() }
        val rootHeightPx = with(density) { maxHeight.toPx() }
        val statusTopPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }
        val fullCardRect = remember(rootWidthPx, rootHeightPx, statusTopPx) {
            with(density) {
                Rect(
                    offset = Offset(ReplyDimens.grid0_5.toPx(), statusTopPx + ReplyDimens.grid1.toPx()),
                    size = Size(
                        rootWidthPx - (ReplyDimens.grid0_5 * 2).toPx(),
                        rootHeightPx - statusTopPx - ReplyDimens.grid1.toPx(),
                    ),
                )
            }
        }
        val fullCardMinHeight = with(density) { fullCardRect.height.toDp() }

        var composeWasShowing by remember { mutableStateOf(false) }
        LaunchedEffect(current) {
            if (current is ComposeRoute) {
                composeExit.snapTo(0f)
                composeWasShowing = true
            } else if (composeWasShowing) {
                composeWasShowing = false
                composeExit.animateTo(rootHeightPx, tween(Durations.MEDIUM, easing = Interpolators.Accelerate))
                composeExit.snapTo(0f)
            }
        }

        val routeContent: @Composable (NavKey) -> Unit = { key ->
            when (key) {
                is HomeRoute -> HomeRouteContent(
                    mailbox = key.mailbox,
                    onEmailClick = navigator::openEmail,
                    onReply = { navigateToCompose(it.id) },
                    scrollConnection = scrollConnection,
                )
                is EmailRoute -> Box(
                    Modifier.graphicsLayer { alpha = if (navigator.transform is Transform.CardToEmail) 0f else 1f },
                ) {
                    EmailRouteContent(emailId = key.emailId, onNavigateUp = navigator::goBack)
                }
                is ComposeRoute -> Box(
                    Modifier.graphicsLayer { alpha = if (navigator.transform is Transform.FabToCompose) 0f else 1f },
                ) {
                    ComposeRouteContent(
                        replyToId = key.replyToId,
                        onClose = navigator::goBack,
                        cardTranslationY = { composeExit.value },
                    )
                }
                is SearchRoute -> SearchScreen(onBack = navigator::goBack)
            }
        }

        val entries = rememberDecoratedNavEntries(
            backStack = navigator.backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<HomeRoute> { routeContent(it) }
                entry<EmailRoute> { routeContent(it) }
                entry<ComposeRoute> { routeContent(it) }
                entry<SearchRoute> { routeContent(it) }
            },
        )
        val depth = navigator.backStack.size
        val screenTransition = updateTransition(targetState = entries.last(), label = "screens")
        screenTransition.AnimatedContent(
            contentKey = { it.contentKey },
            transitionSpec = {
                materialTransition(
                    from = navigator.routeFor(initialState.contentKey),
                    to = navigator.routeFor(targetState.contentKey),
                    pop = navigator.lastChangeWasPop,
                    zIndex = depth.toFloat(),
                )
            },
        ) { entry ->
            val route = navigator.routeFor(entry.contentKey)
            if (route == null || route in navigator.backStack) entry.Content() else routeContent(route)
        }

        navigator.transform?.let { running ->
            TransformOverlay(
                transform = running,
                progress = transformProgress.value,
                fullCardRect = fullCardRect,
                fullCardMinHeight = fullCardMinHeight,
                emailStore = graph.emailStore,
                accountStore = graph.accountStore,
            )
        }

        BottomNavDrawer(
            state = drawer,
            currentMailbox = navigator.currentMailbox,
            accounts = accounts,
            folders = folders,
            onMenuItemClick = {
                drawer.close()
                navigator.navigateToHome(it.mailbox)
            },
            onAccountClick = { graph.accountStore.setCurrentUserAccount(it.id) },
        )

        val fabOverhangPx = with(density) { BottomAppBarFabOverhang.toPx() }
        ReplyBottomBar(
            title = if (current is HomeRoute) NavigationModel.titleFor(navigator.currentMailbox) else null,
            actions = when {
                drawer.isOpen -> BarActions.DrawerOpen
                isEmail -> BarActions.Email
                else -> BarActions.Home
            },
            replyMode = isEmail,
            fabVisibleFraction = { fabScale },
            fabHidden = navigator.transform is Transform.FabToCompose,
            chevronRotation = { 180f * drawer.openFraction - 180f * drawer.sandwichProgress },
            titleAlpha = { 1f - drawer.openFraction },
            onFabClick = { navigateToCompose(navigator.currentEmailId) },
            onFabPositioned = { fabBounds = it },
            onToggleDrawer = drawer::toggle,
            onSearch = {
                barHiddenByScroll = false
                navigator.openSearch()
            },
            onSettings = {
                drawer.close()
                showThemeMenu = true
            },
            onStar = { graph.emailStore.toggleStar(navigator.currentEmailId) },
            onDelete = navigator::deleteCurrentEmail,
            onMore = { showEmailOverflow = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged {
                    barHeightPx = it.height.toFloat()
                    drawer.bottomBarHeight = it.height.toFloat() - fabOverhangPx
                }
                .graphicsLayer { translationY = barTranslation },
        )
    }

    if (showThemeMenu) {
        MenuBottomSheet(
            items = themeMenuItems,
            onDismiss = { showThemeMenu = false },
            onItemClick = { item ->
                onThemeModeChange(
                    when (item.id) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    },
                )
                showThemeMenu = false
            },
        )
    }

    if (showEmailOverflow) {
        MenuBottomSheet(
            items = remember { listOf(MenuSheetItem("forward", "Forward", Res.drawable.ic_twotone_forward)) },
            onDismiss = { showEmailOverflow = false },
            onItemClick = {
                showEmailOverflow = false
                navigateToCompose(navigator.currentEmailId)
            },
        )
    }
}

@Composable
private fun HomeRouteContent(
    mailbox: Mailbox,
    onEmailClick: (email: Email, cardBoundsInRoot: Rect, topLeftCornerPx: Float) -> Unit,
    onReply: (Email) -> Unit,
    scrollConnection: NestedScrollConnection,
) {
    val graph = LocalAppGraph.current
    val viewModel = rememberViewModel(key = "home_$mailbox") { graph.homeViewModelFactory.create(mailbox) }
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    HomeScreen(
        emails = emails,
        onEmailClick = onEmailClick,
        onStarChanged = viewModel::setStarred,
        onReply = onReply,
        onArchive = viewModel::archive,
        onDelete = viewModel::delete,
        scrollConnection = scrollConnection,
    )
}

@Composable
private fun EmailRouteContent(emailId: Long, onNavigateUp: () -> Unit) {
    val graph = LocalAppGraph.current
    val viewModel = rememberViewModel(key = "email_$emailId") { graph.emailViewModelFactory.create(emailId) }
    val email by viewModel.email.collectAsStateWithLifecycle()
    EmailScreen(email = email, onNavigateUp = onNavigateUp)
}

@Composable
private fun ComposeRouteContent(replyToId: Long, onClose: () -> Unit, cardTranslationY: () -> Float) {
    val graph = LocalAppGraph.current
    val viewModel = rememberViewModel(key = "compose_$replyToId") { graph.composeViewModelFactory.create(replyToId) }
    ComposeScreen(draft = viewModel.draft, onClose = onClose, cardTranslationY = cardTranslationY)
}
