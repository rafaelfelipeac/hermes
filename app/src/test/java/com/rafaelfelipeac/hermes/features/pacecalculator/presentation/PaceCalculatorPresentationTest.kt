package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_MILE
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class PaceCalculatorPresentationTest {
    @Test
    fun metricPresets_keepExpectedOrderAndDistances() {
        val presets = paceDistancePresets(KILOMETERS)

        assertEquals(
            listOf(
                R.string.pace_calculator_preset_1_km,
                R.string.pace_calculator_preset_5k,
                R.string.pace_calculator_preset_10k,
                R.string.pace_calculator_preset_15k,
                R.string.pace_calculator_preset_half_marathon,
                R.string.pace_calculator_preset_marathon,
            ),
            presets.map { it.labelRes },
        )
        assertEquals(1_000.0, presets.first().valueMeters, 0.0)
        assertEquals(42_195.0, presets.last().valueMeters, 0.0)
    }

    @Test
    fun imperialPresets_keepMileValuesAndSharedRaceDistances() {
        val presets = paceDistancePresets(MILES)

        assertEquals(5, presets.size)
        assertEquals(METERS_PER_MILE, presets.first().valueMeters, 0.0)
        assertEquals(5.0 * METERS_PER_MILE, presets[1].valueMeters, 0.0)
        assertEquals(21_097.5, presets[3].valueMeters, 0.0)
    }

    @Test
    fun resultFormatters_useExplicitLocaleAndStablePaceShape() {
        assertEquals("1.5 km", formatDistance(1_500.0, 1_000.0, "km", Locale.US))
        assertEquals("1,5 km", formatDistance(1_500.0, 1_000.0, "km", Locale.forLanguageTag("pt-BR")))
        assertEquals("5:05 min/km", formatPaceSeconds(305.4, "min/km"))
    }

    @Test
    fun presetInputFormatting_usesSelectedDistanceUnit() {
        assertEquals("5", formatDistanceInput(5_000.0, KILOMETERS, Locale.US))
        assertEquals("1", formatDistanceInput(METERS_PER_MILE, MILES, Locale.US))
    }
}
