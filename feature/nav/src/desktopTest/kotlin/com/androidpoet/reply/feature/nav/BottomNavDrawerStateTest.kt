package com.androidpoet.reply.feature.nav

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.runtime.BroadcastFrameClock
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavDrawerStateTest {
    private class Harness(val scope: TestScope, val clock: BroadcastFrameClock, val state: BottomNavDrawerState) {
        fun pump(millis: Long = 2_000) {
            var t = 0L
            while (t < millis) {
                t += 16
                scope.advanceTimeBy(16)
                clock.sendFrame(t * 1_000_000)
                scope.runCurrent()
            }
        }
    }

    private fun runDrawer(block: suspend Harness.(BottomNavDrawerState) -> Unit) = runTest {
        val clock = BroadcastFrameClock()
        val state = BottomNavDrawerState(backgroundScope + clock).apply {
            containerHeight = 2000f
            containerWidth = 1000f
            density = 2.5f
            bottomBarHeight = 140f
        }
        Harness(this, clock, state).block(state)
    }

    @Test
    fun openSettlesAtHalfExpanded() = runDrawer { state ->
        state.open()
        pump()
        assertEquals(DrawerValue.HalfExpanded, state.currentValue)
        assertEquals(0.4f, state.position, 0.001f)
        assertEquals(1f, state.openFraction, 0.001f)
    }

    @Test
    fun slowReleaseSettlesToNearestAnchor() = runDrawer { state ->
        state.open()
        pump()
        state.dragBy(-100f)
        state.settle(velocityPx = 10f)
        pump()
        assertEquals(DrawerValue.HalfExpanded, state.currentValue)
    }

    @Test
    fun flingUpExpandsAndFlingDownHides() = runDrawer { state ->
        state.open()
        pump()
        state.settle(velocityPx = -5000f)
        pump()
        assertEquals(DrawerValue.Expanded, state.currentValue)
        state.settle(velocityPx = 5000f)
        pump()
        assertEquals(DrawerValue.Hidden, state.currentValue)
        assertTrue(!state.isOpen)
    }

    @Test
    fun sandwichTranslatesSheetSoAccountListSitsAboveBar() = runDrawer { state ->
        state.open()
        pump()
        state.accountListHeight = 360f
        state.toggleSandwich()
        pump()
        assertEquals(SandwichState.OPEN, state.sandwichState)
        val sheetTop = state.position * state.containerHeight + state.sandwichTranslation
        assertEquals(2000f - 360f - 140f, sheetTop, 0.5f)
    }
}
