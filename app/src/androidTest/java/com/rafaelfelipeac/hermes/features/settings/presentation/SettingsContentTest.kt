package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
                    ),
                appVersion = "0.0.0-test",
                onThemeClick = { invoked = true },
                onLanguageClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithTag("settings_theme_row").performClick()

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
                    ),
                appVersion = "0.0.0-test",
                onThemeClick = {},
                onLanguageClick = { invoked = true },
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithTag("settings_language_row").performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }
}
