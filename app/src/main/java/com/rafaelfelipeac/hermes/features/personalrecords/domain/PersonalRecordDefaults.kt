package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
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
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.KILOGRAMS
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.POUNDS
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM as CUSTOM_UNIT

fun PersonalRecordMetricType.defaultUnit(
    distanceUnit: DistanceUnit = KILOMETERS,
    weightUnit: WeightUnit = KILOGRAMS,
): PersonalRecordUnit {
    return when (this) {
        DISTANCE ->
            when (distanceUnit) {
                KILOMETERS -> KILOMETER
                MILES -> MILE
            }

        TIME -> SECOND
        WEIGHT ->
            when (weightUnit) {
                KILOGRAMS -> KILOGRAM
                POUNDS -> POUND
            }
        POWER -> WATT
        REPS -> REP
        PersonalRecordMetricType.CUSTOM -> CUSTOM_UNIT
    }
}

fun PersonalRecordMetricType.supportedUnits(): List<PersonalRecordUnit> {
    return when (this) {
        DISTANCE -> listOf(KILOMETER, MILE, METER)
        TIME -> listOf(SECOND, MINUTE, HOUR)
        WEIGHT -> listOf(KILOGRAM, POUND)
        POWER -> listOf(WATT)
        REPS -> listOf(REP)
        PersonalRecordMetricType.CUSTOM -> listOf(CUSTOM_UNIT)
    }
}

fun PersonalRecordMetricType.defaultComparisonRule(): PersonalRecordComparisonRule {
    return when (this) {
        DISTANCE,
        WEIGHT,
        POWER,
        REPS,
        -> HIGHER_IS_BETTER

        TIME -> LOWER_IS_BETTER
        PersonalRecordMetricType.CUSTOM -> MANUAL
    }
}
