@file:Suppress("LongParameterList", "NestedBlockDepth", "TooGenericExceptionCaught")

package com.rafaelfelipeac.hermes.features.backup.data

import android.util.Log
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.backup.BACKUP_IMPORT_LOG_TAG
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.SUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec.decode
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupDataStats
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.repository.toImportBackupError
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
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
            val snapshot =
                BackupSnapshotExporter(
                    challengeDao = challengeDao,
                    workoutDao = workoutDao,
                    categoryDao = categoryDao,
                    userActionDao = userActionDao,
                    personalRecordDao = personalRecordDao,
                    settingsRepository = settingsRepository,
                ).buildSnapshot(appVersion)
            return runCatching { BackupJsonCodec.encode(snapshot) }
        }

        @Suppress("LongMethod", "ReturnCount")
        override suspend fun importBackupJson(rawJson: String): ImportBackupResult {
            Log.i(BACKUP_IMPORT_LOG_TAG, "$LOG_IMPORT_STARTED${rawJson.length}")

            val snapshot =
                when (val decoded = decode(rawJson)) {
                    is BackupDecodeResult.Failure -> {
                        Log.e(BACKUP_IMPORT_LOG_TAG, "$LOG_DECODE_FAILED${decoded.error.name}")
                        return Failure(decoded.error.toImportBackupError())
                    }
                    is BackupDecodeResult.Success -> {
                        Log.i(
                            BACKUP_IMPORT_LOG_TAG,
                            "$LOG_DECODE_SUCCEEDED${decoded.snapshot.schemaVersion}",
                        )
                        decoded.snapshot
                    }
                }

            val validationError = BackupSnapshotValidator.validate(snapshot)
            if (validationError != null) {
                Log.e(BACKUP_IMPORT_LOG_TAG, "$LOG_VALIDATION_FAILED${validationError.name}")
                return Failure(validationError)
            }

            val dbResult =
                try {
                    BackupDatabaseWriter(
                        database = database,
                        challengeDao = challengeDao,
                        workoutDao = workoutDao,
                        categoryDao = categoryDao,
                        userActionDao = userActionDao,
                        personalRecordDao = personalRecordDao,
                    ).replace(snapshot)
                    Result.success(Unit)
                } catch (t: Throwable) {
                    Result.failure(t)
                }

            if (dbResult.isFailure) {
                Log.e(
                    BACKUP_IMPORT_LOG_TAG,
                    LOG_DATABASE_WRITE_FAILED,
                    dbResult.exceptionOrNull(),
                )
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
                        BACKUP_IMPORT_LOG_TAG,
                        LOG_SETTINGS_IMPORT_FAILED,
                        it,
                    )
                }
            }

            Log.i(
                BACKUP_IMPORT_LOG_TAG,
                LOG_IMPORT_SUCCEEDED_FORMAT.format(
                    snapshot.schemaVersion,
                    snapshot.challenges.size,
                    snapshot.challengeProgressEntries.size,
                    snapshot.workouts.size,
                    snapshot.categories.size,
                    snapshot.personalRecordFamilies.size,
                    snapshot.personalRecordEntries.size,
                    snapshot.userActions.size,
                ),
            )

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
    }

private const val LOG_IMPORT_STARTED = "Import started; payloadCharacters="
private const val LOG_DECODE_FAILED = "Decode failed; error="
private const val LOG_DECODE_SUCCEEDED = "Decode succeeded; schemaVersion="
private const val LOG_VALIDATION_FAILED = "Snapshot validation failed; error="
private const val LOG_DATABASE_WRITE_FAILED = "Database transaction failed; import rolled back."
private const val LOG_SETTINGS_IMPORT_FAILED = "Backup import committed core data, but settings restore failed."
private const val LOG_IMPORT_SUCCEEDED_FORMAT =
    "Import succeeded; schemaVersion=%d, challenges=%d, challengeProgressEntries=%d, workouts=%d, " +
        "categories=%d, personalRecordFamilies=%d, personalRecordEntries=%d, userActions=%d"
