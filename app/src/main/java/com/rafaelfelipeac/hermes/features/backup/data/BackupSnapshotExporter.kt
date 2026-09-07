package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneOffset.UTC

internal class BackupSnapshotExporter(
    private val challengeDao: ChallengeDao,
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionDao: UserActionDao,
    private val personalRecordDao: PersonalRecordDao,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun buildSnapshot(appVersion: String): BackupSnapshot {
        return BackupSnapshot(
            schemaVersion = BackupJsonCodec.SUPPORTED_SCHEMA_VERSION,
            exportedAt = Instant.now().atOffset(UTC).toString(),
            appVersion = appVersion,
            challenges =
                challengeDao.getAllChallenges().map { challenge ->
                    BackupChallengeRecord(
                        id = challenge.id,
                        title = challenge.title,
                        description = challenge.description,
                        targetType = challenge.targetType.name,
                        targetQuantity = challenge.targetQuantity,
                        categoryId = challenge.categoryId,
                        startDate = challenge.startDate.toString(),
                        endDate = challenge.endDate.toString(),
                        lifecycle = challenge.lifecycle.name,
                        archivedAt = challenge.archivedAt?.let { Instant.ofEpochMilli(it).toString() },
                        createdAt = Instant.ofEpochMilli(challenge.createdAt).toString(),
                        updatedAt = Instant.ofEpochMilli(challenge.updatedAt).toString(),
                    )
                },
            challengeProgressEntries =
                challengeDao.getAllProgressEntries().map { entry ->
                    BackupChallengeProgressEntryRecord(
                        id = entry.id,
                        challengeId = entry.challengeId,
                        quantity = entry.quantity,
                        entryDate = entry.entryDate.toString(),
                        occurredAt = Instant.ofEpochMilli(entry.occurredAt).toString(),
                        createdAt = Instant.ofEpochMilli(entry.createdAt).toString(),
                        updatedAt = Instant.ofEpochMilli(entry.updatedAt).toString(),
                    )
                },
            workouts = workoutDao.getAll().map { it.toBackupRecord() },
            categories = categoryDao.getCategories().map { it.toBackupRecord() },
            personalRecordFamilies = personalRecordDao.getFamilies().map { it.toBackupRecord() },
            personalRecordEntries = personalRecordDao.getEntries().map { it.toBackupRecord() },
            userActions = userActionDao.getAll().map { it.toBackupRecord() },
            settings =
                BackupSettingsRecord(
                    themeMode = settingsRepository.themeMode.first().name,
                    languageTag = settingsRepository.language.first().tag,
                    slotModePolicy = settingsRepository.slotModePolicy.first().name,
                    weekStartDay = settingsRepository.weekStartDay.first().name,
                    distanceUnit = settingsRepository.distanceUnit.first().name,
                    paceUnit = settingsRepository.paceUnit.first().name,
                    weightUnit = settingsRepository.weightUnit.first().name,
                ),
        )
    }
}
