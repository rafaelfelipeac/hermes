package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import java.time.LocalDate

data class ProgressState(
    val summaryCards: List<ProgressSummaryCardUi> = emptyList(),
    val thisWeek: ProgressWeekSnapshotUi = ProgressWeekSnapshotUi(),
    val weeklyTrend: List<ProgressWeekBarUi> = emptyList(),
    val categoryDistribution: List<ProgressCategoryShareUi> = emptyList(),
    val trophyHighlight: FeaturedTrophyUi? = null,
    val recentActivities: List<ActivityItemUi> = emptyList(),
    val upcomingEvent: ProgressUpcomingEventUi? = null,
    val emptyReason: ProgressEmptyReason? = ProgressEmptyReason.NO_WEEKLY_HISTORY,
)

data class ProgressSummaryCardUi(
    val kind: ProgressSummaryCardKind,
    val value: String,
    val supportingText: String? = null,
)

enum class ProgressSummaryCardKind {
    THIS_WEEK,
    CONSISTENCY,
    TOP_CATEGORY,
    UPCOMING,
}

data class ProgressWeekSnapshotUi(
    val plannedWorkouts: Int = 0,
    val completedWorkouts: Int = 0,
    val completionPercent: Int = 0,
    val plannedRestEvents: Int = 0,
    val plannedBusyEvents: Int = 0,
    val plannedSickEvents: Int = 0,
)

data class ProgressWeekBarUi(
    val weekStartDate: LocalDate,
    val plannedWorkouts: Int,
    val completedWorkouts: Int,
    val completionPercent: Int,
    val isCurrentWeek: Boolean,
)

data class ProgressCategoryShareUi(
    val id: Long,
    val name: String,
    val colorId: String,
    val count: Int,
    val sharePercent: Int,
)

data class ProgressUpcomingEventUi(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val daysUntil: Int,
)

enum class ProgressEmptyReason {
    NO_WEEKLY_HISTORY,
}
