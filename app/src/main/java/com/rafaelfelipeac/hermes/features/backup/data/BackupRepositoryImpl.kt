@file:Suppress("LongParameterList", "NestedBlockDepth")

package com.rafaelfelipeac.hermes.features.backup.data

import android.util.Log
import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.SUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.decode
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupDataStats
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.repository.toImportBackupError
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl
    @Inject
    constructor(
        private val database: HermesDatabase,
        private val challengeDao: ChallengeDao,
        private val workoutDao: WorkoutDao,
        private val categoryDao: CategoryDao,
        private val userActionDao: UserActionDao,
        private val personalRecordDao: PersonalRecordDao,
        private val settingsRepository: SettingsRepository,
    ) : BackupRepository {
        override suspend fun exportBackupJson(appVersion: String): Result<String> {
            return runCatching {
                val snapshot =
                    BackupSnapshot(
                        schemaVersion = SUPPORTED_SCHEMA_VERSION,
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

                BackupJsonCodec.encode(snapshot)
            }
        }

        @Suppress("LongMethod", "ReturnCount")
        override suspend fun importBackupJson(rawJson: String): ImportBackupResult {
            val snapshot =
                when (val decoded = decode(rawJson)) {
                    is BackupDecodeResult.Failure -> {
                        return Failure(decoded.error.toImportBackupError())
                    }
                    is BackupDecodeResult.Success -> decoded.snapshot
                }

            val validationError = validateSnapshot(snapshot)
            if (validationError != null) {
                return Failure(validationError)
            }

            val dbResult =
                runCatching {
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

                        val challengeProgressEntries =
                            snapshot.challengeProgressEntries.map { it.toEntity() }
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

            if (dbResult.isFailure) {
                return Failure(ImportBackupError.WRITE_FAILED)
            }

            val settings = snapshot.settings
            if (settings != null) {
                runCatching {
                    settingsRepository.setThemeMode(ThemeMode.valueOf(settings.themeMode))
                    settingsRepository.setLanguage(AppLanguage.fromTag(settings.languageTag))
                    settingsRepository.setSlotModePolicy(SlotModePolicy.valueOf(settings.slotModePolicy))
                    settingsRepository.setWeekStartDay(WeekStartDay.valueOf(settings.weekStartDay))
                    settingsRepository.setDistanceUnit(DistanceUnit.valueOf(settings.distanceUnit))
                    settingsRepository.setPaceUnit(PaceUnit.valueOf(settings.paceUnit))
                    settingsRepository.setWeightUnit(WeightUnit.valueOf(settings.weightUnit))
                }.onFailure {
                    Log.w(
                        BACKUP_REPOSITORY_LOG_TAG,
                        LOG_SETTINGS_IMPORT_FAILED,
                        it,
                    )
                }
            }

            return ImportBackupResult.Success(
                schemaVersion = snapshot.schemaVersion,
                challengesCount = snapshot.challenges.size,
                challengeProgressEntriesCount = snapshot.challengeProgressEntries.size,
                workoutsCount = snapshot.workouts.size,
                categoriesCount = snapshot.categories.size,
                userActionsCount = snapshot.userActions.size,
            )
        }

        override suspend fun getDataStats(): BackupDataStats {
            return BackupDataStats(
                schemaVersion = SUPPORTED_SCHEMA_VERSION,
                challengesCount = challengeDao.getAllChallenges().size,
                challengeProgressEntriesCount = challengeDao.getAllProgressEntries().size,
                workoutsCount = workoutDao.getAll().size,
                categoriesCount = categoryDao.getCategories().size,
                userActionsCount = userActionDao.getAll().size,
            )
        }

        override suspend fun hasAnyData(): Boolean {
            return workoutDao.getAll().isNotEmpty() ||
                categoryDao.getCategories().isNotEmpty() ||
                userActionDao.getAll().isNotEmpty() ||
                personalRecordDao.getFamilies().isNotEmpty() ||
                personalRecordDao.getEntries().isNotEmpty() ||
                challengeDao.getAllChallenges().isNotEmpty() ||
                challengeDao.getAllProgressEntries().isNotEmpty()
        }

        @Suppress("CyclomaticComplexMethod", "LongMethod", "ReturnCount")
        private fun validateSnapshot(snapshot: BackupSnapshot): ImportBackupError? {
            val challengeIds = snapshot.challenges.map { it.id }.toSet()
            val categoryIds = snapshot.categories.map { it.id }.toSet()
            val personalRecordFamilyIds = snapshot.personalRecordFamilies.map { it.id }.toSet()
            val personalRecordEntryIds = snapshot.personalRecordEntries.map { it.id }.toSet()
            val personalRecordEntriesById = snapshot.personalRecordEntries.associateBy { it.id }

            snapshot.challenges.forEach { challenge ->
                if (challenge.title.isBlank()) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { ChallengeTargetType.valueOf(challenge.targetType) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (challenge.targetQuantity <= 0L) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { ChallengeLifecycle.valueOf(challenge.lifecycle) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                try {
                    val startDate = LocalDate.parse(challenge.startDate)
                    val endDate = LocalDate.parse(challenge.endDate)
                    if (startDate.isAfter(endDate)) {
                        return ImportBackupError.INVALID_FIELD_VALUE
                    }
                    if (challenge.targetType == ChallengeTargetType.DAILY.name) {
                        val periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
                        if (runCatching { Math.multiplyExact(challenge.targetQuantity, periodDays) }.isFailure) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                    }
                    Instant.parse(challenge.createdAt)
                    Instant.parse(challenge.updatedAt)
                    challenge.archivedAt?.let(Instant::parse)
                } catch (_: DateTimeParseException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (challenge.lifecycle == ChallengeLifecycle.ACTIVE.name && challenge.archivedAt != null) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (challenge.lifecycle == ChallengeLifecycle.ARCHIVED.name && challenge.archivedAt == null) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            }

            snapshot.challengeProgressEntries.forEach { entry ->
                if (entry.challengeId !in challengeIds) {
                    return ImportBackupError.INVALID_REFERENCE
                }
                if (entry.quantity <= 0L) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                try {
                    val entryDate = LocalDate.parse(entry.entryDate)
                    Instant.parse(entry.occurredAt)
                    Instant.parse(entry.createdAt)
                    Instant.parse(entry.updatedAt)
                    val challenge = snapshot.challenges.first { it.id == entry.challengeId }
                    val startDate = LocalDate.parse(challenge.startDate)
                    val endDate = LocalDate.parse(challenge.endDate)
                    if (entryDate.isBefore(startDate) || entryDate.isAfter(endDate)) {
                        return ImportBackupError.INVALID_FIELD_VALUE
                    }
                } catch (_: DateTimeParseException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            }

            snapshot.workouts.forEach { workout ->
                if (workout.dayOfWeek != null && workout.dayOfWeek !in VALID_DAY_OF_WEEK_RANGE) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }

                workout.timeSlot?.let {
                    if (runCatching { TimeSlot.valueOf(it) }.isFailure) {
                        return ImportBackupError.INVALID_FIELD_VALUE
                    }
                }

                if (runCatching { EventType.valueOf(workout.eventType) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }

                try {
                    LocalDate.parse(workout.weekStartDate)
                } catch (_: DateTimeParseException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }

                workout.categoryId?.let { categoryId ->
                    if (categoryId !in categoryIds) {
                        return ImportBackupError.INVALID_REFERENCE
                    }
                }
            }

            snapshot.personalRecordFamilies.forEach { family ->
                if (
                    runCatching {
                        PersonalRecordMetricType.valueOf(family.metricType)
                    }.isFailure
                ) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (
                    runCatching {
                        PersonalRecordUnit.valueOf(family.defaultUnit)
                    }.isFailure
                ) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (
                    runCatching {
                        PersonalRecordComparisonRule.valueOf(family.comparisonRule)
                    }.isFailure
                ) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                try {
                    Instant.parse(family.createdAt)
                    Instant.parse(family.updatedAt)
                } catch (_: DateTimeParseException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                family.categoryId?.let { categoryId ->
                    if (categoryId !in categoryIds) {
                        return ImportBackupError.INVALID_REFERENCE
                    }
                }
                family.manualCurrentEntryId?.let { entryId ->
                    val referencedEntry = personalRecordEntriesById[entryId]
                    if (entryId !in personalRecordEntryIds || referencedEntry?.familyId != family.id) {
                        return ImportBackupError.INVALID_REFERENCE
                    }
                }
            }

            snapshot.personalRecordEntries.forEach { entry ->
                if (entry.familyId !in personalRecordFamilyIds) {
                    return ImportBackupError.INVALID_REFERENCE
                }
                if (!entry.value.isFinite()) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { PersonalRecordUnit.valueOf(entry.unit) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                try {
                    LocalDate.parse(entry.recordDate)
                    Instant.parse(entry.createdAt)
                    Instant.parse(entry.updatedAt)
                } catch (_: DateTimeParseException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            }

            snapshot.settings?.let { settings ->
                if (runCatching { ThemeMode.valueOf(settings.themeMode) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { SlotModePolicy.valueOf(settings.slotModePolicy) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { WeekStartDay.valueOf(settings.weekStartDay) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { DistanceUnit.valueOf(settings.distanceUnit) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { PaceUnit.valueOf(settings.paceUnit) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { WeightUnit.valueOf(settings.weightUnit) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            }

            return null
        }
    }

private val VALID_DAY_OF_WEEK_RANGE = DayOfWeek.MONDAY.value..DayOfWeek.SUNDAY.value
private const val BACKUP_REPOSITORY_LOG_TAG = "BackupRepository"
private const val LOG_SETTINGS_IMPORT_FAILED = "Backup import committed core data, but settings restore failed."

private fun WorkoutEntity.toBackupRecord(): BackupWorkoutRecord {
    return BackupWorkoutRecord(
        id = id,
        weekStartDate = weekStartDate.toString(),
        dayOfWeek = dayOfWeek,
        timeSlot = timeSlot,
        sortOrder = sortOrder,
        eventType = eventType,
        type = type,
        description = description,
        isCompleted = isCompleted,
        categoryId = categoryId,
    )
}

private fun CategoryEntity.toBackupRecord(): BackupCategoryRecord {
    return BackupCategoryRecord(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}

private fun PersonalRecordFamilyEntity.toBackupRecord(): BackupPersonalRecordFamilyRecord {
    return BackupPersonalRecordFamilyRecord(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = metricType.name,
        defaultUnit = defaultUnit.name,
        comparisonRule = comparisonRule.name,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )
}

private fun PersonalRecordEntryEntity.toBackupRecord(): BackupPersonalRecordEntryRecord {
    return BackupPersonalRecordEntryRecord(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit.name,
        customUnitLabel = customUnitLabel,
        recordDate = recordDate.toString(),
        note = note,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )
}

private fun UserActionEntity.toBackupRecord(): BackupUserActionRecord {
    return BackupUserActionRecord(
        id = id,
        actionType = actionType,
        entityType = entityType,
        entityId = entityId,
        metadata = metadata,
        timestamp = timestamp,
    )
}

private fun BackupChallengeRecord.toEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = id,
        title = title,
        description = description,
        targetType = ChallengeTargetType.valueOf(targetType),
        targetQuantity = targetQuantity,
        categoryId = categoryId,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        lifecycle = ChallengeLifecycle.valueOf(lifecycle),
        archivedAt = archivedAt?.let { Instant.parse(it).toEpochMilli() },
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

private fun BackupChallengeProgressEntryRecord.toEntity(): ChallengeProgressEntryEntity {
    return ChallengeProgressEntryEntity(
        id = id,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = LocalDate.parse(entryDate),
        occurredAt = Instant.parse(occurredAt).toEpochMilli(),
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

private fun BackupCategoryRecord.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}

private fun BackupWorkoutRecord.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        weekStartDate = LocalDate.parse(weekStartDate),
        dayOfWeek = dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = eventType == EventType.REST.name,
        eventType = eventType,
        timeSlot = timeSlot,
        categoryId = categoryId,
        sortOrder = sortOrder,
    )
}

private fun BackupPersonalRecordFamilyRecord.toEntity(): PersonalRecordFamilyEntity {
    return PersonalRecordFamilyEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = PersonalRecordMetricType.valueOf(metricType),
        defaultUnit = PersonalRecordUnit.valueOf(defaultUnit),
        comparisonRule = PersonalRecordComparisonRule.valueOf(comparisonRule),
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

private fun BackupPersonalRecordEntryRecord.toEntity(): PersonalRecordEntryEntity {
    return PersonalRecordEntryEntity(
        id = id,
        familyId = familyId,
        value = value,
        unit = PersonalRecordUnit.valueOf(unit),
        customUnitLabel = customUnitLabel,
        recordDate = LocalDate.parse(recordDate),
        note = note,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

private fun BackupUserActionRecord.toEntity(): UserActionEntity {
    return UserActionEntity(
        id = id,
        actionType = actionType,
        entityType = entityType,
        entityId = entityId,
        metadata = metadata,
        timestamp = timestamp,
    )
}
