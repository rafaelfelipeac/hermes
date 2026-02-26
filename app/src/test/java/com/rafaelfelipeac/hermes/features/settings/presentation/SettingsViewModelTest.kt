package com.rafaelfelipeac.hermes.features.settings.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.EXPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
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
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    @Suppress("LongMethod")
    fun state_emitsRepositoryValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val themeFlow = MutableStateFlow(ThemeMode.SYSTEM)
            val languageFlow = MutableStateFlow(AppLanguage.SYSTEM)
            val slotModePolicyFlow = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
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
            every { repository.lastBackupExportedAt } returns lastBackupExportedAtFlow
            every { repository.lastBackupImportedAt } returns lastBackupImportedAtFlow
            every { repository.initialThemeMode() } returns ThemeMode.SYSTEM
            every { repository.initialLanguage() } returns AppLanguage.SYSTEM
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE

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
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                    ),
                    awaitItem(),
                )

                themeFlow.value = DARK
                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                    ),
                    awaitItem(),
                )

                languageFlow.value = PORTUGUESE_BRAZIL
                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = PORTUGUESE_BRAZIL,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                    ),
                    awaitItem(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun setThemeMode_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { repository.lastBackupExportedAt } returns MutableStateFlow(null)
            every { repository.lastBackupImportedAt } returns MutableStateFlow(null)

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
            val repository = mockk<SettingsRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { repository.lastBackupExportedAt } returns MutableStateFlow(null)
            every { repository.lastBackupImportedAt } returns MutableStateFlow(null)

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
    fun setLanguage_triggersCategoryLocalizationSync() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)
            val languageFlow = MutableStateFlow(PORTUGUESE_BRAZIL)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns PORTUGUESE_BRAZIL
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns languageFlow
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { repository.lastBackupExportedAt } returns MutableStateFlow(null)
            every { repository.lastBackupImportedAt } returns MutableStateFlow(null)

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
    fun exportBackupJson_success_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { repository.lastBackupExportedAt } returns MutableStateFlow(null)
            every { repository.lastBackupImportedAt } returns MutableStateFlow(null)
            io.mockk.coEvery { backupRepository.exportBackupJson(any()) } returns Result.success("{}")

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                    backupRepository,
                )

            viewModel.exportBackupJson("1.3.0")
            advanceUntilIdle()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = EXPORT_BACKUP,
                    entityType = APP,
                    entityId = null,
                    metadata = null,
                    timestamp = any(),
                )
            }
        }

    @Test
    fun importBackupJson_success_logsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)
            val backupRepository = mockk<BackupRepository>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { repository.lastBackupExportedAt } returns MutableStateFlow(null)
            every { repository.lastBackupImportedAt } returns MutableStateFlow(null)
            io.mockk.coEvery { backupRepository.importBackupJson(any()) } returns ImportBackupResult.Success

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
                    metadata = null,
                    timestamp = any(),
                )
            }
        }
}
