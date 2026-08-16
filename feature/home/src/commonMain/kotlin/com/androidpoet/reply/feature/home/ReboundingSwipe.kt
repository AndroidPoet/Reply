package com.androidpoet.reply.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.androidpoet.reply.designsystem.motion.Durations
import com.androidpoet.reply.designsystem.motion.Interpolators
import kotlin.math.abs
import kotlin.math.ln
import kotlinx.coroutines.launch

private const val SWIPE_REBOUNDING_ELASTICITY = 0.8f

const val TRUE_SWIPE_THRESHOLD = 0.4f

@Stable
class ReboundingSwipeState {
    var rawDx by mutableFloatStateOf(0f)
        private set

    var width by mutableFloatStateOf(0f)

    var hasMetThresholdOnce by mutableStateOf(false)
        private set

    val swipePercentage: Float
        get() = if (width == 0f) 0f else abs(rawDx) / width

    val thresholdMet: Boolean get() = swipePercentage >= TRUE_SWIPE_THRESHOLD

    val translationX: Float
        get() {
            if (width == 0f || rawDx <= 0f) return 0f
            val swipeDismissDistance = width * TRUE_SWIPE_THRESHOLD
            val dragFraction = ln((1 + (rawDx / swipeDismissDistance)).toDouble()) / ln(3.0)
            return (dragFraction * swipeDismissDistance * SWIPE_REBOUNDING_ELASTICITY).toFloat()
        }

    internal val settle = Animatable(0f)

    internal fun drag(delta: Float) {
        rawDx = (rawDx + delta).coerceAtLeast(0f)
        if (thresholdMet) hasMetThresholdOnce = true
    }

    internal fun setRaw(value: Float) {
        rawDx = value
    }

    internal fun reset() {
        rawDx = 0f
        hasMetThresholdOnce = false
    }
}

@Composable
fun rememberReboundingSwipeState(): ReboundingSwipeState = remember { ReboundingSwipeState() }

@Composable
fun Modifier.reboundingSwipe(
    state: ReboundingSwipeState,
    enabled: Boolean = true,
    onRebounded: () -> Unit,
): Modifier {
    val scope = rememberCoroutineScope()
    val draggable = rememberDraggableState { delta -> state.drag(delta) }
    return draggable(
        state = draggable,
        orientation = Orientation.Horizontal,
        enabled = enabled,
        onDragStarted = { state.settle.stop() },
        onDragStopped = {
            scope.launch {
                val met = state.hasMetThresholdOnce
                state.settle.snapTo(state.rawDx)

                state.settle.animateTo(
                    0f,
                    tween(Durations.ITEM_TOUCH_HELPER_RECOVER, easing = Interpolators.AccelerateDecelerate),
                ) { state.setRaw(value) }
                state.reset()
                if (met) onRebounded()
            }
        },
    )
}
