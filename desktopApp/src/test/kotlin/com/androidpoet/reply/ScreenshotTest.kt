@file:OptIn(ExperimentalTestApi::class)

package com.androidpoet.reply

import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import com.androidpoet.reply.di.buildAppGraph
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders the app at Pixel-4-ish geometry (411 x 891 dp @ 2.625x) and walks the main flows,
 * writing a PNG per state to `build/screenshots/<theme>/`. Run with
 * `./gradlew :desktopApp:test --tests "*ScreenshotTest*"`.
 */
class ScreenshotTest {

    private val outDir = File("build/screenshots").apply { mkdirs() }

    private fun DesktopComposeUiTest.snap(theme: String, name: String) {
        settle()
        val image = onAllNodes(isRoot()).onFirst().captureToImage().toAwtImage()
        val dir = File(outDir, theme).apply { mkdirs() }
        ImageIO.write(image, "png", File(dir, "$name.png"))
    }

    /** Let running animations (nav transitions, sheets, springs) finish. */
    private fun DesktopComposeUiTest.settle() {
        waitForIdle()
        mainClock.advanceTimeBy(1_500)
        waitForIdle()
    }

    private fun walk(theme: ThemeMode) = runDesktopComposeUiTest(width = 1080, height = 2340) {
        val label = theme.name.lowercase()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(2.625f)) {
                App(buildAppGraph(), initialThemeMode = theme)
            }
        }
        snap(label, "01_home")

        // Drawer
        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        snap(label, "02_drawer")
        onNodeWithContentDescription("profile avatar image").performClick()
        snap(label, "03_sandwich")
        onNodeWithContentDescription("Toggle navigation drawer").performClick() // close sandwich
        settle()
        onNodeWithContentDescription("Toggle navigation drawer").performClick() // close drawer
        settle()

        // Swipe to star (mid-gesture + result)
        onNodeWithText("Brunch this weekend?").performTouchInput {
            down(centerLeft)
            repeat(20) { moveBy(androidx.compose.ui.geometry.Offset(width * 0.03f, 0f)) }
        }
        snap(label, "04_swipe_mid")
        onNodeWithText("Brunch this weekend?").performTouchInput { up() }
        snap(label, "05_swipe_starred")

        // Email detail with attachments
        onNodeWithText("Bonjour from Paris").performClick()
        snap(label, "06_email_detail")
        onNodeWithContentDescription("Reply to email").performClick()
        snap(label, "07_compose")
        onNodeWithContentDescription("Close editing email").performClick()
        settle()
        onNodeWithContentDescription("Navigate back").performClick()
        settle()

        // Search
        onNodeWithContentDescription("Search").performClick()
        snap(label, "08_search")
        onNodeWithContentDescription("Navigate back").performClick()
        settle()

        // Settings sheet
        onNodeWithContentDescription("Toggle navigation drawer").performClick()
        settle()
        onNodeWithContentDescription("Settings").performClick()
        snap(label, "09_theme_menu")

        // Long-press menu (sheets are popup layers the headless test can't click, so this is last)
        onNodeWithText("Brunch this weekend?").performTouchInput { longClick() }
        snap(label, "10_email_menu")
    }

    @Test
    fun light() = walk(ThemeMode.LIGHT)

    @Test
    fun dark() = walk(ThemeMode.DARK)
}
