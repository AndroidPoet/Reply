package com.androidpoet.reply.feature.home

import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReboundingSwipeStateTest {
    private fun state(width: Float = 1000f) = ReboundingSwipeState().apply { this.width = width }

    @Test
    fun translationFollowsLogarithmicSpring() {
        val s = state()
        s.drag(200f)
        val threshold = 1000f * TRUE_SWIPE_THRESHOLD
        val expected = (ln((1 + 200f / threshold).toDouble()) / ln(3.0) * threshold * 0.8f).toFloat()
        assertEquals(expected, s.translationX, 0.01f)
    }

    @Test
    fun thresholdIsRememberedOncePassed() {
        val s = state()
        s.drag(399f)
        assertFalse(s.hasMetThresholdOnce)
        s.drag(2f)
        assertTrue(s.hasMetThresholdOnce)
        s.drag(-300f)
        assertTrue(s.hasMetThresholdOnce)
        assertFalse(s.thresholdMet)
    }

    @Test
    fun leftwardDragIsIgnored() {
        val s = state()
        s.drag(-100f)
        assertEquals(0f, s.rawDx)
        assertEquals(0f, s.translationX)
    }
}
