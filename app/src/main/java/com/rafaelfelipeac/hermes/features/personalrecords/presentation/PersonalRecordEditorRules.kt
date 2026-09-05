package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.time.DurationParts
import com.rafaelfelipeac.hermes.core.time.durationPartsToSeconds
import com.rafaelfelipeac.hermes.core.time.secondsToDurationParts
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordBestSelector
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordValueNormalizer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.defaultComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.POUND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import java.time.LocalDate
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM as CUSTOM_UNIT

internal data class PersonalRecordEntryEditorDefaults(
    val unit: PersonalRecordUnit,
    val valueText: String,
    val time: DurationParts,
    val customUnitLabel: String,
)

internal fun comparisonRuleAfterMetricSelection(
    metricType: PersonalRecordMetricType,
): PersonalRecordComparisonRule = metricType.defaultComparisonRule()

internal fun personalRecordEntryEditorDefaults(
    family: PersonalRecordFamily?,
    entries: List<PersonalRecordEntry>,
    initialEntry: PersonalRecordEntry?,
    isEdit: Boolean,
    settingsDistanceUnit: DistanceUnit,
    settingsWeightUnit: WeightUnit,
): PersonalRecordEntryEditorDefaults {
    if (family == null) {
        return PersonalRecordEntryEditorDefaults(
            unit = initialEntry?.unit ?: KILOMETER,
            valueText = EMPTY,
            time = DurationParts(),
            customUnitLabel = initialEntry?.customUnitLabel.orEmpty(),
        )
    }

    val currentEntry =
        if (isEdit) {
            initialEntry
        } else {
            PersonalRecordBestSelector.selectBest(family, entries)
        }

    if (family.metricType == TIME) {
        val normalizedSeconds =
            currentEntry?.let {
                PersonalRecordValueNormalizer.normalize(it.value, it.unit).toLong()
            } ?: 0L
        return PersonalRecordEntryEditorDefaults(
            unit = SECOND,
            valueText = EMPTY,
            time = secondsToDurationParts(normalizedSeconds),
            customUnitLabel = currentEntry?.customUnitLabel.orEmpty(),
        )
    }

    val unit =
        when {
            isEdit && initialEntry != null -> initialEntry.unit
            family.metricType == DISTANCE -> settingsDistanceUnit.asPersonalRecordUnit()
            family.metricType == WEIGHT -> settingsWeightUnit.asPersonalRecordUnit()
            else -> currentEntry?.unit ?: family.defaultUnit
        }
    val valueText =
        currentEntry?.let { entry ->
            val value =
                if (!isEdit && (family.metricType == DISTANCE || family.metricType == WEIGHT)) {
                    PersonalRecordValueNormalizer.convert(entry.value, entry.unit, unit)
                } else {
                    entry.value
                }
            formatEditablePersonalRecordValue(value)
        }.orEmpty()

    return PersonalRecordEntryEditorDefaults(
        unit = unit,
        valueText = valueText,
        time = DurationParts(),
        customUnitLabel = currentEntry?.customUnitLabel.orEmpty(),
    )
}

internal fun convertPersonalRecordEditorValue(
    valueText: String,
    fromUnit: PersonalRecordUnit,
    toUnit: PersonalRecordUnit,
): String {
    val value = parsePersonalRecordValue(valueText) ?: return valueText
    return formatEditablePersonalRecordValue(
        PersonalRecordValueNormalizer.convert(value, fromUnit, toUnit),
    )
}

internal fun canSavePersonalRecordEntry(
    family: PersonalRecordFamily?,
    valueText: String,
    selectedUnit: PersonalRecordUnit,
    customUnitLabel: String,
    recordDate: LocalDate,
    today: LocalDate,
): Boolean {
    family ?: return false
    val isTimeMetric = family.metricType == TIME
    val requiresCustomLabel =
        (family.metricType == DISTANCE || family.metricType == WEIGHT) && selectedUnit == CUSTOM_UNIT
    return (isTimeMetric || parsePersonalRecordValue(valueText) != null) &&
        (!requiresCustomLabel || customUnitLabel.isNotBlank()) &&
        !recordDate.isAfter(today)
}

internal fun buildPersonalRecordEntryInput(
    family: PersonalRecordFamily?,
    valueText: String,
    selectedUnit: PersonalRecordUnit,
    time: DurationParts,
    recordDate: LocalDate,
    note: String,
    customUnitLabel: String,
): PersonalRecordEntryInput? {
    family ?: return null
    val isTimeMetric = family.metricType == TIME
    val value =
        if (isTimeMetric) {
            durationPartsToSeconds(
                hours = time.hours.toLong(),
                minutes = time.minutes.toLong(),
                seconds = time.seconds.toLong(),
            ).toDouble()
        } else {
            parsePersonalRecordValue(valueText) ?: return null
        }
    return PersonalRecordEntryInput(
        familyId = family.id,
        value = value,
        unit = if (isTimeMetric) SECOND else selectedUnit,
        recordDate = recordDate,
        note = note.trim().ifBlank { null },
        customUnitLabel = customUnitLabel.trim().ifBlank { null },
    )
}

internal fun DistanceUnit.asPersonalRecordUnit(): PersonalRecordUnit {
    return when (this) {
        DistanceUnit.KILOMETERS -> KILOMETER
        DistanceUnit.MILES -> MILE
    }
}

internal fun WeightUnit.asPersonalRecordUnit(): PersonalRecordUnit {
    return when (this) {
        WeightUnit.KILOGRAMS -> KILOGRAM
        WeightUnit.POUNDS -> POUND
    }
}

internal fun parsePersonalRecordValue(valueText: String): Double? {
    return valueText
        .trim()
        .replace(',', '.')
        .toDoubleOrNull()
}
