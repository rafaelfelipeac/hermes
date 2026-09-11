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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.HermesSnackbar
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuickAddValue
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeUiState
import com.rafaelfelipeac.hermes.features.challenges.presentation.model.ChallengeEditorDraft
import com.rafaelfelipeac.hermes.features.challenges.presentation.model.ChallengeEditorOrigin
import java.time.LocalDate

private const val CHALLENGES_ROUTE_LIST = "list"
private const val CHALLENGES_ROUTE_DETAIL = "detail"
internal const val CHALLENGES_TAG_ROOT = "challenges_root"
internal const val CHALLENGES_TAG_ACTIVE_LIST = "challenges_active_list"
internal const val CHALLENGES_TAG_ARCHIVED_LIST = "challenges_archived_list"
internal const val CHALLENGES_TAG_DETAIL = "challenges_detail"
internal const val CHALLENGES_TAG_HEADER_BACK = "challenges_header_back"
private const val CHALLENGES_TAG_CREATE_FAB = "challenges_create_fab"
internal const val CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB = "challenges_detail_add_progress_fab"
internal const val CHALLENGES_TAG_DETAIL_QUICK_ADD = "challenges_detail_quick_add"
internal const val CHALLENGES_TAG_COMPLETION_CELEBRATION = "challenges_completion_celebration"
internal const val CHALLENGES_TAG_COMPLETION_CONFETTI = "challenges_completion_confetti"
internal const val CHALLENGES_TAG_DETAIL_HISTORY = "challenges_detail_history"
internal const val CHALLENGES_TAG_ACTIVE_CARD_PROGRESS = "challenges_active_card_progress"
internal const val CHALLENGES_TAG_ACTIVE_EMPTY_STATE = "challenges_active_empty_state"
internal const val CHALLENGES_TAG_ARCHIVED_EMPTY_STATE = "challenges_archived_empty_state"

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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val undoState by viewModel.undoUiState.collectAsStateWithLifecycle()
    val currentLocale = currentLocale()
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
    var progressDialogQuantity by rememberSaveable { mutableStateOf(EMPTY) }
    var progressDialogDateEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
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

    LaunchedEffect(route, state.selectedChallengeMissing) {
        if (route == CHALLENGES_ROUTE_DETAIL && state.selectedChallengeMissing) {
            route = CHALLENGES_ROUTE_LIST
            viewModel.selectChallenge(null)
            selectedTab = detailOriginTab
        }
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
        progressDialogQuantity = EMPTY
        progressDialogDateEpochDay = defaultDate.toEpochDay()
        progressDialogIsEdit = false
        showProgressDialog = true
    }

    val addQuickProgress: (ChallengeQuickAddValue) -> Unit = addQuickProgress@{ quickAdd ->
        val challenge = state.selectedChallenge ?: return@addQuickProgress
        val date = addProgressDefaultDate ?: return@addQuickProgress
        viewModel.addProgressEntry(
            challenge.id,
            ChallengeQuantity.format(quickAdd.quantity, currentLocale),
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
                    state.selectedChallenge?.takeIf { route == CHALLENGES_ROUTE_DETAIL }?.let { selectedChallenge ->
                        {
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
                    } ?: run {
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
                            progressDialogQuantity = ChallengeQuantity.format(entry.quantity, currentLocale)
                            progressDialogDateEpochDay = entry.entryDate.toEpochDay()
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

    val progressDialogDate = progressDialogDateEpochDay?.let(LocalDate::ofEpochDay)
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
                date = progressDialogDate,
                quantity = progressDialogQuantity,
                validationMessage = state.editorState.validationMessage,
                onDateChange = { progressDialogDateEpochDay = it.toEpochDay() },
                onQuantityChange = { progressDialogQuantity = it },
                onDismiss = {
                    showProgressDialog = false
                    progressDialogChallengeId = null
                    progressDialogEntryId = null
                    progressDialogDateEpochDay = null
                },
                onConfirm = {
                    val saved =
                        if (progressDialogIsEdit) {
                            progressDialogEntryId?.let { entryId ->
                                viewModel.updateProgressEntry(
                                    entryId, progressDialogQuantity,
                                    progressDialogDate,
                                )
                            } ?: false
                        } else {
                            viewModel.addProgressEntry(
                                challengeId,
                                progressDialogQuantity,
                                progressDialogDate,
                            )
                        }
                    if (saved) {
                        showProgressDialog = false
                        progressDialogChallengeId = null
                        progressDialogEntryId = null
                        progressDialogDateEpochDay = null
                    }
                },
            )
        }
    }

    showDeleteDialogForChallengeId?.let { challengeId ->
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

    showDeleteProgressDialogForEntryId?.let { entryId ->
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
internal fun ChallengeProgressEntryRow(
    entry: ChallengeProgressEntry,
    isEditable: Boolean,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val currentLocale = currentLocale()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            Text(text = ChallengeQuantity.format(entry.quantity, currentLocale))
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
