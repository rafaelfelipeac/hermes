@file:Suppress(
    "ArgumentListWrapping",
    "LongMethod",
    "MaximumLineLength",
    "MaxLineLength",
    "TooManyFunctions",
    "Wrapping",
)

package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
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
import java.time.LocalDate
import java.util.Locale

private const val CHALLENGES_ROUTE_LIST = "list"
private const val CHALLENGES_ROUTE_DETAIL = "detail"
private const val CHALLENGES_ROUTE_HISTORY = "history"
private const val CHALLENGES_TAG_ROOT = "challenges_root"
private const val CHALLENGES_TAG_ACTIVE_LIST = "challenges_active_list"
private const val CHALLENGES_TAG_ARCHIVED_LIST = "challenges_archived_list"
private const val CHALLENGES_TAG_DETAIL = "challenges_detail"
private const val CHALLENGES_TAG_HISTORY = "challenges_history"
private const val CHALLENGES_TAG_HEADER_BACK = "challenges_header_back"
private const val CHALLENGES_TAG_CREATE_FAB = "challenges_create_fab"
private const val CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB = "challenges_detail_add_progress_fab"
private const val CHALLENGES_TAG_DETAIL_HISTORY = "challenges_detail_history"
private const val CHALLENGES_TAG_EDITOR = "challenges_editor"
internal const val CHALLENGES_TAG_ADD_PROGRESS_BUTTON = "challenges_add_progress_button"

private enum class ChallengeListTab {
    ACTIVE,
    ARCHIVED,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    modifier: Modifier = Modifier,
    viewModel: ChallengesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val undoState by viewModel.undoUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var route by rememberSaveable { mutableStateOf(CHALLENGES_ROUTE_LIST) }
    var selectedTab by rememberSaveable { mutableStateOf(ChallengeListTab.ACTIVE) }
    var detailOriginTab by rememberSaveable { mutableStateOf(ChallengeListTab.ACTIVE) }
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
            CHALLENGES_ROUTE_HISTORY -> {
                route = CHALLENGES_ROUTE_DETAIL
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

    val openHistory = {
        route = CHALLENGES_ROUTE_HISTORY
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
                Snackbar(snackbarData = data)
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
            )

            when (route) {
                CHALLENGES_ROUTE_LIST -> {
                    ChallengesListRoute(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        selectedTab = selectedTab,
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
                        onEdit = openEditChallenge,
                        onOpenHistory = openHistory,
                        onArchive = { challengeId -> viewModel.archiveChallenge(challengeId) },
                        onReactivate = { challengeId -> viewModel.reactivateChallenge(challengeId) },
                        onDelete = { challengeId -> showDeleteDialogForChallengeId = challengeId },
                        onQuickAdd = addQuickProgress,
                        onRequestAddProgress = openAddProgressDialog,
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

                CHALLENGES_ROUTE_HISTORY -> {
                    ChallengesHistoryRoute(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        state = state,
                        onEditChallenge = openEditChallenge,
                        onDeleteChallenge = { challengeId -> showDeleteDialogForChallengeId = challengeId },
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
            validationMessage = state.editorState.validationMessage,
            onTitleChange = viewModel::updateEditorTitle,
            onDescriptionChange = viewModel::updateEditorDescription,
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
private fun ChallengesHeader(onBack: () -> Unit) {
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
            text = stringResource(R.string.challenges_title),
            style = typography.titleLarge,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChallengesListRoute(
    modifier: Modifier,
    selectedTab: ChallengeListTab,
    state: ChallengeUiState,
    onSelectedTabChange: (ChallengeListTab) -> Unit,
    onChallengeClick: (Challenge) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == ChallengeListTab.ACTIVE,
                onClick = { onSelectedTabChange(ChallengeListTab.ACTIVE) },
                text = { Text(text = stringResource(R.string.challenges_active_title)) },
            )
            Tab(
                selected = selectedTab == ChallengeListTab.ARCHIVED,
                onClick = { onSelectedTabChange(ChallengeListTab.ARCHIVED) },
                text = { Text(text = stringResource(R.string.challenges_archived_title)) },
            )
        }

        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        when (selectedTab) {
            ChallengeListTab.ACTIVE -> {
                val activeChallenges =
                    remember(state.activeChallenges) {
                        state.activeChallenges.sortedByDescending { it.updatedAt }
                    }
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    if (activeChallenges.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Outlined.Flag,
                            title = stringResource(R.string.challenges_empty_active_title),
                            body = stringResource(R.string.challenges_empty_active_body),
                            modifier = Modifier.fillMaxWidth(),
                            actionContent = null,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ACTIVE_LIST),
                            contentPadding =
                                PaddingValues(
                                    start = SpacingXl,
                                    top = SpacingMd,
                                    end = SpacingXl,
                                    bottom = SpacingXl,
                                ),
                            verticalArrangement = Arrangement.spacedBy(SpacingMd),
                        ) {
                            items(activeChallenges, key = { it.id }) { challenge ->
                                ChallengeCard(
                                    challenge = challenge,
                                    calculation = state.challengeCalculations[challenge.id],
                                    onClick = { onChallengeClick(challenge) },
                                )
                            }
                        }
                    }
                }
            }

            ChallengeListTab.ARCHIVED -> {
                val archivedChallenges =
                    remember(state.archivedChallenges) {
                        state.archivedChallenges.sortedByDescending { it.updatedAt }
                    }
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    if (archivedChallenges.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Outlined.Archive,
                            title = stringResource(R.string.challenges_empty_archived_title),
                            body = stringResource(R.string.challenges_empty_archived_body),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ARCHIVED_LIST),
                            contentPadding =
                                PaddingValues(
                                    start = SpacingXl,
                                    top = SpacingMd,
                                    end = SpacingXl,
                                    bottom = SpacingXl,
                                ),
                            verticalArrangement = Arrangement.spacedBy(SpacingMd),
                        ) {
                            items(archivedChallenges, key = { it.id }) { challenge ->
                                ChallengeCard(
                                    challenge = challenge,
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
}

@Composable
private fun ChallengesDetailRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onEdit: () -> Unit,
    onOpenHistory: () -> Unit,
    onArchive: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onQuickAdd: (ChallengeQuickAddValue) -> Unit,
    onRequestAddProgress: () -> Unit,
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
                icon = Icons.Outlined.History,
                title = stringResource(R.string.challenges_empty_detail_title),
                body = stringResource(R.string.challenges_empty_detail_body),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    val latestEntries =
        remember(state.progressEntries) {
            state.progressEntries.sortedWith(
                compareByDescending<ChallengeProgressEntry>({ it.entryDate })
                    .thenByDescending { it.occurredAt }
                    .thenByDescending { it.id },
            ).take(3)
        }
    val quickAdds =
        if (challenge.lifecycle == ChallengeLifecycle.ACTIVE && challenge.targetType == ChallengeTargetType.DAILY) {
            ChallengeQuantity.quickAddValues(challenge.targetQuantity)
        } else {
            emptyList()
        }
    val latestValidDate =
        challenge.endDate.coerceAtMost(LocalDate.now()).takeIf { !it.isBefore(challenge.startDate) }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CHALLENGES_TAG_DETAIL),
        contentPadding =
            PaddingValues(
                start = SpacingXl,
                top = SpacingMd,
                end = SpacingXl,
                bottom = SpacingXl,
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            ChallengeDetailSummaryCard(
                challenge = challenge,
                calculation = calculation,
                onEdit = if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) onEdit else null,
                onArchive =
                    if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) {
                        { onArchive(challenge.id) }
                    } else {
                        null
                    },
                onReactivate =
                    if (challenge.lifecycle == ChallengeLifecycle.ARCHIVED) {
                        { onReactivate(challenge.id) }
                    } else {
                        null
                    },
                onDelete = { onDelete(challenge.id) },
            )
        }

        if (challenge.lifecycle == ChallengeLifecycle.ACTIVE && quickAdds.isNotEmpty()) {
            item {
                ChallengeQuickAddCard(
                    calculation = calculation,
                    quickAdds = quickAdds,
                    isCustomAddEnabled = latestValidDate != null,
                    onQuickAdd = { quickAdd ->
                        onQuickAdd(quickAdd)
                    },
                    onCustomAddClick = onRequestAddProgress,
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
                TextButton(onClick = onOpenHistory) {
                    Icon(imageVector = Icons.Outlined.History, contentDescription = null)
                    Spacer(modifier = Modifier.size(SpacingSm))
                    Text(text = stringResource(R.string.challenges_history_view_all))
                }
            }
        }

        if (latestEntries.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.challenges_empty_detail_title),
                    body = stringResource(R.string.challenges_empty_detail_body),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(latestEntries, key = { it.id }) { entry ->
                ChallengeProgressEntryRow(
                    entry = entry,
                    isEditable = challenge.lifecycle == ChallengeLifecycle.ACTIVE,
                    onEdit = { onRequestEditProgress(entry) },
                    onDelete = { onDeleteProgress(entry.id) },
                )
            }
        }
    }
}

@Composable
private fun ChallengesHistoryRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onEditChallenge: () -> Unit,
    onDeleteChallenge: (Long) -> Unit,
    onRequestEditProgress: (ChallengeProgressEntry) -> Unit,
    onDeleteProgress: (Long) -> Unit,
) {
    val challenge = state.selectedChallenge
    val calculation = state.calculation
    if (challenge == null || calculation == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(SpacingXl).testTag(CHALLENGES_TAG_HISTORY),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateCard(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.challenges_empty_detail_title),
                body = stringResource(R.string.challenges_empty_detail_body),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    val groupedProgressEntries = remember(state.progressEntries) { groupProgressByDate(state.progressEntries) }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CHALLENGES_TAG_HISTORY),
        contentPadding =
            PaddingValues(
                start = SpacingXl,
                top = SpacingMd,
                end = SpacingXl,
                bottom = SpacingXl,
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            ChallengeDetailSummaryCard(
                challenge = challenge,
                calculation = calculation,
                onEdit = if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) onEditChallenge else null,
                onArchive = null,
                onReactivate = null,
                onDelete = { onDeleteChallenge(challenge.id) },
            )
        }

        items(groupedProgressEntries, key = { it.date }) { group ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.medium,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(SpacingLg),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    Text(
                        text = formatWorkoutDate(group.date, Locale.getDefault()),
                        style = typography.titleSmall,
                        color = colorScheme.onSurface,
                    )
                    group.entries.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider()
                        }
                        ChallengeProgressEntryRow(
                            entry = entry,
                            isEditable = challenge.lifecycle == ChallengeLifecycle.ACTIVE,
                            onEdit = { onRequestEditProgress(entry) },
                            onDelete = { onDeleteProgress(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChallengesEditorDialog(
    editorState: ChallengeEditorState,
    validationMessage: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
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

                    ChallengeDialogDateField(
                        label = stringResource(R.string.challenges_field_start_date),
                        date = startDate,
                        onClick = { showStartDatePicker = true },
                    )
                    ChallengeDialogDateField(
                        label = stringResource(R.string.challenges_field_end_date),
                        date = endDate,
                        onClick = { showEndDatePicker = true },
                    )
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
    calculation: ChallengeCalculationResult,
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
    onReactivate: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = shapes.small,
                    color = colorScheme.primaryContainer,
                    modifier = Modifier.size(SpacingLg + SpacingLg),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(SmallIconSize),
                        )
                    }
                }

                Spacer(modifier = Modifier.size(SpacingMd))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    Text(
                        text = challenge.title,
                        style = typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    challenge.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(text = description, color = colorScheme.onSurfaceVariant)
                    }
                }

                ChallengeOverflowMenu(
                    onEdit = onEdit,
                    onArchive = onArchive,
                    onReactivate = onReactivate,
                    onDelete = onDelete,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                TitleChip(
                    label = challengeTargetTypeLabel(challenge.targetType),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
                TitleChip(
                    label =
                        stringResource(
                            if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) {
                                R.string.challenges_lifecycle_active
                            } else {
                                R.string.challenges_lifecycle_archived
                            },
                        ),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
                TitleChip(
                    label = challengeStatusLabel(calculation.status),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                progress = { calculation.visualProgress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                SummaryMetricCard(
                    label = stringResource(R.string.challenges_overall_progress_label),
                    value =
                        stringResource(
                            R.string.challenges_progress_value,
                            ChallengeQuantity.format(calculation.completedTotal, Locale.getDefault()),
                            ChallengeQuantity.format(calculation.plannedTotal, Locale.getDefault()),
                        ),
                )
                SummaryMetricCard(
                    label = stringResource(R.string.challenges_today_label),
                    value =
                        if (calculation.todayTarget != null) {
                            stringResource(
                                R.string.challenges_today_value_daily,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayTarget, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayRemaining ?: 0L, Locale.getDefault()),
                            )
                        } else {
                            stringResource(
                                R.string.challenges_today_value_total,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                            )
                        },
                )
                SummaryMetricCard(
                    label = stringResource(R.string.challenges_required_pace_label),
                    value = ChallengeQuantity.format(calculation.requiredPace, Locale.getDefault()),
                )
            }

            if (calculation.carriedDebt > 0L) {
                Surface(
                    color = colorScheme.secondaryContainer,
                    shape = shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(SpacingMd),
                        verticalArrangement = Arrangement.spacedBy(SpacingXs),
                    ) {
                        Text(
                            text = stringResource(R.string.challenges_debt_label),
                            style = typography.labelLarge,
                            color = colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = ChallengeQuantity.format(calculation.carriedDebt, Locale.getDefault()),
                            style = typography.titleMedium,
                            color = colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeQuickAddCard(
    calculation: ChallengeCalculationResult,
    quickAdds: List<ChallengeQuickAddValue>,
    isCustomAddEnabled: Boolean,
    onQuickAdd: (ChallengeQuickAddValue) -> Unit,
    onCustomAddClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(
                text = stringResource(R.string.challenges_add_progress),
                style = typography.titleMedium,
            )
            Text(
                text =
                    when (calculation.todayTarget) {
                        null ->
                            stringResource(
                                R.string.challenges_today_value_total,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                            )
                        else ->
                            stringResource(
                                R.string.challenges_today_value_daily,
                                ChallengeQuantity.format(calculation.todayProgress, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayTarget, Locale.getDefault()),
                                ChallengeQuantity.format(calculation.todayRemaining ?: 0L, Locale.getDefault()),
                            )
                    },
                color = colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                quickAdds.forEach { quickAdd ->
                    AssistChip(
                        onClick = { onQuickAdd(quickAdd) },
                        label = {
                            Text(
                                text =
                                    stringResource(
                                        R.string.challenges_quick_add_with_quantity,
                                        quickAdd.percentage,
                                        ChallengeQuantity.format(quickAdd.quantity, Locale.getDefault()),
                                    ),
                            )
                        },
                    )
                }
                Button(
                    onClick = onCustomAddClick,
                    enabled = isCustomAddEnabled,
                    modifier = Modifier.testTag(CHALLENGES_TAG_ADD_PROGRESS_BUTTON),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(SpacingSm))
                    Text(text = stringResource(R.string.challenges_add_progress))
                }
            }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            Text(text = ChallengeQuantity.format(entry.quantity, Locale.getDefault()))
            Text(
                text = formatWorkoutDate(entry.entryDate, Locale.getDefault()),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        if (isEditable) {
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
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
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = shapes.small,
                    color = colorScheme.primaryContainer,
                    modifier = Modifier.size(SpacingLg + SpacingLg),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(SmallIconSize),
                        )
                    }
                }

                Spacer(modifier = Modifier.size(SpacingMd))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    Text(
                        text = challenge.title,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = challengeTargetTypeLabel(challenge.targetType),
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            challenge.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(text = description, color = colorScheme.onSurfaceVariant)
            }

            calculation?.let {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    TitleChip(
                        label = challengeStatusLabel(it.status),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                    TitleChip(
                        label =
                            stringResource(
                                R.string.challenges_progress_value,
                                ChallengeQuantity.format(it.completedTotal, Locale.getDefault()),
                                ChallengeQuantity.format(it.plannedTotal, Locale.getDefault()),
                            ),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
private fun SummaryMetricCard(
    label: String,
    value: String,
) {
    Surface(color = colorScheme.surfaceVariant, shape = shapes.medium) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(text = label, style = typography.labelLarge, color = colorScheme.onSurfaceVariant)
            Text(text = value, style = typography.titleMedium, color = colorScheme.onSurface)
        }
    }
}

@Composable
private fun ChallengeDialogDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = date?.let { formatWorkoutDate(it, Locale.getDefault()) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onClick))
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
)

private fun groupProgressByDate(entries: List<ChallengeProgressEntry>): List<ChallengeProgressDateGroup> {
    return entries
        .groupBy { it.entryDate }
        .toSortedMap(compareByDescending { it })
        .map { (date, dayEntries) ->
            ChallengeProgressDateGroup(
                date = date,
                entries = dayEntries.sortedWith(compareByDescending<ChallengeProgressEntry> { it.occurredAt }.thenByDescending { it.id }),
            )
        }
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
