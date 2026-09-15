package io.github.timoptr.mdiicons.sample

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Captures the sample app for the README images in `docs/images`.
 * Record with `./gradlew :sample:recordRoborazziAndroidHostTest`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config
class ReadmeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "w900dp-h520dp-xhdpi")
    fun catalog() {
        composeRule.setContent { App() }

        composeRule.onRoot().captureRoboImage("catalog.png")
    }

    @Test
    @Config(qualifiers = "w900dp-h330dp-xhdpi")
    fun search() {
        composeRule.setContent { App() }

        composeRule.onNode(hasSetTextAction()).performTextInput("home")
        composeRule.onNodeWithContentDescription("home-assistant").performClick()

        composeRule.onRoot().captureRoboImage("search.png")
    }
}
