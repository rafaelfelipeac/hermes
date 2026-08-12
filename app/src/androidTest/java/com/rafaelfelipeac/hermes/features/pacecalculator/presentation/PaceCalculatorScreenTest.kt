package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PaceCalculatorScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun systemBack_invokesOnBackCallback() {
        var backPressed = false

        composeRule.setContent {
            PaceCalculatorScreen(
                settingsDistanceUnit = KILOMETERS,
                settingsPaceUnit = MIN_PER_KM,
                onBack = { backPressed = true },
            )
        }

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.runOnIdle {
            assertTrue(backPressed)
        }
    }

    @Test
    fun distanceAndTime_showCalculatedPace() {
        composeRule.setContent {
            PaceCalculatorScreen(
                settingsDistanceUnit = KILOMETERS,
                settingsPaceUnit = MIN_PER_KM,
                onBack = {},
            )
        }

        composeRule.onNodeWithTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG).performTextInput("5")
        composeRule.onNodeWithTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG).performTextInput("25")

        composeRule.onNodeWithTag(PACE_CALCULATOR_RESULT_TAG).assertTextContains("5:00")
    }
}
