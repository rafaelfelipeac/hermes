package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import java.time.LocalDate

data class ProgressState(
    @Deprecated("Use sections")
    val weeklyReadout: ProgressWeeklyReadoutUi = ProgressWeeklyReadoutUi(),
    @Deprecated("Use sections")
    val weeklyTrend: List<ProgressWeekBarUi> = emptyList(),
    @Deprecated("Use sections")
    val weeklyTrendInsight: ProgressWeeklyTrendInsightUi? = null,
    @Deprecated("Use sections")
    val categoryDistribution: List<ProgressCategoryShareUi> = emptyList(),
    @Deprecated("Use sections")
    val trainingMixInsight: ProgressTrainingMixInsightUi? = null,
    @Deprecated("Use sections")
    val trophyHighlight: FeaturedTrophyUi? = null,
    @Deprecated("Use sections")
    val recentActivities: List<ActivityItemUi> = emptyList(),
    @Deprecated("Use sections")
    val upcomingEvent: ProgressUpcomingEventUi? = null,
    val sections: List<ProgressSectionUi> = emptyList(),
    val emptyReason: ProgressEmptyReason? = ProgressEmptyReason.NO_WEEKLY_HISTORY,
)

data class ProgressWeeklyReadoutUi(
    val plannedWorkouts: Int = 0,
    val completedWorkouts: Int = 0,
    val completionPercent: Int = 0,
    val nextFocus: ProgressNextFocusUi? = null,
)

sealed interface ProgressSectionUi {
    val key: String

    data class WeeklyReadout(
        val readout: ProgressWeeklyReadoutUi,
    ) : ProgressSectionUi {
        override val key: String = KEY_WEEKLY_READOUT
    }

    data class WeeklyTrend(
        val weeks: List<ProgressWeekBarUi>,
        val insight: ProgressWeeklyTrendInsightUi?,
    ) : ProgressSectionUi {
        override val key: String = KEY_WEEKLY_TREND
    }

    data class TrainingMix(
        val items: List<ProgressCategoryShareUi>,
        val insight: ProgressTrainingMixInsightUi?,
    ) : ProgressSectionUi {
        override val key: String = KEY_TRAINING_MIX
    }

    data class SupportingProgress(
        val nextFocus: ProgressNextFocusUi?,
        val upcomingEvent: ProgressUpcomingEventUi?,
        val trophyHighlight: FeaturedTrophyUi?,
    ) : ProgressSectionUi {
        override val key: String = KEY_SUPPORTING_PROGRESS
    }

    data class RecentActivity(
        val items: List<ActivityItemUi>,
    ) : ProgressSectionUi {
        override val key: String = KEY_RECENT_ACTIVITY
    }
}

data class ProgressNextFocusUi(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val daysUntil: Int,
)

data class ProgressWeekBarUi(
    val weekStartDate: LocalDate,
    val plannedWorkouts: Int,
    val completedWorkouts: Int,
    val completionPercent: Int,
    val isCurrentWeek: Boolean,
)

data class ProgressWeeklyTrendInsightUi(
    val kind: ProgressWeeklyTrendInsightKind,
)

enum class ProgressWeeklyTrendInsightKind {
    CURRENT_WEEK_HEAVIEST,
}

data class ProgressCategoryShareUi(
    val id: Long,
    val name: String,
    val colorId: String,
    val count: Int,
    val sharePercent: Int,
)

data class ProgressTrainingMixInsightUi(
    val kind: ProgressTrainingMixInsightKind,
    val categoryName: String? = null,
)

enum class ProgressTrainingMixInsightKind {
    BALANCED,
    DOMINANT_CATEGORY,
}

data class ProgressUpcomingEventUi(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val daysUntil: Int,
)

enum class ProgressEmptyReason {
    NO_WEEKLY_HISTORY,
}

private const val KEY_WEEKLY_READOUT = "weekly_readout"
private const val KEY_WEEKLY_TREND = "weekly_trend"
private const val KEY_TRAINING_MIX = "training_mix"
private const val KEY_SUPPORTING_PROGRESS = "supporting_progress"
private const val KEY_RECENT_ACTIVITY = "recent_activity"
