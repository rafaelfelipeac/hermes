package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalRecordInputParserTest {
    @Test
    fun parsePersonalRecordValue_acceptsDecimalSeparatorWithDotOrComma() {
        assertEquals(4.25, parsePersonalRecordValue("4.25")!!, 0.000001)
        assertEquals(4.25, parsePersonalRecordValue("4,25")!!, 0.000001)
    }

    @Test
    fun parsePersonalRecordValue_rejectsInvalidText() {
        assertNull(parsePersonalRecordValue("abc"))
    }
}
