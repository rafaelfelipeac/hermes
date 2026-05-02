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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
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
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventCardFooterHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventCardHeight
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
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.undoSnackbarMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val TYPE_CHIP_ALPHA = 0.18f
private const val EVENT_CARD_TEXT_LINES = 2
private const val EVENT_GRID_COLUMNS = 2
internal const val EVENT_CARD_TAG_PREFIX = "event-card-"

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (EventDialogDraft) -> Unit = {},
    pendingEventDraft: EventDialogDraft? = null,
    onEventDraftConsumed: () -> Unit = {},
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val undoState by viewModel.undoUiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var editingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftDescription by rememberSaveable { mutableStateOf("") }
    var draftCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var deletingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftConsumedLocally by remember { mutableStateOf(false) }
    val undoLabel = stringResource(R.string.weekly_training_undo_action)

    val editingEvent = editingEventId?.let { id -> state.events.firstOrNull { it.id == id } }
    val deletingEvent = deletingEventId?.let { id -> state.events.firstOrNull { it.id == id } }
    val undoMessage =
        undoState?.let { currentUndo ->
            undoSnackbarMessage(
                message = currentUndo.message,
                eventType = RACE_EVENT,
            )
        }

    LaunchedEffect(undoState?.id) {
        if (undoMessage != null) {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result =
                snackbarHostState.showSnackbar(
                    message = undoMessage,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Indefinite,
                )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastAction()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    LaunchedEffect(undoState) {
        if (undoState == null) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message =
                    when (message) {
                        is EventsMessage.Created ->
                            context.getString(R.string.activity_action_create_race_event, message.title)
                        is EventsMessage.Updated ->
                            context.getString(R.string.activity_action_update_race_event, message.title)
                    },
                duration = SnackbarDuration.Short,
            )
        }
    }

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    actionColor = colorScheme.primary,
                )
            }
        },
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

        EventsContent(
            state = state,
            modifier = modifier,
            contentPadding = contentPadding,
            onEditEvent = ::openEditDialog,
            onToggleCompleted = { eventId, checked ->
                viewModel.updateRaceEventCompletion(
                    eventId = eventId,
                    isCompleted = checked,
                )
            },
            onDeleteEvent = { eventId -> deletingEventId = eventId },
        )
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

    deletingEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { deletingEventId = null },
            title = { Text(text = stringResource(R.string.weekly_training_delete_race_event_title)) },
            text = { Text(text = stringResource(R.string.weekly_training_delete_race_event_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRaceEvent(event.id)
                        deletingEventId = null
                    },
                ) {
                    Text(text = stringResource(R.string.weekly_training_delete_race_event))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEventId = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }
}

@Composable
internal fun EventsContent(
    state: EventsUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onEditEvent: (WorkoutUi) -> Unit,
    onToggleCompleted: (eventId: Long, isCompleted: Boolean) -> Unit,
    onDeleteEvent: (eventId: Long) -> Unit,
) {
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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(contentPadding),
    ) {
        if (upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = SpacingXl, end = SpacingXl, bottom = SpacingXl),
            ) {
                EventsHeader(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(top = SpacingXl),
                )

                EventsEmptyState(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(EVENT_GRID_COLUMNS),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = SpacingXl, top = Zero, end = SpacingXl, bottom = SpacingXl),
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EventsHeader(
                        modifier = Modifier.padding(top = SpacingXl),
                    )
                }

                if (upcomingEvents.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EventsSectionTitle(
                            title = stringResource(R.string.race_events_upcoming_title),
                            modifier = Modifier.padding(top = SpacingLg),
                        )
                    }
                }

                items(upcomingEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEditEvent(event) },
                        onToggleCompleted = { checked -> onToggleCompleted(event.id, checked) },
                        onDelete = { onDeleteEvent(event.id) },
                    )
                }

                if (pastEvents.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EventsSectionTitle(
                            title = stringResource(R.string.race_events_past_title),
                            modifier = Modifier.padding(top = SpacingLg),
                        )
                    }
                }

                items(pastEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEditEvent(event) },
                        onToggleCompleted = { checked -> onToggleCompleted(event.id, checked) },
                        onDelete = { onDeleteEvent(event.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EventsHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
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

@Composable
private fun EventsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        HorizontalDivider(color = colorScheme.outlineVariant)

        Text(
            text = title,
            style = typography.titleMedium,
            color = colorScheme.onSurface,
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
    val frameColor = if (event.isCompleted) colors.background else categoryAccent

    Card(
        onClick = onClick,
        shape = shapes.medium,
        border = BorderStroke(BorderHairline, frameColor ?: colorScheme.outlineVariant),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.background,
                contentColor = colors.content,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .height(EventCardHeight)
                .testTag(EVENT_CARD_TAG_PREFIX + event.id),
    ) {
        Box {
            if (frameColor != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(SpacingXs)
                            .background(frameColor),
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ContentPadding,
                                top = ContentPadding,
                                end = ContentPadding,
                            )
                            .padding(end = SpacingXl),
                    horizontalArrangement = Arrangement.spacedBy(SpacingLg),
                    verticalAlignment = Alignment.CenterVertically,
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

                    TitleChip(
                        label = categoryLabel,
                        containerColor = categoryChipBackground ?: colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                        contentColor = categoryChipContent,
                        modifier = Modifier.wrapContentWidth(),
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = ContentPadding,
                                    top = SpacingXs,
                                    end = ContentPadding,
                                    bottom = SpacingXs,
                                ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(SpacingXs),
                        ) {
                            Text(
                                text = event.type,
                                style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            if (event.description.isNotBlank()) {
                                Text(
                                    text = event.description,
                                    style = typography.bodySmall,
                                    color = colors.content.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = dateLabel,
                            style = typography.bodySmall,
                            color = colors.content.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.35f),
                    thickness = BorderThin,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ContentPadding, vertical = SpacingSm)
                            .height(EventCardFooterHeight),
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
    return when {
        days <= 0 -> stringResource(R.string.race_events_today)
        days == 1L -> stringResource(R.string.race_events_tomorrow)
        days > 1L ->
            pluralStringResource(R.plurals.race_events_days_left, days.toInt(), days.toInt())
        days == -1L -> pluralStringResource(R.plurals.race_events_days_ago, 1, 1)
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
    return formatter.format(date)
}

private fun isDarkBackground(color: Color): Boolean {
    return color.luminance() < 0.5f
}

private fun categoryAccentColor(colorId: String): Color? {
    return when (colorId) {
        "blue" -> TodoBlue
        "green" -> Color(0xFF4CAF50)
        "red" -> Color(0xFFF44336)
        "yellow" -> Color(0xFFFFC107)
        else -> null
    }
}

private fun baseCategoryColor(color: Color): Color = color

private fun contentColorForBackground(color: Color): Color = if (color.luminance() > 0.5f) Color.Black else Color.White

private fun completedCategoryColor(
    accent: Color,
    isDarkTheme: Boolean,
    surface: Color,
): Color {
    return if (isDarkTheme) {
        lerp(accent, surface, 0.25f)
    } else {
        lerp(accent, surface, 0.15f)
    }
}
