package com.rafaelfelipeac.hermes.features.pacecalculator.domain

enum class PaceCalculatorMode {
    PACE,
    TIME,
    DISTANCE,
}

object PaceCalculator {
    fun calculatePaceSecondsPerUnit(
        distanceMeters: Double,
        timeSeconds: Double,
        paceUnitDistanceMeters: Double,
    ): Double {
        require(distanceMeters > 0.0) { "distanceMeters must be greater than 0." }
        require(timeSeconds >= 0.0) { "timeSeconds must be non-negative." }
        require(paceUnitDistanceMeters > 0.0) { "paceUnitDistanceMeters must be greater than 0." }

        return timeSeconds / (distanceMeters / paceUnitDistanceMeters)
    }

    fun calculateFinishTimeSeconds(
        distanceMeters: Double,
        paceSecondsPerUnit: Double,
        paceUnitDistanceMeters: Double,
    ): Double {
        require(distanceMeters >= 0.0) { "distanceMeters must be non-negative." }
        require(paceSecondsPerUnit >= 0.0) { "paceSecondsPerUnit must be non-negative." }
        require(paceUnitDistanceMeters > 0.0) { "paceUnitDistanceMeters must be greater than 0." }

        return paceSecondsPerUnit * (distanceMeters / paceUnitDistanceMeters)
    }

    fun calculateDistanceMeters(
        timeSeconds: Double,
        paceSecondsPerUnit: Double,
        paceUnitDistanceMeters: Double,
    ): Double {
        require(timeSeconds >= 0.0) { "timeSeconds must be non-negative." }
        require(paceSecondsPerUnit > 0.0) { "paceSecondsPerUnit must be greater than 0." }
        require(paceUnitDistanceMeters > 0.0) { "paceUnitDistanceMeters must be greater than 0." }

        return (timeSeconds / paceSecondsPerUnit) * paceUnitDistanceMeters
    }
}
