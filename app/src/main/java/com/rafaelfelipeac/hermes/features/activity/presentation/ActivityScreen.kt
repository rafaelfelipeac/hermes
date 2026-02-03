package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.*
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales.get(0) ?: Locale.getDefault()

    LaunchedEffect(currentLocale) {
        viewModel.updateLocale(currentLocale)
    }

    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Text(
            text = stringResource(R.string.activity_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ActivityContent(
            sections = state.sections,
            currentLocale = currentLocale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun ActivityContent(
    sections: List<ActivitySectionUi>,
    currentLocale: Locale,
    modifier: Modifier = Modifier,
) {
    if (sections.isEmpty()) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(ActivityEmptyPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.activity_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        return
    }

    val dayPattern = stringResource(R.string.activity_week_date_pattern)
    val dayFormatter =
        DateTimeFormatterBuilder()
            .appendPattern(dayPattern)
            .toFormatter(currentLocale)
    val today = LocalDate.now(ZoneId.systemDefault())
    val todayLabel = stringResource(R.string.activity_today)
    val yesterdayLabel = stringResource(R.string.activity_yesterday)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        sections.forEach { section ->
            val header = sectionTitle(section.date, today, dayFormatter, todayLabel, yesterdayLabel)

            item(key = "header-${section.date}") {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items(section.items, key = { it.id }) { item ->
                ActivityRow(item)
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItemUi) {
    Surface(
        tonalElevation = ElevationSm,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = SpacingXl,
                    vertical = SpacingLg,
                ),
        ) {
            Row {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(SpacingLg))

                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.subtitle?.let { subtitle ->
                Divider(modifier = Modifier.padding(vertical = SpacingMd))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private fun sectionTitle(
    date: LocalDate,
    today: LocalDate,
    formatter: DateTimeFormatter,
    todayLabel: String,
    yesterdayLabel: String,
): String {
    return when (date) {
        today -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(formatter)
    }
}
