@file:OptIn(ExperimentalComposeUiApi::class)

package com.androidpoet.reply.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.data.ThemeMode
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
    val state = rememberReplyAppState()
    val navigator = state.navigator
    val drawer = state.drawer

    LaunchedEffect(navigator.transform) {
        val running = navigator.transform ?: return@LaunchedEffect
        state.runTransform()
        if (navigator.transform === running) navigator.transform = null
    }
    LifecycleResumeEffect(state) {
        state.refreshIfStale()
        onPauseOrDispose {}
    }
    BackHandler(enabled = navigator.backStack.size > 1 && !drawer.isOpen) { navigator.goBack() }
    BackHandler(
        enabled = navigator.backStack.size == 1 && navigator.currentMailbox != Mailbox.INBOX && !drawer.isOpen,
    ) { navigator.navigateToHome(Mailbox.INBOX) }
    PrewarmImages()

    BoxWithConstraints(modifier.fillMaxSize().background(ReplyTheme.colors.background)) {
        val density = LocalDensity.current
        val rootHeightPx = with(density) { maxHeight.toPx() }
        val fullCardRect = rememberFullCardRect(maxWidth, maxHeight)
        ComposeExitEffect(state, rootHeightPx)

        ReplyNavHost(navigator = navigator) { key -> RouteContent(key, state) }

        navigator.transform?.let { running ->
            val graph = LocalAppGraph.current
            TransformOverlay(
                transform = running,
                progress = state.transformProgress.value,
                fullCardRect = fullCardRect,
                fullCardMinHeight = with(density) { fullCardRect.height.toDp() },
                emailStore = graph.emailStore,
                accountStore = graph.accountStore,
            )
        }
        Drawer(state)
        SyncOverlays(state)
        BottomBar(state)
    }

    AppMenus(
        showThemeMenu = state.showThemeMenu,
        showEmailOverflow = state.showEmailOverflow,
        onThemeModeChange = onThemeModeChange,
        onDismissTheme = { state.showThemeMenu = false },
        onDismissOverflow = { state.showEmailOverflow = false },
        onForward = { state.navigateToCompose(navigator.currentEmailId) },
    )
}

@Composable
private fun rememberFullCardRect(maxWidth: Dp, maxHeight: Dp): Rect {
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return remember(maxWidth, maxHeight, statusTop, density) {
        with(density) {
            Rect(
                offset = Offset(ReplyDimens.grid0_5.toPx(), (statusTop + ReplyDimens.grid1).toPx()),
                size = Size(
                    (maxWidth - ReplyDimens.grid0_5 * 2).toPx(),
                    (maxHeight - statusTop - ReplyDimens.grid1).toPx(),
                ),
            )
        }
    }
}

@Composable
private fun ComposeExitEffect(state: ReplyAppState, rootHeightPx: Float) {
    var composeWasShowing by remember { mutableStateOf(false) }
    val current = state.current
    LaunchedEffect(current) {
        if (current is ComposeRoute) {
            state.composeExit.snapTo(0f)
            composeWasShowing = true
        } else if (composeWasShowing) {
            composeWasShowing = false
            state.composeExit.animateTo(rootHeightPx, tween(Durations.MEDIUM, easing = Interpolators.Accelerate))
            state.composeExit.snapTo(0f)
        }
    }
}

@Composable
private fun RouteContent(key: NavKey, state: ReplyAppState) {
    val navigator = state.navigator
    when (key) {
        is HomeRoute -> HomeRouteContent(
            mailbox = key.mailbox,
            onEmailClick = navigator::openEmail,
            onReply = { state.navigateToCompose(it.id) },
            onArchive = state::archive,
            onDelete = state::delete,
            scrollConnection = state.scrollConnection,
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
                cardTranslationY = { state.composeExit.value },
            )
        }
        is SearchRoute -> SearchRouteContent(onResultClick = navigator::openEmail, onBack = navigator::goBack)
    }
}

@Composable
private fun BoxScope.Drawer(state: ReplyAppState) {
    val graph = LocalAppGraph.current
    val accounts by graph.accountStore.userAccounts.collectAsStateWithLifecycle()
    val folders by graph.emailStore.folders.collectAsStateWithLifecycle()
    val syncStatus by graph.repository.syncStatus.collectAsStateWithLifecycle()
    val lastSync by graph.settings.lastSyncEpochMillis.collectAsStateWithLifecycle(initialValue = null)
    val now by rememberNow()
    BottomNavDrawer(
        state = state.drawer,
        currentMailbox = state.navigator.currentMailbox,
        accounts = accounts,
        folders = folders,
        onMenuItemClick = { state.openMailbox(it.mailbox) },
        onAccountClick = { graph.accountStore.setCurrentUserAccount(it.id) },
        statusText = syncStatusText(syncStatus, lastSync, now),
    )
}

@Composable
private fun BoxScope.SyncOverlays(state: ReplyAppState) {
    val graph = LocalAppGraph.current
    val density = LocalDensity.current
    val syncStatus by graph.repository.syncStatus.collectAsStateWithLifecycle()
    SyncProgress(status = syncStatus, modifier = Modifier.align(Alignment.TopCenter))
    SyncSnackbarHost(
        status = syncStatus,
        hostState = state.snackbarHostState,
        onRetry = state::retrySync,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = with(density) { state.barHeightPx.toDp() } + ReplyDimens.grid1),
    )
}

@Composable
private fun BoxScope.BottomBar(state: ReplyAppState) {
    val density = LocalDensity.current
    val navigator = state.navigator
    val drawer = state.drawer
    val barVisible = state.barVisible
    val barTranslation by animateFloatAsState(
        targetValue = if (barVisible) 0f else state.barHeightPx,
        animationSpec = if (barVisible) {
            tween(Durations.BOTTOM_VIEW_ENTER, easing = Interpolators.LinearOutSlowIn)
        } else {
            tween(Durations.BOTTOM_VIEW_EXIT, easing = Interpolators.FastOutLinearIn)
        },
        label = "barTranslation",
    )
    val fabScale by animateFloatAsState(
        targetValue = if (state.fabVisible) 1f else 0f,
        animationSpec = tween(Durations.SMALL, easing = Interpolators.FastOutSlowIn),
        label = "fabScale",
    )
    val fabOverhangPx = with(density) { BottomAppBarFabOverhang.toPx() }
    ReplyBottomBar(
        title = if (state.current is HomeRoute) NavigationModel.titleFor(navigator.currentMailbox) else null,
        actions = when {
            drawer.isOpen -> BarActions.DrawerOpen
            state.isEmail -> BarActions.Email
            else -> BarActions.Home
        },
        replyMode = state.isEmail,
        fabVisibleFraction = { fabScale },
        fabHidden = navigator.transform is Transform.FabToCompose,
        chevronRotation = { 180f * drawer.openFraction - 180f * drawer.sandwichProgress },
        titleAlpha = { 1f - drawer.openFraction },
        onFabClick = { state.navigateToCompose(navigator.currentEmailId) },
        onFabPositioned = { state.fabBounds = it },
        onToggleDrawer = drawer::toggle,
        onSearch = state::openSearch,
        onSettings = {
            drawer.close()
            state.showThemeMenu = true
        },
        onStar = state::toggleStar,
        onDelete = state::deleteCurrentEmail,
        onMore = { state.showEmailOverflow = true },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .onSizeChanged {
                state.barHeightPx = it.height.toFloat()
                drawer.bottomBarHeight = it.height.toFloat() - fabOverhangPx
            }
            .graphicsLayer { translationY = barTranslation },
    )
}

@Composable
private fun ReplyNavHost(navigator: ReplyNavigator, routeContent: @Composable (NavKey) -> Unit) {
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
}

@Composable
private fun AppMenus(
    showThemeMenu: Boolean,
    showEmailOverflow: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDismissTheme: () -> Unit,
    onDismissOverflow: () -> Unit,
    onForward: () -> Unit,
) {
    if (showThemeMenu) {
        MenuBottomSheet(
            items = themeMenuItems,
            onDismiss = onDismissTheme,
            onItemClick = { item ->
                onThemeModeChange(
                    when (item.id) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    },
                )
                onDismissTheme()
            },
        )
    }
    if (showEmailOverflow) {
        MenuBottomSheet(
            items = remember { listOf(MenuSheetItem("forward", "Forward", Res.drawable.ic_twotone_forward)) },
            onDismiss = onDismissOverflow,
            onItemClick = {
                onDismissOverflow()
                onForward()
            },
        )
    }
}

@Composable
private fun HomeRouteContent(
    mailbox: Mailbox,
    onEmailClick: (email: Email, cardBoundsInRoot: Rect, topLeftCornerPx: Float) -> Unit,
    onReply: (Email) -> Unit,
    onArchive: (Email) -> Unit,
    onDelete: (Email) -> Unit,
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
        onArchive = onArchive,
        onDelete = onDelete,
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
private fun SearchRouteContent(onResultClick: (Email) -> Unit, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val viewModel = rememberViewModel(key = "search") { graph.searchViewModel }
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    SearchScreen(
        query = query,
        onQueryChange = viewModel::onQueryChange,
        results = results,
        onResultClick = onResultClick,
        onBack = onBack,
    )
}

@Composable
private fun ComposeRouteContent(replyToId: Long, onClose: () -> Unit, cardTranslationY: () -> Float) {
    val graph = LocalAppGraph.current
    val viewModel = rememberViewModel(key = "compose_$replyToId") { graph.composeViewModelFactory.create(replyToId) }
    ComposeScreen(draft = viewModel.draft, onClose = onClose, cardTranslationY = cardTranslationY)
}
