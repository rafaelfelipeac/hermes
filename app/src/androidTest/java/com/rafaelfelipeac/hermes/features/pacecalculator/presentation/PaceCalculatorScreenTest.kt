package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.DpRect
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PaceCalculatorTestViewportHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PaceCalculatorTestViewportWidth
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class PaceCalculatorScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun systemBack_invokesOnBackCallback() {
        var backPressed = false

        setContent(onBack = { backPressed = true })

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.runOnIdle {
            assertTrue(backPressed)
        }
    }

    @Test
    fun header_backButtonAndTitle_stayAligned() {
        var backPressed = false
        val title = composeRule.activity.getString(R.string.pace_calculator_title)

        setContent(
            onBack = { backPressed = true },
        )

        val backBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_BACK_BUTTON_TAG)
                .getUnclippedBoundsInRoot()
        val titleBounds =
            composeRule
                .onNodeWithText(title)
                .getUnclippedBoundsInRoot()

        assertTrue(backBounds.top < titleBounds.bottom && titleBounds.top < backBounds.bottom)
        assertTrue(backBounds.left < titleBounds.left)

        composeRule.onNodeWithTag(PACE_CALCULATOR_BACK_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertTrue(backPressed)
        }
    }

    @Test
    fun presets_wrap_onCompactWidth() {
        val marathon = composeRule.activity.getString(R.string.pace_calculator_preset_marathon)
        val custom = composeRule.activity.getString(R.string.pace_calculator_distance_custom)

        setCompactContent()

        composeRule.onNodeWithTag(PACE_CALCULATOR_PRESETS_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(marathon).assertIsDisplayed()
        composeRule.onNodeWithText(custom).assertIsDisplayed()
    }

    @Test
    fun timeFields_shareOneRowWithEqualWidths_onCompactWidth() {
        setCompactContent()

        val hoursBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_TIME_HOURS_INPUT_TAG)
                .getUnclippedBoundsInRoot()
        val minutesBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG)
                .getUnclippedBoundsInRoot()
        val secondsBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG)
                .getUnclippedBoundsInRoot()

        assertTrue(abs(hoursBounds.top.value - minutesBounds.top.value) <= 1f)
        assertTrue(abs(minutesBounds.top.value - secondsBounds.top.value) <= 1f)
        assertTrue(abs(fieldWidth(hoursBounds) - fieldWidth(minutesBounds)) <= 1f)
        assertTrue(abs(fieldWidth(minutesBounds) - fieldWidth(secondsBounds)) <= 1f)
    }

    @Test
    fun paceFields_shareOneRowWithEqualWidths_onCompactWidth() {
        setCompactContent()
        composeRule.onNodeWithTag(PACE_CALCULATOR_MODE_TIME_TAG).performClick()

        val minutesBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_PACE_MINUTES_INPUT_TAG)
                .getUnclippedBoundsInRoot()
        val secondsBounds =
            composeRule
                .onNodeWithTag(PACE_CALCULATOR_PACE_SECONDS_INPUT_TAG)
                .getUnclippedBoundsInRoot()

        assertTrue(abs(minutesBounds.top.value - secondsBounds.top.value) <= 1f)
        assertTrue(abs(fieldWidth(minutesBounds) - fieldWidth(secondsBounds)) <= 1f)
    }

    @Test
    fun timeFields_startAtZero() {
        setCompactContent()
        val hours = composeRule.activity.getString(R.string.pace_calculator_hours)
        val minutes = composeRule.activity.getString(R.string.pace_calculator_minutes)
        val seconds = composeRule.activity.getString(R.string.pace_calculator_seconds)

        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_HOURS_INPUT_TAG).assertTextEquals(hours, "0")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).assertTextEquals(minutes, "0")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG).assertTextEquals(seconds, "0")
    }

    @Test
    fun timeFields_acceptWatchStyleDurationWithLeadingZeros() {
        setCompactContent()
        val hours = composeRule.activity.getString(R.string.pace_calculator_hours)
        val minutes = composeRule.activity.getString(R.string.pace_calculator_minutes)
        val seconds = composeRule.activity.getString(R.string.pace_calculator_seconds)

        composeRule.onNodeWithTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG).performTextInput("10")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_HOURS_INPUT_TAG).performTextInput("3")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).performTextInput("09")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG).performTextInput("04")

        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_HOURS_INPUT_TAG).assertTextEquals(hours, "3")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).assertTextEquals(minutes, "09")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG).assertTextEquals(seconds, "04")

        composeRule.onNodeWithTag(PACE_CALCULATOR_BODY_TAG).performTouchInput {
            repeat(4) {
                swipeUp()
            }
        }
        composeRule.onNodeWithText("3:09:04", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun distanceMode_hidesDistancePresets() {
        setCompactContent()

        composeRule.onNodeWithTag(PACE_CALCULATOR_MODE_DISTANCE_TAG).performClick()

        composeRule.onAllNodesWithTag(PACE_CALCULATOR_PRESETS_TAG).assertCountEquals(0)
    }

    @Test
    fun distanceAndTime_showCalculatedPaceWithUnit_and_resultCanScrollIntoView() {
        val paceUnit = composeRule.activity.getString(R.string.settings_unit_min_per_km)

        setCompactContent()

        composeRule.onNodeWithTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG).performTextInput("5")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).performTextInput("25")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(PACE_CALCULATOR_BODY_TAG).performTouchInput {
            repeat(4) {
                swipeUp()
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(PACE_CALCULATOR_RESULT_TAG, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("5:00 $paceUnit", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun timeMode_calculatesFinishTime() {
        setCompactContent()

        composeRule.onNodeWithTag(PACE_CALCULATOR_MODE_TIME_TAG).performClick()
        composeRule.onNodeWithTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG).performTextInput("5")
        composeRule.onNodeWithTag(PACE_CALCULATOR_PACE_MINUTES_INPUT_TAG).performTextInput("5")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(PACE_CALCULATOR_BODY_TAG).performTouchInput {
            repeat(4) {
                swipeUp()
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("25:00", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun distanceMode_calculatesDistance() {
        val distanceUnit = composeRule.activity.getString(R.string.settings_unit_kilometers)

        setCompactContent()

        composeRule.onNodeWithTag(PACE_CALCULATOR_MODE_DISTANCE_TAG).performClick()
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).performTextInput("25")
        composeRule.onNodeWithTag(PACE_CALCULATOR_PACE_MINUTES_INPUT_TAG).performTextInput("5")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(PACE_CALCULATOR_BODY_TAG).performTouchInput {
            repeat(4) {
                swipeUp()
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("5 $distanceUnit", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun firstValidResult_logsOneCalculatorSession() {
        var calculations = 0
        setContent(onValidCalculation = { calculations += 1 })

        composeRule.onNodeWithTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG).performTextInput("5")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).performTextInput("25")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG).performTextInput("30")
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertTrue(calculations == 1)
        }
    }

    private fun setContent(
        onBack: () -> Unit = {},
        onValidCalculation: (com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode) -> Unit = {},
    ) {
        composeRule.setContent {
            Box(
                modifier =
                    Modifier.requiredSize(
                        PaceCalculatorTestViewportWidth,
                        PaceCalculatorTestViewportHeight,
                    ),
            ) {
                PaceCalculatorScreen(
                    settingsDistanceUnit = KILOMETERS,
                    settingsPaceUnit = MIN_PER_KM,
                    onBack = onBack,
                    onValidCalculation = onValidCalculation,
                )
            }
        }
    }

    private fun setCompactContent(onBack: () -> Unit = {}) {
        setContent(onBack = onBack)
    }

    private fun fieldWidth(bounds: DpRect): Float = bounds.right.value - bounds.left.value
}
