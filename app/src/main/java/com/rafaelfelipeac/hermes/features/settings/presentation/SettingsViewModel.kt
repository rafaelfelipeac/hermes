package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) : ViewModel() {
        val state: StateFlow<SettingsState> =
            combine(
                repository.themeMode,
                repository.language,
            ) { themeMode, language ->
                SettingsState(
                    themeMode = themeMode,
                    language = language,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SETTINGS_STATE_SHARING_TIMEOUT_MS),
                initialValue =
                    SettingsState(
                        themeMode = repository.initialThemeMode(),
                        language = repository.initialLanguage(),
                    ),
            )

        fun setThemeMode(mode: ThemeMode) =
            viewModelScope.launch {
                repository.setThemeMode(mode)
            }

        fun setLanguage(language: AppLanguage) =
            viewModelScope.launch {
                repository.setLanguage(language)
            }

        private companion object {
            const val SETTINGS_STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
