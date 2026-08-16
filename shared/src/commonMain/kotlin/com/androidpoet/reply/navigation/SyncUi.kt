@file:OptIn(ExperimentalTime::class)

package com.androidpoet.reply.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.SyncStatus
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val TICK_MILLIS = 30_000L

@Composable
internal fun rememberNow(): State<Long> = produceState(Clock.System.now().toEpochMilliseconds()) {
    while (true) {
        delay(TICK_MILLIS)
        value = Clock.System.now().toEpochMilliseconds()
    }
}

internal fun syncStatusText(status: SyncStatus, lastSyncEpochMillis: Long?, now: Long): String = when (status) {
    SyncStatus.Syncing -> "Syncing…"
    is SyncStatus.Synced -> "Synced ${relativeTime(status.atEpochMillis, now)}"
    is SyncStatus.Failed -> if (lastSyncEpochMillis != null) {
        "Offline · synced ${relativeTime(lastSyncEpochMillis, now)}"
    } else {
        "Offline · showing saved mail"
    }
    SyncStatus.Idle -> if (lastSyncEpochMillis != null) "Synced ${relativeTime(lastSyncEpochMillis, now)}" else "Not synced yet"
}

internal fun relativeTime(then: Long, now: Long): String {
    val minutes = ((now - then) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60} h ago"
        else -> "${minutes / (60 * 24)} d ago"
    }
}

@Composable
internal fun SyncProgress(status: SyncStatus, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = status == SyncStatus.Syncing, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        LinearProgressIndicator(
            color = ReplyTheme.colors.secondary,
            trackColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(2.dp),
        )
    }
}

@Composable
internal fun SyncSnackbarHost(
    status: SyncStatus,
    hostState: SnackbarHostState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    LaunchedEffect(status) {
        if (status is SyncStatus.Failed) {
            val result = hostState.showSnackbar(message = "Couldn't refresh · showing saved mail", actionLabel = "Retry")
            if (result == SnackbarResult.ActionPerformed) onRetry()
        }
    }
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            containerColor = colors.primarySurface,
            contentColor = colors.onPrimarySurface,
            action = {
                data.visuals.actionLabel?.let { label ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(label, color = colors.secondary, style = ReplyTheme.typography.button)
                    }
                }
            },
        ) {
            Text(data.visuals.message, style = ReplyTheme.typography.body2)
        }
    }
}
