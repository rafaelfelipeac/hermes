package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.UserActionType.CHANGE_LANGUAGE
import com.rafaelfelipeac.hermes.core.useraction.UserActionType.CHANGE_THEME
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
class SettingsViewModel @Inject constructor(
        private val repository: SettingsRepository,
        private val userActionLogger: UserActionLogger,
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
                }
            }

        private companion object {
            const val SETTINGS_STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
