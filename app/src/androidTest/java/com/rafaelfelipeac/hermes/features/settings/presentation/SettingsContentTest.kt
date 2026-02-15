package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemLabel = context.getString(R.string.settings_theme_system)

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

        composeRule.onNodeWithText(systemLabel).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }

    @Test
    fun selectingLanguageInvokesCallback() {
        var invoked = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemLabel = context.getString(R.string.settings_language_system)

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

        composeRule.onNodeWithText(systemLabel).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }
}
