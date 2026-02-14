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
        var selectedTheme: ThemeMode? = null
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val darkLabel = context.getString(R.string.settings_theme_dark)

        composeRule.setContent {
            SettingsContent(
                state =
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                    ),
                appVersion = "0.0.0-test",
                onThemeSelected = { selectedTheme = it },
                onLanguageSelected = {},
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithText(darkLabel).performClick()

        composeRule.runOnIdle {
            assertEquals(ThemeMode.DARK, selectedTheme)
        }
    }

    @Test
    fun selectingLanguageInvokesCallback() {
        var selectedLanguage: AppLanguage? = null
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val englishLabel = context.getString(R.string.settings_language_english)

        composeRule.setContent {
            SettingsContent(
                state =
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                    ),
                appVersion = "0.0.0-test",
                onThemeSelected = {},
                onLanguageSelected = { selectedLanguage = it },
                onSeedDemoData = {},
                onCategoriesClick = {},
            )
        }

        composeRule.onNodeWithText(englishLabel).performClick()

        composeRule.runOnIdle {
            assertEquals(AppLanguage.ENGLISH, selectedLanguage)
        }
    }
}
