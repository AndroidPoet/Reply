package com.androidpoet.reply.designsystem.motion

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionTest {
    private val spec = ContainerTransformSpec(
        startBounds = Rect(100f, 900f, 200f, 1000f),
        endBounds = Rect(0f, 0f, 1000f, 2000f),
        startCorners = Corners.all(50f),
        endCorners = Corners(),
        startColor = Color.Red,
        endColor = Color.White,
        thresholds = ProgressThresholds.Enter,
        startContent = {},
        endContent = {},
    )

    @Test
    fun lerpRangeClampsOutsideThresholds() {
        assertEquals(0f, lerpRange(0f, 1f, 0.6f, 0.9f, 0.5f))
        assertEquals(1f, lerpRange(0f, 1f, 0.6f, 0.9f, 0.95f))
        assertEquals(0.5f, lerpRange(0f, 1f, 0.6f, 0.9f, 0.75f), 0.0001f)
    }

    @Test
    fun frameStartsAtStartBoundsAndEndsAtEndBounds() {
        val start = containerFrame(spec, 0f)
        assertEquals(100f, start.maskLeft)
        assertEquals(900f, start.maskTop)
        assertEquals(100f, start.maskWidth)
        assertEquals(100f, start.maskHeight)
        assertEquals(1f, start.startScale)
        assertEquals(0f, start.fadeProgress)

        val end = containerFrame(spec, 1f)
        assertEquals(0f, end.maskLeft, 0.001f)
        assertEquals(0f, end.maskTop, 0.001f)
        assertEquals(1000f, end.maskWidth, 0.001f)
        assertEquals(2000f, end.maskHeight, 0.001f)
        assertEquals(1f, end.endScale)
        assertEquals(1f, end.fadeProgress)
    }

    @Test
    fun enterThresholdsFadeInWithinFirstQuarter() {
        assertEquals(1f, containerFrame(spec, 0.25f).fadeProgress, 0.001f)
        assertEquals(1f, containerFrame(spec, 0.75f).shapeMaskProgress, 0.001f)
        assertTrue(containerFrame(spec, 0.5f).shapeMaskProgress < 1f)
    }

    @Test
    fun interpolatorsHitEndpoints() {
        listOf(
            Interpolators.FastOutSlowIn,
            Interpolators.FastOutLinearIn,
            Interpolators.LinearOutSlowIn,
            Interpolators.AccelerateDecelerate,
            Interpolators.Accelerate,
            Interpolators.Decelerate,
            Interpolators.ViewDragSettle,
        ).forEach { easing ->
            assertEquals(0f, easing.transform(0f), 0.0001f)
            assertEquals(1f, easing.transform(1f), 0.0001f)
        }
        assertTrue(Interpolators.ViewDragSettle.transform(0.5f) > 0.9f)
    }
}
