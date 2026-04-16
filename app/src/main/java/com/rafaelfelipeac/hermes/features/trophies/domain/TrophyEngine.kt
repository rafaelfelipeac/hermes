package com.rafaelfelipeac.hermes.features.trophies.domain

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyDefinition
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyMetric
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import java.time.LocalDate
import java.util.ArrayDeque

class TrophyEngine(
    private val definitions: List<TrophyDefinition> = TrophyDefinitions.supportedV1,
    private val categoryTemplates: List<TrophyDefinition> = TrophyDefinitions.categoryTemplates,
) {
    fun compute(
        actions: List<UserActionRecord>,
        categories: List<TrophyCategoryContext> = emptyList(),
    ): List<TrophyProgress> {
        val history = TrophyHistory.from(actions, categories)
        val baseProgress =
            definitions.map { definition ->
                progressForDefinition(
                    definition = definition,
                    history = history,
                )
            }
        val categoryProgress =
            categories.flatMap { category ->
                categoryTemplates.map { definition ->
                    progressForDefinition(
                        definition = definition,
                        history = history,
                        categoryId = category.id,
                        categoryName = category.name,
                        categoryColorId = category.colorId,
                    )
                }
            }

        return baseProgress + categoryProgress
    }

    private fun progressForDefinition(
        definition: TrophyDefinition,
        history: TrophyHistory,
        categoryId: Long? = null,
        categoryName: String? = null,
        categoryColorId: String? = null,
    ): TrophyProgress {
        val milestones = history.milestonesFor(definition.metric, categoryId)
        return TrophyProgress(
            definition = definition,
            currentValue = milestones.size,
            unlockedAt = milestones.getOrNull(definition.target - 1),
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorId = categoryColorId,
        )
    }

    private data class ParsedAction(
        val record: UserActionRecord,
        val actionType: UserActionType?,
        val metadata: Map<String, String>,
        val weekStartDate: LocalDate?,
        val categoryId: Long?,
        val categoryNames: Set<String>,
    )

    private data class WeekCompletion(
        val weekStartDate: LocalDate,
        val completedAt: Long,
    )

    private data class WeekEvent(
        val weekStartDate: LocalDate,
        val timestamp: Long,
    )

    private data class TrophyHistory(
        val completedWeekMilestones: List<Long>,
        val matchFitnessMilestones: List<Long>,
        val longestStreakMilestones: List<Long>,
        val comebackWeekMilestones: List<Long>,
        val gamePlanMilestones: List<Long>,
        val backInFormationMilestones: List<Long>,
        val holdTheLineMilestones: List<Long>,
        val teamSheetMilestones: List<Long>,
        val kitBagMilestones: List<Long>,
        val kickoffMilestones: List<Long>,
        val protectedTimeMilestones: List<Long>,
        val podiumPlaceMilestonesByCategory: Map<Long, List<Long>>,
        val homeGroundMilestonesByCategory: Map<Long, List<Long>>,
        val trainingBlockMilestonesByCategory: Map<Long, List<Long>>,
    ) {
        fun milestonesFor(
            metric: TrophyMetric,
            categoryId: Long?,
        ): List<Long> {
            return when (metric) {
                TrophyMetric.COMPLETED_WEEKS -> completedWeekMilestones
                TrophyMetric.WORKOUT_COMPLETIONS -> matchFitnessMilestones
                TrophyMetric.CONSECUTIVE_COMPLETED_WEEKS -> longestStreakMilestones
                TrophyMetric.COMEBACK_WEEKS -> comebackWeekMilestones
                TrophyMetric.PLANNING_ADJUSTMENTS -> gamePlanMilestones
                TrophyMetric.COPIED_WEEKS -> backInFormationMilestones
                TrophyMetric.COPIED_AND_COMPLETED_WEEKS -> holdTheLineMilestones
                TrophyMetric.CATEGORY_ACTIONS -> teamSheetMilestones
                TrophyMetric.BACKUP_SUCCESSES -> kitBagMilestones
                TrophyMetric.WORKOUT_CREATIONS -> kickoffMilestones
                TrophyMetric.PROTECTED_TIME_BLOCKS -> protectedTimeMilestones
                TrophyMetric.CATEGORY_COMPLETIONS -> categoryId?.let { podiumPlaceMilestonesByCategory[it] }.orEmpty()
                TrophyMetric.CATEGORY_PRESENCE_WEEKS -> categoryId?.let { homeGroundMilestonesByCategory[it] }.orEmpty()
                TrophyMetric.CATEGORY_PLANNING_ACTIONS -> categoryId?.let { trainingBlockMilestonesByCategory[it] }.orEmpty()
            }
        }

        companion object {
            private const val RESULT_SUCCESS = "success"

            fun from(
                actions: List<UserActionRecord>,
                categories: List<TrophyCategoryContext>,
            ): TrophyHistory {
                val categoryAliasesById = buildCategoryAliasesById(actions, categories)
                val parsedActions =
                    actions
                        .map { action ->
                            val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
                            ParsedAction(
                                record = action,
                                actionType = action.actionType.toUserActionTypeOrNull(),
                                metadata = metadata,
                                weekStartDate = metadata[WEEK_START_DATE]?.toLocalDateOrNull(),
                                categoryId = metadata[CATEGORY_ID]?.toLongOrNull(),
                                categoryNames = setOfNotNull(
                                    metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                    metadata[OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                    metadata[NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                ),
                            )
                        }.sortedWith(compareBy<ParsedAction>({ it.record.timestamp }, { it.record.id }))

                val completedWeeks = linkedMapOf<LocalDate, Long>()
                val effectiveCompletionTimestamps = mutableListOf<Long>()
                val effectivePlanningEvents = mutableListOf<WeekEvent>()
                val effectiveCopyEvents = mutableListOf<WeekEvent>()
                val categoryActionTimestamps = mutableListOf<Long>()
                val backupSuccessTimestamps = mutableListOf<Long>()
                val workoutCreationTimestamps = mutableListOf<Long>()
                val protectedTimeTimestamps = mutableListOf<Long>()
                val completionStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<Long>>()
                val moveStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<WeekEvent>>()
                val reorderStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<WeekEvent>>()
                val copyStacksByWeek = mutableMapOf<LocalDate, ArrayDeque<Long>>()
                val categoryCompletionMilestones = mutableMapOf<Long, MutableList<Long>>()
                val categoryCompletionWeekCounts = mutableMapOf<Long, MutableMap<LocalDate, Int>>()
                val categoryTrainingBlockMilestones = mutableMapOf<Long, MutableList<Long>>()

                parsedActions.forEach { action ->
                    val categoryIds = resolveCategoryIds(action, categoryAliasesById)
                    when (action.actionType) {
                        UserActionType.COMPLETE_WEEK_WORKOUTS -> {
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            completedWeeks.putIfAbsent(weekStartDate, action.record.timestamp)
                        }

                        UserActionType.COMPLETE_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            completionStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(action.record.timestamp)
                            categoryIds.forEach { categoryId ->
                                categoryCompletionMilestones
                                    .getOrPut(categoryId, ::mutableListOf)
                                    .add(action.record.timestamp)
                                action.weekStartDate?.let { weekStartDate ->
                                    val weekCounts =
                                        categoryCompletionWeekCounts.getOrPut(categoryId, ::linkedMapOf)
                                    weekCounts[weekStartDate] = (weekCounts[weekStartDate] ?: 0) + 1
                                }
                            }
                        }

                        UserActionType.INCOMPLETE_WORKOUT,
                        UserActionType.UNDO_COMPLETE_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            completionStacksByWorkoutId[workoutId].removeLastIfPresent()
                            categoryIds.forEach { categoryId ->
                                categoryCompletionMilestones[categoryId].removeLastIfPresent()
                                action.weekStartDate?.let { weekStartDate ->
                                    categoryCompletionWeekCounts[categoryId].decrementWeekCount(weekStartDate)
                                }
                            }
                        }

                        UserActionType.UNDO_INCOMPLETE_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            completionStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(action.record.timestamp)
                            categoryIds.forEach { categoryId ->
                                categoryCompletionMilestones
                                    .getOrPut(categoryId, ::mutableListOf)
                                    .add(action.record.timestamp)
                                action.weekStartDate?.let { weekStartDate ->
                                    val weekCounts =
                                        categoryCompletionWeekCounts.getOrPut(categoryId, ::linkedMapOf)
                                    weekCounts[weekStartDate] = (weekCounts[weekStartDate] ?: 0) + 1
                                }
                            }
                        }

                        UserActionType.MOVE_WORKOUT_BETWEEN_DAYS -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            moveStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(WeekEvent(weekStartDate = weekStartDate, timestamp = action.record.timestamp))
                            categoryIds.forEach { categoryId ->
                                categoryTrainingBlockMilestones
                                    .getOrPut(categoryId, ::mutableListOf)
                                    .add(action.record.timestamp)
                            }
                        }

                        UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            moveStacksByWorkoutId[workoutId].removeLastIfPresent()
                            categoryIds.forEach { categoryId ->
                                categoryTrainingBlockMilestones[categoryId].removeLastIfPresent()
                            }
                        }

                        UserActionType.REORDER_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            reorderStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(WeekEvent(weekStartDate = weekStartDate, timestamp = action.record.timestamp))
                            categoryIds.forEach { categoryId ->
                                categoryTrainingBlockMilestones
                                    .getOrPut(categoryId, ::mutableListOf)
                                    .add(action.record.timestamp)
                            }
                        }

                        UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            reorderStacksByWorkoutId[workoutId].removeLastIfPresent()
                            categoryIds.forEach { categoryId ->
                                categoryTrainingBlockMilestones[categoryId].removeLastIfPresent()
                            }
                        }

                        UserActionType.COPY_LAST_WEEK -> {
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            copyStacksByWeek.getOrPut(weekStartDate, ::ArrayDeque).addLast(action.record.timestamp)
                        }

                        UserActionType.UNDO_COPY_LAST_WEEK -> {
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            copyStacksByWeek[weekStartDate].removeLastIfPresent()
                        }

                        UserActionType.CREATE_CATEGORY,
                        UserActionType.UPDATE_CATEGORY_NAME,
                        UserActionType.UPDATE_CATEGORY_COLOR,
                        UserActionType.UPDATE_CATEGORY_VISIBILITY,
                        UserActionType.REORDER_CATEGORY,
                        UserActionType.DELETE_CATEGORY,
                        UserActionType.RESTORE_DEFAULT_CATEGORIES,
                        -> categoryActionTimestamps += action.record.timestamp

                        UserActionType.EXPORT_BACKUP,
                        UserActionType.IMPORT_BACKUP,
                        -> {
                            if (action.metadata[RESULT] == RESULT_SUCCESS) {
                                backupSuccessTimestamps += action.record.timestamp
                            }
                        }

                        UserActionType.CREATE_WORKOUT -> workoutCreationTimestamps += action.record.timestamp

                        UserActionType.CREATE_REST_DAY,
                        UserActionType.CREATE_BUSY,
                        -> protectedTimeTimestamps += action.record.timestamp

                        else -> Unit
                    }

                    if (action.actionType in categoryTrainingBlockActions) {
                        categoryIds.forEach { categoryId ->
                            categoryTrainingBlockMilestones
                                .getOrPut(categoryId, ::mutableListOf)
                                .add(action.record.timestamp)
                        }
                    }
                }

                completionStacksByWorkoutId.values.forEach { stack ->
                    effectiveCompletionTimestamps += stack.toList()
                }
                moveStacksByWorkoutId.values.forEach { stack ->
                    effectivePlanningEvents += stack.toList()
                }
                reorderStacksByWorkoutId.values.forEach { stack ->
                    effectivePlanningEvents += stack.toList()
                }
                copyStacksByWeek.forEach { (weekStartDate, stack) ->
                    stack.forEach { timestamp ->
                        effectiveCopyEvents += WeekEvent(weekStartDate = weekStartDate, timestamp = timestamp)
                    }
                }

                val completedWeekEvents =
                    completedWeeks.entries
                        .map { (weekStartDate, completedAt) ->
                            WeekCompletion(weekStartDate = weekStartDate, completedAt = completedAt)
                        }.sortedBy { it.completedAt }
                val effectivePlanningByWeek =
                    effectivePlanningEvents.groupBy(WeekEvent::weekStartDate)
                        .mapValues { (_, events) -> events.sortedBy(WeekEvent::timestamp) }
                val effectiveCopiesByWeek =
                    effectiveCopyEvents.groupBy(WeekEvent::weekStartDate)
                        .mapValues { (_, events) -> events.sortedBy(WeekEvent::timestamp) }
                val comebackWeekMilestones =
                    completedWeekEvents
                        .filter { completion ->
                            effectivePlanningByWeek[completion.weekStartDate]
                                .orEmpty()
                                .any { event -> event.timestamp < completion.completedAt }
                        }.map(WeekCompletion::completedAt)
                val holdTheLineMilestones =
                    completedWeekEvents
                        .filter { completion ->
                            effectiveCopiesByWeek[completion.weekStartDate]
                                .orEmpty()
                                .any { event -> event.timestamp < completion.completedAt }
                        }.map(WeekCompletion::completedAt)
                val homeGroundMilestonesByCategory =
                    categoryCompletionWeekCounts.mapValues { (_, weeks) ->
                        completedWeekEvents
                            .filter { completion -> (weeks[completion.weekStartDate] ?: 0) > 0 }
                            .map(WeekCompletion::completedAt)
                    }

                return TrophyHistory(
                    completedWeekMilestones = completedWeekEvents.map(WeekCompletion::completedAt),
                    matchFitnessMilestones = effectiveCompletionTimestamps.sorted(),
                    longestStreakMilestones = buildLongestStreakMilestones(completedWeeks),
                    comebackWeekMilestones = comebackWeekMilestones,
                    gamePlanMilestones = effectivePlanningEvents.map(WeekEvent::timestamp).sorted(),
                    backInFormationMilestones = effectiveCopyEvents.map(WeekEvent::timestamp).sorted(),
                    holdTheLineMilestones = holdTheLineMilestones,
                    teamSheetMilestones = categoryActionTimestamps.sorted(),
                    kitBagMilestones = backupSuccessTimestamps.sorted(),
                    kickoffMilestones = workoutCreationTimestamps.sorted(),
                    protectedTimeMilestones = protectedTimeTimestamps.sorted(),
                    podiumPlaceMilestonesByCategory =
                        categoryCompletionMilestones.mapValues { (_, milestones) -> milestones.sorted() },
                    homeGroundMilestonesByCategory = homeGroundMilestonesByCategory,
                    trainingBlockMilestonesByCategory =
                        categoryTrainingBlockMilestones.mapValues { (_, milestones) -> milestones.sorted() },
                )
            }

            private fun buildLongestStreakMilestones(completedWeeks: Map<LocalDate, Long>): List<Long> {
                val sortedWeeks =
                    completedWeeks.entries
                        .sortedBy { it.key }
                        .map { WeekCompletion(weekStartDate = it.key, completedAt = it.value) }
                val milestoneTimestamps = mutableListOf<Long>()
                var currentStreak = 0
                var previousWeekStartDate: LocalDate? = null

                sortedWeeks.forEach { completion ->
                    currentStreak =
                        if (previousWeekStartDate?.plusWeeks(1) == completion.weekStartDate) {
                            currentStreak + 1
                        } else {
                            1
                        }

                    while (milestoneTimestamps.size < currentStreak) {
                        milestoneTimestamps += completion.completedAt
                    }

                    previousWeekStartDate = completion.weekStartDate
                }

                return milestoneTimestamps
            }

            private fun String.toUserActionTypeOrNull(): UserActionType? {
                return runCatching { UserActionType.valueOf(this) }.getOrNull()
            }

            private fun String.toLocalDateOrNull(): LocalDate? {
                return runCatching { LocalDate.parse(this) }.getOrNull()
            }

            private fun <T> ArrayDeque<T>?.removeLastIfPresent() {
                if (this != null && isNotEmpty()) {
                    removeLast()
                }
            }

            private fun <T> MutableList<T>?.removeLastIfPresent() {
                if (this != null && isNotEmpty()) {
                    removeAt(lastIndex)
                }
            }

            private fun MutableMap<LocalDate, Int>?.decrementWeekCount(weekStartDate: LocalDate) {
                if (this == null) return
                val nextCount = (this[weekStartDate] ?: 0) - 1
                if (nextCount > 0) {
                    this[weekStartDate] = nextCount
                } else {
                    remove(weekStartDate)
                }
            }

            private fun resolveCategoryIds(
                action: ParsedAction,
                aliasesById: Map<Long, Set<String>>,
            ): Set<Long> {
                val resolvedById = action.categoryId?.let(::setOf).orEmpty()
                if (resolvedById.isNotEmpty()) return resolvedById

                return aliasesById.entries
                    .filter { (_, aliases) -> aliases.any { it in action.categoryNames } }
                    .mapTo(linkedSetOf()) { (id, _) -> id }
            }

            private fun buildCategoryAliasesById(
                actions: List<UserActionRecord>,
                categories: List<TrophyCategoryContext>,
            ): Map<Long, Set<String>> {
                val aliases =
                    categories.associate { category ->
                        category.id to mutableSetOf(category.name)
                    }.toMutableMap()

                actions.forEach { action ->
                    val categoryId = action.entityId ?: return@forEach
                    val actionType = action.actionType.toUserActionTypeOrNull() ?: return@forEach
                    if (actionType != UserActionType.UPDATE_CATEGORY_NAME) return@forEach

                    val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
                    val names = aliases.getOrPut(categoryId) { mutableSetOf() }
                    metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() }?.let(names::add)
                    metadata[OLD_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
                    metadata[NEW_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
                }

                return aliases.mapValues { (_, names) -> names.toSet() }
            }

            private val categoryTrainingBlockActions =
                setOf(
                    UserActionType.CREATE_WORKOUT,
                    UserActionType.UPDATE_WORKOUT,
                    UserActionType.UNDO_DELETE_WORKOUT,
                    UserActionType.CONVERT_REST_DAY_TO_WORKOUT,
                )
        }
    }
}
