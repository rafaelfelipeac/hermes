package com.rafaelfelipeac.hermes.features.trophies.domain

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.BUSY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SICK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_BUSY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_SICK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WEEK_WORKOUTS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.EXPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.IMPORT_BACKUP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrophyEngineTest {
    private val engine = TrophyEngine()

    @Test
    fun fullTimeAndInForm_useDistinctWeeksAndBestStreak() {
        val jan5 = LocalDate.of(2026, 1, 5)
        val jan12 = jan5.plusWeeks(1)
        val jan19 = jan5.plusWeeks(2)
        val feb2 = jan5.plusWeeks(4)

        val progress =
            engine.compute(
                listOf(
                    weekAction(1L, COMPLETE_WEEK_WORKOUTS, jan5, timestamp = 10L),
                    weekAction(2L, COMPLETE_WEEK_WORKOUTS, jan12, timestamp = 20L),
                    weekAction(3L, COMPLETE_WEEK_WORKOUTS, jan19, timestamp = 30L),
                    weekAction(4L, COMPLETE_WEEK_WORKOUTS, feb2, timestamp = 40L),
                    weekAction(5L, COMPLETE_WEEK_WORKOUTS, jan12, timestamp = 50L),
                ),
            )

        assertEquals(4, progress.require(TrophyId.FULL_TIME).currentValue)
        assertEquals(3, progress.require(TrophyId.IN_FORM).currentValue)
        assertNull(progress.require(TrophyId.IN_FORM).unlockedAt)
        assertFalse(progress.require(TrophyId.IN_FORM).isUnlocked)
        assertEquals(40L, progress.require(TrophyId.FULL_TIME).unlockedAt)
        assertFalse(progress.require(TrophyId.SEASON_BUILDER).isUnlocked)
    }

    @Test
    fun gamePlanAndComebackWeek_ignoreUndonePlanningChanges() {
        val weekA = LocalDate.of(2026, 2, 2)
        val weekB = weekA.plusWeeks(1)

        val progress =
            engine.compute(
                listOf(
                    workoutAction(1L, MOVE_WORKOUT_BETWEEN_DAYS, workoutId = 100L, weekStartDate = weekA, timestamp = 10L),
                    workoutAction(2L, UNDO_MOVE_WORKOUT_BETWEEN_DAYS, workoutId = 100L, weekStartDate = weekA, timestamp = 20L),
                    workoutAction(3L, REORDER_WORKOUT, workoutId = 200L, weekStartDate = weekA, timestamp = 30L),
                    weekAction(4L, COMPLETE_WEEK_WORKOUTS, weekA, timestamp = 40L),
                    workoutAction(5L, MOVE_WORKOUT_BETWEEN_DAYS, workoutId = 300L, weekStartDate = weekB, timestamp = 50L),
                    weekAction(6L, COMPLETE_WEEK_WORKOUTS, weekB, timestamp = 60L),
                ),
            )

        assertEquals(2, progress.require(TrophyId.GAME_PLAN).currentValue)
        assertEquals(2, progress.require(TrophyId.COMEBACK_WEEK).currentValue)
        assertNull(progress.require(TrophyId.COMEBACK_WEEK).unlockedAt)
        assertFalse(progress.require(TrophyId.COMEBACK_WEEK).isUnlocked)
    }

    @Test
    fun backInFormationAndHoldTheLine_ignoreUndoneCopies() {
        val weekA = LocalDate.of(2026, 3, 2)
        val weekB = weekA.plusWeeks(1)

        val progress =
            engine.compute(
                listOf(
                    weekAction(1L, COPY_LAST_WEEK, weekA, timestamp = 10L),
                    weekAction(2L, UNDO_COPY_LAST_WEEK, weekA, timestamp = 20L),
                    weekAction(3L, COMPLETE_WEEK_WORKOUTS, weekA, timestamp = 30L),
                    weekAction(4L, COPY_LAST_WEEK, weekB, timestamp = 40L),
                    weekAction(5L, COMPLETE_WEEK_WORKOUTS, weekB, timestamp = 50L),
                ),
            )

        assertEquals(1, progress.require(TrophyId.BACK_IN_FORMATION).currentValue)
        assertEquals(1, progress.require(TrophyId.HOLD_THE_LINE).currentValue)
        assertFalse(progress.require(TrophyId.BACK_IN_FORMATION).isUnlocked)
        assertFalse(progress.require(TrophyId.HOLD_THE_LINE).isUnlocked)
    }

    @Test
    fun matchFitness_ignoresUndoneCompletionActions() {
        val progress =
            engine.compute(
                listOf(
                    workoutAction(1L, COMPLETE_WORKOUT, workoutId = 10L, weekStartDate = null, timestamp = 10L),
                    workoutAction(2L, UNDO_COMPLETE_WORKOUT, workoutId = 10L, weekStartDate = null, timestamp = 20L),
                    workoutAction(3L, COMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 30L),
                ),
            )

        val matchFitness = progress.require(TrophyId.MATCH_FITNESS)

        assertEquals(1, matchFitness.currentValue)
        assertFalse(matchFitness.isUnlocked)
        assertNull(matchFitness.unlockedAt)
    }

    @Test
    fun matchFitness_dropsWhenWorkoutIsMarkedIncomplete() {
        val progress =
            engine.compute(
                listOf(
                    workoutAction(1L, COMPLETE_WORKOUT, workoutId = 10L, weekStartDate = null, timestamp = 10L),
                    workoutAction(2L, COMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 20L),
                    workoutAction(3L, INCOMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 30L),
                ),
            )

        assertEquals(1, progress.require(TrophyId.MATCH_FITNESS).currentValue)
    }

    @Test
    fun matchFitness_restoresWhenIncompleteActionIsUndone() {
        val progress =
            engine.compute(
                listOf(
                    workoutAction(1L, COMPLETE_WORKOUT, workoutId = 10L, weekStartDate = null, timestamp = 10L),
                    workoutAction(2L, COMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 20L),
                    workoutAction(3L, INCOMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 30L),
                    workoutAction(4L, UNDO_INCOMPLETE_WORKOUT, workoutId = 11L, weekStartDate = null, timestamp = 40L),
                ),
            )

        assertEquals(2, progress.require(TrophyId.MATCH_FITNESS).currentValue)
    }

    @Test
    fun builderTrophies_countCategoryActionsAndSuccessfulBackups() {
        val progress =
            engine.compute(
                listOf(
                    categoryAction(1L, CREATE_CATEGORY, timestamp = 10L),
                    categoryAction(2L, REORDER_CATEGORY, timestamp = 20L),
                    categoryAction(3L, DELETE_CATEGORY, timestamp = 30L),
                    settingsAction(4L, EXPORT_BACKUP, result = "success", timestamp = 40L),
                    settingsAction(5L, IMPORT_BACKUP, result = "failure", timestamp = 50L),
                    settingsAction(6L, IMPORT_BACKUP, result = "success", timestamp = 60L),
                ),
            )

        val teamSheet = progress.require(TrophyId.TEAM_SHEET)
        val kitBag = progress.require(TrophyId.KIT_BAG)

        assertEquals(3, teamSheet.currentValue)
        assertNull(teamSheet.unlockedAt)
        assertEquals(2, kitBag.currentValue)
        assertNull(kitBag.unlockedAt)
    }

    @Test
    fun kickoffSeries_countsManualWorkoutCreationButNotCopiedWeeks() {
        val weekStart = LocalDate.of(2026, 4, 6)

        val progress =
            engine.compute(
                listOf(
                    workoutAction(1L, CREATE_WORKOUT, workoutId = 10L, weekStartDate = weekStart, timestamp = 10L),
                    workoutAction(2L, CREATE_WORKOUT, workoutId = 11L, weekStartDate = weekStart, timestamp = 20L),
                    weekAction(3L, COPY_LAST_WEEK, weekStart.plusWeeks(1), timestamp = 30L),
                ),
            )

        assertEquals(2, progress.require(TrophyId.KICKOFF).currentValue)
        assertEquals(2, progress.require(TrophyId.SET_PIECE).currentValue)
        assertFalse(progress.require(TrophyId.KICKOFF).isUnlocked)
    }

    @Test
    fun protectedTime_countsRestAndBusyButNotSick() {
        val weekStart = LocalDate.of(2026, 4, 6)

        val progress =
            engine.compute(
                listOf(
                    nonWorkoutAction(1L, CREATE_REST_DAY, REST_DAY, weekStart, timestamp = 10L),
                    nonWorkoutAction(2L, CREATE_BUSY, BUSY, weekStart, timestamp = 20L),
                    nonWorkoutAction(3L, CREATE_SICK, SICK, weekStart, timestamp = 30L),
                ),
            )

        val protectedTime = progress.require(TrophyId.PROTECTED_TIME)

        assertEquals(2, protectedTime.currentValue)
        assertFalse(protectedTime.isUnlocked)
        assertNull(protectedTime.unlockedAt)
    }

    @Test
    fun categoryTrophies_areBuiltFromVisibleCategoryHistory() {
        val category = TrophyCategoryContext(id = 10L, name = "Strength", colorId = "strength")
        val weekStart = LocalDate.of(2026, 4, 6)

        val progress =
            engine.compute(
                actions =
                    listOf(
                        workoutAction(
                            id = 1L,
                            actionType = COMPLETE_WORKOUT,
                            workoutId = 300L,
                            weekStartDate = weekStart,
                            timestamp = 10L,
                            categoryId = category.id,
                            categoryName = category.name,
                        ),
                        workoutAction(
                            id = 2L,
                            actionType = MOVE_WORKOUT_BETWEEN_DAYS,
                            workoutId = 300L,
                            weekStartDate = weekStart,
                            timestamp = 20L,
                            categoryId = category.id,
                            categoryName = category.name,
                        ),
                        weekAction(3L, COMPLETE_WEEK_WORKOUTS, weekStart, timestamp = 30L),
                    ),
                categories = listOf(category),
            )

        val podiumPlace =
            progress.first { it.definition.id == TrophyId.PODIUM_PLACE && it.categoryId == category.id }
        val inRotation =
            progress.first { it.definition.id == TrophyId.IN_ROTATION && it.categoryId == category.id }
        val homeGround =
            progress.first { it.definition.id == TrophyId.HOME_GROUND && it.categoryId == category.id }
        val trainingBlock =
            progress.first { it.definition.id == TrophyId.TRAINING_BLOCK && it.categoryId == category.id }

        assertEquals(1, podiumPlace.currentValue)
        assertEquals(1, inRotation.currentValue)
        assertEquals(1, homeGround.currentValue)
        assertEquals(1, trainingBlock.currentValue)
        assertFalse(homeGround.isUnlocked)
        assertFalse(podiumPlace.isUnlocked)
    }

    @Test
    fun categoryTrophies_dropWhenWorkoutIsMarkedIncomplete() {
        val category = TrophyCategoryContext(id = 10L, name = "Strength", colorId = "strength")
        val weekStart = LocalDate.of(2026, 4, 6)

        val progress =
            engine.compute(
                actions =
                    listOf(
                        workoutAction(
                            id = 1L,
                            actionType = COMPLETE_WORKOUT,
                            workoutId = 300L,
                            weekStartDate = weekStart,
                            timestamp = 10L,
                            categoryId = category.id,
                            categoryName = category.name,
                        ),
                        workoutAction(
                            id = 2L,
                            actionType = INCOMPLETE_WORKOUT,
                            workoutId = 300L,
                            weekStartDate = weekStart,
                            timestamp = 20L,
                            categoryId = category.id,
                            categoryName = category.name,
                        ),
                        weekAction(3L, COMPLETE_WEEK_WORKOUTS, weekStart, timestamp = 30L),
                    ),
                categories = listOf(category),
            )

        val podiumPlace =
            progress.first { it.definition.id == TrophyId.PODIUM_PLACE && it.categoryId == category.id }
        val homeGround =
            progress.first { it.definition.id == TrophyId.HOME_GROUND && it.categoryId == category.id }

        assertEquals(0, podiumPlace.currentValue)
        assertEquals(0, homeGround.currentValue)
    }

    @Test
    fun repeatedSeriesUnlockIndependentlyFromSharedMetric() {
        val weeks = (0..15).map { LocalDate.of(2026, 1, 5).plusWeeks(it.toLong()) }
        val progress =
            engine.compute(
                weeks.mapIndexed { index, week ->
                    weekAction(
                        id = index.toLong() + 1,
                        actionType = COMPLETE_WEEK_WORKOUTS,
                        weekStartDate = week,
                        timestamp = (index + 1) * 10L,
                    )
                },
            )

        assertTrue(progress.require(TrophyId.FULL_TIME).isUnlocked)
        assertTrue(progress.require(TrophyId.SEASON_BUILDER).isUnlocked)
        assertFalse(progress.require(TrophyId.SEASON_ANCHOR).isUnlocked)
        assertEquals(160L, progress.require(TrophyId.SEASON_BUILDER).unlockedAt)
    }

    private fun List<com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress>.require(
        trophyId: TrophyId,
    ) = first { it.definition.id == trophyId }

    private fun weekAction(
        id: Long,
        actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
        weekStartDate: LocalDate,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = WEEK.name,
            entityId = weekStartDate.toEpochDay(),
            metadata = metadataJson(WEEK_START_DATE to weekStartDate.toString()),
            timestamp = timestamp,
        )
    }

    private fun workoutAction(
        id: Long,
        actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
        workoutId: Long,
        weekStartDate: LocalDate?,
        timestamp: Long,
        categoryId: Long? = null,
        categoryName: String? = null,
    ): UserActionRecord {
        val metadataPairs = linkedMapOf<String, String>()
        weekStartDate?.let { metadataPairs[WEEK_START_DATE] = it.toString() }
        categoryId?.let { metadataPairs["category_id"] = it.toString() }
        categoryName?.let { metadataPairs["category_name"] = it }
        val metadata = metadataPairs.takeIf { it.isNotEmpty() }?.let { metadataJson(*it.toList().toTypedArray()) }

        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = WORKOUT.name,
            entityId = workoutId,
            metadata = metadata,
            timestamp = timestamp,
        )
    }

    private fun categoryAction(
        id: Long,
        actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = CATEGORY.name,
            entityId = id,
            metadata = null,
            timestamp = timestamp,
        )
    }

    private fun settingsAction(
        id: Long,
        actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
        result: String,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = SETTINGS.name,
            entityId = null,
            metadata = metadataJson(RESULT to result),
            timestamp = timestamp,
        )
    }

    private fun nonWorkoutAction(
        id: Long,
        actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
        entityType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType,
        weekStartDate: LocalDate,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = entityType.name,
            entityId = id,
            metadata = metadataJson(WEEK_START_DATE to weekStartDate.toString()),
            timestamp = timestamp,
        )
    }

    private fun metadataJson(vararg pairs: Pair<String, String>): String {
        return UserActionMetadataSerializer.toJson(pairs.toMap())
    }
}
