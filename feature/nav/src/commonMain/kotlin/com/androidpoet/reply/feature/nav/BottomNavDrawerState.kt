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
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class DrawerValue { Hidden, HalfExpanded, Expanded }

enum class SandwichState { CLOSED, OPEN, SETTLING }

private const val HALF_EXPANDED_RATIO = 0.6f

private const val BASE_SETTLE_DURATION = 256
private const val MAX_SETTLE_DURATION = 600

private const val MIN_VELOCITY_DP = 400f

@Stable
class BottomNavDrawerState(private val scope: CoroutineScope) {
    private var settleJob: Job? = null
    private var sandwichJob: Job? = null

    var containerHeight by mutableFloatStateOf(0f)

    var containerWidth by mutableFloatStateOf(0f)

    var density by mutableFloatStateOf(1f)

    var accountListHeight by mutableFloatStateOf(0f)

    var bottomBarHeight by mutableFloatStateOf(0f)

    var position by mutableFloatStateOf(1f)
        private set

    var currentValue by mutableStateOf(DrawerValue.Hidden)
        private set

    val isOpen: Boolean get() = position < 1f

    val openFraction: Float get() = ((1f - position) / (1f - hiddenToHalf)).coerceIn(0f, 1f)

    val expandFraction: Float
        get() = if (hiddenToHalf == 0f) 0f else ((hiddenToHalf - position) / hiddenToHalf).coerceIn(0f, 1f)

    private val hiddenToHalf: Float get() = 1f - HALF_EXPANDED_RATIO

    var sandwichProgress by mutableFloatStateOf(0f)
        private set
    val sandwichState: SandwichState
        get() = when (sandwichProgress) {
            0f -> SandwichState.CLOSED
            1f -> SandwichState.OPEN
            else -> SandwichState.SETTLING
        }

    val navProgress: Float get() = (sandwichProgress / 0.5f).coerceIn(0f, 1f)

    val accountProgress: Float get() = ((sandwichProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)

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
                animationSpec = tween((distance * Durations.MEDIUM).toInt(), easing = Interpolators.FastOutSlowIn),
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

    fun animateTo(value: DrawerValue, velocityPx: Float = 0f) {
        closeSandwichImmediately()
        settleJob?.cancel()
        val from = position
        val to = anchor(value)
        val duration = computeSettleDuration((to - from) * containerHeight, velocityPx)
        settleJob = scope.launch {
            animate(
                initialValue = from,
                targetValue = to,
                animationSpec = tween(duration, easing = Interpolators.ViewDragSettle),
            ) { v, _ -> position = v }
            currentValue = value
        }
    }

    private fun computeSettleDuration(deltaPx: Float, velocityPx: Float): Int {
        if (deltaPx == 0f) return 0
        val width = if (containerWidth > 0f) containerWidth else containerHeight
        val halfWidth = width / 2f
        val distanceRatio = min(1f, abs(deltaPx) / width)
        val distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio)
        val v = abs(velocityPx)
        val duration = if (v > 0f) {
            4 * (1000f * abs(distance / v)).roundToInt()
        } else {
            val range = abs(deltaPx) / containerHeight
            ((range + 1f) * BASE_SETTLE_DURATION).toInt()
        }
        return min(duration, MAX_SETTLE_DURATION)
    }

    private fun distanceInfluenceForSnapDuration(f: Float): Float =
        sin(((f - 0.5f) * 0.3f * PI).toFloat())

    internal fun onDragStart() {
        settleJob?.cancel()
        closeSandwichImmediately()
    }

    internal fun dragBy(deltaPx: Float): Float {
        if (containerHeight == 0f) return 0f
        val before = position
        val after = (before + deltaPx / containerHeight).coerceIn(0f, 1f)
        if (after == before) return 0f
        position = after
        return (after - before) * containerHeight
    }

    internal fun settle(velocityPx: Float) {
        val minVelocity = MIN_VELOCITY_DP * density
        val v = if (abs(velocityPx) < minVelocity) 0f else velocityPx
        val target = when {
            v < 0f -> DrawerValue.Expanded
            v > 0f -> DrawerValue.Hidden
            else -> nearestAnchor()
        }
        animateTo(target, v)
    }

    internal fun settleAfterNestedScroll(lastDeltaPx: Float) {
        val target = when {
            lastDeltaPx < 0f -> DrawerValue.Expanded
            lastDeltaPx > 0f -> DrawerValue.Hidden
            else -> nearestAnchor()
        }
        animateTo(target)
    }

    private fun nearestAnchor(): DrawerValue =
        DrawerValue.entries.minByOrNull { abs(anchor(it) - position) } ?: DrawerValue.Hidden

    private val atAnchor: Boolean
        get() = DrawerValue.entries.any { abs(anchor(it) - position) < 0.0005f }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        private var lastNestedDelta = 0f

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero

            if (available.y < 0f && position > 0f) {
                settleJob?.cancel()
                lastNestedDelta = available.y
                val consumed = dragBy(available.y)
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero

            if (available.y > 0f && position < 1f) {
                settleJob?.cancel()
                lastNestedDelta = available.y
                val used = dragBy(available.y)
                return Offset(0f, used)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (!atAnchor) {
                settleAfterNestedScroll(lastNestedDelta)
                lastNestedDelta = 0f
                return available
            }
            return Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (!atAnchor) {
                settleAfterNestedScroll(lastNestedDelta)
                lastNestedDelta = 0f
                return available
            }
            return Velocity.Zero
        }
    }
}

@Composable
fun rememberBottomNavDrawerState(): BottomNavDrawerState {
    val scope = rememberCoroutineScope()
    return remember { BottomNavDrawerState(scope) }
}
