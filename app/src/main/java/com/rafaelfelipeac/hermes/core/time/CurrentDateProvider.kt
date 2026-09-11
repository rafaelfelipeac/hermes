package com.rafaelfelipeac.hermes.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
open class CurrentDateProvider
    @Inject
    constructor(
        private val clock: Clock,
    ) {
        open fun today(): LocalDate = LocalDate.now(clock)

        open fun observeToday(): Flow<LocalDate> =
            flow {
                while (true) {
                    emit(today())
                    delay(delayUntilNextMidnight().milliseconds)
                }
            }

        private fun delayUntilNextMidnight(): Long {
            val zone = clock.zone
            val now = LocalDateTime.now(clock)
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            val millis = Duration.between(now.atZone(zone), nextMidnight).toMillis()
            return millis.coerceAtLeast(MIN_DELAY_MS)
        }

        private companion object {
            const val MIN_DELAY_MS = 1L
        }
    }
