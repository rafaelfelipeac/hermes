package com.rafaelfelipeac.hermes.features.settings.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.useraction.UserActionLogger
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
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
            val repository = mockk<SettingsRepository>()
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.themeMode } returns themeFlow
            every { repository.language } returns languageFlow
            every { repository.initialThemeMode() } returns ThemeMode.SYSTEM
            every { repository.initialLanguage() } returns AppLanguage.SYSTEM

            val viewModel = SettingsViewModel(repository, userActionLogger)

            viewModel.state.test {
                assertEquals(SettingsState(ThemeMode.SYSTEM, AppLanguage.SYSTEM), awaitItem())

                themeFlow.value = DARK
                assertEquals(SettingsState(DARK, AppLanguage.SYSTEM), awaitItem())

                languageFlow.value = PORTUGUESE_BRAZIL
                assertEquals(SettingsState(DARK, PORTUGUESE_BRAZIL), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun setThemeMode_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)

            val viewModel = SettingsViewModel(repository, userActionLogger)

            viewModel.setThemeMode(DARK)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setThemeMode(DARK) }
        }

    @Test
    fun setLanguage_delegatesToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<SettingsRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.initialThemeMode() } returns LIGHT
            every { repository.initialLanguage() } returns ENGLISH
            every { repository.themeMode } returns MutableStateFlow(LIGHT)
            every { repository.language } returns MutableStateFlow(ENGLISH)

            val viewModel = SettingsViewModel(repository, userActionLogger)

            viewModel.setLanguage(ENGLISH)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.setLanguage(ENGLISH) }
        }
}
