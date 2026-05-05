package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun buildProgressState(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
    trophyCards: List<TrophyCardUi>,
    recentActivities: List<com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressState {
    val visibleCategories = categories.filter { !it.isHidden }
    val thisWeekWorkouts = workouts.filter { it.weekStartDate == currentWeekStart }
    val thisWeek = buildWeekSnapshot(thisWeekWorkouts)
    val weeklyTrend = buildWeeklyTrend(workouts, currentWeekStart)
    val categoryDistribution = buildCategoryDistribution(workouts, visibleCategories, currentWeekStart)
    val upcomingEvent = buildUpcomingEvent(workouts, today)
    val trophyHighlight = selectFeaturedTrophy(trophyCards)
    val summaryCards =
        buildSummaryCards(
            thisWeek = thisWeek,
            weeklyTrend = weeklyTrend,
            categoryDistribution = categoryDistribution,
            upcomingEvent = upcomingEvent,
        )
    val hasHistory = workouts.any { it.dayOfWeek != null }

    return ProgressState(
        summaryCards = summaryCards,
        thisWeek = thisWeek,
        weeklyTrend = weeklyTrend,
        categoryDistribution = categoryDistribution,
        trophyHighlight = trophyHighlight,
        recentActivities = recentActivities.take(RECENT_ACTIVITY_LIMIT),
        upcomingEvent = upcomingEvent,
        emptyReason = if (hasHistory) null else ProgressEmptyReason.NO_WEEKLY_HISTORY,
    )
}

private fun buildWeekSnapshot(workouts: List<Workout>): ProgressWeekSnapshotUi {
    val scheduledItems = workouts.filter { it.dayOfWeek != null }
    val plannedWorkouts = scheduledItems.count { it.eventType == WORKOUT }
    val completedWorkouts = scheduledItems.count { it.eventType == WORKOUT && it.isCompleted }

    return ProgressWeekSnapshotUi(
        plannedWorkouts = plannedWorkouts,
        completedWorkouts = completedWorkouts,
        completionPercent = percent(completedWorkouts, plannedWorkouts),
        plannedRestEvents = scheduledItems.count { it.eventType == REST },
        plannedBusyEvents = scheduledItems.count { it.eventType == BUSY },
        plannedSickEvents = scheduledItems.count { it.eventType == SICK },
    )
}

private fun buildWeeklyTrend(
    workouts: List<Workout>,
    currentWeekStart: LocalDate,
): List<ProgressWeekBarUi> {
    return (TREND_WEEK_COUNT - 1 downTo 0).map { offset ->
        val weekStart = currentWeekStart.minusWeeks(offset.toLong())
        val weekWorkouts = workouts.filter { it.weekStartDate == weekStart && it.dayOfWeek != null }
        val plannedWorkouts = weekWorkouts.count { it.eventType == WORKOUT }
        val completedWorkouts = weekWorkouts.count { it.eventType == WORKOUT && it.isCompleted }

        ProgressWeekBarUi(
            weekStartDate = weekStart,
            plannedWorkouts = plannedWorkouts,
            completedWorkouts = completedWorkouts,
            completionPercent = percent(completedWorkouts, plannedWorkouts),
            isCurrentWeek = weekStart == currentWeekStart,
        )
    }
}

private fun buildCategoryDistribution(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
    currentWeekStart: LocalDate,
): List<ProgressCategoryShareUi> {
    val windowStart = currentWeekStart.minusWeeks((CATEGORY_WINDOW_WEEK_COUNT - 1).toLong())
    val workoutsInWindow =
        workouts.filter {
            it.eventType == WORKOUT &&
                it.isCompleted &&
                !it.weekStartDate.isBefore(windowStart) &&
                !it.weekStartDate.isAfter(currentWeekStart)
        }

    if (workoutsInWindow.isEmpty()) return emptyList()

    val categoriesById = categories.associateBy { it.id }
    val countsByCategoryId =
        workoutsInWindow.groupingBy { it.categoryId ?: UNCATEGORIZED_ID }.eachCount()

    return countsByCategoryId.mapNotNull { (categoryId, count) ->
        val category = categoriesById[categoryId] ?: return@mapNotNull null
        ProgressCategoryShareUi(
            id = category.id,
            name = category.name,
            colorId = category.colorId,
            count = count,
            sharePercent = percent(count, workoutsInWindow.size),
        )
    }.sortedWith(
        compareBy<ProgressCategoryShareUi> { it.id == UNCATEGORIZED_ID }
            .thenByDescending { it.count }
            .thenBy { it.name },
    ).take(CATEGORY_LIMIT)
}

private fun buildUpcomingEvent(
    workouts: List<Workout>,
    today: LocalDate,
): ProgressUpcomingEventUi? {
    return workouts
        .asSequence()
        .filter { it.eventType == RACE_EVENT && it.dayOfWeek != null }
        .map { workout ->
            val eventDate = workout.weekStartDate.plusDays((requireNotNull(workout.dayOfWeek).value - 1).toLong())
            workout to eventDate
        }
        .filter { (_, eventDate) -> !eventDate.isBefore(today) }
        .minByOrNull { (_, eventDate) -> eventDate }
        ?.let { (workout, eventDate) ->
            ProgressUpcomingEventUi(
                id = workout.id,
                title = workout.type.ifBlank { workout.description }.ifBlank { EMPTY },
                date = eventDate,
                daysUntil = ChronoUnit.DAYS.between(today, eventDate).toInt(),
            )
        }
}

private fun buildSummaryCards(
    thisWeek: ProgressWeekSnapshotUi,
    weeklyTrend: List<ProgressWeekBarUi>,
    categoryDistribution: List<ProgressCategoryShareUi>,
    upcomingEvent: ProgressUpcomingEventUi?,
): List<ProgressSummaryCardUi> {
    val completedWeeks = weeklyTrend.count { it.plannedWorkouts > 0 && it.completedWorkouts == it.plannedWorkouts }
    return buildList {
        add(
            ProgressSummaryCardUi(
                kind = ProgressSummaryCardKind.THIS_WEEK,
                value = "${thisWeek.completionPercent}%",
                supportingText = "${thisWeek.completedWorkouts}/${thisWeek.plannedWorkouts}",
            ),
        )
        add(
            ProgressSummaryCardUi(
                kind = ProgressSummaryCardKind.CONSISTENCY,
                value = completedWeeks.toString(),
                supportingText = weeklyTrend.size.toString(),
            ),
        )
        categoryDistribution.firstOrNull()?.let { topCategory ->
            add(
                ProgressSummaryCardUi(
                    kind = ProgressSummaryCardKind.TOP_CATEGORY,
                    value = topCategory.name,
                    supportingText = "${topCategory.count} completed",
                ),
            )
        }
        upcomingEvent?.let { event ->
            add(
                ProgressSummaryCardUi(
                    kind = ProgressSummaryCardKind.UPCOMING,
                    value = event.daysUntil.toString(),
                    supportingText = event.title,
                ),
            )
        }
    }
}

private fun selectFeaturedTrophy(cards: List<TrophyCardUi>): FeaturedTrophyUi? {
    val recentUnlock =
        cards
            .filter { it.unlockedAt != null }
            .maxByOrNull { it.unlockedAt ?: Long.MIN_VALUE }

    if (recentUnlock != null) {
        return FeaturedTrophyUi(
            trophy = recentUnlock,
            mode = FeaturedTrophyMode.RECENT_UNLOCK,
        )
    }

    val nearest =
        cards
            .filter { !it.isUnlocked }
            .minWithOrNull(
                compareBy<TrophyCardUi>(
                    { (it.target - it.currentValue).coerceAtLeast(0) },
                    { it.family.ordinal },
                    { it.stableId },
                ),
            )

    return nearest?.let {
        FeaturedTrophyUi(
            trophy = it,
            mode = FeaturedTrophyMode.NEAREST_PROGRESS,
        )
    }
}

private fun percent(
    numerator: Int,
    denominator: Int,
): Int {
    if (denominator <= 0) return 0
    return ((numerator.toFloat() / denominator.toFloat()) * PERCENT_FULL).toInt()
}

private const val TREND_WEEK_COUNT = 8
private const val CATEGORY_WINDOW_WEEK_COUNT = 8
private const val CATEGORY_LIMIT = 4
private const val RECENT_ACTIVITY_LIMIT = 5
private const val PERCENT_FULL = 100
