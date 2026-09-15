@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.CUSTOM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.POWER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.REPS
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.HOUR
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.METER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MINUTE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.POUND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.REP
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.WATT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM as CUSTOM_UNIT

@Composable
internal fun familyLabelFor(
    family: PersonalRecordFamily?,
    entries: List<PersonalRecordEntry>,
): String {
    if (family == null) return stringResource(R.string.personal_records_entry_family_empty)
    val count = entries.count { it.familyId == family.id }
    return "${family.title} (${pluralStringResource(R.plurals.personal_records_family_entry_count, count, count)})"
}

@Composable
internal fun metricLabel(metricType: PersonalRecordMetricType): String {
    return when (metricType) {
        DISTANCE -> stringResource(R.string.personal_records_metric_distance)
        TIME -> stringResource(R.string.personal_records_metric_time)
        WEIGHT -> stringResource(R.string.personal_records_metric_weight)
        POWER -> stringResource(R.string.personal_records_metric_power)
        REPS -> stringResource(R.string.personal_records_metric_reps)
        CUSTOM -> stringResource(R.string.personal_records_metric_custom)
    }
}

@Composable
internal fun comparisonLabel(comparisonRule: PersonalRecordComparisonRule): String {
    return when (comparisonRule) {
        HIGHER_IS_BETTER -> stringResource(R.string.personal_records_comparison_higher)
        LOWER_IS_BETTER -> stringResource(R.string.personal_records_comparison_lower)
        MANUAL -> stringResource(R.string.personal_records_comparison_manual)
    }
}

@Composable
internal fun metricDescription(metricType: PersonalRecordMetricType): String {
    return when (metricType) {
        DISTANCE -> stringResource(R.string.personal_records_metric_distance_help)
        TIME -> stringResource(R.string.personal_records_metric_time_help)
        WEIGHT -> stringResource(R.string.personal_records_metric_weight_help)
        POWER -> stringResource(R.string.personal_records_metric_power_help)
        REPS -> stringResource(R.string.personal_records_metric_reps_help)
        CUSTOM -> stringResource(R.string.personal_records_metric_custom_help)
    }
}

@Composable
internal fun unitChoiceLabelFor(unit: PersonalRecordUnit): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.personal_records_unit_kilometer)
        MILE -> stringResource(R.string.personal_records_unit_mile)
        METER -> stringResource(R.string.personal_records_unit_meter)
        SECOND -> stringResource(R.string.personal_records_unit_second)
        MINUTE -> stringResource(R.string.personal_records_unit_minute)
        HOUR -> stringResource(R.string.personal_records_unit_hour)
        KILOGRAM -> stringResource(R.string.personal_records_unit_kilogram)
        POUND -> stringResource(R.string.personal_records_unit_pound)
        WATT -> stringResource(R.string.personal_records_unit_watt)
        REP -> stringResource(R.string.personal_records_unit_rep)
        CUSTOM_UNIT -> stringResource(R.string.personal_records_unit_custom)
    }
}

@Composable
internal fun unitValueLabelFor(unit: PersonalRecordUnit): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.settings_unit_kilometers)
        MILE -> stringResource(R.string.settings_unit_miles)
        METER -> stringResource(R.string.personal_records_unit_meter_symbol)
        SECOND -> stringResource(R.string.personal_records_unit_second_symbol)
        MINUTE -> stringResource(R.string.personal_records_unit_minute_symbol)
        HOUR -> stringResource(R.string.personal_records_unit_hour_symbol)
        KILOGRAM -> stringResource(R.string.settings_unit_kilograms)
        POUND -> stringResource(R.string.settings_unit_pounds)
        WATT -> stringResource(R.string.personal_records_unit_watt_symbol)
        REP -> stringResource(R.string.personal_records_unit_rep_singular)
        CUSTOM_UNIT -> stringResource(R.string.personal_records_unit_custom)
    }
}

@Composable
internal fun unitLabelFor(
    unit: PersonalRecordUnit,
    customLabel: String? = null,
    quantity: Double? = null,
): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.settings_unit_kilometers)
        MILE -> stringResource(R.string.settings_unit_miles)
        METER -> stringResource(R.string.personal_records_unit_meter_symbol)
        SECOND -> stringResource(R.string.personal_records_unit_second_symbol)
        MINUTE -> stringResource(R.string.personal_records_unit_minute_symbol)
        HOUR -> stringResource(R.string.personal_records_unit_hour_symbol)
        KILOGRAM -> stringResource(R.string.settings_unit_kilograms)
        POUND -> stringResource(R.string.settings_unit_pounds)
        WATT -> stringResource(R.string.personal_records_unit_watt_symbol)
        REP ->
            if (quantity == 1.0) {
                stringResource(R.string.personal_records_unit_rep_singular)
            } else {
                stringResource(R.string.personal_records_unit_rep_plural)
            }
        CUSTOM_UNIT -> customLabel?.ifBlank { null } ?: stringResource(R.string.personal_records_unit_custom)
    }
}
