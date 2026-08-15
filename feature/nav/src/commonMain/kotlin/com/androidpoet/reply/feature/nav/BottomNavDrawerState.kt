package com.androidpoet.reply.feature.nav

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.androidpoet.reply.designsystem.theme.ReplyMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/** `BottomSheetBehavior` states used by the drawer (`skipCollapsed`, `hideable`, half ratio 0.6). */
enum class DrawerValue { Hidden, HalfExpanded, Expanded }

/** `BottomNavDrawerFragment.SandwichState`. */
enum class SandwichState { CLOSED, OPEN, SETTLING }

private const val HALF_EXPANDED_RATIO = 0.6f
private const val FLING_VELOCITY_THRESHOLD = 800f

/**
 * Drives the bottom navigation drawer: a draggable sheet with three anchors, plus the "sandwich"
 * that slides the foreground down to reveal the account picker.
 *
 * [position] is the sheet's top edge as a fraction of the container height:
 * 1 = hidden, 0.4 = half expanded, 0 = expanded.
 */
@Stable
class BottomNavDrawerState(private val scope: CoroutineScope) {

    private var settleJob: Job? = null
    private var sandwichJob: Job? = null

    /** Container height in px, reported by layout. */
    var containerHeight by mutableFloatStateOf(0f)

    /** Height of the account list in px, reported by layout (sandwich target). */
    var accountListHeight by mutableFloatStateOf(0f)

    /** Height of the bottom app bar assembly (bar + nav-bar inset) in px. */
    var bottomBarHeight by mutableFloatStateOf(0f)

    var position by mutableFloatStateOf(1f)
        private set

    var currentValue by mutableStateOf(DrawerValue.Hidden)
        private set

    val isOpen: Boolean get() = position < 1f

    /** 0 while hidden → 1 at half expanded (drives scrim, chevron, title, FAB). */
    val openFraction: Float get() = ((1f - position) / (1f - hiddenToHalf)).coerceIn(0f, 1f)

    /** 0 at half expanded → 1 at fully expanded (drives shape squaring + top inset). */
    val expandFraction: Float
        get() = if (hiddenToHalf == 0f) 0f else ((hiddenToHalf - position) / hiddenToHalf).coerceIn(0f, 1f)

    private val hiddenToHalf: Float get() = 1f - HALF_EXPANDED_RATIO

    // ---- Sandwich (account picker) ----

    var sandwichProgress by mutableFloatStateOf(0f)
        private set
    val sandwichState: SandwichState
        get() = when (sandwichProgress) {
            0f -> SandwichState.CLOSED
            1f -> SandwichState.OPEN
            else -> SandwichState.SETTLING
        }

    /** `lerp(0, 1, 0, 0.5, progress)` — the nav foreground's share of the sandwich animation. */
    val navProgress: Float get() = (sandwichProgress / 0.5f).coerceIn(0f, 1f)

    /** `lerp(0, 1, 0.5, 1, progress)` — the account list's share of the sandwich animation. */
    val accountProgress: Float get() = ((sandwichProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)

    /** Extra translation applied to the whole sheet so only the account list peeks above the bar. */
    val sandwichTranslation: Float
        get() {
            if (sandwichProgress == 0f || containerHeight == 0f) return 0f
            val targetTop = containerHeight - accountListHeight - bottomBarHeight
            val currentTop = position * containerHeight
            return sandwichProgress * (targetTop - currentTop)
        }

    fun open() = animateTo(DrawerValue.HalfExpanded)

    fun close() = animateTo(DrawerValue.Hidden)

    fun toggle() {
        when {
            sandwichState == SandwichState.OPEN -> toggleSandwich()
            currentValue == DrawerValue.Hidden && !isOpen -> open()
            else -> close()
        }
    }

    fun toggleSandwich() {
        val target = when (sandwichState) {
            SandwichState.CLOSED -> 1f
            SandwichState.OPEN -> 0f
            SandwichState.SETTLING -> return
        }
        sandwichJob?.cancel()
        sandwichJob = scope.launch {
            val start = sandwichProgress
            val distance = abs(target - start)
            animate(
                initialValue = start,
                targetValue = target,
                animationSpec = tween((distance * ReplyMotion.DURATION_MEDIUM).toInt(), easing = ReplyMotion.Persistent),
            ) { value, _ -> sandwichProgress = value }
        }
    }

    private fun closeSandwichImmediately() {
        sandwichJob?.cancel()
        sandwichProgress = 0f
    }

    private fun anchor(value: DrawerValue): Float = when (value) {
        DrawerValue.Hidden -> 1f
        DrawerValue.HalfExpanded -> hiddenToHalf
        DrawerValue.Expanded -> 0f
    }

    fun animateTo(value: DrawerValue) {
        closeSandwichImmediately()
        settleJob?.cancel()
        settleJob = scope.launch {
            animate(
                initialValue = position,
                targetValue = anchor(value),
                animationSpec = tween(ReplyMotion.DURATION_LARGE, easing = ReplyMotion.Persistent),
            ) { v, _ -> position = v }
            currentValue = value
        }
    }

    // ---- Dragging ----

    internal fun onDragStart() {
        settleJob?.cancel()
        closeSandwichImmediately()
    }

    /** Move the sheet by [deltaPx]; returns the amount actually consumed. */
    internal fun dragBy(deltaPx: Float): Float {
        if (containerHeight == 0f) return 0f
        val before = position
        val after = (before + deltaPx / containerHeight).coerceIn(0f, 1f)
        if (after == before) return 0f
        position = after
        return (after - before) * containerHeight
    }

    /** Settle to the nearest anchor, biased by [velocityPx] (px/s, positive = downward). */
    internal fun settle(velocityPx: Float) {
        val p = position
        val target = when {
            velocityPx > FLING_VELOCITY_THRESHOLD -> if (p < hiddenToHalf) DrawerValue.HalfExpanded else DrawerValue.Hidden
            velocityPx < -FLING_VELOCITY_THRESHOLD -> if (p > hiddenToHalf) DrawerValue.HalfExpanded else DrawerValue.Expanded
            else -> DrawerValue.entries.minByOrNull { abs(anchor(it) - p) } ?: DrawerValue.Hidden
        }
        animateTo(target)
    }

    private val atAnchor: Boolean
        get() = DrawerValue.entries.any { abs(anchor(it) - position) < 0.0005f }

    /** Lets the nav list hand its overscroll to the sheet, like `BottomSheetBehavior` nested scrolling. */
    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Content wants to move up (finger up): expand the sheet first.
            if (available.y < 0f && position > 0f) {
                onDragStartIfNeeded()
                val consumed = dragBy(available.y)
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // List is at its top and finger keeps moving down: pull the sheet down.
            if (available.y > 0f && position < 1f) {
                onDragStartIfNeeded()
                val used = dragBy(available.y)
                return Offset(0f, used)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (!atAnchor) {
                settle(available.y)
                return available
            }
            return Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (!atAnchor) {
                settle(available.y)
                return available
            }
            return Velocity.Zero
        }

        private fun onDragStartIfNeeded() {
            settleJob?.cancel()
        }
    }
}

@Composable
fun rememberBottomNavDrawerState(): BottomNavDrawerState {
    val scope = rememberCoroutineScope()
    return remember { BottomNavDrawerState(scope) }
}
