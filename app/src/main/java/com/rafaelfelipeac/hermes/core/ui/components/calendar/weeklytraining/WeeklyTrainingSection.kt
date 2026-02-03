package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY

private const val SECTION_KEY_TBD = "tbd"

@Composable
internal fun SectionHeader(
    title: String,
    tag: String,
    showHelp: Boolean,
    onHelpClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SpacingXs,
                    vertical = SpacingMd,
                )
                .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showHelp) {
            Surface(
                shape = CircleShape,
                color = colorScheme.surfaceVariant,
                tonalElevation = ElevationSm,
                shadowElevation = ElevationSm,
                modifier = Modifier.size(HelpIconSize),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(onClick = onHelpClick),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.tbd_help_icon),
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptySectionRow() {
    Text(
        text = stringResource(R.string.no_workouts),
        style = typography.bodySmall,
        modifier =
            Modifier.padding(
                horizontal = SpacingLg,
                vertical = SpacingMd,
            ),
    )
}

sealed class SectionKey(val key: String) {
    object ToBeDefined : SectionKey(SECTION_KEY_TBD)

    data class Day(val dayOfWeek: DayOfWeek) : SectionKey(dayOfWeek.name)
}

@Composable
internal fun SectionKey.title(): String {
    return when (this) {
        SectionKey.ToBeDefined -> stringResource(R.string.section_to_be_defined)
        is SectionKey.Day -> stringResource(dayOfWeek.labelRes())
    }
}

internal fun SectionKey.dayOfWeekOrNull(): DayOfWeek? {
    return when (this) {
        SectionKey.ToBeDefined -> null
        is SectionKey.Day -> dayOfWeek
    }
}

private fun DayOfWeek.labelRes(): Int {
    return when (this) {
        MONDAY -> R.string.day_monday
        TUESDAY -> R.string.day_tuesday
        WEDNESDAY -> R.string.day_wednesday
        THURSDAY -> R.string.day_thursday
        FRIDAY -> R.string.day_friday
        SATURDAY -> R.string.day_saturday
        SUNDAY -> R.string.day_sunday
    }
}
