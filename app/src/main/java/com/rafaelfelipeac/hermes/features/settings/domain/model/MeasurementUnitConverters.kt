package com.rafaelfelipeac.hermes.features.settings.domain.model

import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_KILOMETER
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_MILE
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_MI

internal fun DistanceUnit.meters(): Double {
    return when (this) {
        KILOMETERS -> METERS_PER_KILOMETER
        MILES -> METERS_PER_MILE
    }
}

internal fun PaceUnit.meters(): Double {
    return when (this) {
        MIN_PER_KM -> METERS_PER_KILOMETER
        MIN_PER_MI -> METERS_PER_MILE
    }
}
