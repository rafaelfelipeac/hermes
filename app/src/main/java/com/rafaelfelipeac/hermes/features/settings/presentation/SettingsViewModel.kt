@file:Suppress("ImportOrdering")

package com.rafaelfelipeac.hermes.features.settings.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORIES_COUNT
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
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_LANGUAGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_SLOT_MODE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_THEME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CLEAR_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.EXPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SET_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.SYSTEM as SYSTEM_THEME

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
        private val categorySeeder: CategorySeeder,
        private val userActionLogger: UserActionLogger,
        private val demoDataSeeder: DemoDataSeeder,
        private val backupRepository: BackupRepository,
    ) : ViewModel() {
        private val demoSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val demoSeedCompletedEvents: SharedFlow<Unit> = demoSeedEvents.asSharedFlow()

        val state: StateFlow<SettingsState> =
            combine(
                repository.themeMode,
                repository.language,
                repository.slotModePolicy,
                repository.lastBackupExportedAt,
                repository.lastBackupImportedAt,
            ) { themeMode, language, slotModePolicy, lastBackupExportedAt, lastBackupImportedAt ->
                SettingsState(
                    themeMode = themeMode,
                    language = language,
                    slotModePolicy = slotModePolicy,
                    lastBackupExportedAt = lastBackupExportedAt,
                    lastBackupImportedAt = lastBackupImportedAt,
                    backupFolderUri = null,
                )
            }.let { baseState ->
                combine(baseState, repository.backupFolderUri) { base, backupFolderUri ->
                    base.copy(backupFolderUri = backupFolderUri)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SETTINGS_STATE_SHARING_TIMEOUT_MS),
                initialValue =
                    SettingsState(
                        themeMode = repository.initialThemeMode(),
                        language = repository.initialLanguage(),
                        slotModePolicy = repository.initialSlotModePolicy(),
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
            )

        fun setThemeMode(mode: ThemeMode) =
            viewModelScope.launch {
                val previous = state.value.themeMode

                repository.setThemeMode(mode)

                if (previous != mode) {
                    userActionLogger.log(
                        actionType = CHANGE_THEME,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to mode.name,
                            ),
                    )
                }
            }

        fun setLanguage(language: AppLanguage) =
            viewModelScope.launch {
                val previous = state.value.language

                repository.setLanguage(language)

                if (previous != language) {
                    userActionLogger.log(
                        actionType = CHANGE_LANGUAGE,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.tag,
                                NEW_VALUE to language.tag,
                            ),
                    )
                    categorySeeder.syncLocalizedNames(
                        previousLanguage = previous,
                        newLanguage = language,
                        force = false,
                    )
                }
            }

        fun setSlotModePolicy(policy: SlotModePolicy) =
            viewModelScope.launch {
                val previous = state.value.slotModePolicy

                repository.setSlotModePolicy(policy)

                if (previous != policy) {
                    userActionLogger.log(
                        actionType = CHANGE_SLOT_MODE,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to policy.name,
                            ),
                    )
                }
            }

        fun seedDemoData() =
            viewModelScope.launch {
                demoDataSeeder.seed()
                demoSeedEvents.emit(Unit)
            }

        fun syncLocalizedCategories() =
            viewModelScope.launch {
                categorySeeder.syncLocalizedNames()
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

                runCatching {
                    backupRepository.getDataStats()
                }.onSuccess { stats ->
                    metadata[SCHEMA_VERSION] = stats.schemaVersion.toString()
                    metadata[WORKOUTS_COUNT] = stats.workoutsCount.toString()
                    metadata[CATEGORIES_COUNT] = stats.categoriesCount.toString()
                    metadata[USER_ACTIONS_COUNT] = stats.userActionsCount.toString()
                }.onFailure { throwable ->
                    metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                    Log.w(
                        SETTINGS_VIEW_MODEL_LOG_TAG,
                        LOG_EXPORT_STATS_SIDE_EFFECT_FAILED,
                        throwable,
                    )
                }

                runCatching {
                    repository.setLastBackupExportedAt(Instant.now().toString())
                }.onFailure { throwable ->
                    metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                    Log.w(
                        SETTINGS_VIEW_MODEL_LOG_TAG,
                        LOG_EXPORT_TIMESTAMP_SIDE_EFFECT_FAILED,
                        throwable,
                    )
                }
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
                metadata[RESULT] = RESULT_SUCCESS
                metadata[SCHEMA_VERSION] = result.schemaVersion.toString()
                metadata[WORKOUTS_COUNT] = result.workoutsCount.toString()
                metadata[CATEGORIES_COUNT] = result.categoriesCount.toString()
                metadata[USER_ACTIONS_COUNT] = result.userActionsCount.toString()

                runCatching {
                    repository.setLastBackupImportedAt(Instant.now().toString())
                }.onFailure { throwable ->
                    metadata[FAILURE_REASON] = throwable.toSideEffectFailureReason()
                    Log.w(
                        SETTINGS_VIEW_MODEL_LOG_TAG,
                        LOG_IMPORT_TIMESTAMP_SIDE_EFFECT_FAILED,
                        throwable,
                    )
                }
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
            val previous = repository.backupFolderUri.first()

            repository.setBackupFolderUri(value)

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
            val hadFolder = !repository.backupFolderUri.first().isNullOrBlank()

            repository.setBackupFolderUri(null)

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

        suspend fun currentBackupFolderUri(): String? {
            return repository.backupFolderUri.first()
        }

        suspend fun hasBackupData(): Boolean {
            return backupRepository.hasAnyData() || hasNonDefaultSettings()
        }

        private suspend fun hasNonDefaultSettings(): Boolean {
            val themeMode = repository.themeMode.first()
            val language = repository.language.first()
            val slotModePolicy = repository.slotModePolicy.first()

            return themeMode != SYSTEM_THEME ||
                language != SYSTEM ||
                slotModePolicy != AUTO_WHEN_MULTIPLE
        }

        private companion object {
            const val SETTINGS_STATE_SHARING_TIMEOUT_MS = 5_000L
            const val RESULT_SUCCESS = "success"
            const val RESULT_FAILURE = "failure"
            const val UNKNOWN_FAILURE_REASON = "unknown"
            const val BACKUP_FOLDER_DEFAULT = "default"
            const val BACKUP_FOLDER_CONFIGURED = "configured"
            const val SETTINGS_VIEW_MODEL_LOG_TAG = "SettingsViewModel"
            const val LOG_EXPORT_STATS_SIDE_EFFECT_FAILED = "Export side effect failed while gathering backup stats."
            const val LOG_EXPORT_TIMESTAMP_SIDE_EFFECT_FAILED =
                "Export side effect failed while persisting last exported timestamp."
            const val LOG_IMPORT_TIMESTAMP_SIDE_EFFECT_FAILED =
                "Import side effect failed while persisting last imported timestamp."
        }
    }

private fun Throwable.toSideEffectFailureReason(): String {
    return javaClass.simpleName ?: "Exception"
}
