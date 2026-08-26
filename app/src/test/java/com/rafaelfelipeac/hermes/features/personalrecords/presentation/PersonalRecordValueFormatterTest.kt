package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.HOUR
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.METER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MINUTE
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class PersonalRecordValueFormatterTest {
    @Test
    fun formatPersonalRecordValue_normalizesMinuteAndHourUnits() {
        assertEquals("2:00", formatPersonalRecordValue(2.0, MINUTE, Locale.US))
        assertEquals("1:30:00", formatPersonalRecordValue(1.5, HOUR, Locale.US))
    }

    @Test
    fun formatPersonalRecordValue_roundsLongDecimalsWithoutKeepingZeroFraction() {
        assertEquals("137.94 m", formatPersonalRecordValue(137.94440467668812, METER, Locale.US, "m"))
        assertEquals("222,000 m", formatPersonalRecordValue(222000.0001, METER, Locale.US, "m"))
    }

    @Test
    fun formatEditablePersonalRecordValue_roundsConversionNoiseAndStripsTrailingZeros() {
        assertEquals("222000", formatEditablePersonalRecordValue(221999.99999999997))
        assertEquals("137.94", formatEditablePersonalRecordValue(137.94440467668812))
        assertEquals("222", formatEditablePersonalRecordValue(221.99999999999997))
    }
}
