package com.rafaelfelipeac.hermes.features.backup.presentation

import androidx.lifecycle.SavedStateHandle
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
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupDataStats
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun runExclusiveOperation_blocksConcurrentOperationUntilCurrentFinishes() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = mockk(relaxed = true),
                    savedStateHandle = SavedStateHandle(),
                )
            val stateJob = backgroundScope.launch { viewModel.isOperationInProgress.collect() }
            val firstOperationStarted = CompletableDeferred<Unit>()
            val finishFirstOperation = CompletableDeferred<Unit>()
            var secondOperationExecuted = false

            val firstOperation =
                launch {
                    viewModel.runExclusiveOperation {
                        firstOperationStarted.complete(Unit)
                        finishFirstOperation.await()
                    }
                }
            firstOperationStarted.await()
            advanceUntilIdle()

            val secondOperation =
                async {
                    viewModel.runExclusiveOperation {
                        secondOperationExecuted = true
                    }
                }

            assertEquals(true, viewModel.isOperationInProgress.value)
            assertEquals(false, secondOperation.await())
            assertEquals(false, secondOperationExecuted)

            finishFirstOperation.complete(Unit)
            firstOperation.join()
            advanceUntilIdle()

            assertEquals(false, viewModel.isOperationInProgress.value)
            stateJob.cancel()
        }

    @Test
    fun pendingImportToken_isRestoredFromSavedStateHandleAndCanBeCleared() =
        runTest(mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(mapOf("pending_import_token" to "token-1"))
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = mockk(relaxed = true),
                    savedStateHandle = savedStateHandle,
                )

            val stateJob = backgroundScope.launch { viewModel.pendingImportToken.collect() }
            advanceUntilIdle()

            assertEquals("token-1", viewModel.pendingImportToken.value)

            viewModel.clearPendingImportToken()
            advanceUntilIdle()

            assertEquals(null, viewModel.pendingImportToken.value)
            assertEquals(null, savedStateHandle.get<String>("pending_import_token"))
            stateJob.cancel()
        }

    @Test
    fun setPendingImportToken_persistsTokenInSavedStateHandle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle()
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = mockk(relaxed = true),
                    savedStateHandle = savedStateHandle,
                )

            val stateJob = backgroundScope.launch { viewModel.pendingImportToken.collect() }
            advanceUntilIdle()

            viewModel.setPendingImportToken("token-2")
            advanceUntilIdle()

            assertEquals("token-2", viewModel.pendingImportToken.value)
            assertEquals("token-2", savedStateHandle.get<String>("pending_import_token"))
            stateJob.cancel()
        }

    @Test
    fun logExportBackupResult_success_logsActionAndTimestamp() =
        runTest(mainDispatcherRule.testDispatcher) {
            val settingsRepository = FakeSettingsRepository()
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.getDataStats() } returns
                BackupDataStats(
                    schemaVersion = 6,
                    challengesCount = 5,
                    challengeProgressEntriesCount = 7,
                    workoutsCount = 2,
                    categoriesCount = 3,
                    userActionsCount = 4,
                )
            val viewModel =
                BackupViewModel(
                    settingsRepository = settingsRepository,
                    userActionLogger = userActionLogger,
                    backupRepository = backupRepository,
                    savedStateHandle = SavedStateHandle(),
                )

            viewModel.logExportBackupResult(
                exportResult = Result.success("{}"),
                destinationType = "save_as",
                destinationConfigured = false,
            )

            assertEquals(false, settingsRepository.lastBackupExportedAt.value.isNullOrBlank())
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = EXPORT_BACKUP,
                    entityType = APP,
                    entityId = null,
                    metadata =
                        mapOf(
                            RESULT to "success",
                            DESTINATION_TYPE to "save_as",
                            DESTINATION_CONFIGURED to "false",
                            SCHEMA_VERSION to "6",
                            CHALLENGES_COUNT to "5",
                            CHALLENGE_PROGRESS_ENTRIES_COUNT to "7",
                            WORKOUTS_COUNT to "2",
                            CATEGORIES_COUNT to "3",
                            USER_ACTIONS_COUNT to "4",
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun importBackupJson_partialSuccess_logsPartialAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.importBackupJson(any()) } returns
                ImportBackupResult.Success(
                    schemaVersion = 6,
                    challengesCount = 1,
                    challengeProgressEntriesCount = 2,
                    workoutsCount = 3,
                    categoriesCount = 4,
                    userActionsCount = 5,
                    settingsImported = false,
                )
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = userActionLogger,
                    backupRepository = backupRepository,
                    savedStateHandle = SavedStateHandle(),
                )

            viewModel.importBackupJson("{}")

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = IMPORT_BACKUP,
                    entityType = APP,
                    entityId = null,
                    metadata =
                        mapOf(
                            RESULT to "partial",
                            SCHEMA_VERSION to "6",
                            CHALLENGES_COUNT to "1",
                            CHALLENGE_PROGRESS_ENTRIES_COUNT to "2",
                            WORKOUTS_COUNT to "3",
                            CATEGORIES_COUNT to "4",
                            USER_ACTIONS_COUNT to "5",
                            FAILURE_REASON to "settings_import_failed",
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun hasBackupData_returnsTrueWhenRepositoryHasData() =
        runTest(mainDispatcherRule.testDispatcher) {
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns true
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = backupRepository,
                    savedStateHandle = SavedStateHandle(),
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsTrueWhenSettingsAreNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(themeMode = ThemeMode.DARK),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = backupRepository,
                    savedStateHandle = SavedStateHandle(),
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsFalseWhenRepositoryAndSettingsArePristine() =
        runTest(mainDispatcherRule.testDispatcher) {
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = mockk(relaxed = true),
                    backupRepository = backupRepository,
                    savedStateHandle = SavedStateHandle(),
                )

            assertEquals(false, viewModel.hasBackupData())
        }

    @Test
    fun setBackupFolderUri_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    userActionLogger = userActionLogger,
                    backupRepository = mockk(relaxed = true),
                    savedStateHandle = SavedStateHandle(),
                )

            viewModel.setBackupFolderUri("content://tree/test")

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = SET_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    entityId = null,
                    metadata =
                        mapOf(
                            OLD_VALUE to "default",
                            NEW_VALUE to "configured",
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun clearBackupFolderUri_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val viewModel =
                BackupViewModel(
                    settingsRepository = FakeSettingsRepository(backupFolderUri = "content://tree/test"),
                    userActionLogger = userActionLogger,
                    backupRepository = mockk(relaxed = true),
                    savedStateHandle = SavedStateHandle(),
                )

            viewModel.clearBackupFolderUri()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = CLEAR_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    entityId = null,
                    metadata =
                        mapOf(
                            OLD_VALUE to "configured",
                            NEW_VALUE to "default",
                        ),
                    timestamp = any(),
                )
            }
        }

    private class FakeSettingsRepository(
        themeMode: ThemeMode = ThemeMode.SYSTEM,
        language: AppLanguage = AppLanguage.SYSTEM,
        slotModePolicy: SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
        weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
        distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
        paceUnit: PaceUnit = PaceUnit.MIN_PER_KM,
        weightUnit: WeightUnit = WeightUnit.KILOGRAMS,
        backupFolderUri: String? = null,
    ) : SettingsRepository {
        override val themeMode = MutableStateFlow(themeMode)
        override val language = MutableStateFlow(language)
        override val slotModePolicy = MutableStateFlow(slotModePolicy)
        override val weekStartDay = MutableStateFlow(weekStartDay)
        override val distanceUnit = MutableStateFlow(distanceUnit)
        override val paceUnit = MutableStateFlow(paceUnit)
        override val weightUnit = MutableStateFlow(weightUnit)
        override val lastBackupExportedAt = MutableStateFlow<String?>(null)
        override val lastBackupImportedAt = MutableStateFlow<String?>(null)
        override val backupFolderUri = MutableStateFlow(backupFolderUri)
        override val lastSeenTrophyCelebrationToken = MutableStateFlow<String?>(null)

        override fun initialThemeMode(): ThemeMode = themeMode.value

        override fun initialLanguage(): AppLanguage = language.value

        override fun initialSlotModePolicy(): SlotModePolicy = slotModePolicy.value

        override fun initialWeekStartDay(): WeekStartDay = weekStartDay.value

        override fun initialDistanceUnit(): DistanceUnit = distanceUnit.value

        override fun initialPaceUnit(): PaceUnit = paceUnit.value

        override fun initialWeightUnit(): WeightUnit = weightUnit.value

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override suspend fun setLanguage(language: AppLanguage) = Unit

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) = Unit

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) = Unit

        override suspend fun setDistanceUnit(distanceUnit: DistanceUnit) = Unit

        override suspend fun setPaceUnit(paceUnit: PaceUnit) = Unit

        override suspend fun setWeightUnit(weightUnit: WeightUnit) = Unit

        override suspend fun setLastBackupExportedAt(value: String) {
            lastBackupExportedAt.value = value
        }

        override suspend fun setLastBackupImportedAt(value: String) {
            lastBackupImportedAt.value = value
        }

        override suspend fun setBackupFolderUri(value: String?) {
            backupFolderUri.value = value
        }

        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) = Unit
    }
}
