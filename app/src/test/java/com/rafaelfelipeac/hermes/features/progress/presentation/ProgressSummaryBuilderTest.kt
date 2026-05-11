package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_UNCATEGORIZED
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyDefinitions
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyFamilyUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ProgressSummaryBuilderTest {
    private val today = LocalDate.of(2026, 5, 5)
    private val currentWeek = LocalDate.of(2026, 5, 4)
    private val categories =
        listOf(
            CategoryUi(2L, "Run", COLOR_RUN, 0, isHidden = false, isSystem = true),
            CategoryUi(3L, "Cycling", COLOR_CYCLING, 1, isHidden = false, isSystem = true),
            CategoryUi(UNCATEGORIZED_ID, "Uncategorized", COLOR_UNCATEGORIZED, 99, isHidden = false, isSystem = true),
        )

    @Test
    fun buildProgressState_summarizesCurrentWeekAndTrend() {
        val workouts =
            week(currentWeek.minusWeeks(1), completed = 6, pending = 2, categoryId = 2L) +
                week(currentWeek, completed = 5, pending = 2, categoryId = 3L)

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(8, state.weeklyTrend.size)
        assertEquals(currentWeek, state.weeklyTrend.last().weekStartDate)
        assertEquals(5, state.weeklyTrend.last().completedWorkouts)
        assertEquals(7, state.weeklyTrend.last().plannedWorkouts)
        assertEquals(71, state.weeklyReadout.completionPercent)
        assertEquals(5, state.weeklyReadout.completedWorkouts)
        assertEquals(7, state.weeklyReadout.plannedWorkouts)
    }

    @Test
    fun buildProgressState_excludesRaceEventsFromWorkoutCategoryDistribution() {
        val workouts =
            listOf(
                workout(1L, currentWeek, DayOfWeek.MONDAY, EventType.WORKOUT, isCompleted = true, categoryId = 2L),
                workout(2L, currentWeek, DayOfWeek.TUESDAY, RACE_EVENT, isCompleted = true, categoryId = 3L),
                workout(3L, currentWeek, DayOfWeek.WEDNESDAY, EventType.REST, isCompleted = false, categoryId = null),
            )

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(1, state.categoryDistribution.size)
        assertEquals("Run", state.categoryDistribution.first().name)
        assertEquals(1, state.categoryDistribution.first().count)
    }

    @Test
    fun buildProgressState_hidesUnavailableOptionalSections() {
        val state =
            buildProgressState(
                workouts = emptyList(),
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(ProgressEmptyReason.NO_WEEKLY_HISTORY, state.emptyReason)
        assertNull(state.trophyHighlight)
        assertNull(state.upcomingEvent)
        assertTrue(state.recentActivities.isEmpty())
        assertEquals(0, state.weeklyReadout.plannedWorkouts)
        assertEquals(0, state.weeklyReadout.completedWorkouts)
    }

    @Test
    fun buildProgressState_selectsNearestUpcomingRaceEvent() {
        val workouts =
            listOf(
                workout(1L, currentWeek.plusWeeks(2), DayOfWeek.SUNDAY, RACE_EVENT, false, categoryId = 2L),
                workout(2L, currentWeek.plusWeeks(1), DayOfWeek.SATURDAY, RACE_EVENT, false, categoryId = 3L),
            )

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(2L, state.upcomingEvent?.id)
        assertEquals(11, state.upcomingEvent?.daysUntil)
    }

    @Test
    fun buildProgressState_selectsRecentTrophyUnlock() {
        val trophyCards =
            listOf(
                trophyCard(TrophyId.FULL_TIME, currentValue = 1, unlockedAt = 10L, isUnlocked = true),
                trophyCard(TrophyId.MATCH_FITNESS, currentValue = 9, unlockedAt = 40L, isUnlocked = true),
            )

        val state =
            buildProgressState(
                workouts = emptyList(),
                categories = categories,
                trophyCards = trophyCards,
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertNotNull(state.trophyHighlight)
        assertEquals(TrophyId.MATCH_FITNESS, state.trophyHighlight?.trophy?.trophyId)
    }

    @Test
    fun buildProgressState_limitsRecentActivities() {
        val activities =
            (1L..7L).map { id ->
                ActivityItemUi(
                    id = id,
                    title = "Action $id",
                    subtitle = null,
                    time = "10:00",
                )
            }

        val state =
            buildProgressState(
                workouts = emptyList(),
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = activities,
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(5, state.recentActivities.size)
        assertEquals(1L, state.recentActivities.first().id)
    }

    @Test
    fun buildProgressState_buildsWeeklyReadoutWithNextIncompleteWorkout() {
        val workouts =
            listOf(
                workout(1L, currentWeek, DayOfWeek.MONDAY, EventType.WORKOUT, isCompleted = true, categoryId = 2L),
                workout(2L, currentWeek, DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 3L),
                workout(3L, currentWeek, DayOfWeek.FRIDAY, EventType.WORKOUT, isCompleted = false, categoryId = 2L),
            )

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(3, state.weeklyReadout.plannedWorkouts)
        assertEquals(1, state.weeklyReadout.completedWorkouts)
        assertEquals(33, state.weeklyReadout.completionPercent)
        assertEquals(2L, state.weeklyReadout.nextFocus?.id)
        assertEquals("Workout 2", state.weeklyReadout.nextFocus?.title)
        assertEquals(1, state.weeklyReadout.nextFocus?.daysUntil)
    }

    @Test
    fun buildProgressState_selectsNextFocusByDisplayWeekDateWindow() {
        val displayWeekStart = LocalDate.of(2026, 5, 3)
        val storedWeekStart = LocalDate.of(2026, 5, 4)

        val state =
            buildProgressState(
                workouts =
                    listOf(
                        workout(
                            id = 8L,
                            weekStart = displayWeekStart,
                            dayOfWeek = DayOfWeek.SUNDAY,
                            eventType = EventType.WORKOUT,
                            isCompleted = true,
                            categoryId = 2L,
                        ),
                        workout(
                            id = 9L,
                            weekStart = storedWeekStart,
                            dayOfWeek = DayOfWeek.MONDAY,
                            eventType = EventType.WORKOUT,
                            isCompleted = false,
                            categoryId = 2L,
                        ),
                    ),
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = displayWeekStart,
                currentWeekStart = displayWeekStart,
            )

        assertEquals(2, state.weeklyReadout.plannedWorkouts)
        assertEquals(1, state.weeklyReadout.completedWorkouts)
        assertEquals(50, state.weeklyReadout.completionPercent)
        assertEquals(9L, state.weeklyReadout.nextFocus?.id)
        assertEquals(LocalDate.of(2026, 5, 4), state.weeklyReadout.nextFocus?.date)
        assertEquals(1, state.weeklyReadout.nextFocus?.daysUntil)
    }

    @Test
    fun buildProgressState_hidesNextFocusWhenCurrentWeekHasNoIncompleteWorkout() {
        val workouts =
            listOf(
                workout(1L, currentWeek, DayOfWeek.MONDAY, EventType.WORKOUT, isCompleted = true, categoryId = 2L),
                workout(2L, currentWeek.plusWeeks(1), DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 3L),
            )

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertNull(state.weeklyReadout.nextFocus)
    }

    @Test
    fun buildProgressState_keepsUpcomingEventSeparateFromNextFocus() {
        val workouts =
            listOf(
                workout(1L, currentWeek, DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 2L),
                workout(2L, currentWeek.plusWeeks(1), DayOfWeek.SATURDAY, RACE_EVENT, isCompleted = false, categoryId = 3L),
            )

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(1L, state.weeklyReadout.nextFocus?.id)
        assertEquals(2L, state.upcomingEvent?.id)
        assertEquals(11, state.upcomingEvent?.daysUntil)
    }

    @Test
    fun buildProgressState_buildsTrendAndTrainingMixInsights() {
        val workouts =
            week(currentWeek.minusWeeks(2), completed = 3, pending = 0, categoryId = 2L) +
                week(currentWeek.minusWeeks(1), completed = 3, pending = 0, categoryId = 3L) +
                week(currentWeek, completed = 3, pending = 1, categoryId = null)

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(ProgressWeeklyTrendInsightKind.CURRENT_WEEK_HEAVIEST, state.weeklyTrendInsight?.kind)
        assertEquals(ProgressTrainingMixInsightKind.BALANCED, state.trainingMixInsight?.kind)
    }

    @Test
    fun buildProgressState_buildsDominantTrainingMixInsightFromFullDistribution() {
        val extraCategories =
            listOf(
                CategoryUi(4L, "Mobility", COLOR_RUN, 2, isHidden = false, isSystem = false),
                CategoryUi(5L, "Strength", COLOR_CYCLING, 3, isHidden = false, isSystem = false),
            )
        val workouts =
            week(currentWeek, completed = 1, pending = 0, categoryId = 2L) +
                week(currentWeek.minusWeeks(1), completed = 1, pending = 0, categoryId = 3L) +
                week(currentWeek.minusWeeks(2), completed = 1, pending = 0, categoryId = 4L) +
                week(currentWeek.minusWeeks(3), completed = 1, pending = 0, categoryId = 5L) +
                week(currentWeek.minusWeeks(4), completed = 5, pending = 0, categoryId = null)

        val state =
            buildProgressState(
                workouts = workouts,
                categories = categories + extraCategories,
                trophyCards = emptyList(),
                recentActivities = emptyList(),
                today = today,
                currentWeekStart = currentWeek,
            )

        assertEquals(4, state.categoryDistribution.size)
        assertFalse(state.categoryDistribution.any { it.id == UNCATEGORIZED_ID })
        assertEquals(ProgressTrainingMixInsightKind.DOMINANT_CATEGORY, state.trainingMixInsight?.kind)
        assertEquals("Uncategorized", state.trainingMixInsight?.categoryName)
    }

    private fun week(
        weekStart: LocalDate,
        completed: Int,
        pending: Int,
        categoryId: Long?,
    ): List<Workout> {
        val completedItems =
            (0 until completed).map { index ->
                workout(
                    id = weekStart.toEpochDay() + index,
                    weekStart = weekStart,
                    dayOfWeek = DayOfWeek.entries[index % DayOfWeek.entries.size],
                    eventType = EventType.WORKOUT,
                    isCompleted = true,
                    categoryId = categoryId,
                )
            }
        val pendingItems =
            (0 until pending).map { index ->
                workout(
                    id = weekStart.toEpochDay() + completed + index,
                    weekStart = weekStart,
                    dayOfWeek = DayOfWeek.entries[(completed + index) % DayOfWeek.entries.size],
                    eventType = EventType.WORKOUT,
                    isCompleted = false,
                    categoryId = categoryId,
                )
            }
        return completedItems + pendingItems
    }

    private fun workout(
        id: Long,
        weekStart: LocalDate,
        dayOfWeek: DayOfWeek,
        eventType: EventType,
        isCompleted: Boolean,
        categoryId: Long?,
    ): Workout {
        return Workout(
            id = id,
            weekStartDate = weekStart,
            dayOfWeek = dayOfWeek,
            type = "Workout $id",
            description = "",
            isCompleted = isCompleted,
            isRestDay = eventType == EventType.REST,
            eventType = eventType,
            categoryId = categoryId,
            order = id.toInt(),
        )
    }

    private fun trophyCard(
        trophyId: TrophyId,
        currentValue: Int,
        unlockedAt: Long?,
        isUnlocked: Boolean,
    ): TrophyCardUi {
        val progress =
            TrophyProgress(
                definition = (TrophyDefinitions.supportedV1 + TrophyDefinitions.categoryTemplates).first { it.id == trophyId },
                currentValue = currentValue,
                unlockedAt = unlockedAt,
                categoryId = null,
                categoryName = null,
                categoryColorId = null,
            )
        return TrophyCardUi(
            stableId = trophyId.name,
            trophyId = trophyId,
            family = TrophyFamilyUi.FOLLOW_THROUGH,
            sortOrder = 0,
            badgeRank = 1,
            categoryId = null,
            categoryName = null,
            categoryColorId = null,
            currentValue = progress.currentValue,
            target = progress.definition.target,
            isUnlocked = isUnlocked,
            unlockedAt = progress.unlockedAt,
        )
    }
}
