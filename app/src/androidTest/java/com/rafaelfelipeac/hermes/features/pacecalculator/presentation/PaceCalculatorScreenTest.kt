package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
}
