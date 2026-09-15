package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.KILOGRAMS_PER_POUND
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_KILOMETER
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_MILE
import com.rafaelfelipeac.hermes.core.time.TimeConstants.SECONDS_PER_HOUR
import com.rafaelfelipeac.hermes.core.time.TimeConstants.SECONDS_PER_MINUTE
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

object PersonalRecordValueNormalizer {
    fun normalize(
        value: Double,
        unit: PersonalRecordUnit,
    ): Double {
        return when (unit) {
            KILOMETER -> value * METERS_PER_KILOMETER
            MILE -> value * METERS_PER_MILE
            METER -> value
            SECOND -> value
            MINUTE -> value * SECONDS_PER_MINUTE
            HOUR -> value * SECONDS_PER_HOUR
            KILOGRAM -> value
            POUND -> value * KILOGRAMS_PER_POUND
            WATT -> value
            REP -> value
            CUSTOM -> value
        }
    }

    fun denormalize(
        value: Double,
        unit: PersonalRecordUnit,
    ): Double {
        return when (unit) {
            KILOMETER -> value / METERS_PER_KILOMETER
            MILE -> value / METERS_PER_MILE
            METER -> value
            SECOND -> value
            MINUTE -> value / SECONDS_PER_MINUTE
            HOUR -> value / SECONDS_PER_HOUR
            KILOGRAM -> value
            POUND -> value / KILOGRAMS_PER_POUND
            WATT -> value
            REP -> value
            CUSTOM -> value
        }
    }

    fun convert(
        value: Double,
        fromUnit: PersonalRecordUnit,
        toUnit: PersonalRecordUnit,
    ): Double {
        if (fromUnit == toUnit) return value

        return denormalize(normalize(value, fromUnit), toUnit)
    }
}
