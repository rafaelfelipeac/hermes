package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.personalrecords.presentation.formatPersonalRecordValue
import java.text.NumberFormat
import java.util.Locale

private typealias PersonalRecordUnit =
    com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit

internal class ActivityUiFormatterPersonalRecord(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    fun buildPersonalRecordTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val metricLabel =
            personalRecordMetricLabel(metadata[UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE])
                ?: stringProvider.get(R.string.personal_records_metric_custom)
        val entryLabel =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE]
                ?.takeIf { it.isNotBlank() }
                ?: metricLabel

        return when (actionType) {
            UserActionType.CREATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_create_personal_record_family, metricLabel)

            UserActionType.UPDATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_update_personal_record_family, metricLabel)

            UserActionType.DELETE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_family, metricLabel)

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_create_personal_record_entry, entryLabel)

            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_update_personal_record_entry, entryLabel)

            UserActionType.DELETE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_entry, entryLabel)

            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(
                    R.string.activity_action_set_current_personal_record_entry,
                    entryLabel,
                )

            else -> null
        }
    }

    fun buildPersonalRecordFamilySubtitle(metadata: Map<String, String>): String? {
        val category =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: stringProvider.get(R.string.category_uncategorized)
        val metricType = personalRecordMetricLabel(metadata[UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE])
        val comparisonRule =
            personalRecordComparisonRuleLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE],
            )

        if (metricType == null && comparisonRule == null) return null

        return listOfNotNull(
            shared.quoteValue(category),
            metricType,
            comparisonRule,
        ).joinToString(shared.activitySubtitleSeparator())
    }

    fun buildPersonalRecordEntrySubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val unit = metadata[UserActionMetadataKeys.PERSONAL_RECORD_UNIT]
        val newValue =
            personalRecordValueLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE],
                unit,
                currentLocale,
            )
        val oldValue =
            personalRecordValueLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_OLD_VALUE],
                unit,
                currentLocale,
            )
        val normalizedValue =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE]
                ?.takeIf { it.isNotBlank() }
                ?.toDoubleOrNull()
                ?.let { NumberFormat.getNumberInstance(currentLocale).format(it) }
        val recordDate =
            personalRecordDateLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE],
                currentLocale,
            )

        return when (actionType) {
            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY -> {
                val valueChange =
                    when {
                        !oldValue.isNullOrBlank() && !newValue.isNullOrBlank() ->
                            stringProvider.get(
                                R.string.activity_subtitle_change_value,
                                shared.quoteValue(oldValue).orEmpty(),
                                shared.quoteValue(newValue).orEmpty(),
                            )

                        !newValue.isNullOrBlank() && !normalizedValue.isNullOrBlank() -> newValue
                        else -> newValue ?: normalizedValue
                    }

                listOfNotNull(valueChange, recordDate)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(shared.activitySubtitleSeparator())
            }

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY,
            UserActionType.DELETE_PERSONAL_RECORD_ENTRY,
            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY,
            -> {
                val valueWithUnit = newValue ?: normalizedValue

                listOfNotNull(valueWithUnit, recordDate)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(shared.activitySubtitleSeparator())
            }

            else -> null
        }
    }

    private fun personalRecordDateLabel(
        raw: String?,
        currentLocale: Locale,
    ): String? {
        return shared.formatDate(raw, currentLocale)
    }

    private fun personalRecordMetricLabel(raw: String?): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "DISTANCE" -> stringProvider.get(R.string.personal_records_metric_distance)
            "TIME" -> stringProvider.get(R.string.personal_records_metric_time)
            "WEIGHT" -> stringProvider.get(R.string.personal_records_metric_weight)
            "POWER" -> stringProvider.get(R.string.personal_records_metric_power)
            "REPS" -> stringProvider.get(R.string.personal_records_metric_reps)
            "CUSTOM" -> stringProvider.get(R.string.personal_records_metric_custom)
            else -> raw
        }
    }

    private fun personalRecordComparisonRuleLabel(raw: String?): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "HIGHER_IS_BETTER" -> stringProvider.get(R.string.personal_records_comparison_higher)
            "LOWER_IS_BETTER" -> stringProvider.get(R.string.personal_records_comparison_lower)
            "MANUAL" -> stringProvider.get(R.string.personal_records_comparison_manual)
            else -> raw
        }
    }

    private fun personalRecordValueLabel(
        raw: String?,
        unit: String?,
        currentLocale: Locale,
    ): String? {
        val value = raw?.takeIf { it.isNotBlank() } ?: return null
        val parsed = value.toDoubleOrNull()

        return when {
            parsed != null ->
                formatPersonalRecordValue(
                    value = parsed,
                    unit = personalRecordUnit(unit),
                    locale = currentLocale,
                    unitLabel = personalRecordUnitLabel(unit, parsed),
                )

            else -> value
        }
    }

    private fun personalRecordUnitLabel(
        raw: String?,
        value: Double? = null,
    ): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "KILOMETER" -> stringProvider.get(R.string.settings_unit_kilometers)
            "MILE" -> stringProvider.get(R.string.settings_unit_miles)
            "METER" -> stringProvider.get(R.string.personal_records_unit_meter_symbol)
            "SECOND" -> null
            "MINUTE" -> null
            "HOUR" -> null
            "KILOGRAM" -> stringProvider.get(R.string.settings_unit_kilograms)
            "POUND" -> stringProvider.get(R.string.settings_unit_pounds)
            "WATT" -> stringProvider.get(R.string.personal_records_unit_watt_symbol)
            "REP" ->
                if (value == 1.0) {
                    stringProvider.get(R.string.personal_records_unit_rep_singular)
                } else {
                    stringProvider.get(R.string.personal_records_unit_rep_plural)
                }

            else -> null
        }
    }

    private fun personalRecordUnit(raw: String?): PersonalRecordUnit {
        return runCatching {
            PersonalRecordUnit.valueOf(
                raw?.uppercase(Locale.ENGLISH).orEmpty(),
            )
        }.getOrDefault(PersonalRecordUnit.CUSTOM)
    }
}
