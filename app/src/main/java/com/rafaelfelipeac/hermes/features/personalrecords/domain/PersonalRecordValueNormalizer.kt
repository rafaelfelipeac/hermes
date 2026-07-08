package com.rafaelfelipeac.hermes.features.personalrecords.domain

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
            KILOMETER -> value * 1000.0
            MILE -> value * 1609.344
            METER -> value
            SECOND -> value
            MINUTE -> value * 60.0
            HOUR -> value * 3600.0
            KILOGRAM -> value
            POUND -> value * 0.45359237
            WATT -> value
            REP -> value
            CUSTOM -> value
        }
    }
}
