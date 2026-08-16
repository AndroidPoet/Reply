@file:OptIn(ExperimentalTestApi::class)

package com.androidpoet.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.androidpoet.reply.database.buildReplyDatabase
import com.androidpoet.reply.datastore.createTemporarySettingsDataStore
import com.androidpoet.reply.database.inMemoryReplyDatabaseBuilder
import com.androidpoet.reply.data.ThemeMode
import com.androidpoet.reply.di.buildAppGraph
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

private class ResumedLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this).apply { currentState = Lifecycle.State.RESUMED }
    override val lifecycle: Lifecycle get() = registry
}

@Composable
private fun TestHost(content: @Composable () -> Unit) {
    val owner = remember { ResumedLifecycleOwner() }
    CompositionLocalProvider(
        LocalDensity provides Density(2.625f),
        LocalLifecycleOwner provides owner,
        content = content,
    )
}

class ScreenshotTest {
    private val outDir = File("build/screenshots").apply { mkdirs() }

    private fun DesktopComposeUiTest.snap(theme: String, name: String) {
        settle()
        val image = onAllNodes(isRoot()).onFirst().captureToImage().toAwtImage()
        val dir = File(outDir, theme).apply { mkdirs() }
        ImageIO.write(image, "png", File(dir, "$name.png"))
    }

    private fun DesktopComposeUiTest.settle() {
        waitForIdle()
        mainClock.advanceTimeBy(1_500)
        Thread.sleep(400)
        waitForIdle()
        mainClock.advanceTimeBy(100)
        waitForIdle()
    }

    private fun loadedGraph() = buildAppGraph(inMemoryReplyDatabaseBuilder().buildReplyDatabase(), createTemporarySettingsDataStore()).also { runBlocking { it.repository.loadBundled() } }

    private fun walk(theme: ThemeMode) = runDesktopComposeUiTest(width = 1080, height = 2340) {
        val label = theme.name.lowercase()
        val graph = loadedGraph()
        setContent {
            TestHost { App(graph, initialThemeMode = theme) }
        }
        snap(label, "01_home")

        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        snap(label, "02_drawer")
        onNodeWithContentDescription("profile avatar image").performClick()
        snap(label, "03_sandwich")
        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        settle()
        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        settle()

        onNodeWithText("Brunch this weekend?").performTouchInput {
            down(centerLeft)
            repeat(20) { moveBy(androidx.compose.ui.geometry.Offset(width * 0.03f, 0f)) }
        }
        snap(label, "04_swipe_mid")
        onNodeWithText("Brunch this weekend?").performTouchInput { up() }
        snap(label, "05_swipe_starred")

        onNodeWithText("Bonjour from Paris").performClick()
        snap(label, "06_email_detail")
        onNodeWithContentDescription("Reply to email").performClick()
        snap(label, "07_compose")
        onNodeWithContentDescription("Close editing email").performClick()
        settle()
        onNodeWithContentDescription("Navigate back").performClick()
        settle()

        onNodeWithContentDescription("Search").performClick()
        snap(label, "08_search")
        onNodeWithContentDescription("Navigate back").performClick()
        settle()

        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        settle()
        onNodeWithContentDescription("Settings").performClick()
        snap(label, "09_theme_menu")

        onNodeWithText("Brunch this weekend?").performTouchInput { longClick() }
        snap(label, "10_email_menu")
    }

    @Test
    fun motion() = runDesktopComposeUiTest(width = 1080, height = 2340) {
        val graph = loadedGraph()
        setContent {
            TestHost { App(graph, initialThemeMode = ThemeMode.LIGHT) }
        }
        settle()
        mainClock.autoAdvance = false

        fun frames(name: String, total: Int, step: Int = 16, action: () -> Unit) {
            action()
            var t = 0
            while (t <= total) {
                mainClock.advanceTimeBy(if (t == 0) 16 else step.toLong())
                waitForIdle()
                val image = onAllNodes(isRoot()).onFirst().captureToImage().toAwtImage()
                val dir = File(outDir, "motion").apply { mkdirs() }
                ImageIO.write(image, "png", File(dir, "${name}_${t.toString().padStart(3, '0')}.png"))
                t += step
            }
            mainClock.advanceTimeBy(1_000)
            waitForIdle()
        }

        frames("swipe_star", 700) {
            onNodeWithText("Brunch this weekend?").performTouchInput {
                down(centerLeft)
                repeat(20) { moveBy(androidx.compose.ui.geometry.Offset(width * 0.03f, 0f), 16) }
                up()
            }
        }
        frames("card_to_detail", 300) { onNodeWithText("Bonjour from Paris").performClick() }
        frames("detail_to_card", 300) { onNodeWithContentDescription("Navigate back").performClick() }
        frames("fab_to_compose", 300) { onNodeWithContentDescription("Compose new email").performClick() }
        frames("compose_close", 300) { onNodeWithContentDescription("Close editing email").performClick() }
        frames("drawer_open", 450) { onNodeWithContentDescription("Toggle navigation drawer").performClick() }
        frames("sandwich_open", 250) { onNodeWithContentDescription("profile avatar image").performClick() }
        frames("mailbox_switch", 300) {
            onNodeWithContentDescription("Toggle navigation drawer").performClick()
            mainClock.advanceTimeBy(300); waitForIdle()
            onNodeWithText("Starred").performClick()
        }
        frames("search_open", 300) { onNodeWithContentDescription("Search").performClick() }
        frames("search_close", 300) { onNodeWithContentDescription("Navigate back").performClick() }
    }

    @Test
    fun timing() = runDesktopComposeUiTest(width = 1080, height = 2340) {
        val graph = loadedGraph()
        setContent {
            TestHost { App(graph, initialThemeMode = ThemeMode.LIGHT) }
        }
        settle()
        mainClock.autoAdvance = false
        val dir = File(outDir, "timing").apply { mkdirs() }
        onNodeWithText("Bonjour from Paris").performClick()
        repeat(24) { i ->
            mainClock.advanceTimeBy(16)
            val image = onAllNodes(isRoot()).onFirst().captureToImage().toAwtImage()
            ImageIO.write(image, "png", File(dir, "t_${(i * 16).toString().padStart(3, '0')}.png"))
        }
    }

    @Test
    fun light() = walk(ThemeMode.LIGHT)

    @Test
    fun dark() = walk(ThemeMode.DARK)
}
