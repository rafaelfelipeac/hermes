package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_BODY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_KIND
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.NOTE_KIND_IMPORTANT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyTrainingViewModelImportantNotesTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_enrichesWorkoutWithImportantNotes() =
        runTest(mainDispatcherRule.testDispatcher) {
            val actionsFlow = MutableStateFlow<List<UserActionRecord>>(emptyList())
            val userActionRepository = mockk<UserActionRepository>()
            every { userActionRepository.observeActions() } returns actionsFlow
            val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val repository =
                mockk<WeeklyTrainingRepository>(relaxed = true).also {
                    every { it.observeWorkoutsForWeekStarts(any()) } returns
                        MutableStateFlow(
                            listOf(
                                workout(
                                    id = 1L,
                                    weekStart = weekStart,
                                    day = DayOfWeek.MONDAY,
                                    order = 0,
                                ),
                            ),
                        )
                }
            val categoryRepository = mockk<CategoryRepository>(relaxed = true)
            every { categoryRepository.observeCategories() } returns MutableStateFlow(emptyList())
            val settingsRepository = mockk<SettingsRepository>()
            every { settingsRepository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
            every { settingsRepository.weekStartDay } returns MutableStateFlow(WeekStartDay.MONDAY)
            every { settingsRepository.initialWeekStartDay() } returns WeekStartDay.MONDAY
            every { settingsRepository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE
            val viewModel =
                WeeklyTrainingViewModel(
                    repository = repository,
                    userActionLogger = mockk(relaxed = true),
                    userActionRepository = userActionRepository,
                    categoryRepository = categoryRepository,
                    categorySeeder = mockk<CategorySeeder>(relaxed = true),
                    settingsRepository = settingsRepository,
                )
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }
            val modalCollectJob = backgroundScope.launch { viewModel.importantNotesModalState.collect {} }

            advanceUntilIdle()
            actionsFlow.value =
                listOf(
                    noteAction(
                        id = 10L,
                        actionType = UserActionType.CREATE_NOTE,
                        targetEntityType = UserActionEntityType.WORKOUT,
                        targetEntityId = 1L,
                    ),
                )
            advanceUntilIdle()

            assertEquals(1, viewModel.state.value.workouts.first().importantNotes.size)

            viewModel.showImportantNotesForWorkout(1L)
            advanceUntilIdle()

            assertEquals(1, viewModel.importantNotesModalState.value?.notes?.size)

            collectJob.cancel()
            modalCollectJob.cancel()
        }

    private fun noteAction(
        id: Long,
        actionType: UserActionType,
        targetEntityType: UserActionEntityType,
        targetEntityId: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = UserActionEntityType.NOTE.name,
            entityId = id,
            metadata =
                UserActionMetadataSerializer.toJson(
                    mapOf(
                        NOTE_KIND to NOTE_KIND_IMPORTANT,
                        NOTE_TITLE to "Heel pain",
                        NOTE_BODY to "Reduce pace",
                        NOTE_TRIGGER_ENTITY_TYPE to targetEntityType.name,
                        NOTE_TRIGGER_ENTITY_ID to targetEntityId.toString(),
                    ),
                ),
            timestamp = 1L,
        )
    }
}
