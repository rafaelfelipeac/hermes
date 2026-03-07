package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ARABIC
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.FRENCH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.GERMAN
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.HINDI
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ITALIAN
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.JAPANESE
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SPANISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.FRIDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.MONDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.SATURDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.SUNDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.THURSDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.TUESDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.WEDNESDAY

@Composable
internal fun SettingsLanguageScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_language_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_system),
            selected = language == SYSTEM,
            onClick = { onLanguageSelected(SYSTEM) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_english),
            selected = language == ENGLISH,
            onClick = { onLanguageSelected(ENGLISH) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_portuguese_brazil),
            selected = language == PORTUGUESE_BRAZIL,
            onClick = { onLanguageSelected(PORTUGUESE_BRAZIL) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_german),
            selected = language == GERMAN,
            onClick = { onLanguageSelected(GERMAN) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_french),
            selected = language == FRENCH,
            onClick = { onLanguageSelected(FRENCH) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_spanish),
            selected = language == SPANISH,
            onClick = { onLanguageSelected(SPANISH) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_italian),
            selected = language == ITALIAN,
            onClick = { onLanguageSelected(ITALIAN) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_arabic),
            selected = language == ARABIC,
            onClick = { onLanguageSelected(ARABIC) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_hindi),
            selected = language == HINDI,
            onClick = { onLanguageSelected(HINDI) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_language_japanese),
            selected = language == JAPANESE,
            onClick = { onLanguageSelected(JAPANESE) },
        )
    }
}

@Composable
internal fun SettingsThemeScreen(
    themeMode: ThemeMode,
    onBack: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_theme_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        SettingsOptionRow(
            label = stringResource(R.string.settings_theme_system),
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_theme_light),
            selected = themeMode == LIGHT,
            onClick = { onThemeSelected(LIGHT) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_theme_dark),
            selected = themeMode == DARK,
            onClick = { onThemeSelected(DARK) },
        )
    }
}

@Composable
internal fun SettingsSlotModeScreen(
    slotModePolicy: SlotModePolicy,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    onSlotModeSelected: (SlotModePolicy) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_slot_mode_title),
        onBack = onBack,
        onHelpClick = onHelpClick,
        helpContentDescription = stringResource(R.string.settings_slot_mode_help_title),
        modifier = modifier,
    ) {
        SettingsOptionRow(
            label = stringResource(R.string.settings_slot_mode_auto),
            selected = slotModePolicy == AUTO_WHEN_MULTIPLE,
            onClick = { onSlotModeSelected(AUTO_WHEN_MULTIPLE) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.settings_slot_mode_always),
            selected = slotModePolicy == ALWAYS_SHOW,
            onClick = { onSlotModeSelected(ALWAYS_SHOW) },
        )
    }
}

@Composable
internal fun SettingsWeekStartScreen(
    weekStartDay: WeekStartDay,
    onBack: () -> Unit,
    onWeekStartSelected: (WeekStartDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_week_start_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        SettingsOptionRow(
            label = stringResource(R.string.day_monday),
            selected = weekStartDay == MONDAY,
            onClick = { onWeekStartSelected(MONDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_tuesday),
            selected = weekStartDay == TUESDAY,
            onClick = { onWeekStartSelected(TUESDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_wednesday),
            selected = weekStartDay == WEDNESDAY,
            onClick = { onWeekStartSelected(WEDNESDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_thursday),
            selected = weekStartDay == THURSDAY,
            onClick = { onWeekStartSelected(THURSDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_friday),
            selected = weekStartDay == FRIDAY,
            onClick = { onWeekStartSelected(FRIDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_saturday),
            selected = weekStartDay == SATURDAY,
            onClick = { onWeekStartSelected(SATURDAY) },
        )
        SettingsOptionRow(
            label = stringResource(R.string.day_sunday),
            selected = weekStartDay == SUNDAY,
            onClick = { onWeekStartSelected(SUNDAY) },
        )
    }
}
