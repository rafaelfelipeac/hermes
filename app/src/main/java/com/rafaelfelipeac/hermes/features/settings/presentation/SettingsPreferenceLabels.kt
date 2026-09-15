package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.annotation.StringRes
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
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_MI
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
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.KILOGRAMS
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.POUNDS

@StringRes
internal fun themeLabelRes(themeMode: ThemeMode): Int {
    return when (themeMode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        LIGHT -> R.string.settings_theme_light
        DARK -> R.string.settings_theme_dark
    }
}

@StringRes
internal fun languageLabelRes(language: AppLanguage): Int {
    return when (language) {
        SYSTEM -> R.string.settings_language_system
        ENGLISH -> R.string.settings_language_english
        PORTUGUESE_BRAZIL ->
            R.string.settings_language_portuguese_brazil
        GERMAN -> R.string.settings_language_german
        FRENCH -> R.string.settings_language_french
        SPANISH -> R.string.settings_language_spanish
        ITALIAN -> R.string.settings_language_italian
        ARABIC -> R.string.settings_language_arabic
        HINDI -> R.string.settings_language_hindi
        JAPANESE -> R.string.settings_language_japanese
    }
}

@StringRes
internal fun weekStartLabelRes(weekStartDay: WeekStartDay): Int {
    return when (weekStartDay) {
        MONDAY -> R.string.day_monday
        TUESDAY -> R.string.day_tuesday
        WEDNESDAY -> R.string.day_wednesday
        THURSDAY -> R.string.day_thursday
        FRIDAY -> R.string.day_friday
        SATURDAY -> R.string.day_saturday
        SUNDAY -> R.string.day_sunday
    }
}

@StringRes
internal fun distanceUnitLabelRes(distanceUnit: DistanceUnit): Int {
    return when (distanceUnit) {
        KILOMETERS -> R.string.settings_unit_kilometers
        MILES -> R.string.settings_unit_miles
    }
}

@StringRes
internal fun paceUnitLabelRes(paceUnit: PaceUnit): Int {
    return when (paceUnit) {
        MIN_PER_KM -> R.string.settings_unit_min_per_km
        MIN_PER_MI -> R.string.settings_unit_min_per_mi
    }
}

@StringRes
internal fun weightUnitLabelRes(weightUnit: WeightUnit): Int {
    return when (weightUnit) {
        KILOGRAMS -> R.string.settings_unit_kilograms
        POUNDS -> R.string.settings_unit_pounds
    }
}

internal fun unitsSummaryLabelResources(
    distanceUnit: DistanceUnit,
    paceUnit: PaceUnit,
    weightUnit: WeightUnit,
): List<Int> = listOf(distanceUnitLabelRes(distanceUnit), paceUnitLabelRes(paceUnit), weightUnitLabelRes(weightUnit))
