package com.androidpoet.reply.designsystem.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Corner radii (px) of a container, in the order MDC's `ShapeAppearanceModel` uses. */
@Immutable
data class Corners(val topLeft: Float = 0f, val topRight: Float = 0f, val bottomRight: Float = 0f, val bottomLeft: Float = 0f) {
    companion object {
        fun all(px: Float) = Corners(px, px, px, px)
    }
}

/**
 * `MaterialContainerTransform.ProgressThresholdsGroup`. Fractions of the (already interpolated)
 * transition progress within which each sub-animation runs.
 */
@Immutable
data class ProgressThresholds(
    val fadeStart: Float,
    val fadeEnd: Float,
    val scaleStart: Float,
    val scaleEnd: Float,
    val scaleMaskStart: Float,
    val scaleMaskEnd: Float,
    val shapeMaskStart: Float,
    val shapeMaskEnd: Float,
) {
    companion object {
        /** `DEFAULT_ENTER_THRESHOLDS`. */
        val Enter = ProgressThresholds(0f, 0.25f, 0f, 1f, 0f, 1f, 0f, 0.75f)

        /** `DEFAULT_RETURN_THRESHOLDS`. */
        val Return = ProgressThresholds(0.60f, 0.90f, 0f, 1f, 0f, 0.90f, 0.30f, 0.90f)
    }
}

/**
 * Everything a container transform needs. All geometry is in px, in the coordinate space of the
 * composable hosting [ContainerTransform] (normally the app root).
 *
 * Semantics follow `MaterialContainerTransform` with `FADE_MODE_IN` and `FIT_MODE_WIDTH`:
 * the start content stays opaque and is scaled with the container, the end content is drawn on
 * top and fades in over the fade thresholds; the container's width, height, corners and colour
 * morph between the two.
 */
@Immutable
data class ContainerTransformSpec(
    val startBounds: Rect,
    val endBounds: Rect,
    val startCorners: Corners,
    val endCorners: Corners,
    val startColor: Color,
    val endColor: Color,
    val thresholds: ProgressThresholds,
    val startElevation: Dp = 0.dp,
    val endElevation: Dp = 0.dp,
    val startContent: @Composable () -> Unit,
    val endContent: @Composable () -> Unit,
)

/**
 * Draws one frame of a container transform at [progress] (0..1, **already interpolated** with the
 * transition's easing, like MDC feeds `updateProgress`). Place it in a full-size Box at the
 * root so [ContainerTransformSpec] bounds line up.
 */
@Composable
fun ContainerTransform(
    spec: ContainerTransformSpec,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val t = spec.thresholds
    val s = spec.startBounds
    val e = spec.endBounds

    val scaleProgress = lerpRange(0f, 1f, t.scaleStart, t.scaleEnd, progress)
    val scaleMaskProgress = lerpRange(0f, 1f, t.scaleMaskStart, t.scaleMaskEnd, progress)
    val shapeMaskProgress = lerpRange(0f, 1f, t.shapeMaskStart, t.shapeMaskEnd, progress)
    val fadeProgress = lerpRange(0f, 1f, t.fadeStart, t.fadeEnd, progress)

    // FitModeEvaluators.WIDTH
    val startScale = lerpRange(1f, e.width / s.width, 0f, 1f, scaleProgress)
    val endScale = lerpRange(s.width / e.width, 1f, 0f, 1f, scaleProgress)
    val currentWidth = s.width * startScale // == e.width * endScale
    val currentStartHeight = s.height * startScale
    val currentEndHeight = e.height * endScale

    // Linear motion path from the start's top-centre to the end's top-centre.
    val motionX = s.center.x + (e.center.x - s.center.x) * scaleProgress
    val motionY = s.top + (e.top - s.top) * scaleProgress

    // The mask: width from fit-mode, height between the two current heights.
    val maskLeft = motionX - currentWidth / 2f
    val maskTop = motionY
    val maskHeight = lerpRange(currentStartHeight, currentEndHeight, 0f, 1f, scaleMaskProgress)

    val corners = RoundedCornerShape(
        topStart = with(density) { lerpRange(spec.startCorners.topLeft, spec.endCorners.topLeft, 0f, 1f, shapeMaskProgress).toDp() },
        topEnd = with(density) { lerpRange(spec.startCorners.topRight, spec.endCorners.topRight, 0f, 1f, shapeMaskProgress).toDp() },
        bottomEnd = with(density) { lerpRange(spec.startCorners.bottomRight, spec.endCorners.bottomRight, 0f, 1f, shapeMaskProgress).toDp() },
        bottomStart = with(density) { lerpRange(spec.startCorners.bottomLeft, spec.endCorners.bottomLeft, 0f, 1f, shapeMaskProgress).toDp() },
    )
    val color = lerp(spec.startColor, spec.endColor, progress)
    val elevation = spec.startElevation + (spec.endElevation - spec.startElevation) * progress

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .offset { IntOffset(maskLeft.roundToInt(), maskTop.roundToInt()) }
                .requiredSize(with(density) { currentWidth.toDp() }, with(density) { maskHeight.toDp() })
                .shadow(elevation, corners, clip = false)
                .clip(corners)
                .background(color),
        ) {
            // Start content: laid out at its natural size, scaled about its top-left.
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(with(density) { s.width.toDp() }, with(density) { s.height.toDp() })
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = startScale
                        scaleY = startScale
                    },
            ) {
                spec.startContent()
            }
            // End content: fades in on top (FADE_MODE_IN), scaled about its top-left.
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(with(density) { e.width.toDp() }, with(density) { e.height.toDp() })
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = endScale
                        scaleY = endScale
                        alpha = fadeProgress
                    },
            ) {
                spec.endContent()
            }
        }
    }
}
