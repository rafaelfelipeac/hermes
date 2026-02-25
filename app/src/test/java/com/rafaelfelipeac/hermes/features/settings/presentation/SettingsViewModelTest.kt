package com.rafaelfelipeac.hermes.features.settings.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
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
    fun state_emitsRepositoryValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val themeFlow = MutableStateFlow(ThemeMode.SYSTEM)
            val languageFlow = MutableStateFlow(AppLanguage.SYSTEM)
            val slotModePolicyFlow = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            val repository = mockk<SettingsRepository>()
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)
            val demoDataSeeder = mockk<DemoDataSeeder>(relaxed = true)

            every { repository.themeMode } returns themeFlow
            every { repository.language } returns languageFlow
            every { repository.slotModePolicy } returns slotModePolicyFlow
            every { repository.initialThemeMode() } returns ThemeMode.SYSTEM
            every { repository.initialLanguage() } returns AppLanguage.SYSTEM
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
                )

            viewModel.state.test {
                assertEquals(
                    SettingsState(
                        themeMode = ThemeMode.SYSTEM,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                    ),
                    awaitItem(),
                )

                themeFlow.value = DARK
                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = AppLanguage.SYSTEM,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                    ),
                    awaitItem(),
                )

                languageFlow.value = PORTUGUESE_BRAZIL
                assertEquals(
                    SettingsState(
                        themeMode = DARK,
                        language = PORTUGUESE_BRAZIL,
                        slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
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

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
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

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
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
            val languageFlow = MutableStateFlow(PORTUGUESE_BRAZIL)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns PORTUGUESE_BRAZIL
            every { repository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns languageFlow
            every { repository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)

            val viewModel =
                SettingsViewModel(
                    repository,
                    categorySeeder,
                    userActionLogger,
                    demoDataSeeder,
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
}
