@file:Suppress(
    "ArgumentListWrapping",
    "LongMethod",
    "MaximumLineLength",
    "MaxLineLength",
    "TooManyFunctions",
    "Wrapping",
    "ImportOrdering",
)

package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerField
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerOption
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.HermesSnackbar
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ChallengeCompletionIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ChallengeProgressBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuickAddValue
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeUiState
import com.rafaelfelipeac.hermes.features.challenges.presentation.model.ChallengeEditorDraft
import com.rafaelfelipeac.hermes.features.challenges.presentation.model.ChallengeEditorOrigin
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val CHALLENGES_ROUTE_LIST = "list"
private const val CHALLENGES_ROUTE_DETAIL = "detail"
internal const val CHALLENGES_TAG_ROOT = "challenges_root"
private const val CHALLENGES_TAG_ACTIVE_LIST = "challenges_active_list"
private const val CHALLENGES_TAG_ARCHIVED_LIST = "challenges_archived_list"
internal const val CHALLENGES_TAG_DETAIL = "challenges_detail"
private const val CHALLENGES_TAG_HEADER_BACK = "challenges_header_back"
private const val CHALLENGES_TAG_CREATE_FAB = "challenges_create_fab"
internal const val CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB = "challenges_detail_add_progress_fab"
internal const val CHALLENGES_TAG_DETAIL_QUICK_ADD = "challenges_detail_quick_add"
internal const val CHALLENGES_TAG_COMPLETION_CELEBRATION = "challenges_completion_celebration"
internal const val CHALLENGES_TAG_COMPLETION_CONFETTI = "challenges_completion_confetti"
private const val CHALLENGES_TAG_DETAIL_HISTORY = "challenges_detail_history"
internal const val CHALLENGES_TAG_ACTIVE_CARD_PROGRESS = "challenges_active_card_progress"
internal const val CHALLENGES_TAG_ACTIVE_EMPTY_STATE = "challenges_active_empty_state"
internal const val CHALLENGES_TAG_ARCHIVED_EMPTY_STATE = "challenges_archived_empty_state"
internal const val CHALLENGES_TAG_DETAIL_HISTORY_GROUP_PREFIX = "challenges_detail_history_group_"
private const val CHALLENGES_TAG_EDITOR = "challenges_editor"
private const val CHALLENGE_TAG_CONTAINER_ALPHA = 0.16f
private const val CHALLENGE_CONFETTI_CENTER_X = 0.5
private const val CHALLENGE_CONFETTI_CENTER_Y = 0.34
private const val CHALLENGE_CONFETTI_LEFT_ANGLE = 180
private const val CHALLENGE_CONFETTI_RIGHT_ANGLE = 0
private const val CHALLENGE_CONFETTI_SPREAD = 52
private const val CHALLENGE_CONFETTI_EMITTER_DURATION_MS = 250L
private const val CHALLENGE_CONFETTI_PARTICLE_COUNT = 42
private const val CHALLENGE_CONFETTI_VISIBLE_DURATION_MS = 2_000L
private val ChallengeProgressAheadColor = Color(0xFF2E7D32)
private val ChallengeProgressBehindColor = Color(0xFFC62828)

internal enum class ChallengeListTab {
    ACTIVE,
    ARCHIVED,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChallengesScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (ChallengeEditorDraft) -> Unit = {},
    pendingChallengeDraft: ChallengeEditorDraft? = null,
    onChallengeDraftConsumed: () -> Unit = {},
    viewModel: ChallengesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val undoState by viewModel.undoUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var route by rememberSaveable { mutableStateOf(CHALLENGES_ROUTE_LIST) }
    var selectedTab by rememberSaveable { mutableStateOf(ChallengeListTab.ACTIVE) }
    var detailOriginTab by rememberSaveable { mutableStateOf(ChallengeListTab.ACTIVE) }
    val pagerState =
        rememberPagerState(
            initialPage = selectedTab.ordinal,
            pageCount = { ChallengeListTab.entries.size },
        )
    var showEditorDialog by rememberSaveable { mutableStateOf(false) }
    var editorChallengeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    var progressDialogChallengeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var progressDialogEntryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var progressDialogQuantity by rememberSaveable { mutableStateOf("") }
    var progressDialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var progressDialogIsEdit by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialogForChallengeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteProgressDialogForEntryId by rememberSaveable { mutableStateOf<Long?>(null) }
    val undoChallengeMessage = stringResource(R.string.challenges_undo_deleted_challenge)
    val undoProgressMessage = stringResource(R.string.challenges_undo_deleted_progress_entry)
    val undoActionLabel = stringResource(R.string.weekly_training_undo_action)

    val onInternalBack = {
        when (route) {
            CHALLENGES_ROUTE_LIST -> onBack()
            CHALLENGES_ROUTE_DETAIL -> {
                route = CHALLENGES_ROUTE_LIST
                viewModel.selectChallenge(null)
                selectedTab = detailOriginTab
            }
        }
    }

    BackHandler(enabled = route != CHALLENGES_ROUTE_LIST || showEditorDialog) {
        if (showEditorDialog) {
            showEditorDialog = false
            editorChallengeId = null
        } else {
            onInternalBack()
        }
    }

    val openCreateChallenge = {
        editorChallengeId = null
        viewModel.beginCreateChallenge()
        showEditorDialog = true
    }

    val openEditChallenge: () -> Unit = openEditChallenge@{
        val selected = state.selectedChallenge ?: return@openEditChallenge
        editorChallengeId = selected.id
        viewModel.beginEditChallenge(selected.id)
        showEditorDialog = true
    }

    LaunchedEffect(pendingChallengeDraft) {
        val draft = pendingChallengeDraft ?: return@LaunchedEffect
        viewModel.restoreEditorState(draft.editorState)
        selectedTab = draft.originTab
        detailOriginTab = draft.originTab
        when (draft.origin) {
            ChallengeEditorOrigin.LIST -> route = CHALLENGES_ROUTE_LIST
            ChallengeEditorOrigin.DETAIL -> {
                route = CHALLENGES_ROUTE_DETAIL
                draft.editorState.challengeId?.let(viewModel::selectChallenge)
            }
        }
        editorChallengeId = draft.editorState.challengeId
        showEditorDialog = true
        onChallengeDraftConsumed()
    }

    val addProgressDefaultDate =
        state.selectedChallenge?.let { challenge ->
            challenge.endDate.coerceAtMost(LocalDate.now()).takeIf { !it.isBefore(challenge.startDate) }
        }

    val openAddProgressDialog: () -> Unit = openAddProgressDialog@{
        val challenge = state.selectedChallenge ?: return@openAddProgressDialog
        val defaultDate = addProgressDefaultDate ?: return@openAddProgressDialog
        progressDialogChallengeId = challenge.id
        progressDialogEntryId = null
        progressDialogQuantity = ""
        progressDialogDate = defaultDate
        progressDialogIsEdit = false
        showProgressDialog = true
    }

    val addQuickProgress: (ChallengeQuickAddValue) -> Unit = addQuickProgress@{ quickAdd ->
        val challenge = state.selectedChallenge ?: return@addQuickProgress
        val date = addProgressDefaultDate ?: return@addQuickProgress
        viewModel.addProgressEntry(
            challenge.id,
            ChallengeQuantity.format(quickAdd.quantity, Locale.getDefault()),
            date,
        )
    }

    LaunchedEffect(undoState?.id) {
        val undo = undoState ?: return@LaunchedEffect
        val result =
            snackbarHostState.showSnackbar(
                message =
                    when (undo.message) {
                        ChallengeUndoMessage.DeletedChallenge -> undoChallengeMessage
                        ChallengeUndoMessage.DeletedProgressEntry -> undoProgressMessage
                    },
                actionLabel = undoActionLabel,
                duration = SnackbarDuration.Short,
            )

        if (result == SnackbarResult.ActionPerformed) {
            viewModel.restoreUndo()
        } else {
            viewModel.clearUndo()
        }
    }

    LaunchedEffect(route, selectedTab) {
        if (route == CHALLENGES_ROUTE_LIST && pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }

    LaunchedEffect(route, pagerState.currentPage) {
        if (route == CHALLENGES_ROUTE_LIST) {
            selectedTab = ChallengeListTab.entries[pagerState.currentPage]
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag(CHALLENGES_TAG_ROOT),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            when {
                route == CHALLENGES_ROUTE_LIST && selectedTab == ChallengeListTab.ACTIVE -> {
                    FloatingActionButton(
                        onClick = openCreateChallenge,
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = SpacingXl).testTag(CHALLENGES_TAG_CREATE_FAB),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.challenges_create),
                        )
                    }
                }

                route == CHALLENGES_ROUTE_DETAIL &&
                    state.selectedChallenge?.lifecycle == ChallengeLifecycle.ACTIVE &&
                    addProgressDefaultDate != null -> {
                    FloatingActionButton(
                        onClick = openAddProgressDialog,
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = SpacingXl).testTag(CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.challenges_add_progress),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                HermesSnackbar(snackbarData = data)
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            ChallengesHeader(
                onBack = onInternalBack,
                title =
                    if (route == CHALLENGES_ROUTE_DETAIL) {
                        state.selectedChallenge?.title ?: stringResource(R.string.challenges_editor_title)
                    } else {
                        stringResource(R.string.challenges_title)
                    },
                trailingContent =
                    if (route == CHALLENGES_ROUTE_DETAIL && state.selectedChallenge != null) {
                        {
                            val selectedChallenge = state.selectedChallenge!!
                            ChallengeOverflowMenu(
                                onEdit =
                                    if (selectedChallenge.lifecycle == ChallengeLifecycle.ACTIVE) {
                                        openEditChallenge
                                    } else {
                                        null
                                    },
                                onArchive =
                                    if (selectedChallenge.lifecycle == ChallengeLifecycle.ACTIVE) {
                                        { viewModel.archiveChallenge(selectedChallenge.id) }
                                    } else {
                                        null
                                    },
                                onReactivate =
                                    if (selectedChallenge.lifecycle == ChallengeLifecycle.ARCHIVED) {
                                        { viewModel.reactivateChallenge(selectedChallenge.id) }
                                    } else {
                                        null
                                    },
                                onDelete = { showDeleteDialogForChallengeId = selectedChallenge.id },
                            )
                        }
                    } else {
                        null
                    },
            )

            when (route) {
                CHALLENGES_ROUTE_LIST -> {
                    ChallengesListRoute(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        selectedTab = selectedTab,
                        pagerState = pagerState,
                        state = state,
                        onSelectedTabChange = { tab ->
                            selectedTab = tab
                        },
                        onChallengeClick = { challenge ->
                            detailOriginTab = selectedTab
                            viewModel.selectChallenge(challenge.id)
                            route = CHALLENGES_ROUTE_DETAIL
                        },
                    )
                }

                CHALLENGES_ROUTE_DETAIL -> {
                    ChallengesDetailRoute(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        state = state,
                        onQuickAdd = addQuickProgress,
                        onRequestEditProgress = { entry ->
                            progressDialogChallengeId = entry.challengeId
                            progressDialogEntryId = entry.id
                            progressDialogQuantity = ChallengeQuantity.format(entry.quantity, Locale.getDefault())
                            progressDialogDate = entry.entryDate
                            progressDialogIsEdit = true
                            showProgressDialog = true
                        },
                        onDeleteProgress = { entryId -> showDeleteProgressDialogForEntryId = entryId },
                    )
                }
            }
        }
    }

    if (showEditorDialog) {
        ChallengesEditorDialog(
            editorState = state.editorState,
            categories = state.categories,
            validationMessage = state.editorState.validationMessage,
            onManageCategories = { editor ->
                showEditorDialog = false
                editorChallengeId = null
                onManageCategories(
                    ChallengeEditorDraft(
                        originTab = selectedTab,
                        origin =
                            if (route == CHALLENGES_ROUTE_DETAIL) {
                                ChallengeEditorOrigin.DETAIL
                            } else {
                                ChallengeEditorOrigin.LIST
                            },
                        editorState = editor,
                    ),
                )
            },
            onTitleChange = viewModel::updateEditorTitle,
            onDescriptionChange = viewModel::updateEditorDescription,
            onCategoryChange = viewModel::updateEditorCategory,
            onTargetTypeChange = viewModel::updateEditorTargetType,
            onTargetQuantityChange = viewModel::updateEditorTargetQuantity,
            onStartDateChange = viewModel::updateEditorStartDate,
            onEndDateChange = viewModel::updateEditorEndDate,
            onSave = {
                if (viewModel.saveEditorChallenge()) {
                    showEditorDialog = false
                    editorChallengeId = null
                }
            },
            onCancel = {
                showEditorDialog = false
                editorChallengeId = null
            },
        )
    }

    if (showProgressDialog && progressDialogDate != null) {
        val challengeId = progressDialogChallengeId ?: state.selectedChallenge?.id
        if (challengeId != null) {
            ChallengeProgressDialog(
                title =
                    if (progressDialogIsEdit) {
                        stringResource(R.string.challenges_edit_progress)
                    } else {
                        stringResource(R.string.challenges_add_progress)
                    },
                date = progressDialogDate!!,
                quantity = progressDialogQuantity,
                validationMessage = state.editorState.validationMessage,
                onDateChange = { progressDialogDate = it },
                onQuantityChange = { progressDialogQuantity = it },
                onDismiss = {
                    showProgressDialog = false
                    progressDialogChallengeId = null
                    progressDialogEntryId = null
                },
                onConfirm = {
                    val saved =
                        if (progressDialogIsEdit) {
                            progressDialogEntryId?.let { entryId ->
                                viewModel.updateProgressEntry(entryId, progressDialogQuantity, progressDialogDate!!)
                            } ?: false
                        } else {
                            viewModel.addProgressEntry(challengeId, progressDialogQuantity, progressDialogDate!!)
                        }
                    if (saved) {
                        showProgressDialog = false
                        progressDialogChallengeId = null
                        progressDialogEntryId = null
                    }
                },
            )
        }
    }

    if (showDeleteDialogForChallengeId != null) {
        val challengeId = showDeleteDialogForChallengeId!!
        val challenge =
            state.activeChallenges.firstOrNull { it.id == challengeId }
                ?: state.archivedChallenges.firstOrNull { it.id == challengeId }
        AlertDialog(
            onDismissRequest = { showDeleteDialogForChallengeId = null },
            title = { Text(text = stringResource(R.string.challenges_delete_title)) },
            text = {
                Text(
                    text =
                        if (challenge == null) {
                            stringResource(R.string.challenges_delete_message)
                        } else {
                            stringResource(R.string.challenges_delete_message_named, challenge.title)
                        },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChallenge(challengeId)
                        showDeleteDialogForChallengeId = null
                        if (route == CHALLENGES_ROUTE_DETAIL && state.selectedChallenge?.id == challengeId) {
                            route = CHALLENGES_ROUTE_LIST
                        }
                    },
                ) {
                    Text(text = stringResource(R.string.challenges_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogForChallengeId = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }

    if (showDeleteProgressDialogForEntryId != null) {
        val entryId = showDeleteProgressDialogForEntryId!!
        AlertDialog(
            onDismissRequest = { showDeleteProgressDialogForEntryId = null },
            title = { Text(text = stringResource(R.string.challenges_delete_progress_title)) },
            text = { Text(text = stringResource(R.string.challenges_delete_progress_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProgressEntry(entryId)
                        showDeleteProgressDialogForEntryId = null
                    },
                ) {
                    Text(text = stringResource(R.string.challenges_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProgressDialogForEntryId = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }
}

@Composable
private fun ChallengesHeader(
    onBack: () -> Unit,
    title: String,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SpacingSm,
                    end = SpacingXl,
                    top = SpacingSm,
                    bottom = SpacingSm,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag(CHALLENGES_TAG_HEADER_BACK),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.trophies_back),
            )
        }

        Text(
            text = title,
            style = typography.titleLarge,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        trailingContent?.invoke()
    }
}

@Composable
private fun ChallengesListRoute(
    modifier: Modifier,
    selectedTab: ChallengeListTab,
    pagerState: androidx.compose.foundation.pager.PagerState,
    state: ChallengeUiState,
    onSelectedTabChange: (ChallengeListTab) -> Unit,
    onChallengeClick: (Challenge) -> Unit,
) {
    val activeChallenges =
        remember(state.activeChallenges) {
            state.activeChallenges.sortedByDescending { it.updatedAt }
        }
    val archivedChallenges =
        remember(state.archivedChallenges) {
            state.archivedChallenges.sortedByDescending { it.updatedAt }
        }
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == ChallengeListTab.ACTIVE,
                    onClick = { onSelectedTabChange(ChallengeListTab.ACTIVE) },
                    selectedContentColor = colorScheme.primary,
                    unselectedContentColor = colorScheme.onSurfaceVariant,
                    text = { Text(text = stringResource(R.string.challenges_active_title)) },
                )
                Tab(
                    selected = selectedTab == ChallengeListTab.ARCHIVED,
                    onClick = { onSelectedTabChange(ChallengeListTab.ARCHIVED) },
                    selectedContentColor = colorScheme.primary,
                    unselectedContentColor = colorScheme.onSurfaceVariant,
                    text = { Text(text = stringResource(R.string.challenges_archived_title)) },
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                when (ChallengeListTab.entries[page]) {
                    ChallengeListTab.ACTIVE -> {
                        if (activeChallenges.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ACTIVE_LIST),
                                contentPadding =
                                    PaddingValues(
                                        start = SpacingXl,
                                        top = SpacingMd,
                                        end = SpacingXl,
                                        bottom = FloatingActionContentBottomPadding,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                            ) {
                                items(activeChallenges, key = { it.id }) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        category = state.categories.firstOrNull { it.id == challenge.categoryId },
                                        calculation = state.challengeCalculations[challenge.id],
                                        onClick = { onChallengeClick(challenge) },
                                    )
                                }
                            }
                        }
                    }

                    ChallengeListTab.ARCHIVED -> {
                        if (archivedChallenges.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ARCHIVED_LIST),
                                contentPadding =
                                    PaddingValues(
                                        start = SpacingXl,
                                        top = SpacingMd,
                                        end = SpacingXl,
                                        bottom = FloatingActionContentBottomPadding,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                            ) {
                                items(archivedChallenges, key = { it.id }) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        category = state.categories.firstOrNull { it.id == challenge.categoryId },
                                        calculation = state.challengeCalculations[challenge.id],
                                        onClick = { onChallengeClick(challenge) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedTab == ChallengeListTab.ACTIVE && activeChallenges.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.TrackChanges,
                title = stringResource(R.string.challenges_empty_active_title),
                body = stringResource(R.string.challenges_empty_active_body),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SpacingXl)
                        .testTag(CHALLENGES_TAG_ACTIVE_EMPTY_STATE),
                actionContent = null,
            )
        }

        if (selectedTab == ChallengeListTab.ARCHIVED && archivedChallenges.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Archive,
                title = stringResource(R.string.challenges_empty_archived_title),
                body = stringResource(R.string.challenges_empty_archived_body),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SpacingXl)
                        .testTag(CHALLENGES_TAG_ARCHIVED_EMPTY_STATE),
            )
        }
    }
}

@Composable
private fun ChallengesDetailRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onQuickAdd: (ChallengeQuickAddValue) -> Unit,
    onRequestEditProgress: (ChallengeProgressEntry) -> Unit,
    onDeleteProgress: (Long) -> Unit,
) {
    val challenge = state.selectedChallenge
    val calculation = state.calculation
    if (challenge == null || calculation == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(SpacingXl).testTag(CHALLENGES_TAG_DETAIL),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateCard(
                icon = Icons.Outlined.TrackChanges,
                title = stringResource(R.string.challenges_empty_detail_title),
                body = stringResource(R.string.challenges_empty_detail_body),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    val groupedProgressEntries = remember(state.progressEntries) { groupProgressByDate(state.progressEntries) }
    val category = state.categories.firstOrNull { it.id == challenge.categoryId }
    val addProgressDefaultDate =
        challenge.endDate.coerceAtMost(LocalDate.now()).takeIf { !it.isBefore(challenge.startDate) }
    val quickAddBase =
        when (challenge.targetType) {
            ChallengeTargetType.DAILY -> challenge.targetQuantity
            ChallengeTargetType.TOTAL -> initialAveragePace(challenge)
        }
    val quickAdds =
        if (challenge.lifecycle == ChallengeLifecycle.ACTIVE && addProgressDefaultDate != null && quickAddBase > 0L) {
            ChallengeQuantity.quickAddValues(quickAddBase)
        } else {
            emptyList()
        }
    val isComplete = calculation.status == ChallengeStatus.COMPLETED || calculation.status == ChallengeStatus.EXCEEDED
    var hadCompletion by remember(challenge.id) { mutableStateOf(isComplete) }
    var completionBurstKey by remember(challenge.id) { mutableIntStateOf(0) }
    LaunchedEffect(isComplete) {
        if (isComplete && !hadCompletion) completionBurstKey++
        hadCompletion = isComplete
    }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_DETAIL),
            contentPadding =
                PaddingValues(
                    start = SpacingXl,
                    top = SpacingXs,
                    end = SpacingXl,
                    bottom = FloatingActionContentBottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            item {
                ChallengeDetailSummaryCard(
                    challenge = challenge,
                    category = category,
                    calculation = calculation,
                )
            }

            if (isComplete) {
                item {
                    ChallengeCompletionHero(calculation = calculation)
                }
            }

            item {
                ChallengeTodayCard(
                    calculation = calculation,
                )
            }

            if (quickAdds.isNotEmpty()) {
                item {
                    ChallengeQuickAddCard(
                        quickAdds = quickAdds,
                        onQuickAdd = onQuickAdd,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.challenges_history_title),
                        style = typography.titleMedium,
                        modifier = Modifier.testTag(CHALLENGES_TAG_DETAIL_HISTORY),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (groupedProgressEntries.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.TrackChanges,
                        title = stringResource(R.string.challenges_history_empty_title),
                        body = stringResource(R.string.challenges_history_empty_body),
                        modifier = Modifier.fillMaxWidth().padding(top = SpacingSm),
                    )
                }
            } else {
                items(groupedProgressEntries, key = { it.date }) { group ->
                    ChallengeProgressHistoryCard(
                        group = group,
                        isEditable = challenge.lifecycle == ChallengeLifecycle.ACTIVE,
                        onRequestEditProgress = onRequestEditProgress,
                        onDeleteProgress = onDeleteProgress,
                    )
                }
            }
        }
        ChallengeCompletionConfetti(
            burstKey = completionBurstKey,
            category = category,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ChallengeProgressHistoryCard(
    group: ChallengeProgressDateGroup,
    isEditable: Boolean,
    onRequestEditProgress: (ChallengeProgressEntry) -> Unit,
    onDeleteProgress: (Long) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(challengeHistoryGroupTag(group.date)),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatWorkoutDate(group.date, Locale.getDefault()),
                    style = typography.titleSmall,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text =
                        stringResource(
                            R.string.challenges_history_day_completed,
                            ChallengeQuantity.format(group.completedQuantity, Locale.getDefault()),
                        ),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            group.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider()
                }
                ChallengeProgressEntryRow(
                    entry = entry,
                    isEditable = isEditable,
                    onEdit = { onRequestEditProgress(entry) },
                    onDelete = { onDeleteProgress(entry.id) },
                )
            }
        }
    }
}

@Composable
private fun ChallengeCompletionHero(calculation: ChallengeCalculationResult) {
    val isExceeded = calculation.status == ChallengeStatus.EXCEEDED
    Card(
        modifier = Modifier.fillMaxWidth().testTag(CHALLENGES_TAG_COMPLETION_CELEBRATION),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(ChallengeCompletionIconSize),
                )
                Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                    Text(
                        text =
                            stringResource(
                                if (isExceeded) {
                                    R.string.challenges_completion_exceeded_title
                                } else {
                                    R.string.challenges_completion_title
                                },
                            ),
                        style = typography.titleMedium,
                        color = colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.challenges_completion_summary,
                                ChallengeQuantity.format(calculation.completedTotal, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.plannedTotal, Locale.getDefault()),
                            ),
                        style = typography.bodyMedium,
                        color = colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeCompletionConfetti(
    burstKey: Int,
    category: Category?,
    modifier: Modifier = Modifier,
) {
    var parties by remember { mutableStateOf(emptyList<Party>()) }
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val categoryAccent = category?.let { categoryAccentColor(it.colorId) }
    val palette =
        listOf(
            (categoryAccent ?: colorScheme.primary).toArgb(),
            colorScheme.primary.toArgb(),
            colorScheme.secondary.toArgb(),
            colorScheme.tertiary.toArgb(),
        ).distinct()
    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
        parties =
            listOf(
                Party(
                    angle = CHALLENGE_CONFETTI_LEFT_ANGLE,
                    spread = CHALLENGE_CONFETTI_SPREAD,
                    colors = palette,
                    position = Position.Relative(CHALLENGE_CONFETTI_CENTER_X, CHALLENGE_CONFETTI_CENTER_Y),
                    emitter =
                        Emitter(
                            duration = CHALLENGE_CONFETTI_EMITTER_DURATION_MS,
                            TimeUnit.MILLISECONDS,
                        ).max(CHALLENGE_CONFETTI_PARTICLE_COUNT),
                ),
                Party(
                    angle = CHALLENGE_CONFETTI_RIGHT_ANGLE,
                    spread = CHALLENGE_CONFETTI_SPREAD,
                    colors = palette,
                    position = Position.Relative(CHALLENGE_CONFETTI_CENTER_X, CHALLENGE_CONFETTI_CENTER_Y),
                    emitter =
                        Emitter(
                            duration = CHALLENGE_CONFETTI_EMITTER_DURATION_MS,
                            TimeUnit.MILLISECONDS,
                        ).max(CHALLENGE_CONFETTI_PARTICLE_COUNT),
                ),
            )
        kotlinx.coroutines.delay(CHALLENGE_CONFETTI_VISIBLE_DURATION_MS)
        parties = emptyList()
    }
    if (parties.isNotEmpty()) {
        KonfettiView(
            modifier = modifier.testTag(CHALLENGES_TAG_COMPLETION_CONFETTI),
            parties = parties,
        )
    }
}

@Composable
internal fun ChallengesEditorDialog(
    editorState: ChallengeEditorState,
    categories: List<Category>,
    validationMessage: String?,
    onManageCategories: (ChallengeEditorState) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTargetTypeChange: (ChallengeTargetType) -> Unit,
    onTargetQuantityChange: (String) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val dialogKey = editorState.challengeId ?: -1L
    var showStartDatePicker by rememberSaveable(dialogKey) { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable(dialogKey) { mutableStateOf(false) }
    val currentLocale = Locale.getDefault()
    val startDate = editorState.startDate
    val endDate = editorState.endDate
    val visibleCategories =
        remember(categories, editorState.categoryId) {
            categories
                .filter { !it.isHidden || it.id == editorState.categoryId }
                .map { category ->
                    CategoryPickerOption(
                        id = category.id,
                        name = category.name,
                        colorId = category.colorId,
                    )
                }
        }
    val inclusiveDays =
        startDate?.let { start ->
            endDate?.let { end ->
                if (end.isBefore(start)) null else (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1)
            }
        }
    val previewText =
        remember(editorState.targetType, editorState.targetQuantityText, startDate, endDate) {
            val quantity = ChallengeQuantity.parseLocalized(editorState.targetQuantityText, currentLocale)
            if (quantity == null || inclusiveDays == null) {
                null
            } else {
                val planned =
                    when (editorState.targetType) {
                        ChallengeTargetType.DAILY -> runCatching { ChallengeQuantity.multiply(quantity, inclusiveDays) }.getOrNull()
                        ChallengeTargetType.TOTAL -> quantity
                    }
                planned?.let { ChallengeQuantity.format(it, currentLocale) }
            }
        }

    AlertDialog(
        modifier = Modifier.testTag(CHALLENGES_TAG_EDITOR),
        onDismissRequest = onCancel,
        title = {
            Text(
                text =
                    if (editorState.challengeId == null) {
                        stringResource(R.string.challenges_create)
                    } else {
                        stringResource(R.string.challenges_editor_title)
                    },
            )
        },
        text = {
            KeyboardAwareDialogForm {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    validationMessage?.let { Text(text = it, color = colorScheme.error) }

                    OutlinedTextField(
                        value = editorState.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_title)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )

                    OutlinedTextField(
                        value = editorState.description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_description)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )

                    CategoryPickerField(
                        label = stringResource(R.string.challenges_field_category),
                        categories = visibleCategories,
                        selectedCategoryId = editorState.categoryId,
                        onCategorySelected = onCategoryChange,
                        onManageCategories = { onManageCategories(editorState) },
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ChallengeTargetType.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = editorState.targetType == option,
                                onClick = { onTargetTypeChange(option) },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index,
                                        ChallengeTargetType.entries.size,
                                    ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = challengeTargetTypeLabel(option))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editorState.targetQuantityText,
                        onValueChange = onTargetQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_target_quantity)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )

                    if (inclusiveDays != null && previewText != null) {
                        Text(
                            text =
                                when (editorState.targetType) {
                                    ChallengeTargetType.DAILY ->
                                        stringResource(
                                            R.string.challenges_daily_preview,
                                            ChallengeQuantity.format(
                                                ChallengeQuantity.parseLocalized(editorState.targetQuantityText, currentLocale)
                                                    ?: 0L,
                                                currentLocale,
                                            ),
                                            inclusiveDays,
                                            previewText,
                                        )
                                    ChallengeTargetType.TOTAL ->
                                        stringResource(
                                            R.string.challenges_total_preview,
                                            previewText,
                                        )
                                },
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd), modifier = Modifier.fillMaxWidth()) {
                        ChallengeDialogDateField(
                            label = stringResource(R.string.challenges_field_start_date),
                            date = startDate,
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                        ChallengeDialogDateField(
                            label = stringResource(R.string.challenges_field_end_date),
                            date = endDate,
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )

    if (showStartDatePicker) {
        ChallengeDatePickerDialog(
            date = editorState.startDate ?: LocalDate.now(),
            onDateSelected = onStartDateChange,
            onDismiss = { showStartDatePicker = false },
        )
    }

    if (showEndDatePicker) {
        ChallengeDatePickerDialog(
            date = editorState.endDate ?: editorState.startDate ?: LocalDate.now(),
            onDateSelected = onEndDateChange,
            onDismiss = { showEndDatePicker = false },
        )
    }
}

@Composable
private fun ChallengeDetailSummaryCard(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            ChallengeSummaryContent(
                challenge = challenge,
                category = category,
                calculation = calculation,
                showProgressBar = true,
            )
        }
    }
}

@Composable
private fun ChallengeTodayCard(calculation: ChallengeCalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(
                text = stringResource(R.string.challenges_today_label),
                style = typography.titleMedium,
            )
            Text(
                text =
                    when {
                        calculation.todayTarget != null ->
                            stringResource(
                                R.string.challenges_today_value_daily,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayRemaining ?: 0L, Locale.getDefault()),
                            )
                        else ->
                            stringResource(
                                R.string.challenges_today_value_total,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayRemaining ?: 0L, Locale.getDefault()),
                            )
                    },
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_today_completed_label),
                        value = ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_today_remaining_label),
                        value =
                            ChallengeQuantity.format(
                                calculation.todayRemaining ?: 0L,
                                Locale.getDefault(),
                            ),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_required_pace_label),
                        value = ChallengeQuantity.format(calculation.requiredPace, Locale.getDefault()),
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                    )
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_debt_label),
                        value = ChallengeQuantity.format(calculation.carriedDebt, Locale.getDefault()),
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeQuickAddCard(
    quickAdds: List<ChallengeQuickAddValue>,
    onQuickAdd: (ChallengeQuickAddValue) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(CHALLENGES_TAG_DETAIL_QUICK_ADD),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(
                text = stringResource(R.string.challenges_quick_add_title),
                style = typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                quickAdds.forEach { quickAdd ->
                    FilledTonalButton(
                        onClick = { onQuickAdd(quickAdd) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string.challenges_quick_add_button,
                                    ChallengeQuantity.format(quickAdd.quantity, Locale.getDefault()),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeTodayMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
) {
    Card(
        modifier = modifier,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = label,
                style = typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = typography.titleMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChallengeProgressEntryRow(
    entry: ChallengeProgressEntry,
    isEditable: Boolean,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            Text(text = ChallengeQuantity.format(entry.quantity, Locale.getDefault()))
        }

        if (isEditable && (onEdit != null || onDelete != null)) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.challenges_progress_entry_actions_menu),
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    onEdit?.let {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.challenges_edit)) },
                            onClick = {
                                expanded = false
                                it()
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            },
                        )
                    }
                    onDelete?.let {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.challenges_delete)) },
                            onClick = {
                                expanded = false
                                it()
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            ChallengeSummaryContent(
                challenge = challenge,
                category = category,
                calculation = calculation,
                showProgressBar = challenge.lifecycle == ChallengeLifecycle.ACTIVE,
                progressBarModifier = Modifier.testTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS),
            )
        }
    }
}

@Composable
private fun ChallengeSummaryContent(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult?,
    showProgressBar: Boolean,
    progressBarModifier: Modifier = Modifier,
) {
    val categoryAccent = category?.let { categoryAccentColor(it.colorId) }

    Text(
        text = challenge.title,
        style = typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
        verticalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        TitleChip(
            label = challengeTargetTypeLabel(challenge.targetType),
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
        )
        category?.let {
            TitleChip(
                label = it.name,
                containerColor = categoryAccent ?: colorScheme.surfaceVariant,
                contentColor = categoryAccent?.let { accent -> contentColorForBackground(accent) } ?: colorScheme.onSurfaceVariant,
            )
        }
        calculation?.let { calculationResult ->
            TitleChip(
                label = challengeStatusLabel(calculationResult.status),
                containerColor = challengeProgressContainerColor(calculationResult.status),
                contentColor = challengeProgressColor(calculationResult.status),
            )
        }
    }

    challenge.description?.takeIf { it.isNotBlank() }?.let { description ->
        Text(
            text = description,
            color = colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }

    Text(
        text =
            stringResource(
                R.string.challenges_date_range,
                formatWorkoutDate(challenge.startDate, Locale.getDefault()),
                formatWorkoutDate(challenge.endDate, Locale.getDefault()),
            ),
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
    )

    calculation?.let { calculationResult ->
        if (showProgressBar) {
            ChallengeProgressBar(
                progress = calculationResult.visualProgress,
                color = challengeProgressColor(calculationResult.status),
                modifier = progressBarModifier,
            )
        }

        Text(
            text =
                challengeProgressLabel(calculationResult),
            modifier = Modifier.fillMaxWidth(),
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun challengeProgressLabel(calculation: ChallengeCalculationResult): String {
    val locale = Locale.getDefault()
    val progressValue =
        stringResource(
            R.string.challenges_progress_value,
            ChallengeQuantity.format(calculation.completedTotal, locale),
            ChallengeQuantity.format(calculation.plannedTotal, locale),
        )
    val progressPercent =
        stringResource(
            R.string.challenges_progress_percent,
            challengeProgressPercent(calculation),
        )
    return stringResource(
        R.string.challenges_progress_value_with_percent,
        progressValue,
        progressPercent,
    )
}

private fun challengeProgressPercent(calculation: ChallengeCalculationResult): String {
    if (calculation.plannedTotal <= 0L) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(0.0)
    }
    val exactPercent = calculation.completedTotal.toDouble() / calculation.plannedTotal.toDouble() * 100.0
    val roundedPercent = (exactPercent * 10.0).roundToInt() / 10.0
    val displayPercent =
        if (calculation.completedTotal < calculation.plannedTotal && roundedPercent >= 100.0) {
            99.9
        } else {
            roundedPercent
        }
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(displayPercent)
}

@Composable
private fun ChallengeProgressBar(
    progress: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChallengeProgressBarHeight)
                .clip(shapes.small)
                .background(colorScheme.surfaceVariant),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color),
        )
    }
}

@Composable
private fun ChallengeOverflowMenu(
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
    onReactivate: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hasActions = onEdit != null || onArchive != null || onReactivate != null || onDelete != null
    if (!hasActions) return

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.challenges_actions_menu),
            )
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (onEdit != null) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_edit)) },
                    onClick = {
                        expanded = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    },
                )
            }
            if (onArchive != null) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_archive)) },
                    onClick = {
                        expanded = false
                        onArchive()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Unarchive, contentDescription = null)
                    },
                )
            }
            if (onReactivate != null) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_reactivate)) },
                    onClick = {
                        expanded = false
                        onReactivate()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                    },
                )
            }
            if (onDelete != null) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_delete)) },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChallengeDialogDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = date?.let { formatWorkoutDate(it, Locale.getDefault()) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

@Composable
private fun ChallengeProgressDialog(
    title: String,
    date: LocalDate,
    quantity: String,
    validationMessage: String?,
    onDateChange: (LocalDate) -> Unit,
    onQuantityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            KeyboardAwareDialogForm {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    validationMessage?.let { message ->
                        Text(text = message, color = colorScheme.error)
                    }
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = onQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_progress_quantity)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )
                    ChallengeDialogDateField(
                        label = stringResource(R.string.challenges_field_progress_date),
                        date = date,
                        onClick = { showDatePicker = true },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )

    if (showDatePicker) {
        ChallengeDatePickerDialog(
            date = date,
            onDateSelected = onDateChange,
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeDatePickerDialog(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedDateMillis = remember(date) { date.toUtcEpochMillis() }
    val datePickerState =
        remember(selectedDateMillis) {
            DatePickerState(
                locale = Locale.getDefault(),
                initialSelectedDateMillis = selectedDateMillis,
            )
        }

    HermesDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it.toUtcLocalDate()) }
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

private data class ChallengeProgressDateGroup(
    val date: LocalDate,
    val entries: List<ChallengeProgressEntry>,
    val completedQuantity: Long,
)

private fun groupProgressByDate(entries: List<ChallengeProgressEntry>): List<ChallengeProgressDateGroup> {
    return entries
        .groupBy { it.entryDate }
        .toSortedMap(compareByDescending { it })
        .map { (date, dayEntries) ->
            ChallengeProgressDateGroup(
                date = date,
                entries = dayEntries.sortedWith(compareByDescending<ChallengeProgressEntry> { it.occurredAt }.thenByDescending { it.id }),
                completedQuantity = dayEntries.fold(0L) { total, entry -> ChallengeQuantity.add(total, entry.quantity) },
            )
        }
}

private fun challengeHistoryGroupTag(date: LocalDate): String = "$CHALLENGES_TAG_DETAIL_HISTORY_GROUP_PREFIX$date"

private fun initialAveragePace(challenge: Challenge): Long {
    val totalDays = (ChronoUnit.DAYS.between(challenge.startDate, challenge.endDate) + 1).coerceAtLeast(1L)
    return ChallengeQuantity.ceilDiv(challenge.targetQuantity, totalDays)
}

@Composable
private fun challengeTargetTypeLabel(targetType: ChallengeTargetType): String {
    return when (targetType) {
        ChallengeTargetType.DAILY -> stringResource(R.string.challenge_target_type_daily)
        ChallengeTargetType.TOTAL -> stringResource(R.string.challenge_target_type_total)
    }
}

@Composable
private fun challengeStatusLabel(status: ChallengeStatus): String {
    return stringResource(
        when (status) {
            ChallengeStatus.NOT_STARTED -> R.string.challenges_status_not_started
            ChallengeStatus.EXCEEDED -> R.string.challenges_status_exceeded
            ChallengeStatus.COMPLETED -> R.string.challenges_status_completed
            ChallengeStatus.EXPIRED_INCOMPLETE -> R.string.challenges_status_expired
            ChallengeStatus.AHEAD -> R.string.challenges_status_ahead
            ChallengeStatus.ON_TRACK -> R.string.challenges_status_on_track
            ChallengeStatus.BEHIND -> R.string.challenges_status_behind
        },
    )
}

@Composable
private fun challengeProgressColor(status: ChallengeStatus): Color {
    return when (status) {
        ChallengeStatus.AHEAD,
        ChallengeStatus.ON_TRACK,
        ChallengeStatus.COMPLETED,
        ChallengeStatus.EXCEEDED,
        -> ChallengeProgressAheadColor

        ChallengeStatus.BEHIND,
        ChallengeStatus.EXPIRED_INCOMPLETE,
        -> ChallengeProgressBehindColor

        ChallengeStatus.NOT_STARTED -> colorScheme.primary
    }
}

@Composable
private fun challengeProgressContainerColor(status: ChallengeStatus): Color {
    return challengeProgressColor(status).copy(alpha = CHALLENGE_TAG_CONTAINER_ALPHA)
}
