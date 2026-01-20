package com.rafaelfelipeac.hermes.features.settings.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
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
            val repository = mockk<SettingsRepository>()
            every { repository.themeMode } returns themeFlow
            every { repository.language } returns languageFlow
            every { repository.initialThemeMode() } returns ThemeMode.SYSTEM
            every { repository.initialLanguage() } returns AppLanguage.SYSTEM
            val viewModel = SettingsViewModel(repository)

            viewModel.state.test {
                assertEquals(SettingsState(ThemeMode.SYSTEM, AppLanguage.SYSTEM), awaitItem())

                themeFlow.value = ThemeMode.DARK
                assertEquals(SettingsState(ThemeMode.DARK, AppLanguage.SYSTEM), awaitItem())

                languageFlow.value = AppLanguage.PORTUGUESE_BRAZIL
                assertEquals(SettingsState(ThemeMode.DARK, AppLanguage.PORTUGUESE_BRAZIL), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun setThemeMode_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            every { repository.initialThemeMode() } returns ThemeMode.LIGHT
            every { repository.initialLanguage() } returns AppLanguage.ENGLISH
            every { repository.themeMode } returns MutableStateFlow(ThemeMode.LIGHT)
            every { repository.language } returns MutableStateFlow(AppLanguage.ENGLISH)
            val viewModel = SettingsViewModel(repository)

            viewModel.setThemeMode(ThemeMode.DARK)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun setLanguage_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            every { repository.initialThemeMode() } returns ThemeMode.LIGHT
            every { repository.initialLanguage() } returns AppLanguage.ENGLISH
            every { repository.themeMode } returns MutableStateFlow(ThemeMode.LIGHT)
            every { repository.language } returns MutableStateFlow(AppLanguage.ENGLISH)
            val viewModel = SettingsViewModel(repository)

            viewModel.setLanguage(AppLanguage.ENGLISH)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setLanguage(AppLanguage.ENGLISH) }
        }
}
