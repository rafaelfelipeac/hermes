package com.rafaelfelipeac.hermes.features.backup.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORIES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_ENTRIES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DESTINATION_CONFIGURED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DESTINATION_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.FAILURE_REASON
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.SCHEMA_VERSION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.USER_ACTIONS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WORKOUTS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CLEAR_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.EXPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SET_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.features.backup.BACKUP_IMPORT_LOG_TAG
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.KILOGRAMS
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import java.time.Instant
import javax.inject.Inject
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.SYSTEM as SYSTEM_THEME

@HiltViewModel
class BackupViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val userActionLogger: UserActionLogger,
        private val backupRepository: BackupRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val operationMutex = Mutex()
        private val _isOperationInProgress = MutableStateFlow(false)
        val isOperationInProgress: StateFlow<Boolean> = _isOperationInProgress.asStateFlow()
        private val _pendingImportToken = MutableStateFlow(savedStateHandle.get<String>(PENDING_IMPORT_TOKEN_KEY))
        val pendingImportToken: StateFlow<String?> = _pendingImportToken.asStateFlow()

        fun setPendingImportToken(token: String) {
            savedStateHandle[PENDING_IMPORT_TOKEN_KEY] = token
            _pendingImportToken.value = token
        }

        fun clearPendingImportToken() {
            savedStateHandle.remove<String>(PENDING_IMPORT_TOKEN_KEY)
            _pendingImportToken.value = null
        }

        suspend fun runExclusiveOperation(block: suspend () -> Unit): Boolean {
            if (!operationMutex.tryLock()) {
                return false
            }

            _isOperationInProgress.value = true
            return try {
                block()
                true
            } finally {
                _isOperationInProgress.value = false
                operationMutex.unlock()
            }
        }

        suspend fun exportBackupJson(appVersion: String): Result<String> {
            return backupRepository.exportBackupJson(appVersion)
        }

        suspend fun logExportBackupResult(
            exportResult: Result<String>,
            destinationType: String,
            destinationConfigured: Boolean,
        ) {
            val metadata =
                mutableMapOf(
                    RESULT to RESULT_FAILURE,
                    DESTINATION_TYPE to destinationType,
                    DESTINATION_CONFIGURED to destinationConfigured.toString(),
                )

            if (exportResult.isSuccess) {
                metadata[RESULT] = RESULT_SUCCESS
                addExportStats(metadata)
                persistLastExportedAt(metadata)
            } else {
                metadata[FAILURE_REASON] =
                    exportResult.exceptionOrNull()?.javaClass?.simpleName ?: UNKNOWN_FAILURE_REASON
            }

            userActionLogger.log(
                actionType = EXPORT_BACKUP,
                entityType = APP,
                metadata = metadata,
            )
        }

        suspend fun importBackupJson(rawJson: String): ImportBackupResult {
            val result = backupRepository.importBackupJson(rawJson)
            val metadata = mutableMapOf(RESULT to RESULT_FAILURE)

            if (result is ImportBackupResult.Success) {
                metadata[RESULT] = if (result.settingsImported) RESULT_SUCCESS else RESULT_PARTIAL
                metadata[SCHEMA_VERSION] = result.schemaVersion.toString()
                metadata[CHALLENGES_COUNT] = result.challengesCount.toString()
                metadata[CHALLENGE_PROGRESS_ENTRIES_COUNT] = result.challengeProgressEntriesCount.toString()
                metadata[WORKOUTS_COUNT] = result.workoutsCount.toString()
                metadata[CATEGORIES_COUNT] = result.categoriesCount.toString()
                metadata[USER_ACTIONS_COUNT] = result.userActionsCount.toString()
                if (!result.settingsImported) {
                    metadata[FAILURE_REASON] = SETTINGS_IMPORT_FAILED
                }

                persistLastImportedAt(metadata)
            } else if (result is ImportBackupResult.Failure) {
                metadata[FAILURE_REASON] = result.error.name
            }

            userActionLogger.log(
                actionType = IMPORT_BACKUP,
                entityType = APP,
                metadata = metadata,
            )

            return result
        }

        suspend fun setBackupFolderUri(value: String?) {
            val previous = settingsRepository.backupFolderUri.first()

            settingsRepository.setBackupFolderUri(value)

            if (value != null && previous != value) {
                val oldValue =
                    if (previous.isNullOrBlank()) {
                        BACKUP_FOLDER_DEFAULT
                    } else {
                        BACKUP_FOLDER_CONFIGURED
                    }

                userActionLogger.log(
                    actionType = SET_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    metadata =
                        mapOf(
                            OLD_VALUE to oldValue,
                            NEW_VALUE to BACKUP_FOLDER_CONFIGURED,
                        ),
                )
            }
        }

        suspend fun clearBackupFolderUri(logUserAction: Boolean = true) {
            val hadFolder = !settingsRepository.backupFolderUri.first().isNullOrBlank()

            settingsRepository.setBackupFolderUri(null)

            if (hadFolder && logUserAction) {
                userActionLogger.log(
                    actionType = CLEAR_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    metadata =
                        mapOf(
                            OLD_VALUE to BACKUP_FOLDER_CONFIGURED,
                            NEW_VALUE to BACKUP_FOLDER_DEFAULT,
                        ),
                )
            }
        }

        suspend fun hasBackupData(): Boolean {
            return backupRepository.hasAnyData() || hasNonDefaultSettings()
        }

        private suspend fun addExportStats(metadata: MutableMap<String, String>) {
            runCatching {
                backupRepository.getDataStats()
            }.onSuccess { stats ->
                metadata[SCHEMA_VERSION] = stats.schemaVersion.toString()
                metadata[CHALLENGES_COUNT] = stats.challengesCount.toString()
                metadata[CHALLENGE_PROGRESS_ENTRIES_COUNT] = stats.challengeProgressEntriesCount.toString()
                metadata[WORKOUTS_COUNT] = stats.workoutsCount.toString()
                metadata[CATEGORIES_COUNT] = stats.categoriesCount.toString()
                metadata[USER_ACTIONS_COUNT] = stats.userActionsCount.toString()
            }.onFailure { throwable ->
                metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                Log.w(BACKUP_VIEW_MODEL_LOG_TAG, LOG_EXPORT_STATS_SIDE_EFFECT_FAILED, throwable)
            }
        }

        private suspend fun persistLastExportedAt(metadata: MutableMap<String, String>) {
            runCatching {
                settingsRepository.setLastBackupExportedAt(Instant.now().toString())
            }.onFailure { throwable ->
                metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                Log.w(BACKUP_VIEW_MODEL_LOG_TAG, LOG_EXPORT_TIMESTAMP_SIDE_EFFECT_FAILED, throwable)
            }
        }

        private suspend fun persistLastImportedAt(metadata: MutableMap<String, String>) {
            runCatching {
                settingsRepository.setLastBackupImportedAt(Instant.now().toString())
            }.onFailure { throwable ->
                metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                Log.w(BACKUP_IMPORT_LOG_TAG, LOG_IMPORT_TIMESTAMP_SIDE_EFFECT_FAILED, throwable)
            }
        }

        private suspend fun hasNonDefaultSettings(): Boolean {
            val themeMode = settingsRepository.themeMode.first()
            val language = settingsRepository.language.first()
            val slotModePolicy = settingsRepository.slotModePolicy.first()
            val weekStartDay = settingsRepository.weekStartDay.first()
            val distanceUnit = settingsRepository.distanceUnit.first()
            val paceUnit = settingsRepository.paceUnit.first()
            val weightUnit = settingsRepository.weightUnit.first()

            return themeMode != SYSTEM_THEME ||
                language != SYSTEM ||
                slotModePolicy != AUTO_WHEN_MULTIPLE ||
                weekStartDay != WeekStartDay.MONDAY ||
                distanceUnit != KILOMETERS ||
                paceUnit != MIN_PER_KM ||
                weightUnit != KILOGRAMS
        }

        private companion object {
            const val RESULT_SUCCESS = "success"
            const val RESULT_PARTIAL = "partial"
            const val RESULT_FAILURE = "failure"
            const val SETTINGS_IMPORT_FAILED = "settings_import_failed"
            const val UNKNOWN_FAILURE_REASON = "unknown"
            const val BACKUP_FOLDER_DEFAULT = "default"
            const val BACKUP_FOLDER_CONFIGURED = "configured"
            const val BACKUP_VIEW_MODEL_LOG_TAG = "BackupViewModel"
            const val LOG_EXPORT_STATS_SIDE_EFFECT_FAILED = "Export side effect failed while gathering backup stats."
            const val LOG_EXPORT_TIMESTAMP_SIDE_EFFECT_FAILED =
                "Export side effect failed while persisting last exported timestamp."
            const val LOG_IMPORT_TIMESTAMP_SIDE_EFFECT_FAILED =
                "Import side effect failed while persisting last imported timestamp."
            const val PENDING_IMPORT_TOKEN_KEY = "pending_import_token"
        }
    }

private fun Throwable.toSideEffectFailureReason(): String {
    return javaClass.simpleName ?: "Exception"
}
