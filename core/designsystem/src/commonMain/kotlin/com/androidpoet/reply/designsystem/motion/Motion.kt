package com.androidpoet.reply.designsystem.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import kotlin.math.PI
import kotlin.math.cos

/**
 * The exact interpolators used by the Views implementation (`android.view.animation` +
 * `androidx.interpolator`), so timing curves match frame for frame.
 */
object Interpolators {
    /** `@android:interpolator/fast_out_slow_in` — MDC's standard curve. */
    val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** `@android:interpolator/fast_out_linear_in` — bottom app bar slide-out. */
    val FastOutLinearIn: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    /** `@android:interpolator/linear_out_slow_in` — bottom app bar slide-in. */
    val LinearOutSlowIn: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    /** `AccelerateDecelerateInterpolator` — ItemTouchHelper's recover animation. */
    val AccelerateDecelerate: Easing = Easing { (cos((it + 1) * PI) / 2.0 + 0.5).toFloat() }

    /** `AccelerateInterpolator(1)` — used by `Slide` when a view disappears. */
    val Accelerate: Easing = Easing { it * it }

    /** `DecelerateInterpolator(1)` — used by `Slide` when a view appears. */
    val Decelerate: Easing = Easing { 1f - (1f - it) * (1f - it) }

    /** `ViewDragHelper.sInterpolator` — quintic ease-out used to settle bottom sheets. */
    val ViewDragSettle: Easing = Easing { t -> val u = t - 1f; u * u * u * u * u + 1f }
}

/** `res/values/motion.xml`. */
object Durations {
    const val LARGE = 300
    const val MEDIUM = 225
    const val SMALL = 175

    /** `ItemTouchHelper.DEFAULT_SWIPE_ANIMATION_DURATION`. */
    const val ITEM_TOUCH_HELPER_RECOVER = 250

    /** `HideBottomViewOnScrollBehavior` enter / exit. */
    const val BOTTOM_VIEW_ENTER = 225
    const val BOTTOM_VIEW_EXIT = 175
}

/**
 * Screen transitions from the Material motion library (`com.google.android.material.transition`),
 * expressed as Compose [EnterTransition]/[ExitTransition] pairs. All run over [Durations.LARGE]
 * with [Interpolators.FastOutSlowIn], exactly like `MaterialVisibility`.
 */
object MaterialMotion {
    private const val FADE_THROUGH_THRESHOLD = 0.35f
    private const val ELEVATION_SCALE = 0.85f
    private const val FADE_THROUGH_SCALE = 0.92f
    private const val SHARED_AXIS_Z_SCALE_IN = 0.80f
    private const val SHARED_AXIS_Z_SCALE_OUT = 1.10f

    private fun spec(duration: Int = Durations.LARGE, delay: Int = 0, easing: Easing = Interpolators.FastOutSlowIn) =
        tween<Float>(durationMillis = duration, delayMillis = delay, easing = easing)

    private val fadeThroughOutMillis = (Durations.LARGE * FADE_THROUGH_THRESHOLD).toInt()
    private val fadeThroughInMillis = Durations.LARGE - fadeThroughOutMillis

    /** `MaterialElevationScale(growing = false)` — outgoing screen shrinks to 85% and fades. */
    val elevationScaleExit: ExitTransition =
        fadeOut(spec()) + scaleOut(spec(), targetScale = ELEVATION_SCALE)

    /** `MaterialElevationScale(growing = true)` — returning screen grows from 85% and fades in. */
    val elevationScaleReenter: EnterTransition =
        fadeIn(spec()) + scaleIn(spec(), initialScale = ELEVATION_SCALE)

    /** `MaterialFadeThrough` — outgoing fades over the first 35%. */
    val fadeThroughExit: ExitTransition = fadeOut(spec(duration = fadeThroughOutMillis))

    /** `MaterialFadeThrough` — incoming fades in over the last 65% while scaling 92% → 100%. */
    val fadeThroughEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = FADE_THROUGH_SCALE)

    /** `MaterialSharedAxis(Z, forward = true)` on the incoming screen. */
    val sharedAxisZForwardEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = SHARED_AXIS_Z_SCALE_IN)

    /** `MaterialSharedAxis(Z, forward = true)` on the outgoing screen. */
    val sharedAxisZForwardExit: ExitTransition =
        fadeOut(spec(duration = fadeThroughOutMillis)) + scaleOut(spec(), targetScale = SHARED_AXIS_Z_SCALE_OUT)

    /** `MaterialSharedAxis(Z, forward = false)` on the incoming (returning) screen. */
    val sharedAxisZBackwardEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = SHARED_AXIS_Z_SCALE_OUT)

    /** `MaterialSharedAxis(Z, forward = false)` on the outgoing screen. */
    val sharedAxisZBackwardExit: ExitTransition =
        fadeOut(spec(duration = fadeThroughOutMillis)) + scaleOut(spec(), targetScale = SHARED_AXIS_Z_SCALE_IN)

    /** `Slide()` (bottom edge) as a return transition: accelerates off the bottom in 225ms. */
    val slideOutBottom: ExitTransition =
        slideOutVertically(tween(Durations.MEDIUM, easing = Interpolators.Accelerate)) { it }

    /** Jumps straight to the end value but still occupies the full duration. */
    private val step: Easing = Easing { 1f }

    /**
     * Appear immediately, while keeping the transition alive for [Durations.LARGE] so the other
     * side's 300ms exit is not cut short (used where a container transform draws the screen).
     */
    val instantEnter: EnterTransition = fadeIn(tween(Durations.LARGE, easing = step))
    val instantExit: ExitTransition = fadeOut(tween(Durations.LARGE, easing = step))

    /** Stays fully visible for [Durations.MEDIUM] (while `Slide()` moves its card), then vanishes. */
    val holdThenVanish: ExitTransition =
        fadeOut(tween(Durations.MEDIUM, easing = Easing { if (it >= 1f) 1f else 0f }))
}

/** `com.materialstudies.reply.util.lerp(start, end, startFraction, endFraction, fraction)`. */
fun lerpRange(startValue: Float, endValue: Float, startFraction: Float, endFraction: Float, fraction: Float): Float {
    if (fraction < startFraction) return startValue
    if (fraction > endFraction) return endValue
    return startValue + (endValue - startValue) * ((fraction - startFraction) / (endFraction - startFraction))
}
