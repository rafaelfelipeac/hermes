package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoWorkoutSeeder
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val stringProvider: StringProvider,
    ) {
        suspend fun seedDemoWorkouts(
            historyWeekStarts: List<LocalDate>,
            currentWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ) {
            buildDemoWorkouts(
                stringProvider = stringProvider,
                historyWeekStarts = historyWeekStarts,
                currentWeekStart = currentWeekStart,
                nextWeekStart = nextWeekStart,
            ).forEach { workoutDao.insert(it) }
        }
    }
