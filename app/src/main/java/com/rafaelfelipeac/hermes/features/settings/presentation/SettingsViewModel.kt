@file:Suppress("ImportOrdering")

package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_LANGUAGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_SLOT_MODE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_THEME
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
        private val categorySeeder: CategorySeeder,
        private val userActionLogger: UserActionLogger,
        private val demoDataSeeder: DemoDataSeeder,
    ) : ViewModel() {
        private val demoSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val demoSeedCompletedEvents: SharedFlow<Unit> = demoSeedEvents.asSharedFlow()

        val state: StateFlow<SettingsState> =
            combine(
                repository.themeMode,
                repository.language,
                repository.slotModePolicy,
            ) { themeMode, language, slotModePolicy ->
                SettingsState(
                    themeMode = themeMode,
                    language = language,
                    slotModePolicy = slotModePolicy,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SETTINGS_STATE_SHARING_TIMEOUT_MS),
                initialValue =
                    SettingsState(
                        themeMode = repository.initialThemeMode(),
                        language = repository.initialLanguage(),
                        slotModePolicy = repository.initialSlotModePolicy(),
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

        private companion object {
            const val SETTINGS_STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
