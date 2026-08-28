@file:Suppress(
    "ArgumentListWrapping",
    "LargeClass",
    "LongMethod",
    "MaxLineLength",
    "CyclomaticComplexMethod",
    "NestedBlockDepth",
    "PropertyWrapping",
    "ReturnCount",
)

package com.rafaelfelipeac.hermes.features.trophies.domain

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ARCHIVED_AT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_END_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_LIFECYCLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.ARCHIVE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REACTIVATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.RESTORE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_DELETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.features.challenges.domain.ChallengeCalculator
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyDefinition
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyMetric
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import java.time.Instant
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
        val challengeId: Long?,
        val challengeProgressEntryId: Long?,
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

    private data class WorkoutCompletion(
        val timestamp: Long,
        val weekStartDate: LocalDate?,
    )

    private data class DeletedRaceEventState(
        val creationTimestamp: Long?,
        val completionTimestamp: Long?,
    )

    private data class ChallengeMilestone(
        val challengeId: Long,
        val timestamp: Long,
    )

    private data class DeletedChallengeState(
        val challenge: Challenge,
        val entries: List<ChallengeProgressEntry>,
    )

    private data class TrophyHistory(
        val challengeCreationMilestones: List<Long>,
        val challengeCompletionMilestones: List<Long>,
        val challengeRecoveryMilestones: List<Long>,
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
        val personalRecordFamilyCreationMilestones: List<Long>,
        val personalRecordEntryCreationMilestones: List<Long>,
        val paceCalculationMilestones: List<Long>,
        val protectedTimeMilestones: List<Long>,
        val raceEventCreationMilestones: List<Long>,
        val raceEventCompletionMilestones: List<Long>,
        val podiumPlaceMilestonesByCategory: Map<Long, List<Long>>,
        val homeGroundMilestonesByCategory: Map<Long, List<Long>>,
        val trainingBlockMilestonesByCategory: Map<Long, List<Long>>,
    ) {
        fun milestonesFor(
            metric: TrophyMetric,
            categoryId: Long?,
        ): List<Long> {
            return when (metric) {
                TrophyMetric.CHALLENGE_CREATIONS -> challengeCreationMilestones
                TrophyMetric.CHALLENGE_COMPLETIONS -> challengeCompletionMilestones
                TrophyMetric.CHALLENGE_RECOVERIES -> challengeRecoveryMilestones
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
                TrophyMetric.PERSONAL_RECORD_FAMILY_CREATIONS -> personalRecordFamilyCreationMilestones
                TrophyMetric.PERSONAL_RECORD_ENTRY_CREATIONS -> personalRecordEntryCreationMilestones
                TrophyMetric.PACE_CALCULATIONS -> paceCalculationMilestones
                TrophyMetric.PROTECTED_TIME_BLOCKS -> protectedTimeMilestones
                TrophyMetric.RACE_EVENT_CREATIONS -> raceEventCreationMilestones
                TrophyMetric.RACE_EVENT_COMPLETIONS -> raceEventCompletionMilestones
                TrophyMetric.CATEGORY_COMPLETIONS ->
                    categoryId?.let { podiumPlaceMilestonesByCategory[it] }.orEmpty()
                TrophyMetric.CATEGORY_PRESENCE_WEEKS ->
                    categoryId?.let { homeGroundMilestonesByCategory[it] }.orEmpty()
                TrophyMetric.CATEGORY_PLANNING_ACTIONS ->
                    categoryId?.let { trainingBlockMilestonesByCategory[it] }.orEmpty()
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
                                challengeId =
                                    metadata[CHALLENGE_ID]?.toLongOrNull()
                                        ?: action.entityId?.takeIf { action.actionType.toUserActionTypeOrNull() in challengeActions },
                                challengeProgressEntryId = metadata[CHALLENGE_PROGRESS_ENTRY_ID]?.toLongOrNull(),
                                weekStartDate = metadata[WEEK_START_DATE]?.toLocalDateOrNull(),
                                categoryId = metadata[CATEGORY_ID]?.toLongOrNull(),
                                categoryNames =
                                    setOfNotNull(
                                        metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                        metadata[OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                        metadata[NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                                    ),
                            )
                        }.sortedWith(compareBy<ParsedAction>({ it.record.timestamp }, { it.record.id }))

                val challengeCreationMilestones = mutableListOf<Long>()
                val challengeCompletionMilestones = mutableListOf<ChallengeMilestone>()
                val challengeRecoveryMilestones = mutableListOf<ChallengeMilestone>()
                val activeChallengeCompletionById = mutableMapOf<Long, ChallengeMilestone>()
                val activeChallengeRecoveryById = mutableMapOf<Long, ChallengeMilestone>()
                val challengesById = mutableMapOf<Long, Challenge>()
                val challengeEntriesById = mutableMapOf<Long, MutableMap<Long, ChallengeProgressEntry>>()
                val deletedChallengeStacksById = mutableMapOf<Long, ArrayDeque<DeletedChallengeState>>()
                val deletedProgressEntryStacksById = mutableMapOf<Long, ArrayDeque<ChallengeProgressEntry>>()
                val completedWeeks = linkedMapOf<LocalDate, Long>()
                val effectiveCompletionTimestamps = mutableListOf<Long>()
                val effectivePlanningEvents = mutableListOf<WeekEvent>()
                val effectiveCopyEvents = mutableListOf<WeekEvent>()
                val categoryActionTimestamps = mutableListOf<Long>()
                val backupSuccessTimestamps = mutableListOf<Long>()
                val workoutCreationTimestamps = mutableListOf<Long>()
                val personalRecordFamilyCreationTimestamps = mutableListOf<Long>()
                val personalRecordEntryCreationTimestamps = mutableListOf<Long>()
                val paceCalculationTimestamps = mutableListOf<Long>()
                val protectedTimeTimestamps = mutableListOf<Long>()
                val raceEventCreationStacksByEventId = mutableMapOf<Long, ArrayDeque<Long>>()
                val raceEventCompletionStacksByEventId = mutableMapOf<Long, ArrayDeque<Long>>()
                val deletedRaceEventStacksByEventId = mutableMapOf<Long, ArrayDeque<DeletedRaceEventState>>()
                val completionStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<WorkoutCompletion>>()
                val moveStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<WeekEvent>>()
                val reorderStacksByWorkoutId = mutableMapOf<Long, ArrayDeque<WeekEvent>>()
                val copyStacksByWeek = mutableMapOf<LocalDate, ArrayDeque<Long>>()
                val categoryCompletionMilestones = mutableMapOf<Long, MutableList<Long>>()
                val categoryCompletionWeekCounts = mutableMapOf<Long, MutableMap<LocalDate, Int>>()
                val categoryTrainingBlockMilestones = mutableMapOf<Long, MutableList<Long>>()

                fun currentChallengeEntries(challengeId: Long): List<ChallengeProgressEntry> {
                    return challengeEntriesById[challengeId]
                        ?.values
                        ?.sortedWith(compareBy<ChallengeProgressEntry>({ it.entryDate }, { it.occurredAt }, { it.id }))
                        .orEmpty()
                }

                fun currentChallengeResult(
                    challengeId: Long,
                ): com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult? {
                    val challenge = challengesById[challengeId] ?: return null
                    return ChallengeCalculator().calculate(
                        challenge = challenge,
                        progressEntries = currentChallengeEntries(challengeId),
                        today = challenge.endDate,
                    )
                }

                fun isChallengeCompleted(challengeId: Long): Boolean {
                    return currentChallengeResult(challengeId)
                        ?.status
                        ?.let {
                            it == com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus.COMPLETED ||
                                it == com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus.EXCEEDED
                        } == true
                }

                fun removeChallengeMilestone(
                    milestones: MutableList<ChallengeMilestone>,
                    challengeId: Long,
                ): ChallengeMilestone? {
                    val index = milestones.indexOfLast { it.challengeId == challengeId }
                    if (index < 0) return null
                    return milestones.removeAt(index)
                }

                fun syncChallengeMilestones(
                    challengeId: Long,
                    timestamp: Long,
                ) {
                    val isCompletedNow = isChallengeCompleted(challengeId)
                    val hasCompletion = activeChallengeCompletionById.containsKey(challengeId)
                    val recoveredNow = currentChallengeResult(challengeId)?.recoveredCompletionAt != null
                    val hasRecovery = activeChallengeRecoveryById.containsKey(challengeId)
                    when {
                        hasCompletion && !isCompletedNow -> {
                            removeChallengeMilestone(challengeCompletionMilestones, challengeId)
                            if (activeChallengeRecoveryById.remove(challengeId) != null) {
                                removeChallengeMilestone(challengeRecoveryMilestones, challengeId)
                            }
                            activeChallengeCompletionById.remove(challengeId)
                        }

                        !hasCompletion && isCompletedNow -> {
                            val completionMilestone = ChallengeMilestone(challengeId = challengeId, timestamp = timestamp)
                            challengeCompletionMilestones += completionMilestone
                            activeChallengeCompletionById[challengeId] = completionMilestone
                            if (recoveredNow) {
                                val recoveryMilestone = ChallengeMilestone(challengeId = challengeId, timestamp = timestamp)
                                challengeRecoveryMilestones += recoveryMilestone
                                activeChallengeRecoveryById[challengeId] = recoveryMilestone
                            }
                        }

                        hasCompletion && isCompletedNow -> {
                            if (recoveredNow && !hasRecovery) {
                                val recoveryMilestone = ChallengeMilestone(challengeId = challengeId, timestamp = timestamp)
                                challengeRecoveryMilestones += recoveryMilestone
                                activeChallengeRecoveryById[challengeId] = recoveryMilestone
                            }
                            if (!recoveredNow && hasRecovery) {
                                removeChallengeMilestone(challengeRecoveryMilestones, challengeId)
                                activeChallengeRecoveryById.remove(challengeId)
                            }
                        }
                    }
                }

                fun challengeFromMetadata(
                    action: ParsedAction,
                    createdAt: Long,
                    updatedAt: Long,
                ): Challenge? {
                    val challengeId = action.challengeId ?: return null
                    val title = action.metadata[CHALLENGE_TITLE]?.takeIf { it.isNotBlank() } ?: return null
                    val targetType =
                        action.metadata[CHALLENGE_TARGET_TYPE]
                            ?.takeIf { it.isNotBlank() }
                            ?.let { runCatching { ChallengeTargetType.valueOf(it) }.getOrNull() }
                            ?: ChallengeTargetType.DAILY
                    val targetQuantity = action.metadata[CHALLENGE_TARGET_QUANTITY]?.toLongOrNull() ?: return null
                    val startDate = action.metadata[CHALLENGE_START_DATE]?.let(LocalDate::parse) ?: return null
                    val endDate = action.metadata[CHALLENGE_END_DATE]?.let(LocalDate::parse) ?: return null
                    val lifecycle =
                        action.metadata[CHALLENGE_LIFECYCLE]
                            ?.let {
                                runCatching { ChallengeLifecycle.valueOf(it) }.getOrNull()
                            }
                            ?: ChallengeLifecycle.ACTIVE
                    val archivedAt =
                        action.metadata[CHALLENGE_ARCHIVED_AT]
                            ?.takeIf { it.isNotBlank() }
                            ?.let { runCatching { Instant.parse(it) }.getOrNull() }
                    return Challenge(
                        id = challengeId,
                        title = title,
                        description =
                            action.metadata[com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_DESCRIPTION]
                                ?.takeIf { it.isNotBlank() },
                        targetType = targetType,
                        targetQuantity = targetQuantity,
                        startDate = startDate,
                        endDate = endDate,
                        lifecycle = lifecycle,
                        archivedAt = archivedAt,
                        createdAt = Instant.ofEpochMilli(createdAt),
                        updatedAt = Instant.ofEpochMilli(updatedAt),
                    )
                }

                fun recordChallengeSnapshot(challengeId: Long) {
                    val challenge = challengesById[challengeId] ?: return
                    val entries = currentChallengeEntries(challengeId)
                    deletedChallengeStacksById
                        .getOrPut(challengeId, ::ArrayDeque)
                        .addLast(
                            DeletedChallengeState(
                                challenge = challenge,
                                entries = entries,
                            ),
                        )
                }

                fun restoreChallengeSnapshot(challengeId: Long): Boolean {
                    val snapshot =
                        deletedChallengeStacksById[challengeId]
                            ?.removeLastIfPresent()
                            ?: return false
                    challengesById[challengeId] = snapshot.challenge
                    challengeEntriesById[challengeId] = snapshot.entries.associateByTo(linkedMapOf()) { it.id }
                    return true
                }

                fun restoreDeletedProgressEntry(entryId: Long): Boolean {
                    val entry =
                        deletedProgressEntryStacksById[entryId]
                            ?.removeLastIfPresent()
                            ?: return false
                    challengeEntriesById.getOrPut(entry.challengeId, ::linkedMapOf)[entry.id] = entry
                    return true
                }

                parsedActions.forEach { action ->
                    val categoryIds = resolveCategoryIds(action, categoryAliasesById)
                    val affectedChallengeIds = linkedSetOf<Long>()

                    fun markChallengeAffected(challengeId: Long?) {
                        challengeId ?: return
                        affectedChallengeIds += challengeId
                    }

                    when (action.actionType) {
                        CREATE_CHALLENGE -> {
                            val challenge =
                                challengeFromMetadata(action, action.record.timestamp, action.record.timestamp)
                                    ?: return@forEach
                            challengesById[challenge.id] = challenge
                            challengeEntriesById.getOrPut(challenge.id, ::linkedMapOf)
                            challengeCreationMilestones += action.record.timestamp
                            markChallengeAffected(challenge.id)
                        }

                        UPDATE_CHALLENGE,
                        ARCHIVE_CHALLENGE,
                        REACTIVATE_CHALLENGE,
                        -> {
                            val challengeId = action.challengeId ?: return@forEach
                            val currentChallenge = challengesById[challengeId] ?: return@forEach
                            val updatedChallenge =
                                challengeFromMetadata(
                                    action = action,
                                    createdAt = currentChallenge.createdAt.toEpochMilli(),
                                    updatedAt = action.record.timestamp,
                                ) ?: return@forEach
                            challengesById[challengeId] = updatedChallenge
                            markChallengeAffected(challengeId)
                        }

                        DELETE_CHALLENGE -> {
                            val challengeId = action.challengeId ?: return@forEach
                            markChallengeAffected(challengeId)
                            recordChallengeSnapshot(challengeId)
                            challengesById.remove(challengeId)
                            challengeEntriesById.remove(challengeId)
                            if (activeChallengeCompletionById.remove(challengeId) != null) {
                                removeChallengeMilestone(challengeCompletionMilestones, challengeId)
                            }
                            if (activeChallengeRecoveryById.remove(challengeId) != null) {
                                removeChallengeMilestone(challengeRecoveryMilestones, challengeId)
                            }
                        }

                        CREATE_CHALLENGE_PROGRESS_ENTRY -> {
                            val challengeId = action.challengeId ?: return@forEach
                            val quantity = action.metadata[CHALLENGE_PROGRESS_QUANTITY]?.toLongOrNull() ?: return@forEach
                            val entryDate =
                                action.metadata[CHALLENGE_PROGRESS_DATE]?.let(LocalDate::parse) ?: return@forEach
                            val entry =
                                ChallengeProgressEntry(
                                    id = action.record.entityId ?: return@forEach,
                                    challengeId = challengeId,
                                    quantity = quantity,
                                    entryDate = entryDate,
                                    occurredAt = Instant.ofEpochMilli(action.record.timestamp),
                                    createdAt = Instant.ofEpochMilli(action.record.timestamp),
                                    updatedAt = Instant.ofEpochMilli(action.record.timestamp),
                                )
                            challengeEntriesById.getOrPut(challengeId, ::linkedMapOf)[entry.id] = entry
                            markChallengeAffected(challengeId)
                        }

                        UPDATE_CHALLENGE_PROGRESS_ENTRY -> {
                            val entryId = action.challengeProgressEntryId ?: action.record.entityId ?: return@forEach
                            val quantity = action.metadata[CHALLENGE_PROGRESS_QUANTITY]?.toLongOrNull() ?: return@forEach
                            val entryDate =
                                action.metadata[CHALLENGE_PROGRESS_DATE]?.let(LocalDate::parse) ?: return@forEach
                            val challengeId = action.challengeId
                            val currentEntry =
                                challengeId?.let { challengeEntriesById[it]?.get(entryId) }
                                    ?: challengeEntriesById.values.firstNotNullOfOrNull { entries -> entries[entryId] }
                                    ?: return@forEach
                            val updatedEntry =
                                currentEntry.copy(
                                    quantity = quantity,
                                    entryDate = entryDate,
                                    updatedAt = Instant.ofEpochMilli(action.record.timestamp),
                                )
                            challengeEntriesById.getOrPut(updatedEntry.challengeId, ::linkedMapOf)[entryId] = updatedEntry
                            markChallengeAffected(updatedEntry.challengeId)
                        }

                        DELETE_CHALLENGE_PROGRESS_ENTRY -> {
                            val entryId = action.challengeProgressEntryId ?: action.record.entityId ?: return@forEach
                            val challengeId =
                                action.challengeId ?: challengeEntriesById.entries.firstOrNull { (_, entries) ->
                                    entries.containsKey(entryId)
                                }?.key ?: return@forEach
                            val removedEntry = challengeEntriesById[challengeId]?.remove(entryId) ?: return@forEach
                            deletedProgressEntryStacksById
                                .getOrPut(entryId, ::ArrayDeque)
                                .addLast(removedEntry)
                            markChallengeAffected(challengeId)
                        }

                        RESTORE_CHALLENGE -> {
                            val challengeId = action.challengeId ?: return@forEach
                            if (!restoreChallengeSnapshot(challengeId)) return@forEach
                            markChallengeAffected(challengeId)
                        }

                        RESTORE_CHALLENGE_PROGRESS_ENTRY -> {
                            val progressEntryId = action.challengeProgressEntryId
                            if (progressEntryId != null) {
                                val restoredEntry =
                                    deletedProgressEntryStacksById[progressEntryId]
                                        ?.removeLastIfPresent()
                                        ?: return@forEach
                                challengeEntriesById.getOrPut(restoredEntry.challengeId, ::linkedMapOf)[restoredEntry.id] = restoredEntry
                                markChallengeAffected(restoredEntry.challengeId)
                            } else {
                                val challengeId = action.challengeId ?: return@forEach
                                if (!restoreChallengeSnapshot(challengeId)) return@forEach
                                markChallengeAffected(challengeId)
                            }
                        }

                        UserActionType.COMPLETE_WEEK_WORKOUTS -> {
                            val weekStartDate = action.weekStartDate ?: return@forEach
                            completedWeeks.putIfAbsent(weekStartDate, action.record.timestamp)
                        }

                        UserActionType.COMPLETE_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            completionStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(
                                    WorkoutCompletion(
                                        timestamp = action.record.timestamp,
                                        weekStartDate = action.weekStartDate,
                                    ),
                                )
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
                        UserActionType.UNDO_COMPLETE_WORKOUT,
                        -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            val removedCompletion = completionStacksByWorkoutId[workoutId].removeLastIfPresent()
                            val weekStartDate = action.weekStartDate ?: removedCompletion?.weekStartDate
                            categoryIds.forEach { categoryId ->
                                categoryCompletionMilestones[categoryId].removeLastIfPresent()
                                weekStartDate?.let { completionWeekStartDate ->
                                    categoryCompletionWeekCounts[categoryId].decrementWeekCount(completionWeekStartDate)
                                }
                            }
                            if (
                                weekStartDate != null &&
                                !hasCompletionInWeek(completionStacksByWorkoutId, weekStartDate)
                            ) {
                                completedWeeks.remove(weekStartDate)
                            }
                        }

                        UserActionType.UNDO_INCOMPLETE_WORKOUT -> {
                            val workoutId = action.record.entityId ?: return@forEach
                            completionStacksByWorkoutId
                                .getOrPut(workoutId, ::ArrayDeque)
                                .addLast(
                                    WorkoutCompletion(
                                        timestamp = action.record.timestamp,
                                        weekStartDate = action.weekStartDate,
                                    ),
                                )
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

                        UserActionType.CREATE_PERSONAL_RECORD_FAMILY ->
                            personalRecordFamilyCreationTimestamps += action.record.timestamp

                        UserActionType.CREATE_PERSONAL_RECORD_ENTRY ->
                            personalRecordEntryCreationTimestamps += action.record.timestamp

                        UserActionType.USE_PACE_CALCULATOR ->
                            paceCalculationTimestamps += action.record.timestamp

                        UserActionType.CREATE_REST_DAY,
                        UserActionType.CREATE_BUSY,
                        -> protectedTimeTimestamps += action.record.timestamp

                        CREATE_RACE_EVENT -> {
                            val eventId = action.record.entityId ?: return@forEach
                            raceEventCreationStacksByEventId
                                .getOrPut(eventId, ::ArrayDeque)
                                .addLast(action.record.timestamp)
                        }

                        COMPLETE_RACE_EVENT -> {
                            val eventId = action.record.entityId ?: return@forEach
                            raceEventCompletionStacksByEventId
                                .getOrPut(eventId, ::ArrayDeque)
                                .addLast(action.record.timestamp)
                        }

                        INCOMPLETE_RACE_EVENT,
                        UNDO_COMPLETE_RACE_EVENT,
                        -> {
                            val eventId = action.record.entityId ?: return@forEach
                            raceEventCompletionStacksByEventId[eventId].removeLastIfPresent()
                        }

                        UNDO_INCOMPLETE_RACE_EVENT -> {
                            val eventId = action.record.entityId ?: return@forEach
                            raceEventCompletionStacksByEventId
                                .getOrPut(eventId, ::ArrayDeque)
                                .addLast(action.record.timestamp)
                        }

                        DELETE_RACE_EVENT -> {
                            val eventId = action.record.entityId ?: return@forEach
                            val creationTimestamp = raceEventCreationStacksByEventId[eventId].removeLastIfPresent()
                            val completionTimestamp = raceEventCompletionStacksByEventId[eventId].removeLastIfPresent()
                            if (creationTimestamp != null || completionTimestamp != null) {
                                deletedRaceEventStacksByEventId
                                    .getOrPut(eventId, ::ArrayDeque)
                                    .addLast(
                                        DeletedRaceEventState(
                                            creationTimestamp = creationTimestamp,
                                            completionTimestamp = completionTimestamp,
                                        ),
                                    )
                            }
                        }

                        UNDO_DELETE_RACE_EVENT -> {
                            val eventId = action.record.entityId ?: return@forEach
                            val deletedState =
                                deletedRaceEventStacksByEventId[eventId]
                                    .removeLastIfPresent()
                                    ?: return@forEach
                            deletedState.creationTimestamp?.let {
                                raceEventCreationStacksByEventId
                                    .getOrPut(eventId, ::ArrayDeque)
                                    .addLast(it)
                            }
                            deletedState.completionTimestamp?.let {
                                raceEventCompletionStacksByEventId
                                    .getOrPut(eventId, ::ArrayDeque)
                                    .addLast(it)
                            }
                        }

                        else -> Unit
                    }

                    affectedChallengeIds.forEach { challengeId ->
                        syncChallengeMilestones(
                            challengeId = challengeId,
                            timestamp = action.record.timestamp,
                        )
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
                    effectiveCompletionTimestamps += stack.map(WorkoutCompletion::timestamp)
                }
                val effectiveRaceEventCreationTimestamps =
                    raceEventCreationStacksByEventId.values.flatMap { stack -> stack.toList() }
                val effectiveRaceEventCompletionTimestamps =
                    raceEventCompletionStacksByEventId.values.flatMap { stack -> stack.toList() }
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
                    challengeCreationMilestones = challengeCreationMilestones.sorted(),
                    challengeCompletionMilestones = challengeCompletionMilestones.map(ChallengeMilestone::timestamp).sorted(),
                    challengeRecoveryMilestones = challengeRecoveryMilestones.map(ChallengeMilestone::timestamp).sorted(),
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
                    personalRecordFamilyCreationMilestones = personalRecordFamilyCreationTimestamps.sorted(),
                    personalRecordEntryCreationMilestones = personalRecordEntryCreationTimestamps.sorted(),
                    paceCalculationMilestones = paceCalculationTimestamps.sorted(),
                    protectedTimeMilestones = protectedTimeTimestamps.sorted(),
                    raceEventCreationMilestones = effectiveRaceEventCreationTimestamps.sorted(),
                    raceEventCompletionMilestones = effectiveRaceEventCompletionTimestamps.sorted(),
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

            private fun <T> ArrayDeque<T>?.removeLastIfPresent(): T? {
                return if (!isNullOrEmpty()) {
                    removeLast()
                } else {
                    null
                }
            }

            private fun <T> MutableList<T>?.removeLastIfPresent() {
                if (!isNullOrEmpty()) {
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

            private fun hasCompletionInWeek(
                stacksByWorkoutId: Map<Long, ArrayDeque<WorkoutCompletion>>,
                weekStartDate: LocalDate,
            ): Boolean {
                return stacksByWorkoutId.values.any { stack ->
                    stack.any { completion -> completion.weekStartDate == weekStartDate }
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

            private val challengeActions =
                setOf(
                    CREATE_CHALLENGE,
                    UPDATE_CHALLENGE,
                    ARCHIVE_CHALLENGE,
                    REACTIVATE_CHALLENGE,
                    DELETE_CHALLENGE,
                    CREATE_CHALLENGE_PROGRESS_ENTRY,
                    UPDATE_CHALLENGE_PROGRESS_ENTRY,
                    DELETE_CHALLENGE_PROGRESS_ENTRY,
                    RESTORE_CHALLENGE,
                    RESTORE_CHALLENGE_PROGRESS_ENTRY,
                )
        }
    }
}
