package com.rafaelfelipeac.hermes.features.settings.presentation

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPreferenceLabelsTest {
    @Test
    fun themeLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                ThemeMode.SYSTEM to R.string.settings_theme_system,
                ThemeMode.LIGHT to R.string.settings_theme_light,
                ThemeMode.DARK to R.string.settings_theme_dark,
            )

        assertEquals(ThemeMode.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, themeLabelRes(value)) }
    }

    @Test
    fun languageLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                AppLanguage.SYSTEM to R.string.settings_language_system,
                AppLanguage.ENGLISH to R.string.settings_language_english,
                AppLanguage.PORTUGUESE_BRAZIL to R.string.settings_language_portuguese_brazil,
                AppLanguage.GERMAN to R.string.settings_language_german,
                AppLanguage.FRENCH to R.string.settings_language_french,
                AppLanguage.SPANISH to R.string.settings_language_spanish,
                AppLanguage.ITALIAN to R.string.settings_language_italian,
                AppLanguage.ARABIC to R.string.settings_language_arabic,
                AppLanguage.HINDI to R.string.settings_language_hindi,
                AppLanguage.JAPANESE to R.string.settings_language_japanese,
            )

        assertEquals(AppLanguage.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, languageLabelRes(value)) }
    }

    @Test
    fun weekStartLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                WeekStartDay.MONDAY to R.string.day_monday,
                WeekStartDay.TUESDAY to R.string.day_tuesday,
                WeekStartDay.WEDNESDAY to R.string.day_wednesday,
                WeekStartDay.THURSDAY to R.string.day_thursday,
                WeekStartDay.FRIDAY to R.string.day_friday,
                WeekStartDay.SATURDAY to R.string.day_saturday,
                WeekStartDay.SUNDAY to R.string.day_sunday,
            )

        assertEquals(WeekStartDay.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, weekStartLabelRes(value)) }
    }

    @Test
    fun distanceUnitLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                DistanceUnit.KILOMETERS to R.string.settings_unit_kilometers,
                DistanceUnit.MILES to R.string.settings_unit_miles,
            )

        assertEquals(DistanceUnit.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, distanceUnitLabelRes(value)) }
    }

    @Test
    fun paceUnitLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                PaceUnit.MIN_PER_KM to R.string.settings_unit_min_per_km,
                PaceUnit.MIN_PER_MI to R.string.settings_unit_min_per_mi,
            )

        assertEquals(PaceUnit.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, paceUnitLabelRes(value)) }
    }

    @Test
    fun weightUnitLabelResPreservesEveryExistingOption() {
        val expected =
            mapOf(
                WeightUnit.KILOGRAMS to R.string.settings_unit_kilograms,
                WeightUnit.POUNDS to R.string.settings_unit_pounds,
            )

        assertEquals(WeightUnit.entries.toSet(), expected.keys)
        expected.forEach { (value, resource) -> assertEquals(resource, weightUnitLabelRes(value)) }
    }

    @Test
    fun unitsSummaryKeepsDistancePaceWeightOrderIncludingMixedUnits() {
        assertEquals(
            listOf(R.string.settings_unit_miles, R.string.settings_unit_min_per_km, R.string.settings_unit_pounds),
            unitsSummaryLabelResources(DistanceUnit.MILES, PaceUnit.MIN_PER_KM, WeightUnit.POUNDS),
        )
        assertEquals(
            listOf(
                R.string.settings_unit_kilometers,
                R.string.settings_unit_min_per_mi,
                R.string.settings_unit_kilograms,
            ),
            unitsSummaryLabelResources(DistanceUnit.KILOMETERS, PaceUnit.MIN_PER_MI, WeightUnit.KILOGRAMS),
        )
    }
}
