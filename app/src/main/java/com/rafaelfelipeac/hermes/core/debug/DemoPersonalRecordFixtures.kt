@file:Suppress("LongMethod")

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.OTHER_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit

internal fun personalRecordSeeds(stringProvider: StringProvider): List<PersonalRecordSeed> {
    return listOf(
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_5_km),
            categoryId = RUN_ID,
            metricType = PersonalRecordMetricType.TIME,
            unit = PersonalRecordUnit.SECOND,
            comparisonRule = PersonalRecordComparisonRule.LOWER_IS_BETTER,
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 1_532.0, daysAgo = 45),
                    PersonalRecordEntrySeed(value = 1_485.0, daysAgo = 24),
                    PersonalRecordEntrySeed(value = 1_438.0, daysAgo = 7),
                ),
        ),
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_longest_run),
            categoryId = RUN_ID,
            metricType = PersonalRecordMetricType.DISTANCE,
            unit = PersonalRecordUnit.KILOMETER,
            comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 10.0, daysAgo = 52),
                    PersonalRecordEntrySeed(value = 15.0, daysAgo = 31),
                    PersonalRecordEntrySeed(value = 21.1, daysAgo = 10),
                ),
        ),
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_deadlift),
            categoryId = STRENGTH_ID,
            metricType = PersonalRecordMetricType.WEIGHT,
            unit = PersonalRecordUnit.KILOGRAM,
            comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 100.0, daysAgo = 60),
                    PersonalRecordEntrySeed(value = 110.0, daysAgo = 35),
                    PersonalRecordEntrySeed(value = 120.0, daysAgo = 12),
                ),
        ),
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_cycling_power),
            categoryId = CYCLING_ID,
            metricType = PersonalRecordMetricType.POWER,
            unit = PersonalRecordUnit.WATT,
            comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 245.0, daysAgo = 48),
                    PersonalRecordEntrySeed(value = 268.0, daysAgo = 27),
                    PersonalRecordEntrySeed(value = 286.0, daysAgo = 5),
                ),
        ),
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_push_ups),
            categoryId = STRENGTH_ID,
            metricType = PersonalRecordMetricType.REPS,
            unit = PersonalRecordUnit.REP,
            comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 24.0, daysAgo = 42),
                    PersonalRecordEntrySeed(value = 31.0, daysAgo = 19),
                    PersonalRecordEntrySeed(value = 38.0, daysAgo = 3),
                ),
        ),
        PersonalRecordSeed(
            title = stringProvider.get(R.string.mock_personal_record_weekly_consistency),
            categoryId = OTHER_ID,
            metricType = PersonalRecordMetricType.CUSTOM,
            unit = PersonalRecordUnit.CUSTOM,
            comparisonRule = PersonalRecordComparisonRule.MANUAL,
            customUnitLabel = stringProvider.get(R.string.mock_personal_record_sessions_unit),
            entries =
                listOf(
                    PersonalRecordEntrySeed(value = 3.0, daysAgo = 20),
                    PersonalRecordEntrySeed(value = 5.0, daysAgo = 13),
                    PersonalRecordEntrySeed(value = 4.0, daysAgo = 6),
                ),
            manualCurrentEntryIndex = 1,
        ),
    )
}
