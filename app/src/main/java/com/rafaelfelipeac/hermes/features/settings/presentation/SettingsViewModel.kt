@file:Suppress("ImportOrdering")

package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.debug.DemoDataSeeder
import com.rafaelfelipeac.hermes.core.flow.stateInWhileSubscribed
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_DISTANCE_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_LANGUAGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_PACE_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_SLOT_MODE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_THEME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_WEEK_START
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_WEIGHT_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SEED_DEMO_DATA
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
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
        private val challengeDemoSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val challengeDemoSeedCompletedEvents: SharedFlow<Unit> = challengeDemoSeedEvents.asSharedFlow()
        private val mixedTrophiesSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val mixedTrophiesSeedCompletedEvents: SharedFlow<Unit> = mixedTrophiesSeedEvents.asSharedFlow()
        private val lockedTrophiesSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val lockedTrophiesSeedCompletedEvents: SharedFlow<Unit> = lockedTrophiesSeedEvents.asSharedFlow()
        private val completedTrophiesSeedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val completedTrophiesSeedCompletedEvents: SharedFlow<Unit> = completedTrophiesSeedEvents.asSharedFlow()
        private val databaseClearEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val databaseClearCompletedEvents: SharedFlow<Unit> = databaseClearEvents.asSharedFlow()

        val state: StateFlow<SettingsState> =
            combine(
                repository.themeMode,
                repository.language,
                repository.slotModePolicy,
                repository.weekStartDay,
            ) { themeMode, language, slotModePolicy, weekStartDay ->
                SettingsState(
                    themeMode = themeMode,
                    language = language,
                    slotModePolicy = slotModePolicy,
                    weekStartDay = weekStartDay,
                    distanceUnit = repository.initialDistanceUnit(),
                    paceUnit = repository.initialPaceUnit(),
                    weightUnit = repository.initialWeightUnit(),
                    lastBackupExportedAt = null,
                    lastBackupImportedAt = null,
                    backupFolderUri = null,
                )
            }.let { baseSettings ->
                combine(
                    baseSettings,
                    repository.distanceUnit,
                    repository.paceUnit,
                    repository.weightUnit,
                ) { base, distanceUnit, paceUnit, weightUnit ->
                    base.copy(
                        distanceUnit = distanceUnit,
                        paceUnit = paceUnit,
                        weightUnit = weightUnit,
                    )
                }
            }.let { baseSettings ->
                combine(
                    baseSettings,
                    repository.lastBackupExportedAt,
                    repository.lastBackupImportedAt,
                ) { base, lastBackupExportedAt, lastBackupImportedAt ->
                    base.copy(
                        lastBackupExportedAt = lastBackupExportedAt,
                        lastBackupImportedAt = lastBackupImportedAt,
                    )
                }
            }.let { baseState ->
                combine(baseState, repository.backupFolderUri) { base, backupFolderUri ->
                    base.copy(backupFolderUri = backupFolderUri)
                }
            }.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue =
                    SettingsState(
                        themeMode = repository.initialThemeMode(),
                        language = repository.initialLanguage(),
                        slotModePolicy = repository.initialSlotModePolicy(),
                        weekStartDay = repository.initialWeekStartDay(),
                        distanceUnit = repository.initialDistanceUnit(),
                        paceUnit = repository.initialPaceUnit(),
                        weightUnit = repository.initialWeightUnit(),
                        lastBackupExportedAt = null,
                        lastBackupImportedAt = null,
                        backupFolderUri = null,
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

        fun setWeekStartDay(weekStartDay: WeekStartDay) =
            viewModelScope.launch {
                val previous = state.value.weekStartDay

                repository.setWeekStartDay(weekStartDay)

                if (previous != weekStartDay) {
                    userActionLogger.log(
                        actionType = CHANGE_WEEK_START,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to weekStartDay.name,
                            ),
                    )
                }
            }

        fun setDistanceUnit(distanceUnit: DistanceUnit) =
            viewModelScope.launch {
                val previous = state.value.distanceUnit

                repository.setDistanceUnit(distanceUnit)

                if (previous != distanceUnit) {
                    userActionLogger.log(
                        actionType = CHANGE_DISTANCE_UNIT,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to distanceUnit.name,
                            ),
                    )
                }
            }

        fun setPaceUnit(paceUnit: PaceUnit) =
            viewModelScope.launch {
                val previous = state.value.paceUnit

                repository.setPaceUnit(paceUnit)

                if (previous != paceUnit) {
                    userActionLogger.log(
                        actionType = CHANGE_PACE_UNIT,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to paceUnit.name,
                            ),
                    )
                }
            }

        fun setWeightUnit(weightUnit: WeightUnit) =
            viewModelScope.launch {
                val previous = state.value.weightUnit

                repository.setWeightUnit(weightUnit)

                if (previous != weightUnit) {
                    userActionLogger.log(
                        actionType = CHANGE_WEIGHT_UNIT,
                        entityType = SETTINGS,
                        metadata =
                            mapOf(
                                OLD_VALUE to previous.name,
                                NEW_VALUE to weightUnit.name,
                            ),
                    )
                }
            }

        fun seedDemoData() =
            viewModelScope.launch {
                if (demoDataSeeder.seed()) {
                    logDemoSeedMutation()
                    demoSeedEvents.emit(Unit)
                }
            }

        fun seedChallengeDemoData() =
            viewModelScope.launch {
                if (demoDataSeeder.seedChallenges()) {
                    logDemoSeedMutation()
                    challengeDemoSeedEvents.emit(Unit)
                }
            }

        fun seedCompletedTrophies() =
            viewModelScope.launch {
                if (demoDataSeeder.seedCompletedTrophies()) {
                    logDemoSeedMutation()
                    completedTrophiesSeedEvents.emit(Unit)
                }
            }

        fun seedLockedTrophies() =
            viewModelScope.launch {
                if (demoDataSeeder.seedLockedTrophies()) {
                    logDemoSeedMutation()
                    lockedTrophiesSeedEvents.emit(Unit)
                }
            }

        fun seedMixedTrophies() =
            viewModelScope.launch {
                if (demoDataSeeder.seed()) {
                    logDemoSeedMutation()
                    mixedTrophiesSeedEvents.emit(Unit)
                }
            }

        fun clearDatabase() =
            viewModelScope.launch {
                if (demoDataSeeder.clearDatabase()) {
                    databaseClearEvents.emit(Unit)
                }
            }

        private suspend fun logDemoSeedMutation() {
            userActionLogger.log(
                actionType = SEED_DEMO_DATA,
                entityType = APP,
                metadata = mapOf(RESULT to RESULT_SUCCESS),
            )
        }

        private companion object {
            const val RESULT_SUCCESS = "success"
        }
    }
