package com.rafaelfelipeac.hermes.features.backup.data

import android.util.Log
import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.SUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.decode
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupKnowledgeNoteRecord
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
import com.rafaelfelipeac.hermes.features.knowledgebase.data.local.KnowledgeNoteDao
import com.rafaelfelipeac.hermes.features.knowledgebase.data.local.KnowledgeNoteEntity
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
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
        private val workoutDao: WorkoutDao,
        private val knowledgeNoteDao: KnowledgeNoteDao,
        private val categoryDao: CategoryDao,
        private val userActionDao: UserActionDao,
        private val settingsRepository: SettingsRepository,
    ) : BackupRepository {
        override suspend fun exportBackupJson(appVersion: String): Result<String> {
            return runCatching {
                val snapshot =
                    BackupSnapshot(
                        schemaVersion = SUPPORTED_SCHEMA_VERSION,
                        exportedAt = Instant.now().atOffset(UTC).toString(),
                        appVersion = appVersion,
                        workouts = workoutDao.getAll().map { it.toBackupRecord() },
                        notes = knowledgeNoteDao.getAll().map { it.toBackupRecord() },
                        categories = categoryDao.getCategories().map { it.toBackupRecord() },
                        userActions = userActionDao.getAll().map { it.toBackupRecord() },
                        settings =
                            BackupSettingsRecord(
                                themeMode = settingsRepository.themeMode.first().name,
                                languageTag = settingsRepository.language.first().tag,
                                slotModePolicy = settingsRepository.slotModePolicy.first().name,
                                weekStartDay = settingsRepository.weekStartDay.first().name,
                            ),
                    )

                BackupJsonCodec.encode(snapshot)
            }
        }

        @Suppress("ReturnCount")
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
                        workoutDao.deleteAll()
                        knowledgeNoteDao.deleteAll()
                        categoryDao.deleteAll()
                        userActionDao.deleteAll()

                        val notes = snapshot.notes.map { it.toEntity() }
                        if (notes.isNotEmpty()) {
                            knowledgeNoteDao.insertAll(notes)
                        }

                        val categories = snapshot.categories.map { it.toEntity() }
                        if (categories.isNotEmpty()) {
                            categoryDao.insertAll(categories)
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
                workoutsCount = snapshot.workouts.size,
                notesCount = snapshot.notes.size,
                categoriesCount = snapshot.categories.size,
                userActionsCount = snapshot.userActions.size,
            )
        }

        override suspend fun getDataStats(): BackupDataStats {
            return BackupDataStats(
                schemaVersion = SUPPORTED_SCHEMA_VERSION,
                workoutsCount = workoutDao.getAll().size,
                notesCount = knowledgeNoteDao.getAll().size,
                categoriesCount = categoryDao.getCategories().size,
                userActionsCount = userActionDao.getAll().size,
            )
        }

        override suspend fun hasAnyData(): Boolean {
            return workoutDao.getAll().isNotEmpty() ||
                knowledgeNoteDao.getAll().isNotEmpty() ||
                categoryDao.getCategories().isNotEmpty() ||
                userActionDao.getAll().isNotEmpty()
        }

        @Suppress("CyclomaticComplexMethod", "ReturnCount")
        private fun validateSnapshot(snapshot: BackupSnapshot): ImportBackupError? {
            val categoryIds = snapshot.categories.map { it.id }.toSet()
            val workoutIds = snapshot.workouts.map { it.id }.toSet()

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

            snapshot.notes.forEach { note ->
                if (runCatching { KnowledgeNoteKind.valueOf(note.kind) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
                if (runCatching { KnowledgeNoteStatus.valueOf(note.status) }.isFailure) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }

                when (KnowledgeNoteKind.valueOf(note.kind)) {
                    KnowledgeNoteKind.SESSION -> {
                        if (note.sourceWorkoutId == null) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                        if (note.sourceWorkoutId !in workoutIds) {
                            return ImportBackupError.INVALID_REFERENCE
                        }
                        if (note.sourceType == null || runCatching { KnowledgeNoteSourceType.valueOf(note.sourceType) }.isFailure) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                        if (note.triggerScope != null) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                        if (note.categoryId != null) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                    }
                    KnowledgeNoteKind.NOTE -> {
                        if (note.sourceWorkoutId != null || note.sourceType != null || note.triggerScope != null || note.categoryId != null) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                    }
                    KnowledgeNoteKind.IMPORTANT -> {
                        if (note.triggerScope == null || runCatching { KnowledgeNoteTriggerScope.valueOf(note.triggerScope) }.isFailure) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                        if (note.sourceWorkoutId != null || note.sourceType != null) {
                            return ImportBackupError.INVALID_FIELD_VALUE
                        }
                        note.categoryId?.let { categoryId ->
                            if (categoryId !in categoryIds) {
                                return ImportBackupError.INVALID_REFERENCE
                            }
                        }
                    }
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

private fun KnowledgeNoteEntity.toBackupRecord(): BackupKnowledgeNoteRecord {
    return BackupKnowledgeNoteRecord(
        id = id,
        kind = kind,
        status = status,
        title = title,
        body = body,
        sourceWorkoutId = sourceWorkoutId,
        sourceType = sourceType,
        sourceTitle = sourceTitle,
        categoryId = categoryId,
        triggerScope = triggerScope,
        createdAt = createdAt,
        updatedAt = updatedAt,
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

private fun BackupKnowledgeNoteRecord.toEntity(): KnowledgeNoteEntity {
    return KnowledgeNoteEntity(
        id = id,
        kind = kind,
        status = status,
        title = title,
        body = body,
        sourceWorkoutId = sourceWorkoutId,
        sourceType = sourceType,
        sourceTitle = sourceTitle,
        categoryId = categoryId,
        triggerScope = triggerScope,
        createdAt = createdAt,
        updatedAt = updatedAt,
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
