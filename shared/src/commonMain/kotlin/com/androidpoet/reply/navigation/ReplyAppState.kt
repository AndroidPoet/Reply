package com.androidpoet.reply.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.data.ReplyRepository
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.feature.nav.BottomNavDrawerState
import com.androidpoet.reply.feature.nav.rememberBottomNavDrawerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val SCROLL_HIDE_THRESHOLD_PX = 2f

@Stable
internal class ReplyAppState(
    val navigator: ReplyNavigator,
    val drawer: BottomNavDrawerState,
    val snackbarHostState: SnackbarHostState,
    private val emailStore: EmailStore,
    private val repository: ReplyRepository,
    private val scope: CoroutineScope,
) {
    var barHiddenByScroll by mutableStateOf(false)
    var fabBounds by mutableStateOf(Rect.Zero)
    var barHeightPx by mutableFloatStateOf(0f)
    var showThemeMenu by mutableStateOf(false)
    var showEmailOverflow by mutableStateOf(false)

    val transformProgress = Animatable(0f)
    val composeExit = Animatable(0f)

    val scrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (consumed.y < -SCROLL_HIDE_THRESHOLD_PX) barHiddenByScroll = true
            if (consumed.y > SCROLL_HIDE_THRESHOLD_PX) barHiddenByScroll = false
            return Offset.Zero
        }
    }

    val current get() = navigator.current
    val isEmail: Boolean get() = current is EmailRoute
    val barVisible: Boolean get() = isEmail || (current is HomeRoute && !barHiddenByScroll)
    val fabVisible: Boolean get() = barVisible && !drawer.isOpen

    fun navigateToCompose(replyToId: Long) {
        barHiddenByScroll = false
        navigator.navigateToCompose(replyToId, fabBounds)
    }

    fun openSearch() {
        barHiddenByScroll = false
        navigator.openSearch()
    }

    fun openMailbox(mailbox: Mailbox) {
        drawer.close()
        navigator.navigateToHome(mailbox)
    }

    fun toggleStar() = emailStore.toggleStar(navigator.currentEmailId)

    fun deleteCurrentEmail() {
        emailStore.get(navigator.currentEmailId)?.let { moveWithUndo(it, Mailbox.TRASH, "Moved to Trash") }
        navigator.leaveEmailAfterDelete()
    }

    fun archive(email: Email) = moveWithUndo(email, Mailbox.TRASH, "Archived")

    fun delete(email: Email) = moveWithUndo(email, Mailbox.TRASH, "Moved to Trash")

    fun retrySync() {
        scope.launch { repository.refresh() }
    }

    fun refreshIfStale() {
        scope.launch { repository.refreshIfStale() }
    }

    suspend fun runTransform() {
        transformProgress.snapTo(0f)
        transformProgress.animateTo(1f, tween(Durations.LARGE, easing = Interpolators.FastOutSlowIn))
    }

    private fun moveWithUndo(email: Email, target: Mailbox, message: String) {
        val previous = emailStore.moveTo(email.id, target) ?: return
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) emailStore.moveTo(email.id, previous)
        }
    }
}

@Composable
internal fun rememberReplyAppState(): ReplyAppState {
    val graph = LocalAppGraph.current
    val navigator = rememberReplyNavigator(graph.emailStore)
    val drawer = rememberBottomNavDrawerState()
    val scope = rememberCoroutineScope()
    return remember(navigator, drawer) {
        ReplyAppState(navigator, drawer, SnackbarHostState(), graph.emailStore, graph.repository, scope)
    }
}
