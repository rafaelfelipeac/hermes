package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.HOUR
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MINUTE
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class PersonalRecordValueFormatterTest {
    @Test
    fun formatPersonalRecordValue_normalizesMinuteAndHourUnits() {
        assertEquals("02:00", formatPersonalRecordValue(2.0, MINUTE, Locale.US))
        assertEquals("1:30:00", formatPersonalRecordValue(1.5, HOUR, Locale.US))
    }
}
