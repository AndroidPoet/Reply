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

@Immutable
data class Corners(val topLeft: Float = 0f, val topRight: Float = 0f, val bottomRight: Float = 0f, val bottomLeft: Float = 0f) {
    companion object {
        fun all(px: Float) = Corners(px, px, px, px)
    }
}

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
        val Enter = ProgressThresholds(0f, 0.25f, 0f, 1f, 0f, 1f, 0f, 0.75f)

        val Return = ProgressThresholds(0.60f, 0.90f, 0f, 1f, 0f, 0.90f, 0.30f, 0.90f)
    }
}

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

@Immutable
data class ContainerFrame(
    val maskLeft: Float,
    val maskTop: Float,
    val maskWidth: Float,
    val maskHeight: Float,
    val startScale: Float,
    val endScale: Float,
    val fadeProgress: Float,
    val shapeMaskProgress: Float,
)

fun containerFrame(spec: ContainerTransformSpec, progress: Float): ContainerFrame {
    val t = spec.thresholds
    val s = spec.startBounds
    val e = spec.endBounds
    val scaleProgress = lerpRange(0f, 1f, t.scaleStart, t.scaleEnd, progress)
    val scaleMaskProgress = lerpRange(0f, 1f, t.scaleMaskStart, t.scaleMaskEnd, progress)
    val shapeMaskProgress = lerpRange(0f, 1f, t.shapeMaskStart, t.shapeMaskEnd, progress)
    val fadeProgress = lerpRange(0f, 1f, t.fadeStart, t.fadeEnd, progress)
    val startScale = lerpRange(1f, e.width / s.width, 0f, 1f, scaleProgress)
    val endScale = lerpRange(s.width / e.width, 1f, 0f, 1f, scaleProgress)
    val currentWidth = s.width * startScale
    val motionX = s.center.x + (e.center.x - s.center.x) * scaleProgress
    val motionY = s.top + (e.top - s.top) * scaleProgress
    return ContainerFrame(
        maskLeft = motionX - currentWidth / 2f,
        maskTop = motionY,
        maskWidth = currentWidth,
        maskHeight = lerpRange(s.height * startScale, e.height * endScale, 0f, 1f, scaleMaskProgress),
        startScale = startScale,
        endScale = endScale,
        fadeProgress = fadeProgress,
        shapeMaskProgress = shapeMaskProgress,
    )
}

private fun corner(start: Float, end: Float, progress: Float): Float = lerpRange(start, end, 0f, 1f, progress)

@Composable
fun ContainerTransform(
    spec: ContainerTransformSpec,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val s = spec.startBounds
    val e = spec.endBounds
    val frame = containerFrame(spec, progress)
    val startScale = frame.startScale
    val endScale = frame.endScale
    val fadeProgress = frame.fadeProgress
    val shapeMaskProgress = frame.shapeMaskProgress
    val currentWidth = frame.maskWidth
    val maskLeft = frame.maskLeft
    val maskTop = frame.maskTop
    val maskHeight = frame.maskHeight

    val corners = RoundedCornerShape(
        topStart = with(density) { corner(spec.startCorners.topLeft, spec.endCorners.topLeft, shapeMaskProgress).toDp() },
        topEnd = with(density) { corner(spec.startCorners.topRight, spec.endCorners.topRight, shapeMaskProgress).toDp() },
        bottomEnd = with(density) { corner(spec.startCorners.bottomRight, spec.endCorners.bottomRight, shapeMaskProgress).toDp() },
        bottomStart = with(density) { corner(spec.startCorners.bottomLeft, spec.endCorners.bottomLeft, shapeMaskProgress).toDp() },
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
