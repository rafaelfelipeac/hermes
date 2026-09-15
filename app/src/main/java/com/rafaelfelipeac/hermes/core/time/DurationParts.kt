package com.rafaelfelipeac.hermes.core.time

data class DurationParts(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
)

fun secondsToDurationParts(totalSeconds: Long): DurationParts {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    return DurationParts(
        hours = (safeSeconds / TimeConstants.SECONDS_PER_HOUR).toInt(),
        minutes = ((safeSeconds % TimeConstants.SECONDS_PER_HOUR) / TimeConstants.SECONDS_PER_MINUTE).toInt(),
        seconds = (safeSeconds % TimeConstants.SECONDS_PER_MINUTE).toInt(),
    )
}

fun durationPartsToSeconds(
    hours: Long = 0L,
    minutes: Long = 0L,
    seconds: Long = 0L,
): Long =
    hours * TimeConstants.SECONDS_PER_HOUR +
        minutes * TimeConstants.SECONDS_PER_MINUTE +
        seconds
