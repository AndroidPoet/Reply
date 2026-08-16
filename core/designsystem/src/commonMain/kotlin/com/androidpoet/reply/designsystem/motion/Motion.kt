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
import kotlin.math.PI
import kotlin.math.cos

object Interpolators {
    val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    val FastOutLinearIn: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    val LinearOutSlowIn: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    val AccelerateDecelerate: Easing = Easing { (cos((it + 1) * PI) / 2.0 + 0.5).toFloat() }

    val Accelerate: Easing = Easing { it * it }

    val Decelerate: Easing = Easing { 1f - (1f - it) * (1f - it) }

    val ViewDragSettle: Easing = Easing { t -> val u = t - 1f; u * u * u * u * u + 1f }
}

object Durations {
    const val LARGE = 300
    const val MEDIUM = 225
    const val SMALL = 175

    const val ITEM_TOUCH_HELPER_RECOVER = 250

    const val BOTTOM_VIEW_ENTER = 225
    const val BOTTOM_VIEW_EXIT = 175
}

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

    val elevationScaleExit: ExitTransition =
        fadeOut(spec()) + scaleOut(spec(), targetScale = ELEVATION_SCALE)

    val elevationScaleReenter: EnterTransition =
        fadeIn(spec()) + scaleIn(spec(), initialScale = ELEVATION_SCALE)

    val fadeThroughExit: ExitTransition = fadeOut(spec(duration = fadeThroughOutMillis))

    val fadeThroughEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = FADE_THROUGH_SCALE)

    val sharedAxisZForwardEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = SHARED_AXIS_Z_SCALE_IN)

    val sharedAxisZForwardExit: ExitTransition =
        fadeOut(spec(duration = fadeThroughOutMillis)) + scaleOut(spec(), targetScale = SHARED_AXIS_Z_SCALE_OUT)

    val sharedAxisZBackwardEnter: EnterTransition =
        fadeIn(spec(duration = fadeThroughInMillis, delay = fadeThroughOutMillis)) +
            scaleIn(spec(), initialScale = SHARED_AXIS_Z_SCALE_OUT)

    val sharedAxisZBackwardExit: ExitTransition =
        fadeOut(spec(duration = fadeThroughOutMillis)) + scaleOut(spec(), targetScale = SHARED_AXIS_Z_SCALE_IN)


    private val step: Easing = Easing { 1f }

    val instantEnter: EnterTransition = fadeIn(tween(Durations.LARGE, easing = step))
    val instantExit: ExitTransition = fadeOut(tween(Durations.LARGE, easing = step))

    val holdThenVanish: ExitTransition =
        fadeOut(tween(Durations.MEDIUM, easing = Easing { if (it >= 1f) 1f else 0f }))
}

fun lerpRange(startValue: Float, endValue: Float, startFraction: Float, endFraction: Float, fraction: Float): Float {
    if (fraction < startFraction) return startValue
    if (fraction > endFraction) return endValue
    return startValue + (endValue - startValue) * ((fraction - startFraction) / (endFraction - startFraction))
}
