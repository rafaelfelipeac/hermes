package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoTrophySeeder
    @Inject
    constructor(
        private val userActionDao: UserActionDao,
        private val stringProvider: StringProvider,
    ) {
        fun buildLockedTrophyWorkouts(
            currentWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ): List<WorkoutEntity> {
            return buildLockedTrophyWorkouts(
                stringProvider = stringProvider,
                currentWeekStart = currentWeekStart,
                nextWeekStart = nextWeekStart,
            )
        }

        suspend fun seedCompletedTrophyActions(currentWeekStart: LocalDate) {
            buildCompletedTrophyActions(
                stringProvider = stringProvider,
                currentWeekStart = currentWeekStart,
            ).forEach { userActionDao.insert(it) }
        }
    }
