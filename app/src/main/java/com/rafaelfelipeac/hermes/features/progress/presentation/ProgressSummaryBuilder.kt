package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Suppress("LongParameterList")
internal fun buildProgressState(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
    trophyCards: List<TrophyCardUi>,
    recentActivities: List<com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressState {
    val visibleCategories = categories.filter { !it.isHidden }
    val weeklyTrend = buildWeeklyTrend(workouts, currentWeekStart)
    val fullCategoryDistribution = buildCategoryDistribution(workouts, visibleCategories, currentWeekStart)
    val categoryDistribution = fullCategoryDistribution.take(CATEGORY_LIMIT)
    val upcomingEvent = buildUpcomingEvent(workouts, today)
    val trophyHighlight = selectFeaturedTrophy(trophyCards)
    val hasHistory = workouts.any { it.dayOfWeek != null }
    val weeklyReadout = buildWeeklyReadout(workouts, today, currentWeekStart)
    val weeklyTrendInsight = buildWeeklyTrendInsight(weeklyTrend)
    val trainingMixInsight = buildTrainingMixInsight(fullCategoryDistribution)
    val sections =
        buildSections(
            weeklyReadout = weeklyReadout,
            weeklyTrend = weeklyTrend,
            weeklyTrendInsight = weeklyTrendInsight,
            categoryDistribution = categoryDistribution,
            trainingMixInsight = trainingMixInsight,
            trophyHighlight = trophyHighlight,
            recentActivities = recentActivities.take(RECENT_ACTIVITY_PREVIEW_LIMIT),
            upcomingEvent = upcomingEvent,
        )

    return ProgressState(
        weeklyReadout = weeklyReadout,
        weeklyTrend = weeklyTrend,
        weeklyTrendInsight = weeklyTrendInsight,
        categoryDistribution = categoryDistribution,
        trainingMixInsight = trainingMixInsight,
        trophyHighlight = trophyHighlight,
        recentActivities = recentActivities.take(RECENT_ACTIVITY_PREVIEW_LIMIT),
        upcomingEvent = upcomingEvent,
        sections = sections,
        emptyReason = if (hasHistory) null else ProgressEmptyReason.NO_WEEKLY_HISTORY,
    )
}

private fun buildWeeklyReadout(
    workouts: List<Workout>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressWeeklyReadoutUi {
    val displayWeekWorkouts = workoutsInDisplayWeek(workouts, currentWeekStart)
    val plannedWorkouts = displayWeekWorkouts.size
    val completedWorkouts = displayWeekWorkouts.count { it.isCompleted }

    return ProgressWeeklyReadoutUi(
        plannedWorkouts = plannedWorkouts,
        completedWorkouts = completedWorkouts,
        completionPercent = percent(completedWorkouts, plannedWorkouts),
        nextFocus = buildNextFocus(workouts, today, currentWeekStart),
    )
}

private fun workoutsInDisplayWeek(
    workouts: List<Workout>,
    currentWeekStart: LocalDate,
): List<Workout> {
    val currentWeekEnd = currentWeekStart.plusDays(WEEK_END_OFFSET_DAYS)

    return workouts.filter { workout ->
        if (workout.dayOfWeek == null || !workout.countsTowardProgressSummary()) return@filter false
        val date = workoutDate(workout)
        !date.isBefore(currentWeekStart) && !date.isAfter(currentWeekEnd)
    }
}

private fun buildNextFocus(
    workouts: List<Workout>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressNextFocusUi? {
    val currentWeekEnd = currentWeekStart.plusDays(WEEK_END_OFFSET_DAYS)

    return workouts
        .asSequence()
        .filter {
            it.dayOfWeek != null &&
                it.countsTowardProgressSummary() &&
                !it.isCompleted
        }
        .map { workout ->
            val date = workoutDate(workout)
            workout to date
        }
        .filter { (_, date) ->
            !date.isBefore(currentWeekStart) &&
                !date.isAfter(currentWeekEnd) &&
                !date.isBefore(today)
        }
        .minWithOrNull(
            compareBy<Pair<Workout, LocalDate>> { (_, date) -> date }
                .thenBy { (workout, _) -> workout.order },
        )
        ?.let { (workout, date) ->
            ProgressNextFocusUi(
                id = workout.id,
                title = workout.type.ifBlank { workout.description }.ifBlank { EMPTY },
                date = date,
                daysUntil = ChronoUnit.DAYS.between(today, date).toInt(),
            )
        }
}

private fun workoutDate(workout: Workout): LocalDate {
    return workout.weekStartDate.plusDays((requireNotNull(workout.dayOfWeek).value - 1).toLong())
}

private fun Workout.countsTowardProgressSummary(): Boolean {
    return eventType == WORKOUT || eventType == RACE_EVENT
}

private fun buildWeeklyTrend(
    workouts: List<Workout>,
    currentWeekStart: LocalDate,
): List<ProgressWeekBarUi> {
    return (TREND_WEEK_COUNT - 1 downTo 0).map { offset ->
        val weekStart = currentWeekStart.minusWeeks(offset.toLong())
        val weekWorkouts = workouts.filter { it.weekStartDate == weekStart && it.dayOfWeek != null }
        val plannedWorkouts = weekWorkouts.count { it.countsTowardProgressSummary() }
        val completedWorkouts = weekWorkouts.count { it.countsTowardProgressSummary() && it.isCompleted }

        ProgressWeekBarUi(
            weekStartDate = weekStart,
            plannedWorkouts = plannedWorkouts,
            completedWorkouts = completedWorkouts,
            completionPercent = percent(completedWorkouts, plannedWorkouts),
            isCurrentWeek = weekStart == currentWeekStart,
        )
    }
}

private fun buildWeeklyTrendInsight(weeklyTrend: List<ProgressWeekBarUi>): ProgressWeeklyTrendInsightUi? {
    val currentWeek = weeklyTrend.firstOrNull { it.isCurrentWeek }
    val maxPlannedWorkouts = weeklyTrend.maxOfOrNull { it.plannedWorkouts }

    return currentWeek
        ?.takeIf {
            it.plannedWorkouts > 0 &&
                it.plannedWorkouts == maxPlannedWorkouts
        }
        ?.let {
            ProgressWeeklyTrendInsightUi(ProgressWeeklyTrendInsightKind.CURRENT_WEEK_HEAVIEST)
        }
}

@Suppress("LongParameterList")
private fun buildSections(
    weeklyReadout: ProgressWeeklyReadoutUi,
    weeklyTrend: List<ProgressWeekBarUi>,
    weeklyTrendInsight: ProgressWeeklyTrendInsightUi?,
    categoryDistribution: List<ProgressCategoryShareUi>,
    trainingMixInsight: ProgressTrainingMixInsightUi?,
    trophyHighlight: FeaturedTrophyUi?,
    recentActivities: List<com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi>,
    upcomingEvent: ProgressUpcomingEventUi?,
): List<ProgressSectionUi> {
    return buildList {
        add(ProgressSectionUi.WeeklyReadout(weeklyReadout))
        add(ProgressSectionUi.WeeklyTrend(weeklyTrend, weeklyTrendInsight))
        if (categoryDistribution.isNotEmpty()) {
            add(ProgressSectionUi.TrainingMix(categoryDistribution, trainingMixInsight))
        }
        if (trophyHighlight != null || upcomingEvent != null || weeklyReadout.nextFocus != null) {
            add(
                ProgressSectionUi.SupportingProgress(
                    nextFocus = weeklyReadout.nextFocus,
                    upcomingEvent = upcomingEvent,
                    trophyHighlight = trophyHighlight,
                ),
            )
        }
        if (recentActivities.isNotEmpty()) {
            add(ProgressSectionUi.RecentActivity(recentActivities))
        }
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
            it.countsTowardProgressSummary() &&
                it.isCompleted &&
                !it.weekStartDate.isBefore(windowStart) &&
                !it.weekStartDate.isAfter(currentWeekStart)
        }

    return if (workoutsInWindow.isEmpty()) {
        emptyList()
    } else {
        val categoriesById = categories.associateBy { it.id }
        val countsByCategoryId =
            workoutsInWindow.groupingBy { it.categoryId ?: UNCATEGORIZED_ID }.eachCount()
        val visibleCountsByCategoryId =
            countsByCategoryId.filterKeys { categoryId -> categoryId in categoriesById }
        val visibleTotal = visibleCountsByCategoryId.values.sum()

        if (visibleTotal == 0) {
            emptyList()
        } else {
            visibleCountsByCategoryId.mapNotNull { (categoryId, count) ->
                categoriesById[categoryId]?.let { category ->
                    ProgressCategoryShareUi(
                        id = category.id,
                        name = category.name,
                        colorId = category.colorId,
                        count = count,
                        sharePercent = percent(count, visibleTotal),
                    )
                }
            }.sortedWith(
                compareBy<ProgressCategoryShareUi> { it.id == UNCATEGORIZED_ID }
                    .thenByDescending { it.count }
                    .thenBy { it.name },
            )
        }
    }
}

private fun buildTrainingMixInsight(items: List<ProgressCategoryShareUi>): ProgressTrainingMixInsightUi? {
    val topCategory =
        items.maxWithOrNull(
            compareBy<ProgressCategoryShareUi> { it.sharePercent }
                .thenBy { it.count },
        ) ?: return null

    return when {
        topCategory.sharePercent > DOMINANT_CATEGORY_PERCENT ->
            ProgressTrainingMixInsightUi(
                kind = ProgressTrainingMixInsightKind.DOMINANT_CATEGORY,
                categoryName = topCategory.name,
            )
        items.size >= BALANCED_CATEGORY_MIN_COUNT &&
            topCategory.sharePercent <= BALANCED_CATEGORY_MAX_TOP_PERCENT ->
            ProgressTrainingMixInsightUi(ProgressTrainingMixInsightKind.BALANCED)
        else -> null
    }
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
private const val WEEK_END_OFFSET_DAYS = 6L
private const val RECENT_ACTIVITY_PREVIEW_LIMIT = 5
private const val BALANCED_CATEGORY_MIN_COUNT = 3
private const val BALANCED_CATEGORY_MAX_TOP_PERCENT = 35
private const val DOMINANT_CATEGORY_PERCENT = 50
private const val PERCENT_FULL = 100
