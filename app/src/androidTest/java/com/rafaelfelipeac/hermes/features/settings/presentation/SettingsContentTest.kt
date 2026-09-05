package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE_TOKEN
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val APP_VERSION_TEST = "0.0.0-test"
private const val APP_VERSION_WITH_RELEASE_NOTES = "1.12.0-dev"

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
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
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
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
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
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
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
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).assertHasClickAction()
        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).performScrollTo().performClick()

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
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
            )
        }

        composeRule.onNodeWithTag(SETTINGS_APP_VERSION_CARD_TAG).assertHasNoClickAction()
        composeRule.onAllNodesWithTag(SETTINGS_RELEASE_NOTES_SHEET_TAG).assertCountEquals(0)
    }

    @Test
    fun selectingUnitsInvokesCallback() {
        var invoked = false
        composeRule.setContent {
            SettingsContent(
                state = settingsState(),
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = {},
                onWeekStartClick = {},
                onSlotModeClick = {},
                onUnitsClick = { invoked = true },
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
            )
        }
        composeRule.onNodeWithTag(SETTINGS_UNITS_ROW_TAG).performClick()
        composeRule.runOnIdle { assertEquals(true, invoked) }
    }

    @Test
    fun preferenceRowsReactToUpdatedState() {
        val state = mutableStateOf(settingsState())
        composeRule.setContent {
            SettingsContent(
                state = state.value,
                appVersion = APP_VERSION_TEST,
                onThemeClick = {},
                onLanguageClick = {},
                onWeekStartClick = {},
                onSlotModeClick = {},
                onUnitsClick = {},
                onFeedbackClick = { _, _ -> },
                onRateClick = {},
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNodeWithTag(SETTINGS_THEME_ROW_TAG)
            .assertTextContains(context.getString(R.string.settings_theme_system))
        composeRule.runOnIdle {
            state.value =
                state.value.copy(
                    themeMode = ThemeMode.DARK,
                    language = AppLanguage.PORTUGUESE_BRAZIL,
                    distanceUnit = DistanceUnit.MILES,
                    paceUnit = PaceUnit.MIN_PER_MI,
                    weightUnit = WeightUnit.POUNDS,
                )
        }
        composeRule.onNodeWithTag(SETTINGS_THEME_ROW_TAG)
            .assertTextContains(context.getString(R.string.settings_theme_dark))
        composeRule.onNodeWithTag(SETTINGS_LANGUAGE_ROW_TAG)
            .assertTextContains(context.getString(R.string.settings_language_portuguese_brazil))
        composeRule.onNodeWithTag(SETTINGS_UNITS_ROW_TAG)
            .assertTextContains(
                context.getString(
                    R.string.settings_units_summary,
                    context.getString(R.string.settings_unit_miles),
                    context.getString(R.string.settings_unit_min_per_mi),
                    context.getString(R.string.settings_unit_pounds),
                ),
            )
    }

    @Test
    fun unitOptionsDispatchAndUpdateSelectionIndependently() {
        val distance = mutableStateOf(DistanceUnit.KILOMETERS)
        val pace = mutableStateOf(PaceUnit.MIN_PER_KM)
        val weight = mutableStateOf(WeightUnit.KILOGRAMS)
        composeRule.setContent {
            SettingsUnitsScreen(
                distanceUnit = distance.value,
                paceUnit = pace.value,
                weightUnit = weight.value,
                onBack = {},
                onDistanceUnitSelected = { distance.value = it },
                onPaceUnitSelected = { pace.value = it },
                onWeightUnitSelected = { weight.value = it },
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNodeWithText(context.getString(R.string.settings_unit_miles)).performClick()
        composeRule.runOnIdle {
            assertEquals(DistanceUnit.MILES, distance.value)
            assertEquals(PaceUnit.MIN_PER_KM, pace.value)
            assertEquals(WeightUnit.KILOGRAMS, weight.value)
        }
        composeRule.onNodeWithText(context.getString(R.string.settings_unit_min_per_mi)).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_unit_pounds)).performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(DistanceUnit.MILES, distance.value)
            assertEquals(PaceUnit.MIN_PER_MI, pace.value)
            assertEquals(WeightUnit.POUNDS, weight.value)
        }
    }

    @Test
    fun aboutActionsDispatchLocalizedFeedbackAndRating() {
        var feedback: Pair<String, String>? = null
        var rated = false
        composeRule.setContent {
            SettingsAboutSection(
                appVersion = APP_VERSION_TEST,
                hasReleaseNotes = false,
                onReleaseNotesClick = {},
                onFeedbackClick = { subject, body -> feedback = subject to body },
                onRateClick = { rated = true },
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNodeWithText(context.getString(R.string.settings_feedback_title)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_rate_title)).performClick()
        composeRule.runOnIdle {
            assertEquals(
                context.getString(R.string.settings_feedback_subject, context.getString(R.string.app_name)),
                feedback?.first,
            )
            assertEquals(
                context.getString(R.string.settings_feedback_email_body, APP_VERSION_TEST)
                    .replace(NEW_LINE_TOKEN, NEW_LINE),
                feedback?.second,
            )
            assertEquals(true, rated)
        }
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
