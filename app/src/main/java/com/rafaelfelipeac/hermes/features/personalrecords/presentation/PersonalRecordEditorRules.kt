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

internal data class PersonalRecordEntryEditorSource(
    val family: PersonalRecordFamily?,
    val entries: List<PersonalRecordEntry>,
    val initialEntry: PersonalRecordEntry?,
    val isEdit: Boolean,
    val settingsDistanceUnit: DistanceUnit,
    val settingsWeightUnit: WeightUnit,
)

internal data class PersonalRecordEntryEditorFields(
    val valueText: String,
    val selectedUnit: PersonalRecordUnit,
    val time: DurationParts,
    val recordDate: LocalDate,
    val note: String,
    val customUnitLabel: String,
)

internal fun comparisonRuleAfterMetricSelection(
    metricType: PersonalRecordMetricType,
): PersonalRecordComparisonRule = metricType.defaultComparisonRule()

internal fun personalRecordEntryEditorDefaults(
    source: PersonalRecordEntryEditorSource,
): PersonalRecordEntryEditorDefaults {
    val family = source.family
    val currentEntry =
        if (family == null || source.isEdit) {
            source.initialEntry
        } else {
            PersonalRecordBestSelector.selectBest(family, source.entries)
        }
    return when {
        family == null -> emptyEditorDefaults(source.initialEntry)
        family.metricType == TIME -> timeEditorDefaults(currentEntry)
        else -> measuredEditorDefaults(source, family, currentEntry)
    }
}

private fun emptyEditorDefaults(initialEntry: PersonalRecordEntry?) =
    PersonalRecordEntryEditorDefaults(
        unit = initialEntry?.unit ?: KILOMETER,
        valueText = EMPTY,
        time = DurationParts(),
        customUnitLabel = initialEntry?.customUnitLabel.orEmpty(),
    )

private fun timeEditorDefaults(currentEntry: PersonalRecordEntry?): PersonalRecordEntryEditorDefaults {
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

private fun measuredEditorDefaults(
    source: PersonalRecordEntryEditorSource,
    family: PersonalRecordFamily,
    currentEntry: PersonalRecordEntry?,
): PersonalRecordEntryEditorDefaults {
    val unit =
        when {
            source.isEdit && source.initialEntry != null -> source.initialEntry.unit
            family.metricType == DISTANCE -> source.settingsDistanceUnit.asPersonalRecordUnit()
            family.metricType == WEIGHT -> source.settingsWeightUnit.asPersonalRecordUnit()
            else -> currentEntry?.unit ?: family.defaultUnit
        }
    val valueText =
        currentEntry?.let { entry ->
            val shouldConvert =
                !source.isEdit && (family.metricType == DISTANCE || family.metricType == WEIGHT)
            val value =
                if (shouldConvert) {
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
    fields: PersonalRecordEntryEditorFields,
    today: LocalDate,
): Boolean {
    family ?: return false
    val isTimeMetric = family.metricType == TIME
    val requiresCustomLabel =
        (family.metricType == DISTANCE || family.metricType == WEIGHT) && fields.selectedUnit == CUSTOM_UNIT
    return (isTimeMetric || parsePersonalRecordValue(fields.valueText) != null) &&
        (!requiresCustomLabel || fields.customUnitLabel.isNotBlank()) &&
        !fields.recordDate.isAfter(today)
}

internal fun buildPersonalRecordEntryInput(
    family: PersonalRecordFamily?,
    fields: PersonalRecordEntryEditorFields,
): PersonalRecordEntryInput? {
    return family?.let { resolvedFamily ->
        val isTimeMetric = resolvedFamily.metricType == TIME
        val value =
            if (isTimeMetric) {
                durationPartsToSeconds(
                    hours = fields.time.hours.toLong(),
                    minutes = fields.time.minutes.toLong(),
                    seconds = fields.time.seconds.toLong(),
                ).toDouble()
            } else {
                parsePersonalRecordValue(fields.valueText)
            }
        value?.let {
            PersonalRecordEntryInput(
                familyId = resolvedFamily.id,
                value = it,
                unit = if (isTimeMetric) SECOND else fields.selectedUnit,
                recordDate = fields.recordDate,
                note = fields.note.trim().ifBlank { null },
                customUnitLabel = fields.customUnitLabel.trim().ifBlank { null },
            )
        }
    }
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
