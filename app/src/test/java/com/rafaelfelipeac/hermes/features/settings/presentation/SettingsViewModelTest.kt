package com.rafaelfelipeac.hermes.features.settings.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORIES_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DESTINATION_CONFIGURED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DESTINATION_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.SCHEMA_VERSION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.USER_ACTIONS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WORKOUTS_COUNT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_DISTANCE_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_PACE_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_WEIGHT_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CLEAR_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.EXPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SEED_DEMO_DATA
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SET_BACKUP_FOLDER
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupDataStats
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_MI
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.KILOGRAMS
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.POUNDS
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LargeClass")
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_initialValueExposesDefaultUnits() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialDistanceUnit = KILOMETERS,
                    initialPaceUnit = MIN_PER_KM,
                    initialWeightUnit = KILOGRAMS,
                    distanceUnit = KILOMETERS,
                    paceUnit = MIN_PER_KM,
                    weightUnit = KILOGRAMS,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(KILOMETERS, viewModel.state.value.distanceUnit)
            assertEquals(MIN_PER_KM, viewModel.state.value.paceUnit)
            assertEquals(KILOGRAMS, viewModel.state.value.weightUnit)
        }

    @Test
    @Suppress("LongMethod")
    fun state_emitsRepositoryValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val themeFlow = MutableStateFlow(DARK)
            val languageFlow = MutableStateFlow(PORTUGUESE_BRAZIL)
            val slotModePolicyFlow = MutableStateFlow(ALWAYS_SHOW)
            val weekStartDayFlow = MutableStateFlow(WeekStartDay.WEDNESDAY)
            val distanceUnitFlow = MutableStateFlow(MILES)
            val paceUnitFlow = MutableStateFlow(MIN_PER_MI)
            val weightUnitFlow = MutableStateFlow(POUNDS)
            val lastBackupExportedAtFlow = MutableStateFlow<String?>(null)
            val lastBackupImportedAtFlow = MutableStateFlow<String?>(null)
            val repository = mockk<SettingsRepository>()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            every { repository.themeMode } returns themeFlow
            every { repository.language } returns languageFlow
            every { repository.slotModePolicy } returns slotModePolicyFlow
            every { repository.weekStartDay } returns weekStartDayFlow
            every { repository.distanceUnit } returns distanceUnitFlow
            every { repository.paceUnit } returns paceUnitFlow
            every { repository.weightUnit } returns weightUnitFlow
            every { repository.lastBackupExportedAt } returns lastBackupExportedAtFlow
            every { repository.lastBackupImportedAt } returns lastBackupImportedAtFlow
            every { repository.backupFolderUri } returns MutableStateFlow(null)
            every { repository.initialThemeMode() } returns ThemeMode.SYSTEM
            every { repository.initialLanguage() } returns AppLanguage.SYSTEM
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.initialWeekStartDay() } returns WeekStartDay.MONDAY
            every { repository.initialDistanceUnit() } returns KILOMETERS
            every { repository.initialPaceUnit() } returns MIN_PER_KM
            every { repository.initialWeightUnit() } returns KILOGRAMS

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.state.test {
                assertEquals(
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                        weekStartDay = WeekStartDay.MONDAY,
                        distanceUnit = KILOMETERS,
                        paceUnit = MIN_PER_KM,
                        weightUnit = KILOGRAMS,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                    awaitItem(),
                )

                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = PORTUGUESE_BRAZIL,
                        slotModePolicy = ALWAYS_SHOW,
                        weekStartDay = WeekStartDay.WEDNESDAY,
                        distanceUnit = MILES,
                        paceUnit = MIN_PER_MI,
                        weightUnit = POUNDS,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                    awaitItem(),
                )

                weightUnitFlow.value = KILOGRAMS
                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = PORTUGUESE_BRAZIL,
                        slotModePolicy = ALWAYS_SHOW,
                        weekStartDay = WeekStartDay.WEDNESDAY,
                        distanceUnit = MILES,
                        paceUnit = MIN_PER_MI,
                        weightUnit = KILOGRAMS,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
                    ),
                    awaitItem(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun setThemeMode_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setThemeMode(DARK)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setThemeMode(DARK) }
        }

    @Test
    fun setLanguage_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setLanguage(ENGLISH)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setLanguage(ENGLISH) }
        }

    @Test
    fun setWeekStartDay_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setWeekStartDay(WeekStartDay.WEDNESDAY)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setWeekStartDay(WeekStartDay.WEDNESDAY) }
        }

    @Test
    fun setDistanceUnit_updatesRepositoryAndLogsChange() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository(initialDistanceUnit = KILOMETERS, distanceUnit = KILOMETERS)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setDistanceUnit(MILES)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setDistanceUnit(MILES) }
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = CHANGE_DISTANCE_UNIT,
                    entityType = SETTINGS,
                    entityId = null,
                    metadata =
                        mapOf(
                            OLD_VALUE to KILOMETERS.name,
                            NEW_VALUE to MILES.name,
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun setPaceUnit_updatesRepositoryAndLogsChange() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository(initialPaceUnit = MIN_PER_KM, paceUnit = MIN_PER_KM)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setPaceUnit(MIN_PER_MI)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setPaceUnit(MIN_PER_MI) }
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = CHANGE_PACE_UNIT,
                    entityType = SETTINGS,
                    entityId = null,
                    metadata =
                        mapOf(
                            OLD_VALUE to MIN_PER_KM.name,
                            NEW_VALUE to MIN_PER_MI.name,
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun setWeightUnit_updatesRepositoryAndLogsChange() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository(initialWeightUnit = KILOGRAMS, weightUnit = KILOGRAMS)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setWeightUnit(POUNDS)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setWeightUnit(POUNDS) }
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = CHANGE_WEIGHT_UNIT,
                    entityType = SETTINGS,
                    entityId = null,
                    metadata =
                        mapOf(
                            OLD_VALUE to KILOGRAMS.name,
                            NEW_VALUE to POUNDS.name,
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun setUnitMethods_sameCurrentValue_doNotLogChange() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialDistanceUnit = MILES,
                    initialPaceUnit = MIN_PER_MI,
                    initialWeightUnit = POUNDS,
                    distanceUnit = MILES,
                    paceUnit = MIN_PER_MI,
                    weightUnit = POUNDS,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setDistanceUnit(MILES)
            viewModel.setPaceUnit(MIN_PER_MI)
            viewModel.setWeightUnit(POUNDS)
            advanceUntilIdle()

            coVerify(exactly = 0) {
                userActionLogger.log(
                    actionType = CHANGE_DISTANCE_UNIT,
                    entityType = SETTINGS,
                    entityId = any(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
            coVerify(exactly = 0) {
                userActionLogger.log(
                    actionType = CHANGE_PACE_UNIT,
                    entityType = SETTINGS,
                    entityId = any(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
            coVerify(exactly = 0) {
                userActionLogger.log(
                    actionType = CHANGE_WEIGHT_UNIT,
                    entityType = SETTINGS,
                    entityId = any(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun setLanguage_triggersCategoryLocalizationSync() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialLanguage = PORTUGUESE_BRAZIL,
                    language = PORTUGUESE_BRAZIL,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setLanguage(ENGLISH)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                categorySeeder.syncLocalizedNames(
                    previousLanguage = PORTUGUESE_BRAZIL,
                    newLanguage = ENGLISH,
                    force = false,
                )
            }
        }

    @Test
    fun logExportBackupResult_success_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.exportBackupJson(any()) } returns Result.success("{}")
            coEvery { backupRepository.getDataStats() } returns
                BackupDataStats(
                    schemaVersion = 1,
                    workoutsCount = 2,
                    categoriesCount = 3,
                    userActionsCount = 4,
                )

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            val exportResult = viewModel.exportBackupJson("1.3.0")
            viewModel.logExportBackupResult(
                exportResult = exportResult,
                destinationType = "save_as",
                destinationConfigured = false,
            )
            advanceUntilIdle()

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
                            SCHEMA_VERSION to "1",
                            WORKOUTS_COUNT to "2",
                            CATEGORIES_COUNT to "3",
                            USER_ACTIONS_COUNT to "4",
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun importBackupJson_success_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.importBackupJson(any()) } returns
                ImportBackupResult.Success(
                    schemaVersion = 1,
                    workoutsCount = 2,
                    categoriesCount = 3,
                    userActionsCount = 4,
                )

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.importBackupJson("{}")
            advanceUntilIdle()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = IMPORT_BACKUP,
                    entityType = APP,
                    entityId = null,
                    metadata =
                        mapOf(
                            RESULT to "success",
                            SCHEMA_VERSION to "1",
                            WORKOUTS_COUNT to "2",
                            CATEGORIES_COUNT to "3",
                            USER_ACTIONS_COUNT to "4",
                        ),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun hasBackupData_returnsTrue_whenRepositoryHasData() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns true

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun seedActions_whenSeederMutates_emitEventsAndLogAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            coEvery { demoDataSeeder.seedCompletedTrophies() } returns true
            coEvery { demoDataSeeder.seedLockedTrophies() } returns true
            coEvery { demoDataSeeder.seed() } returns true
            val viewModel =
                createViewModel(
                    userActionLogger = userActionLogger,
                    demoDataSeeder = demoDataSeeder,
                )

            viewModel.demoSeedCompletedEvents.test {
                viewModel.seedDemoData()
                assertEquals(Unit, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.completedTrophiesSeedCompletedEvents.test {
                viewModel.seedCompletedTrophies()
                assertEquals(Unit, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.lockedTrophiesSeedCompletedEvents.test {
                viewModel.seedLockedTrophies()
                assertEquals(Unit, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.mixedTrophiesSeedCompletedEvents.test {
                viewModel.seedMixedTrophies()
                assertEquals(Unit, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 4) {
                userActionLogger.log(
                    actionType = SEED_DEMO_DATA,
                    entityType = APP,
                    entityId = null,
                    metadata = mapOf(RESULT to "success"),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun seedActions_whenSeederNoOps_doNotEmitEventsOrLogAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            coEvery { demoDataSeeder.seedCompletedTrophies() } returns false
            coEvery { demoDataSeeder.seedLockedTrophies() } returns false
            coEvery { demoDataSeeder.seed() } returns false
            val viewModel =
                createViewModel(
                    userActionLogger = userActionLogger,
                    demoDataSeeder = demoDataSeeder,
                )

            viewModel.demoSeedCompletedEvents.test {
                viewModel.seedDemoData()
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.completedTrophiesSeedCompletedEvents.test {
                viewModel.seedCompletedTrophies()
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.lockedTrophiesSeedCompletedEvents.test {
                viewModel.seedLockedTrophies()
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.mixedTrophiesSeedCompletedEvents.test {
                viewModel.seedMixedTrophies()
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 0) {
                userActionLogger.log(
                    actionType = SEED_DEMO_DATA,
                    entityType = any(),
                    entityId = any(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun clearDatabase_whenSeederMutates_emitsEventWithoutLoggingAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            coEvery { demoDataSeeder.clearDatabase() } returns true
            val viewModel =
                createViewModel(
                    userActionLogger = userActionLogger,
                    demoDataSeeder = demoDataSeeder,
                )

            viewModel.databaseClearCompletedEvents.test {
                viewModel.clearDatabase()
                assertEquals(Unit, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) { demoDataSeeder.clearDatabase() }
            coVerify(exactly = 0) {
                userActionLogger.log(
                    actionType = any(),
                    entityType = any(),
                    entityId = any(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
        }

    @Test
    fun hasBackupData_returnsTrue_whenSettingsAreNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = DARK,
                    language = AppLanguage.SYSTEM,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsFalse_whenRepositoryIsPristine() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(false, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsTrue_whenWeekStartDayIsNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                    weekStartDay = WeekStartDay.WEDNESDAY,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsTrue_whenDistanceUnitIsNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                    distanceUnit = MILES,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsTrue_whenPaceUnitIsNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                    paceUnit = MIN_PER_MI,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun hasBackupData_returnsTrue_whenWeightUnitIsNonDefault() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                createSettingsRepository(
                    initialThemeMode = ThemeMode.SYSTEM,
                    initialLanguage = AppLanguage.SYSTEM,
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                    weightUnit = POUNDS,
                )
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            coEvery { backupRepository.hasAnyData() } returns false

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            assertEquals(true, viewModel.hasBackupData())
        }

    @Test
    fun setBackupFolderUri_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.setBackupFolderUri("content://tree/test")
            advanceUntilIdle()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = SET_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    metadata = any(),
                    entityId = null,
                    timestamp = any(),
                )
            }
        }

    @Test
    fun clearBackupFolderUri_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = createSettingsRepository(backupFolderUri = "content://tree/test")
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.clearBackupFolderUri()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = CLEAR_BACKUP_FOLDER,
                    entityType = SETTINGS,
                    metadata = any(),
                    entityId = null,
                    timestamp = any(),
                )
            }
        }

    private fun createViewModel(
        userActionLogger: UserActionLogger,
        demoDataSeeder: DemoDataSeeder,
    ): SettingsViewModel {
        val repository = createSettingsRepository()
        val categorySeeder = mockk<CategorySeeder>(relaxed = true)
        val backupRepository = mockk<BackupRepository>(relaxed = true)

        return SettingsViewModel(
            repository,
            categorySeeder,
            userActionLogger,
            demoDataSeeder,
            backupRepository,
        )
    }

    private fun createSettingsRepository(
        initialThemeMode: ThemeMode = LIGHT,
        initialLanguage: AppLanguage = ENGLISH,
        initialSlotModePolicy: SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
        initialWeekStartDay: WeekStartDay = WeekStartDay.MONDAY,
        initialDistanceUnit: DistanceUnit = KILOMETERS,
        initialPaceUnit: PaceUnit = MIN_PER_KM,
        initialWeightUnit: WeightUnit = KILOGRAMS,
        themeMode: ThemeMode = initialThemeMode,
        language: AppLanguage = initialLanguage,
        slotModePolicy: SlotModePolicy = initialSlotModePolicy,
        weekStartDay: WeekStartDay = initialWeekStartDay,
        distanceUnit: DistanceUnit = initialDistanceUnit,
        paceUnit: PaceUnit = initialPaceUnit,
        weightUnit: WeightUnit = initialWeightUnit,
        lastBackupExportedAt: String? = null,
        lastBackupImportedAt: String? = null,
        backupFolderUri: String? = null,
    ): SettingsRepository {
        val repository = mockk<SettingsRepository>(relaxed = true)

        every { repository.initialThemeMode() } returns initialThemeMode
        every { repository.initialLanguage() } returns initialLanguage
        every { repository.initialSlotModePolicy() } returns initialSlotModePolicy
        every { repository.initialWeekStartDay() } returns initialWeekStartDay
        every { repository.initialDistanceUnit() } returns initialDistanceUnit
        every { repository.initialPaceUnit() } returns initialPaceUnit
        every { repository.initialWeightUnit() } returns initialWeightUnit
        every { repository.themeMode } returns MutableStateFlow(themeMode)
        every { repository.language } returns MutableStateFlow(language)
        every { repository.slotModePolicy } returns MutableStateFlow(slotModePolicy)
        every { repository.weekStartDay } returns MutableStateFlow(weekStartDay)
        every { repository.distanceUnit } returns MutableStateFlow(distanceUnit)
        every { repository.paceUnit } returns MutableStateFlow(paceUnit)
        every { repository.weightUnit } returns MutableStateFlow(weightUnit)
        every { repository.lastBackupExportedAt } returns MutableStateFlow(lastBackupExportedAt)
        every { repository.lastBackupImportedAt } returns MutableStateFlow(lastBackupImportedAt)
        every { repository.backupFolderUri } returns MutableStateFlow(backupFolderUri)
        every { repository.lastSeenTrophyCelebrationToken } returns MutableStateFlow(null)

        return repository
    }
}
