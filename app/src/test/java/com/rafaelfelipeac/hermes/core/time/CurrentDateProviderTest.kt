package com.rafaelfelipeac.hermes.core.time

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentDateProviderTest {
    @Test
    fun observeToday_emitsCurrentDateWhenCollectedAgain() =
        runTest {
            val clock = MutableClock(Instant.parse("2026-05-18T12:00:00Z"))
            val provider = CurrentDateProvider(clock, zoneProvider = { ZoneOffset.UTC })

            provider.observeToday().test {
                assertEquals("2026-05-18", awaitItem().toString())
                cancelAndIgnoreRemainingEvents()
            }

            clock.instant = Instant.parse("2026-05-19T12:00:00Z")

            provider.observeToday().test {
                assertEquals("2026-05-19", awaitItem().toString())
                cancelAndIgnoreRemainingEvents()
            }
        }

    private class MutableClock(
        var instant: Instant,
        private val zone: ZoneId = ZoneOffset.UTC,
    ) : Clock() {
        override fun getZone(): ZoneId = zone

        override fun withZone(zone: ZoneId): Clock = MutableClock(instant = instant, zone = zone)

        override fun instant(): Instant = instant
    }
}
