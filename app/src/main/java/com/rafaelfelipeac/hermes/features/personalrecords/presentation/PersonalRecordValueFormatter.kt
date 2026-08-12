package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM
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
import java.text.NumberFormat
import java.time.Duration
import java.util.Locale

fun formatPersonalRecordValue(
    value: Double,
    unit: PersonalRecordUnit,
    locale: Locale,
    unitLabel: String? = null,
): String {
    return when (unit) {
        SECOND,
        MINUTE,
        HOUR,
        -> formatDuration(value.toLong())

        REP,
        WATT,
        -> formatWholeNumber(value, locale)

        KILOMETER,
        MILE,
        METER,
        KILOGRAM,
        POUND,
        CUSTOM,
        -> {
            val formattedNumber = formatNumber(value, locale)
            val suffix =
                unitLabel?.takeIf { it.isNotBlank() }
            if (suffix == null) {
                formattedNumber
            } else {
                "$formattedNumber $suffix"
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val duration = Duration.ofSeconds(totalSeconds)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    val seconds = duration.minusHours(hours).minusMinutes(minutes).seconds

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatWholeNumber(
    value: Double,
    locale: Locale,
): String {
    return NumberFormat.getIntegerInstance(locale).format(value)
}

private fun formatNumber(
    value: Double,
    locale: Locale,
): String {
    val formatter = NumberFormat.getNumberInstance(locale)
    formatter.minimumFractionDigits = if (value % 1.0 == 0.0) 0 else 1
    formatter.maximumFractionDigits = 2
    return formatter.format(value)
}
