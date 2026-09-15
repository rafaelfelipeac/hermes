package com.rafaelfelipeac.hermes.core.strings

import com.rafaelfelipeac.hermes.core.time.secondsToDurationParts

fun formatElapsedTime(totalSeconds: Long): String {
    val (hours, minutes, seconds) = secondsToDurationParts(totalSeconds)

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
