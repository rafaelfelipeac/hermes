@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.personalrecords.domain.defaultUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit

internal const val PERSONAL_RECORDS_ROOT_TAG = "personal_records_root"
internal const val PERSONAL_RECORDS_BACK_BUTTON_TAG = "personal_records_back_button"
internal const val PERSONAL_RECORDS_ACTION_FAB_TAG = "personal_records_action_fab"
internal const val PERSONAL_RECORDS_ACTION_MENU_TAG = "personal_records_action_menu"
internal const val PERSONAL_RECORDS_FAMILY_CARD_TAG_PREFIX = "personal_records_family_"
internal const val PERSONAL_RECORDS_CREATE_FAMILY_DIALOG_TAG = "personal_records_create_family_dialog"
internal const val PERSONAL_RECORDS_EDIT_FAMILY_DIALOG_TAG = "personal_records_edit_family_dialog"
internal const val PERSONAL_RECORDS_ADD_ENTRY_DIALOG_TAG = "personal_records_add_entry_dialog"
internal const val PERSONAL_RECORDS_EDIT_ENTRY_DIALOG_TAG = "personal_records_edit_entry_dialog"
internal const val PERSONAL_RECORDS_ENTRY_FAMILY_FIELD_TAG = "personal_records_entry_family_field"
internal const val PERSONAL_RECORDS_ENTRY_VALUE_FIELD_TAG = "personal_records_entry_value_field"
internal const val PERSONAL_RECORDS_ENTRY_DATE_FIELD_TAG = "personal_records_entry_date_field"
internal const val PERSONAL_RECORDS_EDIT_FAMILY_BUTTON_TAG = "personal_records_edit_family_button"
internal const val PERSONAL_RECORDS_DELETE_FAMILY_BUTTON_TAG = "personal_records_delete_family_button"
internal const val PERSONAL_RECORDS_ENTRY_CARD_TAG_PREFIX = "personal_records_entry_"
internal const val PERSONAL_RECORDS_SET_CURRENT_TAG_PREFIX = "personal_records_set_current_"
private const val PERSONAL_RECORDS_ADD_MENU_SCRIM_ALPHA = 0.30f

@Composable
fun PersonalRecordsScreen(
    modifier: Modifier = Modifier,
    settingsDistanceUnit: DistanceUnit,
    settingsWeightUnit: WeightUnit,
    viewModel: PersonalRecordsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var selectedFamilyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCreateFamilyDialog by rememberSaveable { mutableStateOf(false) }
    var showAddEntryDialog by rememberSaveable { mutableStateOf(false) }
    var addEntryFamilyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingFamily by remember { mutableStateOf<PersonalRecordFamily?>(null) }
    var deletingFamily by remember { mutableStateOf<PersonalRecordFamily?>(null) }
    var editingEntry by remember { mutableStateOf<PersonalRecordEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<PersonalRecordEntry?>(null) }

    BackHandler {
        if (selectedFamilyId != null) {
            selectedFamilyId = null
        } else {
            onBack()
        }
    }

    PersonalRecordsContent(
        modifier = modifier,
        state = state,
        selectedFamilyId = selectedFamilyId,
        onFamilySelected = { selectedFamilyId = it },
        onBack = onBack,
        onBackToShelf = { selectedFamilyId = null },
        onCreateFamily = { showCreateFamilyDialog = true },
        onAddResult = { familyId ->
            addEntryFamilyId = familyId
            showAddEntryDialog = true
        },
        onEditFamily = { editingFamily = it },
        onDeleteFamily = { deletingFamily = it },
        onEditEntry = { editingEntry = it },
        onSetManualCurrentEntry = viewModel::setManualCurrentEntry,
    )

    if (showCreateFamilyDialog) {
        PersonalRecordFamilyEditorDialog(
            categories = state.categories,
            onDismiss = { showCreateFamilyDialog = false },
            onSave = { categoryId, title, metricType, comparisonRule ->
                viewModel.createFamily(
                    categoryId = categoryId,
                    title = title,
                    metricType = metricType,
                    defaultUnit =
                        metricType.defaultUnit(
                            distanceUnit = settingsDistanceUnit,
                            weightUnit = settingsWeightUnit,
                        ),
                    comparisonRule = comparisonRule,
                )
                showCreateFamilyDialog = false
            },
        )
    }

    if (showAddEntryDialog) {
        PersonalRecordEntryEditorDialog(
            families = state.families,
            entries = state.entries,
            initialFamilyId = addEntryFamilyId,
            settingsDistanceUnit = settingsDistanceUnit,
            settingsWeightUnit = settingsWeightUnit,
            onDismiss = { showAddEntryDialog = false },
            onSave = { familyId, value, unit, recordDate, note, customUnitLabel ->
                viewModel.addEntry(
                    PersonalRecordEntryInput(
                        familyId = familyId,
                        value = value,
                        unit = unit,
                        recordDate = recordDate,
                        note = note,
                        customUnitLabel = customUnitLabel,
                    ),
                )
                showAddEntryDialog = false
            },
        )
    }

    editingFamily?.let { family ->
        PersonalRecordFamilyEditorDialog(
            categories = state.categories,
            initialFamily = family,
            onDismiss = { editingFamily = null },
            onSave = { categoryId, title, _, comparisonRule ->
                viewModel.updateFamily(
                    familyId = family.id,
                    categoryId = categoryId,
                    title = title,
                    comparisonRule = comparisonRule,
                )
                editingFamily = null
            },
        )
    }

    deletingFamily?.let { family ->
        val entryCount = state.entries.count { it.familyId == family.id }
        PersonalRecordDeleteFamilyDialog(
            entryCount = entryCount,
            onDismiss = { deletingFamily = null },
            onConfirm = {
                viewModel.deleteFamily(family.id)
                deletingFamily = null
            },
        )
    }

    editingEntry?.let { entry ->
        PersonalRecordEntryEditorDialog(
            families = state.families,
            entries = state.entries,
            initialFamilyId = entry.familyId,
            settingsDistanceUnit = settingsDistanceUnit,
            settingsWeightUnit = settingsWeightUnit,
            initialEntry = entry,
            isEdit = true,
            onDismiss = { editingEntry = null },
            onSave = { familyId, value, unit, recordDate, note, customUnitLabel ->
                viewModel.updateEntry(
                    entryId = entry.id,
                    input =
                        PersonalRecordEntryInput(
                            familyId = familyId,
                            value = value,
                            unit = unit,
                            recordDate = recordDate,
                            note = note,
                            customUnitLabel = customUnitLabel,
                        ),
                )
                editingEntry = null
            },
            onDeleteRequested = {
                deletingEntry = entry
                editingEntry = null
            },
        )
    }

    deletingEntry?.let { entry ->
        PersonalRecordDeleteEntryDialog(
            onDismiss = { deletingEntry = null },
            onConfirm = {
                viewModel.deleteEntry(entry.id)
                deletingEntry = null
            },
        )
    }
}

@Composable
fun PersonalRecordsContent(
    state: PersonalRecordsState,
    selectedFamilyId: Long?,
    onFamilySelected: (Long) -> Unit,
    onBack: () -> Unit,
    onBackToShelf: () -> Unit,
    onCreateFamily: () -> Unit,
    onAddResult: (Long?) -> Unit,
    onEditFamily: (PersonalRecordFamily) -> Unit,
    onDeleteFamily: (PersonalRecordFamily) -> Unit,
    onEditEntry: (PersonalRecordEntry) -> Unit,
    onSetManualCurrentEntry: (familyId: Long, entryId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddMenuVisible by rememberSaveable { mutableStateOf(false) }
    val currentLocale = currentLocale()
    val selectedFamily = state.families.firstOrNull { it.id == selectedFamilyId }
    val contentTitle =
        selectedFamily?.title ?: stringResource(R.string.personal_records_title)
    val handleBack = {
        if (selectedFamily == null) {
            onBack()
        } else {
            onBackToShelf()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                Box {
                    if (selectedFamily != null) {
                        FloatingActionButton(
                            onClick = { onAddResult(selectedFamily.id) },
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                            modifier =
                                Modifier
                                    .padding(bottom = SpacingXl)
                                    .testTag(PERSONAL_RECORDS_ACTION_FAB_TAG),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.personal_records_add_result),
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { isAddMenuVisible = !isAddMenuVisible },
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                            modifier =
                                Modifier
                                    .padding(bottom = SpacingXl)
                                    .testTag(PERSONAL_RECORDS_ACTION_FAB_TAG),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.weekly_training_add_item),
                            )
                        }
                    }
                }
            },
        ) { contentPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val screenContentPadding =
                PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = Zero,
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding(),
                )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(screenContentPadding)
                        .testTag(PERSONAL_RECORDS_ROOT_TAG),
                verticalArrangement = Arrangement.spacedBy(SpacingLg),
            ) {
                PersonalRecordsHeader(
                    title = contentTitle,
                    onBack = handleBack,
                    actions =
                        if (selectedFamily != null) {
                            {
                                IconButton(
                                    onClick = { onEditFamily(selectedFamily) },
                                    modifier = Modifier.testTag(PERSONAL_RECORDS_EDIT_FAMILY_BUTTON_TAG),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = stringResource(R.string.save_changes),
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteFamily(selectedFamily) },
                                    modifier =
                                        Modifier.testTag(
                                            PERSONAL_RECORDS_DELETE_FAMILY_BUTTON_TAG,
                                        ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription =
                                            stringResource(
                                                R.string.personal_records_delete_family_confirm,
                                            ),
                                        tint = colorScheme.onSurface,
                                    )
                                }
                            }
                        } else {
                            {}
                        },
                )

                key(selectedFamilyId) {
                    val hasScrollableContent =
                        if (selectedFamily == null) {
                            state.families.isNotEmpty()
                        } else {
                            state.entries.any { it.familyId == selectedFamily.id }
                        }
                    val bodyModifier =
                        if (hasScrollableContent) {
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = FloatingActionContentBottomPadding)
                        } else {
                            Modifier.fillMaxSize()
                        }

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = SpacingXl),
                    ) {
                        if (selectedFamily == null) {
                            PersonalRecordsShelf(
                                state = state,
                                currentLocale = currentLocale,
                                onFamilySelected = onFamilySelected,
                                modifier = bodyModifier,
                            )
                        } else {
                            PersonalRecordDetail(
                                state = state,
                                family = selectedFamily,
                                currentLocale = currentLocale,
                                onEditEntry = onEditEntry,
                                onSetManualCurrentEntry = onSetManualCurrentEntry,
                                modifier = bodyModifier,
                            )
                        }
                    }
                }
            }
        }

        if (isAddMenuVisible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            colorScheme.scrim.copy(
                                alpha = PERSONAL_RECORDS_ADD_MENU_SCRIM_ALPHA,
                            ),
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            isAddMenuVisible = false
                        },
            )
        }

        if (isAddMenuVisible) {
            Column(
                modifier =
                    Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.BottomEnd)
                        .padding(end = SpacingXl, bottom = FloatingActionContentBottomPadding)
                        .testTag(PERSONAL_RECORDS_ACTION_MENU_TAG),
                verticalArrangement = Arrangement.spacedBy(SpacingLg),
                horizontalAlignment = Alignment.End,
            ) {
                AddActionPill(
                    icon = Icons.Outlined.Leaderboard,
                    label = stringResource(R.string.personal_records_new_family),
                    onClick = {
                        isAddMenuVisible = false
                        onCreateFamily()
                    },
                )
                if (state.families.isNotEmpty()) {
                    AddActionPill(
                        icon = Icons.Outlined.Add,
                        label = stringResource(R.string.personal_records_add_result),
                        onClick = {
                            isAddMenuVisible = false
                            onAddResult(if (state.families.size == 1) state.families.first().id else null)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordsHeader(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
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
            modifier = Modifier.testTag(PERSONAL_RECORDS_BACK_BUTTON_TAG),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.settings_back),
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

        actions()
    }
}
