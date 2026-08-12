package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalRecordDefaultsTest {
    @Test
    fun defaultUnit_usesAppDistanceUnit_forDistanceMetrics() {
        assertEquals(KILOMETER, DISTANCE.defaultUnit(KILOMETERS))
        assertEquals(MILE, DISTANCE.defaultUnit(MILES))
    }

    @Test
    fun defaultUnit_keepsExistingBehavior_forOtherMetricTypes() {
        assertEquals(SECOND, TIME.defaultUnit(MILES))
        assertEquals(KILOGRAM, WEIGHT.defaultUnit(KILOMETERS))
    }
}
