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
import com.androidpoet.reply.designsystem.theme.ReplyMotion
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ln

/** How strongly the drag is damped as it nears the threshold (`swipeReboundingElasticity`). */
private const val SWIPE_REBOUNDING_ELASTICITY = 0.8f

/** Fraction of the item width that counts as a completed swipe (`trueSwipeThreshold`). */
const val TRUE_SWIPE_THRESHOLD = 0.4f

/**
 * State for `ReboundingSwipeActionCallback`: a rightward swipe that never dismisses. The card is
 * translated with a logarithmic spring, and once the drag passes [TRUE_SWIPE_THRESHOLD] the item
 * is flagged so the release triggers the action.
 */
@Stable
class ReboundingSwipeState {
    /** Raw pointer displacement (>= 0). */
    var rawDx by mutableFloatStateOf(0f)
        private set

    /** Width of the item, set from layout. */
    var width by mutableFloatStateOf(0f)

    /** Whether, during the current contiguous interaction, the threshold has been met at least once. */
    var hasMetThresholdOnce by mutableStateOf(false)
        private set

    val swipePercentage: Float
        get() = if (width == 0f) 0f else abs(rawDx) / width

    val thresholdMet: Boolean get() = swipePercentage >= TRUE_SWIPE_THRESHOLD

    /** `translateReboundingView`: progressively decrease the translation to give a spring feel. */
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

/**
 * Attach the rebounding horizontal swipe. On release the card springs back over
 * [ReplyMotion.DURATION_MEDIUM]; if the threshold was reached, [onRebounded] fires after the
 * spring-back completes (matching `clearView`).
 */
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
                    tween(ReplyMotion.DURATION_MEDIUM, easing = ReplyMotion.Persistent),
                ) { state.setRaw(value) }
                state.reset()
                if (met) onRebounded()
            }
        },
    )
}
