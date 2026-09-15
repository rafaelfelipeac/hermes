@file:Suppress("LongParameterList", "ReturnCount")

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataSeeder
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val userActionDao: UserActionDao,
        private val personalRecordDao: PersonalRecordDao,
        private val categorySeeder: CategorySeeder,
        private val challengeRepository: ChallengeRepository,
        private val settingsRepository: SettingsRepository,
        private val workoutSeeder: DemoWorkoutSeeder,
        private val personalRecordSeeder: DemoPersonalRecordSeeder,
        private val activitySeeder: DemoActivitySeeder,
        private val challengeSeeder: DemoChallengeSeeder,
        private val trophySeeder: DemoTrophySeeder,
    ) {
        suspend fun clearDatabase(): Boolean {
            if (!BuildConfig.DEBUG) return false

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()
            challengeRepository.deleteAllProgressEntries()
            challengeRepository.deleteAllChallenges()
            categorySeeder.ensureSeeded()
            settingsRepository.setLastSeenTrophyCelebrationToken(null)

            return true
        }

        suspend fun seedCompletedTrophies(): Boolean {
            if (!BuildConfig.DEBUG) return false

            if (!seed()) return false

            val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(MONDAY))
            trophySeeder.seedCompletedTrophyActions(currentWeekStart)
            return true
        }

        suspend fun seedLockedTrophies(): Boolean {
            if (!BuildConfig.DEBUG) return false

            categorySeeder.restoreDefaults()

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()
            challengeRepository.deleteAllProgressEntries()
            challengeRepository.deleteAllChallenges()
            settingsRepository.setLastSeenTrophyCelebrationToken(null)

            val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(MONDAY))
            val nextWeekStart = currentWeekStart.plusWeeks(1)

            trophySeeder
                .buildLockedTrophyWorkouts(
                    currentWeekStart = currentWeekStart,
                    nextWeekStart = nextWeekStart,
                ).forEach { workoutDao.insert(it) }

            return true
        }

        suspend fun seed(): Boolean {
            if (!BuildConfig.DEBUG) return false

            categorySeeder.restoreDefaults()

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()
            challengeRepository.deleteAllProgressEntries()
            challengeRepository.deleteAllChallenges()

            val today = LocalDate.now()
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(MONDAY))
            val previousWeekStart = currentWeekStart.minusWeeks(1)
            val activityHistoryWeekStarts =
                listOf(
                    previousWeekStart.minusWeeks(3),
                    previousWeekStart.minusWeeks(2),
                    previousWeekStart.minusWeeks(1),
                    previousWeekStart,
                )
            val progressHistoryWeekStarts =
                (1..7)
                    .map { weekOffset -> currentWeekStart.minusWeeks(weekOffset.toLong()) }
                    .reversed()
            val nextWeekStart = currentWeekStart.plusWeeks(1)

            workoutSeeder.seedDemoWorkouts(
                historyWeekStarts = progressHistoryWeekStarts,
                currentWeekStart = currentWeekStart,
                nextWeekStart = nextWeekStart,
            )
            personalRecordSeeder.seed(today)
            activitySeeder.seed(
                currentWeekStart = currentWeekStart,
                olderWeekStarts = activityHistoryWeekStarts,
                nextWeekStart = nextWeekStart,
            )
            challengeSeeder.seed(
                today = today,
                clearExisting = false,
            )

            return true
        }

        suspend fun seedChallenges(): Boolean {
            if (!BuildConfig.DEBUG) return false

            categorySeeder.restoreDefaults()
            challengeSeeder.seed(today = LocalDate.now())
            return true
        }
    }
