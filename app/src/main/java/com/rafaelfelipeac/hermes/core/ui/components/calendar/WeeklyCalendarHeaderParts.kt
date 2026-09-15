package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.IndicatorSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.core.ui.theme.isDarkBackground
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDayIndicator
import java.time.LocalDate
import java.time.format.TextStyle.SHORT
import java.util.Locale

@Composable
internal fun WeeklyCalendarDayIndicator(
    date: LocalDate,
    isSelected: Boolean,
    indicator: WorkoutDayIndicator?,
) {
    val currentLocale = currentLocale()
    val label =
        date.dayOfWeek.getDisplayName(SHORT, currentLocale)
            .take(1)
            .uppercase(currentLocale)
    val isDarkTheme = isDarkBackground(colorScheme.background)
    val indicatorColor =
        indicator?.let {
            workoutIndicatorColor(
                workout = it.workout,
                isDarkTheme = isDarkTheme,
                surface = colorScheme.surface,
                nonWorkoutColor = colorScheme.outlineVariant,
            )
        }
    val contentColor =
        when {
            indicator == null -> colorScheme.onSurface
            indicator.workout.eventType != WORKOUT &&
                indicator.workout.eventType != RACE_EVENT -> colorScheme.onSurfaceVariant
            indicatorColor != null -> contentColorForBackground(indicatorColor)
            else -> colorScheme.onSurface
        }
    val containerHeight = IndicatorSize + SpacingXxl
    val containerWidth = IndicatorSize + SpacingXs
    val shape = RoundedCornerShape(SpacingSm)

    Box(
        modifier =
            Modifier
                .size(width = containerWidth, height = containerHeight)
                .clip(shape)
                .background(indicatorColor ?: Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontWeight = if (isSelected) Bold else Normal,
                color = contentColor,
            )

            if (indicator?.isDayCompleted == true && indicator.workout.eventType == WORKOUT) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier =
                        Modifier
                            .size(SpacingXxl)
                            .padding(bottom = SpacingXs),
                )
            } else if (indicator?.workout?.eventType != null && indicator.workout.eventType != WORKOUT) {
                Icon(
                    imageVector = nonWorkoutEventIcon(indicator.workout.eventType),
                    contentDescription = null,
                    tint = contentColor,
                    modifier =
                        Modifier
                            .size(SpacingXxl)
                            .padding(bottom = SpacingXs),
                )
            } else {
                Spacer(
                    modifier =
                        Modifier
                            .size(SpacingXxl)
                            .padding(vertical = SpacingXxs),
                )
            }
        }
    }
}

internal fun formatWeekRange(
    start: LocalDate,
    end: LocalDate,
    locale: Locale,
): String {
    val startDay = start.dayOfMonth
    val endDay = end.dayOfMonth
    val startMonth = start.month.getDisplayName(SHORT, locale)
    val endMonth = end.month.getDisplayName(SHORT, locale)

    return when {
        start.year == end.year && start.month == end.month ->
            String.format(
                locale,
                "%d-%d %s %d",
                startDay,
                endDay,
                startMonth,
                start.year,
            )
        start.year == end.year ->
            String.format(
                locale,
                "%d %s - %d %s %d",
                startDay,
                startMonth,
                endDay,
                endMonth,
                start.year,
            )
        else ->
            String.format(
                locale,
                "%d %s %d - %d %s %d",
                startDay,
                startMonth,
                start.year,
                endDay,
                endMonth,
                end.year,
            )
    }
}

private fun nonWorkoutEventIcon(eventType: EventType): ImageVector {
    return when (eventType) {
        EventType.REST -> Icons.Outlined.Bedtime
        EventType.BUSY -> Icons.Outlined.EventBusy
        EventType.SICK -> Icons.Outlined.MedicalServices
        RACE_EVENT -> Icons.Outlined.Flag
        WORKOUT -> Icons.Outlined.Check
    }
}
