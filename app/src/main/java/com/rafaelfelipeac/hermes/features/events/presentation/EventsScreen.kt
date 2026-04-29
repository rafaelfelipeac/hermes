package com.rafaelfelipeac.hermes.features.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.AddRaceEventDialog
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.calendar.baseCategoryColor
import com.rafaelfelipeac.hermes.core.ui.components.calendar.completedCategoryColor
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CheckboxBoxSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CheckboxSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ContentPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventCardMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventFlagIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.core.ui.theme.LIGHTER_TONE_BLEND_DARK
import com.rafaelfelipeac.hermes.core.ui.theme.LIGHTER_TONE_BLEND_LIGHT
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.core.ui.theme.isDarkBackground
import com.rafaelfelipeac.hermes.features.events.presentation.model.EventDialogDraft
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val TYPE_CHIP_ALPHA = 0.18f

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (EventDialogDraft) -> Unit = {},
    pendingEventDraft: EventDialogDraft? = null,
    onEventDraftConsumed: () -> Unit = {},
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var editingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftDescription by rememberSaveable { mutableStateOf("") }
    var draftCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var draftConsumedLocally by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val upcomingEvents =
        state.events
            .filter { it.eventDate() >= today }
            .sortedWith(compareBy<WorkoutUi> { it.eventDate() }.thenBy { it.order }.thenBy { it.id })
    val pastEvents =
        state.events
            .filter { it.eventDate() < today }
            .sortedWith(
                compareByDescending<WorkoutUi> { it.eventDate() }
                    .thenByDescending { it.order }
                    .thenByDescending { it.id },
            )

    val editingEvent = editingEventId?.let { id -> state.events.firstOrNull { it.id == id } }

    LaunchedEffect(pendingEventDraft, state.events, state.categories) {
        if (pendingEventDraft == null) {
            draftConsumedLocally = false
            return@LaunchedEffect
        }

        if (draftConsumedLocally) return@LaunchedEffect

        if (pendingEventDraft.eventId == null) {
            draftTitle = pendingEventDraft.title
            draftDescription = pendingEventDraft.description
            draftCategoryId = pendingEventDraft.categoryId
            draftDate = pendingEventDraft.eventDate
            editingEventId = null
            isDialogVisible = true
            draftConsumedLocally = true
            onEventDraftConsumed()
        } else {
            val event = state.events.firstOrNull { it.id == pendingEventDraft.eventId }

            if (event != null) {
                editingEventId = event.id
                draftTitle = pendingEventDraft.title
                draftDescription = pendingEventDraft.description
                draftCategoryId = pendingEventDraft.categoryId
                draftDate = pendingEventDraft.eventDate
                isDialogVisible = true
                draftConsumedLocally = true
                onEventDraftConsumed()
            }
        }
    }

    fun openEditDialog(event: WorkoutUi) {
        editingEventId = event.id
        draftTitle = event.type
        draftDescription = event.description
        draftCategoryId = event.categoryId
        draftDate = event.eventDate()
        isDialogVisible = true
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEventId = null
                    draftTitle = ""
                    draftDescription = ""
                    draftCategoryId = null
                    draftDate = null
                    isDialogVisible = true
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = stringResource(R.string.race_events_add),
                )
            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding =
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = Zero,
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding(),
            )

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(EventCardMinWidth),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = SpacingXl, top = Zero, end = SpacingXl, bottom = SpacingXl),
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.padding(top = SpacingXl),
                        verticalArrangement = Arrangement.spacedBy(SpacingSm),
                    ) {
                        Text(
                            text = stringResource(R.string.race_events_title),
                            style = typography.titleLarge,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.race_events_subtitle),
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EventsEmptyState(
                            modifier = Modifier.padding(top = SpacingXl),
                        )
                    }
                } else {
                    items(upcomingEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { openEditDialog(event) },
                            onToggleCompleted = { checked ->
                                viewModel.updateRaceEventCompletion(
                                    eventId = event.id,
                                    isCompleted = checked,
                                )
                            },
                            onDelete = { viewModel.deleteRaceEvent(event.id) },
                        )
                    }

                    if (pastEvents.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(R.string.race_events_past_title),
                                style = typography.titleMedium,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(top = SpacingLg),
                            )
                        }

                        items(pastEvents, key = { it.id }) { event ->
                            EventCard(
                                event = event,
                                onClick = { openEditDialog(event) },
                                onToggleCompleted = { checked ->
                                    viewModel.updateRaceEventCompletion(
                                        eventId = event.id,
                                        isCompleted = checked,
                                    )
                                },
                                onDelete = { viewModel.deleteRaceEvent(event.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (isDialogVisible) {
        AddRaceEventDialog(
            onDismiss = {
                isDialogVisible = false
                editingEventId = null
            },
            onSave = { title, description, categoryId, eventDate ->
                val currentEditingEventId = editingEventId

                if (currentEditingEventId == null) {
                    viewModel.addRaceEvent(
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    )
                } else {
                    viewModel.updateRaceEvent(
                        eventId = currentEditingEventId,
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    )
                }

                isDialogVisible = false
                editingEventId = null
            },
            onManageCategories = { title, description, categoryId, eventDate ->
                isDialogVisible = false
                onManageCategories(
                    EventDialogDraft(
                        eventId = editingEventId,
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    ),
                )
            },
            isEdit = editingEvent != null,
            categories = state.categories,
            selectedCategoryId = draftCategoryId,
            selectedDate = draftDate,
            initialTitle = draftTitle,
            initialDescription = draftDescription,
        )
    }
}

@Composable
private fun EventsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.race_events_empty_title),
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.race_events_empty_body),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventCard(
    event: WorkoutUi,
    onClick: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val eventDate = event.eventDate()
    val categoryAccent = event.categoryColorId?.let(::categoryAccentColor)?.let(::baseCategoryColor)
    val isDarkTheme = isDarkBackground(colorScheme.background)
    val colors = eventCardColors(event = event, categoryAccent = categoryAccent, isDarkTheme = isDarkTheme)
    val categoryChipBase =
        categoryAccent?.let { accent ->
            if (event.isCompleted) {
                completedCategoryColor(
                    accent = accent,
                    isDarkTheme = isDarkTheme,
                    surface = colorScheme.surface,
                )
            } else {
                accent
            }
        }
    val categoryChipBackground =
        categoryChipBase?.let { base ->
            lighterTone(base, isDarkTheme = isDarkTheme)
        }
    val categoryChipContent = Color.White
    val countdown = countdownLabel(eventDate)
    val dateLabel = formatDate(eventDate)
    val categoryLabel = event.categoryName ?: stringResource(R.string.category_uncategorized)

    Card(
        onClick = onClick,
        shape = shapes.medium,
        border = BorderStroke(BorderHairline, categoryAccent ?: colorScheme.outlineVariant),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.background,
                contentColor = colors.content,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = EventCardMinHeight),
    ) {
        Box {
            if (categoryAccent != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(SpacingXs)
                            .background(categoryAccent),
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(ContentPadding)
                            .padding(end = SpacingXl),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(CheckboxBoxSize + SpacingSm)
                                .offset(y = Zero),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (event.isCompleted) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(CheckboxSize + SpacingSm)
                                        .clickable { onToggleCompleted(false) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = stringResource(R.string.weekly_training_workout_completed),
                                    tint = colors.content,
                                    modifier = Modifier.size(CheckboxSize - SpacingXs),
                                )
                            }
                        } else {
                            Checkbox(
                                checked = false,
                                onCheckedChange = onToggleCompleted,
                                modifier = Modifier.size(CheckboxSize + SpacingSm),
                                colors =
                                    CheckboxDefaults.colors(
                                        checkedColor = colors.content,
                                        uncheckedColor = colors.content,
                                        checkmarkColor = colors.background,
                                    ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(SpacingLg))

                    Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                        TitleChip(
                            label = categoryLabel,
                            containerColor = categoryChipBackground ?: colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                            contentColor = categoryChipContent,
                            modifier = Modifier.wrapContentWidth(),
                        )

                        Text(
                            text = event.type,
                            style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.content,
                            modifier = Modifier.padding(start = SpacingXs),
                        )

                        Text(
                            text = dateLabel,
                            style = typography.bodySmall,
                            color = colors.content.copy(alpha = 0.85f),
                            modifier = Modifier.padding(start = SpacingXs),
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.35f),
                    thickness = BorderThin,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(SpacingXs))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ContentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = stringResource(R.string.race_event_label),
                        tint = Color.White,
                        modifier = Modifier.size(EventFlagIconSize),
                    )

                    Text(
                        text = countdown,
                        style = typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                    )
                }

                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = typography.bodyMedium,
                        color = colors.content.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = ContentPadding),
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.weekly_training_delete_race_event),
                tint = colors.content,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = SpacingMd, end = SpacingMd)
                        .size(SmallIconSize)
                        .clickable { onDelete() },
            )
        }
    }
}

private data class EventCardColors(
    val background: Color,
    val content: Color,
)

@Composable
private fun eventCardColors(
    event: WorkoutUi,
    categoryAccent: Color?,
    isDarkTheme: Boolean,
): EventCardColors {
    val background =
        when {
            event.isCompleted && categoryAccent == null -> TodoBlue
            event.isCompleted && categoryAccent != null ->
                completedCategoryColor(
                    accent = categoryAccent,
                    isDarkTheme = isDarkTheme,
                    surface = colorScheme.surface,
                )
            categoryAccent != null -> categoryAccent
            else -> CompletedBlue
        }
    val content =
        when {
            event.isCompleted && categoryAccent == null -> TodoBlueContent
            event.isCompleted && categoryAccent != null -> contentColorForBackground(background)
            categoryAccent != null -> contentColorForBackground(categoryAccent)
            else -> CompletedBlueContent
        }

    return EventCardColors(background = background, content = content)
}

private fun WorkoutUi.eventDate(): LocalDate {
    return weekStartDate.plusDays((dayOfWeek?.value?.minus(1) ?: 0).toLong())
}

private fun lighterTone(
    color: Color,
    isDarkTheme: Boolean,
): Color {
    val blend = if (isDarkTheme) LIGHTER_TONE_BLEND_DARK else LIGHTER_TONE_BLEND_LIGHT
    return lerp(color, Color.White, blend)
}

@Composable
private fun countdownLabel(eventDate: LocalDate): String {
    val days = ChronoUnit.DAYS.between(LocalDate.now(), eventDate)
    return when (days) {
        0L -> stringResource(R.string.race_events_today)
        1L -> stringResource(R.string.race_events_tomorrow)
        in 2L..Long.MAX_VALUE ->
            pluralStringResource(R.plurals.race_events_days_left, days.toInt(), days.toInt())
        -1L -> pluralStringResource(R.plurals.race_events_days_ago, 1, 1)
        else ->
            pluralStringResource(
                R.plurals.race_events_days_ago,
                kotlin.math.abs(days).toInt(),
                kotlin.math.abs(days).toInt(),
            )
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    return date.format(formatter)
}
