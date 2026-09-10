package com.rafaelfelipeac.hermes.features.backup.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORIES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_ENTRIES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.FAILURE_REASON
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.SCHEMA_VERSION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.USER_ACTIONS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WORKOUTS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        override val language = MutableStateFlow(AppLanguage.SYSTEM)
        override val slotModePolicy = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
        override val weekStartDay = MutableStateFlow(WeekStartDay.MONDAY)
        override val distanceUnit = MutableStateFlow(DistanceUnit.KILOMETERS)
        override val paceUnit = MutableStateFlow(PaceUnit.MIN_PER_KM)
        override val weightUnit = MutableStateFlow(WeightUnit.KILOGRAMS)
        override val lastBackupExportedAt = MutableStateFlow<String?>(null)
        override val lastBackupImportedAt = MutableStateFlow<String?>(null)
        override val backupFolderUri = MutableStateFlow<String?>(null)
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
