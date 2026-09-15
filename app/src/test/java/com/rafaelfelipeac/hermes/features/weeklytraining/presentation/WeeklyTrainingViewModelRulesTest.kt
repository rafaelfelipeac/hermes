package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate

class WeeklyTrainingViewModelRulesTest {
    @Test
    fun mapWorkoutsToUi_usesUncategorizedFallbackAndHidesCategoriesForNonWorkouts() {
        val categories = listOf(CategoryUi(UNCATEGORIZED_ID, "Other", "other", 0, false, true))
        val mapped =
            mapWorkoutsToUi(
                workouts =
                    listOf(
                        Workout(
                            id = 1L,
                            weekStartDate = LocalDate.of(2026, 1, 12),
                            dayOfWeek = MONDAY,
                            type = "Run",
                            description = "Easy",
                            isCompleted = false,
                            isRestDay = false,
                            categoryId = null,
                            order = 0,
                        ),
                        Workout(
                            id = 2L,
                            weekStartDate = LocalDate.of(2026, 1, 12),
                            dayOfWeek = MONDAY,
                            type = "Rest",
                            description = "",
                            isCompleted = false,
                            isRestDay = true,
                            categoryId = UNCATEGORIZED_ID,
                            order = 1,
                        ),
                    ),
                categories = categories,
            )

        assertEquals(UNCATEGORIZED_ID, mapped[0].categoryId)
        assertEquals("Other", mapped[0].categoryName)
        assertEquals(null, mapped[1].categoryId)
        assertEquals(null, mapped[1].categoryName)
    }

    @Test
    fun completionActionMappings_distinguishWorkoutsAndRaceEvents() {
        assertEquals(COMPLETE_WORKOUT, EventType.WORKOUT.toCompletionActionType(true))
        assertEquals(INCOMPLETE_RACE_EVENT, RACE_EVENT.toCompletionActionType(false))
        assertEquals(UNDO_COMPLETE_WORKOUT, EventType.WORKOUT.toUndoCompletionActionType(true))
        assertEquals(UNDO_INCOMPLETE_RACE_EVENT, RACE_EVENT.toUndoCompletionActionType(false))
    }
}
