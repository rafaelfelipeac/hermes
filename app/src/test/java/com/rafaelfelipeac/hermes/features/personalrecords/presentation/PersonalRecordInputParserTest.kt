package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalRecordInputParserTest {
    @Test
    fun parsePersonalRecordValue_acceptsDecimalSeparatorWithDotOrComma() {
        val dotValue = parsePersonalRecordValue("4.25")
        val commaValue = parsePersonalRecordValue("4,25")

        assertNotNull(dotValue)
        assertNotNull(commaValue)
        assertEquals(4.25, dotValue ?: Double.NaN, 0.000001)
        assertEquals(4.25, commaValue ?: Double.NaN, 0.000001)
    }

    @Test
    fun parsePersonalRecordValue_rejectsInvalidText() {
        assertNull(parsePersonalRecordValue("abc"))
    }
}
