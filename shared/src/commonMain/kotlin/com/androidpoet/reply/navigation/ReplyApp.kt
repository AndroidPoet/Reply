@file:OptIn(ExperimentalComposeUiApi::class)

package com.androidpoet.reply.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import com.androidpoet.reply.ThemeMode
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.component.BottomAppBarFabOverhang
import com.androidpoet.reply.designsystem.component.BottomAppBarWithFab
import com.androidpoet.reply.designsystem.component.EditReplyIcon
import com.androidpoet.reply.designsystem.component.MenuBottomSheet
import com.androidpoet.reply.designsystem.component.MenuSheetItem
import com.androidpoet.reply.designsystem.component.ReplyBottomAppBar
import com.androidpoet.reply.designsystem.component.ReplyFab
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.motion.ContainerTransform
import com.androidpoet.reply.designsystem.motion.ContainerTransformSpec
import com.androidpoet.reply.designsystem.motion.Corners
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import com.androidpoet.reply.designsystem.motion.MaterialMotion
import com.androidpoet.reply.designsystem.motion.ProgressThresholds
import com.androidpoet.reply.designsystem.rememberViewModel
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_drop_up
import com.androidpoet.reply.designsystem.resources.ic_more_vert
import com.androidpoet.reply.designsystem.resources.ic_reply_logo
import com.androidpoet.reply.designsystem.resources.ic_search
import com.androidpoet.reply.designsystem.resources.ic_settings
import com.androidpoet.reply.designsystem.resources.ic_twotone_delete
import com.androidpoet.reply.designsystem.resources.ic_twotone_forward
import com.androidpoet.reply.designsystem.resources.ic_twotone_star
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.feature.compose.ComposeCard
import com.androidpoet.reply.feature.compose.ComposeScreen
import com.androidpoet.reply.feature.email.EmailDetailCard
import com.androidpoet.reply.feature.email.EmailScreen
import com.androidpoet.reply.feature.home.EmailCardBody
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
 * A running `MaterialContainerTransform`. While one is active the destination screen is kept
 * invisible and the overlay draws the morphing container instead; when it finishes the real
 * screen is revealed in exactly the same place.
 */
private sealed interface Transform {
    val entering: Boolean

    /** Home card ⇄ email detail (`EmailFragment.sharedElementEnterTransition`). */
    data class CardToEmail(
        val email: Email,
        val cardBounds: Rect,
        val topLeftCornerPx: Float,
        override val entering: Boolean,
    ) : Transform

    /** FAB → compose card (`ComposeFragment.enterTransition`). */
    data class FabToCompose(
        val replyToId: Long,
        val fabBounds: Rect,
        override val entering: Boolean,
    ) : Transform
}

/**
 * `MainActivity` + `activity_main.xml`: the nav host, the bottom navigation drawer above it, and
 * the bottom app bar + FAB on top of everything, reconfigured per destination — with the same
 * Material motion as the Views app: container transforms, elevation scale, fade through, shared
 * axis Z, and the FAB's edit ⇄ reply icon morph.
 */
@Composable
fun ReplyApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val graph = LocalAppGraph.current
    val colors = ReplyTheme.colors
    val density = LocalDensity.current
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute()) }
    val current = backStack.lastOrNull() ?: HomeRoute()
    val currentMailbox = (backStack.firstOrNull { it is HomeRoute } as? HomeRoute)?.mailbox ?: Mailbox.INBOX
    val accounts by graph.accountStore.userAccounts.collectAsStateWithLifecycle()

    val drawer = rememberBottomNavDrawerState()
    val fabOverhangPx = with(density) { BottomAppBarFabOverhang.toPx() }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showEmailOverflow by remember { mutableStateOf(false) }

    // ---- Container transforms ----
    var transform by remember { mutableStateOf<Transform?>(null) }
    val transformProgress = remember { Animatable(0f) }
    LaunchedEffect(transform) {
        val running = transform ?: return@LaunchedEffect
        transformProgress.snapTo(0f)
        transformProgress.animateTo(1f, tween(Durations.LARGE, easing = Interpolators.FastOutSlowIn))
        if (transform === running) transform = null
    }
    var fabBounds by remember { mutableStateOf(Rect.Zero) }
    val cardGeometry = remember { mutableMapOf<Long, Pair<Rect, Float>>() }

    // Compose's return transition: `Slide()` on the card only, 225ms accelerate.
    val composeExit = remember { Animatable(0f) }
    var composeWasShowing by remember { mutableStateOf(false) }

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
    // HideBottomViewOnScrollBehavior: 225ms linear_out_slow_in in, 175ms fast_out_linear_in out.
    val barTranslation by animateFloatAsState(
        targetValue = if (barVisible) 0f else barHeightPx,
        animationSpec = if (barVisible) {
            tween(Durations.BOTTOM_VIEW_ENTER, easing = Interpolators.LinearOutSlowIn)
        } else {
            tween(Durations.BOTTOM_VIEW_EXIT, easing = Interpolators.FastOutLinearIn)
        },
        label = "barTranslation",
    )
    // fab_show / fab_hide: scale + opacity, 175ms fast_out_slow_in.
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = tween(Durations.SMALL, easing = Interpolators.FastOutSlowIn),
        label = "fabScale",
    )

    fun navigateToHome(mailbox: Mailbox) {
        drawer.close()
        backStack.clear()
        backStack.add(HomeRoute(mailbox))
    }

    fun navigateToCompose(replyToId: Long) {
        barHiddenByScroll = false
        transform = Transform.FabToCompose(replyToId, fabBounds, entering = true)
        backStack.add(ComposeRoute(replyToId))
    }

    fun openEmail(email: Email, cardBounds: Rect, topLeftCornerPx: Float) {
        cardGeometry[email.id] = cardBounds to topLeftCornerPx
        transform = Transform.CardToEmail(email, cardBounds, topLeftCornerPx, entering = true)
        backStack.add(EmailRoute(email.id))
    }

    fun goBack() {
        when (val top = backStack.lastOrNull()) {
            is EmailRoute -> {
                val email = graph.emailStore.get(top.emailId)
                val geometry = cardGeometry[top.emailId]
                if (email != null && geometry != null) {
                    transform = Transform.CardToEmail(email, geometry.first, geometry.second, entering = false)
                }
                backStack.removeLastOrNull()
            }
            is HomeRoute -> if (backStack.size == 1 && top.mailbox != Mailbox.INBOX) navigateToHome(Mailbox.INBOX)
            else -> backStack.removeLastOrNull()
        }
    }

    // Home returns to Inbox before leaving the app (`nonInboxOnBackCallback`).
    BackHandler(enabled = backStack.size == 1 && (current as? HomeRoute)?.mailbox != Mailbox.INBOX && !drawer.isOpen) {
        navigateToHome(Mailbox.INBOX)
    }

    // android:windowBackground = ?android:colorBackground
    BoxWithConstraints(Modifier.fillMaxSize().background(colors.background)) {
        val rootWidthPx = with(density) { maxWidth.toPx() }
        val rootHeightPx = with(density) { maxHeight.toPx() }
        val statusTopPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }
        // Both the email detail card and the compose card sit 4dp in from the sides, 8dp below the
        // status bar, and fill the viewport (`fillViewport="true"`).
        val fullCardRect = Rect(
            offset = Offset(
                with(density) { ReplyDimens.grid0_5.toPx() },
                statusTopPx + with(density) { ReplyDimens.grid1.toPx() },
            ),
            size = Size(
                rootWidthPx - with(density) { (ReplyDimens.grid0_5 * 2).toPx() },
                rootHeightPx - statusTopPx - with(density) { ReplyDimens.grid1.toPx() },
            ),
        )
        val fullCardMinHeight = with(density) { fullCardRect.height.toDp() }

        // Slide the compose card down when it is popped (`returnTransition = Slide()`).
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

        // Nav3 keeps the back stack, decorators and entries; the transition itself is a plain
        // AnimatedContent so every enter/exit runs for its full Material duration.
        val routeContent: @Composable (NavKey) -> Unit = { key ->
            when (key) {
                is HomeRoute -> HomeScreen(
                    viewModel = rememberViewModel(key = "home_${key.mailbox}") {
                        graph.homeViewModelFactory.create(key.mailbox)
                    },
                    onEmailClick = { email, bounds, corner -> openEmail(email, bounds, corner) },
                    onReply = { navigateToCompose(it.id) },
                    scrollConnection = scrollConnection,
                )
                // Invisible while the container transform draws it.
                is EmailRoute -> Box(Modifier.graphicsLayer { alpha = if (transform is Transform.CardToEmail) 0f else 1f }) {
                    EmailScreen(
                        viewModel = rememberViewModel(key = "email_${key.emailId}") {
                            graph.emailViewModelFactory.create(key.emailId)
                        },
                        onNavigateUp = { goBack() },
                    )
                }
                is ComposeRoute -> Box(Modifier.graphicsLayer { alpha = if (transform is Transform.FabToCompose) 0f else 1f }) {
                    ComposeScreen(
                        viewModel = rememberViewModel(key = "compose_${key.replyToId}") {
                            graph.composeViewModelFactory.create(key.replyToId)
                        },
                        onClose = { goBack() },
                        cardTranslationY = { composeExit.value },
                    )
                }
                is SearchRoute -> SearchScreen(onBack = { goBack() })
                else -> Unit
            }
        }

        val entries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<HomeRoute> { key -> routeContent(key) }
                entry<EmailRoute> { key -> routeContent(key) }
                entry<ComposeRoute> { key -> routeContent(key) }
                entry<SearchRoute> { key -> routeContent(key) }
            },
        )
        val topEntry = entries.last()
        val popTracker = remember { PopTracker(backStack.toList()) }
        popTracker.update(entries.map { it.contentKey }, backStack.toList())
        BackHandler(enabled = backStack.size > 1) { goBack() }
        val screenTransition = updateTransition(targetState = topEntry, label = "screens")
        screenTransition.AnimatedContent(
            contentKey = { it.contentKey },
            transitionSpec = {
                materialTransition(
                    from = popTracker.routeFor(initialState.contentKey),
                    to = popTracker.routeFor(targetState.contentKey),
                    pop = popTracker.isPop,
                    // Deeper screens draw above shallower ones, so a popped screen exits on top.
                    zIndex = backStack.size.toFloat(),
                )
            },
        ) { entry ->
            val route = popTracker.routeFor(entry.contentKey)
            if (route != null && route !in backStack) {
                // A popped screen: Nav3's decorators no longer render it, so draw it directly for
                // the length of its exit transition.
                routeContent(route)
            } else {
                entry.Content()
            }
        }

        // ---- Container transform overlay (under the bottom app bar, like drawingViewId = nav host) ----
        transform?.let { running ->
            val cardColor = colors.elevated(colors.surface, ReplyDimens.plane01)
            val spec = when (running) {
                is Transform.CardToEmail -> {
                    val cardContent: @Composable () -> Unit = { EmailCardBody(running.email) }
                    val detailContent: @Composable () -> Unit = {
                        EmailDetailCard(email = running.email, onNavigateUp = {}, minHeight = fullCardMinHeight)
                    }
                    if (running.entering) {
                        ContainerTransformSpec(
                            startBounds = running.cardBounds, endBounds = fullCardRect,
                            startCorners = Corners(topLeft = running.topLeftCornerPx), endCorners = Corners(),
                            startColor = colors.surface, endColor = cardColor,
                            thresholds = ProgressThresholds.Enter,
                            startElevation = 0.dp, endElevation = ReplyDimens.plane01,
                            startContent = cardContent, endContent = detailContent,
                        )
                    } else {
                        ContainerTransformSpec(
                            startBounds = fullCardRect, endBounds = running.cardBounds,
                            startCorners = Corners(), endCorners = Corners(topLeft = running.topLeftCornerPx),
                            startColor = cardColor, endColor = colors.surface,
                            thresholds = ProgressThresholds.Return,
                            startElevation = ReplyDimens.plane01, endElevation = 0.dp,
                            startContent = detailContent, endContent = cardContent,
                        )
                    }
                }
                is Transform.FabToCompose -> {
                    val fabContent: @Composable () -> Unit = {
                        Box(Modifier.size(ReplyDimens.fabSize), contentAlignment = Alignment.Center) {
                            EditReplyIcon(
                                activated = running.replyToId >= 0,
                                tint = colors.onSecondary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    val composeContent: @Composable () -> Unit = {
                        val vm = remember(running.replyToId) { graph.composeViewModelFactory.create(running.replyToId) }
                        ComposeCard(viewModel = vm, onClose = {}, minHeight = fullCardMinHeight)
                    }
                    ContainerTransformSpec(
                        startBounds = running.fabBounds, endBounds = fullCardRect,
                        startCorners = Corners.all(with(density) { (ReplyDimens.fabSize / 2).toPx() }),
                        endCorners = Corners(),
                        startColor = colors.secondary, endColor = cardColor,
                        thresholds = ProgressThresholds.Enter,
                        startElevation = ReplyDimens.plane06, endElevation = ReplyDimens.plane01,
                        startContent = fabContent, endContent = composeContent,
                    )
                }
            }
            ContainerTransform(spec = spec, progress = transformProgress.value)
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
        val isEmail = current is EmailRoute
        BottomAppBarWithFab(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged {
                    barHeightPx = it.height.toFloat()
                    drawer.bottomBarHeight = it.height.toFloat() - fabOverhangPx
                }
                .graphicsLayer { translationY = barTranslation },
            fab = {
                ReplyFab(
                    onClick = { navigateToCompose(currentEmailId) },
                    modifier = Modifier
                        .semantics { contentDescription = if (isEmail) "Reply to email" else "Compose new email" }
                        .onGloballyPositioned { fabBounds = it.boundsInRoot() }
                        .graphicsLayer {
                            // The transform draws the FAB itself while it morphs into the compose card.
                            val hidden = transform is Transform.FabToCompose
                            scaleX = if (hidden) 0f else fabScale
                            scaleY = if (hidden) 0f else fabScale
                            alpha = if (hidden) 0f else fabScale
                        },
                ) {
                    EditReplyIcon(
                        activated = isEmail,
                        tint = colors.onSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
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
                                        transform = null
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

/**
 * Decides whether the latest back-stack change was a pop (new keys are a prefix of the old ones)
 * and remembers which route each Nav3 `contentKey` (a string) came from.
 */
private class PopTracker(initial: List<NavKey>) {
    private var stack: List<NavKey> = initial
    private val routes = mutableMapOf<Any, NavKey>()
    var isPop: Boolean = false
        private set

    fun update(contentKeys: List<Any>, current: List<NavKey>) {
        contentKeys.forEachIndexed { i, key -> current.getOrNull(i)?.let { routes[key] = it } }
        if (current == stack) return
        isPop = current.size < stack.size && stack.take(current.size) == current
        stack = current
    }

    fun routeFor(contentKey: Any): NavKey? = routes[contentKey]
}

/**
 * The Material motion pattern for each pair of destinations, exactly as `MainActivity` and the
 * fragments set them:
 * - Home → Email / Compose: home exits with `MaterialElevationScale`; the destination is drawn by
 *   the container transform, so it simply appears underneath.
 * - Email → Home: home re-enters with `MaterialElevationScale` while the container transform returns.
 * - Compose → Home: the card `Slide`s off the bottom (225ms) while home re-enters.
 * - Home ⇄ Search: `MaterialSharedAxis(Z)`.
 * - Home → Home (mailbox switch): `MaterialFadeThrough`.
 */
private fun materialTransition(from: Any?, to: Any?, pop: Boolean, zIndex: Float): ContentTransform {
    val transform = when {
        !pop && to is EmailRoute -> MaterialMotion.instantEnter togetherWith MaterialMotion.elevationScaleExit
        pop && from is EmailRoute -> MaterialMotion.elevationScaleReenter togetherWith MaterialMotion.instantExit
        !pop && to is ComposeRoute -> MaterialMotion.instantEnter togetherWith MaterialMotion.elevationScaleExit
        // Slide() only targets the card; the compose root stays put and vanishes when the slide ends.
        pop && from is ComposeRoute -> MaterialMotion.elevationScaleReenter togetherWith MaterialMotion.holdThenVanish
        !pop && to is SearchRoute -> MaterialMotion.sharedAxisZForwardEnter togetherWith MaterialMotion.sharedAxisZForwardExit
        pop && from is SearchRoute -> MaterialMotion.sharedAxisZBackwardEnter togetherWith MaterialMotion.sharedAxisZBackwardExit
        else -> MaterialMotion.fadeThroughEnter togetherWith MaterialMotion.fadeThroughExit
    }
    transform.targetContentZIndex = zIndex
    return transform
}
