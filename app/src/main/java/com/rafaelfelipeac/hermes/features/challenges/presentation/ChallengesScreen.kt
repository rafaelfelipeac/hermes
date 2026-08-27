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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeUiState
import java.time.LocalDate
import java.util.Locale

private const val CHALLENGES_ROUTE_LIST = "list"
private const val CHALLENGES_ROUTE_DETAIL = "detail"
private const val CHALLENGES_ROUTE_EDITOR = "editor"
private const val CHALLENGES_ROUTE_ARCHIVED = "archived"
private const val CHALLENGES_TAG_ROOT = "challenges_root"
private const val CHALLENGES_TAG_ACTIVE_LIST = "challenges_active_list"
private const val CHALLENGES_TAG_ARCHIVED_LIST = "challenges_archived_list"
private const val CHALLENGES_TAG_DETAIL = "challenges_detail"

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
    var showDeleteDialogForChallengeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteProgressDialogForEntryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editorOriginRoute by rememberSaveable { mutableStateOf(CHALLENGES_ROUTE_LIST) }
    val undoChallengeMessage = stringResource(R.string.challenges_undo_deleted_challenge)
    val undoProgressMessage = stringResource(R.string.challenges_undo_deleted_progress_entry)
    val undoActionLabel = stringResource(R.string.weekly_training_undo_action)

    BackHandler(enabled = route != CHALLENGES_ROUTE_LIST) {
        when (route) {
            CHALLENGES_ROUTE_DETAIL -> {
                route = CHALLENGES_ROUTE_LIST
                viewModel.selectChallenge(null)
            }
            CHALLENGES_ROUTE_ARCHIVED -> {
                route = CHALLENGES_ROUTE_LIST
            }
            CHALLENGES_ROUTE_EDITOR -> {
                route = editorOriginRoute
            }
        }
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
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.challenges_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (route == CHALLENGES_ROUTE_LIST) {
                                onBack()
                            } else {
                                route = CHALLENGES_ROUTE_LIST
                                viewModel.selectChallenge(null)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.weekly_training_tbd_help_confirm),
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
    ) { padding ->
        when (route) {
            CHALLENGES_ROUTE_LIST -> {
                ChallengesListRoute(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onChallengeClick = { challenge ->
                        viewModel.selectChallenge(challenge.id)
                        route = CHALLENGES_ROUTE_DETAIL
                    },
                    onCreateChallenge = {
                        viewModel.beginCreateChallenge()
                        editorOriginRoute = CHALLENGES_ROUTE_LIST
                        route = CHALLENGES_ROUTE_EDITOR
                    },
                    onOpenArchived = { route = CHALLENGES_ROUTE_ARCHIVED },
                )
            }

            CHALLENGES_ROUTE_DETAIL -> {
                ChallengesDetailRoute(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onEdit = {
                        state.selectedChallenge?.let { selected ->
                            viewModel.beginEditChallenge(selected.id)
                            editorOriginRoute = CHALLENGES_ROUTE_DETAIL
                            route = CHALLENGES_ROUTE_EDITOR
                        }
                    },
                    onArchive = { challengeId -> viewModel.archiveChallenge(challengeId) },
                    onReactivate = { challengeId -> viewModel.reactivateChallenge(challengeId) },
                    onDelete = { challengeId -> showDeleteDialogForChallengeId = challengeId },
                    onAddProgress = { quantity, date ->
                        state.selectedChallenge?.id?.let { challengeId ->
                            viewModel.addProgressEntry(challengeId, quantity, date)
                        }
                    },
                    onEditProgress = { entryId, quantity, date ->
                        viewModel.updateProgressEntry(entryId, quantity, date)
                    },
                    onDeleteProgress = { entryId -> showDeleteProgressDialogForEntryId = entryId },
                )
            }

            CHALLENGES_ROUTE_ARCHIVED -> {
                ChallengesArchivedRoute(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onChallengeClick = { challenge ->
                        viewModel.selectChallenge(challenge.id)
                        route = CHALLENGES_ROUTE_DETAIL
                    },
                    onReactivate = { challengeId -> viewModel.reactivateChallenge(challengeId) },
                    onDelete = { challengeId -> showDeleteDialogForChallengeId = challengeId },
                )
            }

            CHALLENGES_ROUTE_EDITOR -> {
                ChallengesEditorRoute(
                    modifier = Modifier.padding(padding),
                    editorState = state.editorState,
                    validationMessage = state.validationMessage,
                    onTitleChange = viewModel::updateEditorTitle,
                    onDescriptionChange = viewModel::updateEditorDescription,
                    onTargetQuantityChange = viewModel::updateEditorTargetQuantity,
                    onUnitChange = viewModel::updateEditorUnit,
                    onStartDateChange = viewModel::updateEditorStartDate,
                    onEndDateChange = viewModel::updateEditorEndDate,
                    onLifecycleChange = viewModel::updateEditorLifecycle,
                    onSave = {
                        viewModel.saveEditorChallenge()
                        route = editorOriginRoute
                    },
                    onCancel = { route = editorOriginRoute },
                )
            }
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
private fun ChallengesListRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onChallengeClick: (Challenge) -> Unit,
    onCreateChallenge: () -> Unit,
    onOpenArchived: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CHALLENGES_TAG_ACTIVE_LIST),
        contentPadding = PaddingValues(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.challenges_active_title), style = typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                AssistChip(
                    onClick = onOpenArchived,
                    label = { Text(text = stringResource(R.string.challenges_archived_title)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Archive, contentDescription = null, modifier = Modifier.size(SmallIconSize))
                    },
                )
            }
        }

        item {
            Button(onClick = onCreateChallenge, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(SpacingSm))
                Text(text = stringResource(R.string.challenges_create))
            }
        }

        if (state.activeChallenges.isEmpty()) {
            item {
                ChallengesEmptyCard(
                    title = stringResource(R.string.challenges_empty_active_title),
                    body = stringResource(R.string.challenges_empty_active_body),
                )
            }
        } else {
            items(state.activeChallenges, key = { it.id }) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    calculation = if (state.selectedChallengeId == challenge.id) state.calculation else null,
                    onClick = { onChallengeClick(challenge) },
                )
            }
        }
    }
}

@Composable
private fun ChallengesArchivedRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onChallengeClick: (Challenge) -> Unit,
    onReactivate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CHALLENGES_TAG_ARCHIVED_LIST),
        contentPadding = PaddingValues(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            Text(text = stringResource(R.string.challenges_archived_title), style = typography.titleMedium)
        }

        if (state.archivedChallenges.isEmpty()) {
            item {
                ChallengesEmptyCard(
                    title = stringResource(R.string.challenges_empty_archived_title),
                    body = stringResource(R.string.challenges_empty_archived_body),
                )
            }
        } else {
            items(state.archivedChallenges, key = { it.id }) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    calculation = null,
                    archived = true,
                    onClick = { onChallengeClick(challenge) },
                    onReactivate = { onReactivate(challenge.id) },
                    onDelete = { onDelete(challenge.id) },
                )
            }
        }
    }
}

@Composable
private fun ChallengesDetailRoute(
    modifier: Modifier,
    state: ChallengeUiState,
    onEdit: () -> Unit,
    onArchive: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onAddProgress: (String, LocalDate) -> Unit,
    onEditProgress: (Long, String, LocalDate) -> Unit,
    onDeleteProgress: (Long) -> Unit,
) {
    val challenge = state.selectedChallenge
    val calculation = state.calculation
    if (challenge == null || calculation == null) {
        ChallengesEmptyCard(
            modifier = modifier.padding(SpacingXl),
            title = stringResource(R.string.challenges_empty_detail_title),
            body = stringResource(R.string.challenges_empty_detail_body),
        )
        return
    }

    val today = LocalDate.now()
    val validDate = challenge.endDate.coerceAtMost(today).takeIf { !it.isBefore(challenge.startDate) }
    var showProgressDialog by rememberSaveable(challenge.id) { mutableStateOf(false) }
    var showDatePicker by rememberSaveable(challenge.id) { mutableStateOf(false) }
    var customEntryDate by rememberSaveable(challenge.id) { mutableStateOf(validDate ?: challenge.endDate) }
    var customEntryQuantity by rememberSaveable(challenge.id) { mutableStateOf("") }
    var showEditProgressDialog by rememberSaveable(challenge.id) { mutableStateOf(false) }
    var selectedProgressEntryId by rememberSaveable(challenge.id) { mutableStateOf<Long?>(null) }
    var editProgressQuantity by rememberSaveable(challenge.id) { mutableStateOf("") }
    var editProgressDate by rememberSaveable(challenge.id) { mutableStateOf(today) }

    Column(
        modifier = modifier.fillMaxSize().padding(SpacingXl).testTag(CHALLENGES_TAG_DETAIL),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        ChallengeCard(
            challenge = challenge,
            calculation = calculation,
            onClick = {},
            onEdit = onEdit,
            onArchive = { onArchive(challenge.id) },
            onReactivate = { onReactivate(challenge.id) },
            onDelete = { onDelete(challenge.id) },
            archived = challenge.lifecycle == ChallengeLifecycle.ARCHIVED,
        )

        if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                val quickAdds = ChallengeQuantity.quickAddValues(challenge.targetQuantity)
                quickAdds.forEach { scaled ->
                    AssistChip(
                        onClick = {
                            if (validDate != null) {
                                onAddProgress(ChallengeQuantity.format(scaled, Locale.getDefault()), validDate)
                            }
                        },
                        label = {
                            Text(
                                text =
                                    stringResource(
                                        when (scaled) {
                                            quickAdds.firstOrNull() -> R.string.challenges_quick_add_25
                                            quickAdds.getOrNull(1) -> R.string.challenges_quick_add_50
                                            else -> R.string.challenges_quick_add_100
                                        },
                                    ),
                            )
                        },
                        enabled = validDate != null,
                    )
                }
                Button(
                    onClick = { showProgressDialog = true },
                    enabled = validDate != null,
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(SpacingSm))
                    Text(text = stringResource(R.string.challenges_add_progress))
                }
            }
        }

        Text(text = stringResource(R.string.challenges_history_title), style = typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
            items(groupProgressByDate(state.progressEntries)) { group ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(SpacingMd), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                        Text(text = group.date.toString(), style = typography.titleSmall)
                        group.entries.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = ChallengeQuantity.format(entry.quantity, Locale.getDefault()),
                                    modifier = Modifier.weight(1f),
                                )
                                if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) {
                                    IconButton(onClick = {
                                        selectedProgressEntryId = entry.id
                                        editProgressQuantity = ChallengeQuantity.format(entry.quantity, Locale.getDefault())
                                        editProgressDate = entry.entryDate
                                        showEditProgressDialog = true
                                    }) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                                    }
                                    IconButton(onClick = { onDeleteProgress(entry.id) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProgressDialog && validDate != null) {
        ChallengeProgressDialog(
            title = stringResource(R.string.challenges_add_progress),
            date = customEntryDate,
            quantity = customEntryQuantity,
            onDateChange = { customEntryDate = it },
            onQuantityChange = { customEntryQuantity = it },
            onDismiss = { showProgressDialog = false },
            onConfirm = {
                onAddProgress(customEntryQuantity, customEntryDate)
                showProgressDialog = false
            },
        )
    }

    if (showEditProgressDialog && selectedProgressEntryId != null) {
        ChallengeProgressDialog(
            title = stringResource(R.string.challenges_edit_progress),
            date = editProgressDate,
            quantity = editProgressQuantity,
            onDateChange = { editProgressDate = it },
            onQuantityChange = { editProgressQuantity = it },
            onDismiss = { showEditProgressDialog = false },
            onConfirm = {
                onEditProgress(selectedProgressEntryId!!, editProgressQuantity, editProgressDate)
                showEditProgressDialog = false
                selectedProgressEntryId = null
            },
        )
    }
}

@Composable
private fun ChallengesEditorRoute(
    modifier: Modifier,
    editorState: ChallengeEditorState,
    validationMessage: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTargetQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onLifecycleChange: (ChallengeLifecycle) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.challenges_editor_title), style = typography.titleMedium)
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
        )
        OutlinedTextField(
            value = editorState.targetQuantityText,
            onValueChange = onTargetQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.challenges_field_target_quantity)) },
            keyboardOptions = DefaultTextFieldKeyboardOptions,
        )
        OutlinedTextField(
            value = editorState.unit,
            onValueChange = onUnitChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.challenges_field_unit)) },
            keyboardOptions = DefaultTextFieldKeyboardOptions,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
            FilterChip(
                selected = editorState.lifecycle == ChallengeLifecycle.ACTIVE,
                onClick = { onLifecycleChange(ChallengeLifecycle.ACTIVE) },
                label = { Text(text = stringResource(R.string.challenges_lifecycle_active)) },
            )
            FilterChip(
                selected = editorState.lifecycle == ChallengeLifecycle.ARCHIVED,
                onClick = { onLifecycleChange(ChallengeLifecycle.ARCHIVED) },
                label = { Text(text = stringResource(R.string.challenges_lifecycle_archived)) },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
            TextButton(onClick = {
                if (editorState.startDate != null) {
                    onStartDateChange(editorState.startDate)
                }
            }) {
                Text(text = stringResource(R.string.challenges_field_start_date))
            }
            TextButton(onClick = {
                if (editorState.endDate != null) {
                    onEndDateChange(editorState.endDate)
                }
            }) {
                Text(text = stringResource(R.string.challenges_field_end_date))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
            Button(onClick = onSave) {
                Text(text = stringResource(R.string.save_changes))
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    calculation: ChallengeCalculationResult?,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
    onReactivate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    archived: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(SpacingMd), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
            Text(text = challenge.title, style = typography.titleSmall)
            Text(
                text =
                    stringResource(
                        R.string.challenges_summary_line,
                        ChallengeQuantity.format(challenge.targetQuantity, Locale.getDefault()),
                        challenge.unit,
                    ),
            )
            calculation?.let {
                Text(
                    text =
                        stringResource(
                            when (it.status) {
                                ChallengeStatus.NOT_STARTED -> R.string.challenges_status_not_started
                                ChallengeStatus.EXCEEDED -> R.string.challenges_status_exceeded
                                ChallengeStatus.COMPLETED -> R.string.challenges_status_completed
                                ChallengeStatus.EXPIRED_INCOMPLETE -> R.string.challenges_status_expired
                                ChallengeStatus.AHEAD -> R.string.challenges_status_ahead
                                ChallengeStatus.ON_TRACK -> R.string.challenges_status_on_track
                                ChallengeStatus.BEHIND -> R.string.challenges_status_behind
                            },
                        ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                onEdit?.let {
                    AssistChip(onClick = it, label = {
                        Text(text = stringResource(R.string.challenges_edit))
                    }, leadingIcon = { Icon(Icons.Filled.Edit, null, modifier = Modifier.size(SmallIconSize)) })
                }
                if (archived) {
                    onReactivate?.let {
                        AssistChip(onClick = it, label = {
                            Text(text = stringResource(R.string.challenges_reactivate))
                        }, leadingIcon = { Icon(Icons.Filled.Restore, null, modifier = Modifier.size(SmallIconSize)) })
                    }
                } else {
                    onArchive?.let {
                        AssistChip(onClick = it, label = {
                            Text(text = stringResource(R.string.challenges_archive))
                        }, leadingIcon = { Icon(Icons.Outlined.Unarchive, null, modifier = Modifier.size(SmallIconSize)) })
                    }
                }
                onDelete?.let {
                    AssistChip(onClick = it, label = {
                        Text(text = stringResource(R.string.challenges_delete))
                    }, leadingIcon = { Icon(Icons.Filled.Delete, null, modifier = Modifier.size(SmallIconSize)) })
                }
            }
        }
    }
}

@Composable
private fun ChallengesEmptyCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(SpacingLg), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
            Text(text = title, style = typography.titleSmall)
            Text(text = body)
        }
    }
}

@Composable
private fun ChallengeProgressDialog(
    title: String,
    date: LocalDate,
    quantity: String,
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
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.challenges_field_progress_quantity)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text(text = date.toString())
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
        val datePickerState =
            androidx.compose.material3.DatePickerState(
                locale = Locale.getDefault(),
                initialSelectedDateMillis = date.toUtcEpochMillis(),
            )
        HermesDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateChange(it.toUtcLocalDate()) }
                        showDatePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
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
