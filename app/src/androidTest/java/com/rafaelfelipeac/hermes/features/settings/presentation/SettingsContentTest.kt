package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val APP_VERSION_TEST = "0.0.0-test"
private const val APP_VERSION_WITH_RELEASE_NOTES = "1.7.0-dev"

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
                        weekStartDay = WeekStartDay.MONDAY,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                appVersion = APP_VERSION_TEST,
                onThemeClick = { invoked = true },
                onLanguageClick = {},
                onWeekStartClick = {},
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onSeedMixedTrophies = {},
                onSeedLockedTrophies = {},
                onSeedCompletedTrophies = {},
                onCategoriesClick = {},
                onBackupClick = {},
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
                        weekStartDay = WeekStartDay.MONDAY,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = { invoked = true },
                onWeekStartClick = {},
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onSeedMixedTrophies = {},
                onSeedLockedTrophies = {},
                onSeedCompletedTrophies = {},
                onCategoriesClick = {},
                onBackupClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_LANGUAGE_ROW_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }

    @Test
    fun selectingWeekStartInvokesCallback() {
        var invoked = false

        composeRule.setContent {
            SettingsContent(
                state =
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                        weekStartDay = WeekStartDay.MONDAY,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = {},
                onWeekStartClick = { invoked = true },
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onSeedMixedTrophies = {},
                onSeedLockedTrophies = {},
                onSeedCompletedTrophies = {},
                onCategoriesClick = {},
                onBackupClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_WEEK_START_ROW_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(true, invoked)
        }
    }

    @Test
    fun tappingVersionCardOpensReleaseNotesWhenVersionHasNotes() {
        composeRule.setContent {
            SettingsContent(
                state = settingsState(),
                appVersion = APP_VERSION_WITH_RELEASE_NOTES,
                onThemeClick = {},
                onLanguageClick = {},
                onWeekStartClick = {},
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onSeedMixedTrophies = {},
                onSeedLockedTrophies = {},
                onSeedCompletedTrophies = {},
                onCategoriesClick = {},
                onBackupClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).assertHasClickAction()
        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).performClick()

        composeRule.onNodeWithTag(SETTINGS_RELEASE_NOTES_SHEET_TAG).assertIsDisplayed()
    }

    @Test
    fun versionCardIsPassiveWhenVersionHasNoReleaseNotes() {
        composeRule.setContent {
            SettingsContent(
                state = settingsState(),
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = {},
                onWeekStartClick = {},
                onSlotModeClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
                onSeedDemoData = {},
                onSeedMixedTrophies = {},
                onSeedLockedTrophies = {},
                onSeedCompletedTrophies = {},
                onCategoriesClick = {},
                onBackupClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).assertHasNoClickAction()
        composeRule.onAllNodesWithTag(SETTINGS_RELEASE_NOTES_SHEET_TAG).assertCountEquals(0)
    }
}

private fun settingsState(): SettingsState {
    return SettingsState(
        themeMode = ThemeMode.SYSTEM,
        language = AppLanguage.SYSTEM,
        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
        weekStartDay = WeekStartDay.MONDAY,
        lastBackupExportedAt = null,
        lastBackupImportedAt = null,
        backupFolderUri = null,
    )
}
