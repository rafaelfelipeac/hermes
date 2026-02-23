package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val APP_VERSION_TEST = "0.0.0-test"

class SettingsContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingThemeInvokesCallback() {
        var invoked = false
        composeRule.setContent {
            SettingsContent(
                state =
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                    ),
                appVersion = APP_VERSION_TEST,
                onThemeClick = { invoked = true },
                onLanguageClick = {},
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_THEME_ROW_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }

    @Test
    fun selectingLanguageInvokesCallback() {
        var invoked = false
        composeRule.setContent {
            SettingsContent(
                state =
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                    ),
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = { invoked = true },
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_LANGUAGE_ROW_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }
}
