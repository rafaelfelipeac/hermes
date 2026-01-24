package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingThemeInvokesCallback() {
        var selectedTheme: ThemeMode? = null
        val context = composeRule.activity
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
        val context = composeRule.activity
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
            )
        }

        composeRule.onNodeWithText(englishLabel).performClick()

        composeRule.runOnIdle {
            assertEquals(AppLanguage.ENGLISH, selectedLanguage)
        }
    }
}
