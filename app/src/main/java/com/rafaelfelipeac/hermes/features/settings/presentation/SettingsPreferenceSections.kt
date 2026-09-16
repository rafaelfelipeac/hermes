package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit

@Composable
internal fun SettingsWorkoutSection(
    weekStartDay: WeekStartDay,
    distanceUnit: DistanceUnit,
    paceUnit: PaceUnit,
    weightUnit: WeightUnit,
    onSlotModeClick: () -> Unit,
    onWeekStartClick: () -> Unit,
    onUnitsClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.settings_workouts_title),
        contentInsideCard = false,
    ) {
        SettingsNavigationRow(
            label = stringResource(R.string.settings_slot_mode_title),
            onClick = onSlotModeClick,
        )

        SettingsNavigationRow(
            label = stringResource(R.string.settings_week_start_title),
            detail = weekStartLabel(weekStartDay),
            onClick = onWeekStartClick,
            modifier = Modifier.testTag(SETTINGS_WEEK_START_ROW_TAG),
        )

        SettingsNavigationRow(
            label = stringResource(R.string.settings_units_title),
            detail = unitsSummaryLabel(distanceUnit, paceUnit, weightUnit),
            onClick = onUnitsClick,
            modifier = Modifier.testTag(SETTINGS_UNITS_ROW_TAG),
        )
    }
}

@Composable
internal fun SettingsThemeSection(
    themeMode: ThemeMode,
    onThemeClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.settings_theme_title),
        contentInsideCard = false,
    ) {
        SettingsNavigationRow(
            label = themeLabel(themeMode),
            onClick = onThemeClick,
            modifier = Modifier.testTag(SETTINGS_THEME_ROW_TAG),
        )
    }
}

@Composable
internal fun SettingsLanguageSection(
    language: AppLanguage,
    onLanguageClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.settings_language_title),
        contentInsideCard = false,
    ) {
        SettingsNavigationRow(
            label = languageLabel(language),
            onClick = onLanguageClick,
            modifier = Modifier.testTag(SETTINGS_LANGUAGE_ROW_TAG),
        )
    }
}
