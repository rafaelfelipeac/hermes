package com.rafaelfelipeac.hermes.features.backup.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao

internal class BackupDatabaseWriter(
    private val database: HermesDatabase,
    private val challengeDao: ChallengeDao,
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionDao: UserActionDao,
    private val personalRecordDao: PersonalRecordDao,
) {
    suspend fun replace(snapshot: BackupSnapshot) {
        database.withTransaction {
            challengeDao.deleteAllProgressEntries()
            challengeDao.deleteAllChallenges()
            workoutDao.deleteAll()
            categoryDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()

            val challenges = snapshot.challenges.map { it.toEntity() }
            if (challenges.isNotEmpty()) {
                challengeDao.insertChallenges(challenges)
            }

            val challengeProgressEntries = snapshot.challengeProgressEntries.map { it.toEntity() }
            if (challengeProgressEntries.isNotEmpty()) {
                challengeDao.insertProgressEntries(challengeProgressEntries)
            }

            val categories = snapshot.categories.map { it.toEntity() }
            if (categories.isNotEmpty()) {
                categoryDao.insertAll(categories)
            }

            val families = snapshot.personalRecordFamilies.map { it.toEntity() }
            if (families.isNotEmpty()) {
                personalRecordDao.insertFamilies(families)
            }

            val entries = snapshot.personalRecordEntries.map { it.toEntity() }
            if (entries.isNotEmpty()) {
                personalRecordDao.insertEntries(entries)
            }

            val workouts = snapshot.workouts.map { it.toEntity() }
            if (workouts.isNotEmpty()) {
                workoutDao.insertAllReplace(workouts)
            }

            val userActions = snapshot.userActions.map { it.toEntity() }
            if (userActions.isNotEmpty()) {
                userActionDao.insertAll(userActions)
            }
        }
    }
}
