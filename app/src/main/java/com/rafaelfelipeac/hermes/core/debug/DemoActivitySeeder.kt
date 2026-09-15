package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoActivitySeeder
    @Inject
    constructor(
        private val userActionDao: UserActionDao,
        private val stringProvider: StringProvider,
    ) {
        suspend fun seed(
            currentWeekStart: LocalDate,
            olderWeekStarts: List<LocalDate>,
            nextWeekStart: LocalDate,
        ) {
            val now = System.currentTimeMillis()
            buildActivityHistoryActions(
                stringProvider = stringProvider,
                currentWeekStart = currentWeekStart,
                olderWeekStarts = olderWeekStarts,
                nextWeekStart = nextWeekStart,
                now = now,
                zoneId = ZoneId.systemDefault(),
            ).forEach { userActionDao.insert(it) }
        }
    }
