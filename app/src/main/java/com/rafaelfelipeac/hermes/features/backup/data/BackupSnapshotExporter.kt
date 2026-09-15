@file:Suppress("LongParameterList")

package com.rafaelfelipeac.hermes.features.backup.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.settings.domain.model.SettingsSnapshot
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import java.time.Instant
import java.time.ZoneOffset.UTC

internal class BackupSnapshotExporter(
    private val database: HermesDatabase,
    private val challengeDao: ChallengeDao,
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionDao: UserActionDao,
    private val personalRecordDao: PersonalRecordDao,
    private val settingsDataSource: BackupSettingsDataSource,
) {
    suspend fun buildSnapshot(appVersion: String): BackupSnapshot {
        val roomSnapshot =
            database.withTransaction {
                BackupRoomSnapshot(
                    challenges = challengeDao.getAllChallenges(),
                    challengeProgressEntries = challengeDao.getAllProgressEntries(),
                    workouts = workoutDao.getAll(),
                    categories = categoryDao.getCategories(),
                    personalRecordFamilies = personalRecordDao.getFamilies(),
                    personalRecordEntries = personalRecordDao.getEntries(),
                    userActions = userActionDao.getAll(),
                )
            }
        val settingsSnapshot = settingsDataSource.snapshot()

        return BackupSnapshot(
            schemaVersion = BackupJsonCodec.SUPPORTED_SCHEMA_VERSION,
            exportedAt = Instant.now().atOffset(UTC).toString(),
            appVersion = appVersion,
            challenges = roomSnapshot.challenges.map { it.toBackupRecord() },
            challengeProgressEntries = roomSnapshot.challengeProgressEntries.map { it.toBackupRecord() },
            workouts = roomSnapshot.workouts.map { it.toBackupRecord() },
            categories = roomSnapshot.categories.map { it.toBackupRecord() },
            personalRecordFamilies = roomSnapshot.personalRecordFamilies.map { it.toBackupRecord() },
            personalRecordEntries = roomSnapshot.personalRecordEntries.map { it.toBackupRecord() },
            userActions = roomSnapshot.userActions.map { it.toBackupRecord() },
            settings = settingsSnapshot.toBackupRecord(),
        )
    }
}

private data class BackupRoomSnapshot(
    val challenges: List<ChallengeEntity>,
    val challengeProgressEntries: List<ChallengeProgressEntryEntity>,
    val workouts: List<WorkoutEntity>,
    val categories: List<CategoryEntity>,
    val personalRecordFamilies: List<PersonalRecordFamilyEntity>,
    val personalRecordEntries: List<PersonalRecordEntryEntity>,
    val userActions: List<UserActionEntity>,
)

private fun ChallengeEntity.toBackupRecord(): BackupChallengeRecord =
    BackupChallengeRecord(
        id = id,
        title = title,
        description = description,
        targetType = targetType.name,
        targetQuantity = targetQuantity,
        categoryId = categoryId,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        lifecycle = lifecycle.name,
        archivedAt = archivedAt?.let { Instant.ofEpochMilli(it).toString() },
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )

private fun ChallengeProgressEntryEntity.toBackupRecord(): BackupChallengeProgressEntryRecord =
    BackupChallengeProgressEntryRecord(
        id = id,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = entryDate.toString(),
        occurredAt = Instant.ofEpochMilli(occurredAt).toString(),
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )

private fun SettingsSnapshot.toBackupRecord(): BackupSettingsRecord =
    BackupSettingsRecord(
        themeMode = themeMode.name,
        languageTag = language.tag,
        slotModePolicy = slotModePolicy.name,
        weekStartDay = weekStartDay.name,
        distanceUnit = distanceUnit.name,
        paceUnit = paceUnit.name,
        weightUnit = weightUnit.name,
    )
