package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.POUND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.REP
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.WATT
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalRecordValueNormalizerTest {
    @Test
    fun normalize_returnsCanonicalDistanceInMeters() {
        assertEquals(1000.0, PersonalRecordValueNormalizer.normalize(1.0, KILOMETER), 0.000001)
        assertEquals(1609.344, PersonalRecordValueNormalizer.normalize(1.0, MILE), 0.000001)
    }

    @Test
    fun normalize_returnsCanonicalWeightInKilograms() {
        assertEquals(45.359237, PersonalRecordValueNormalizer.normalize(100.0, POUND), 0.000001)
        assertEquals(100.0, PersonalRecordValueNormalizer.normalize(100.0, KILOGRAM), 0.000001)
    }

    @Test
    fun normalize_keepsPowerAndRepsAsIs() {
        assertEquals(240.0, PersonalRecordValueNormalizer.normalize(240.0, WATT), 0.000001)
        assertEquals(52.0, PersonalRecordValueNormalizer.normalize(52.0, REP), 0.000001)
    }
}
