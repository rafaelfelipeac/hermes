@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerField
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerOption
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.capitalizedFirstCharacter
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelColumnMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelContentPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelItemHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_DARK
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_LIGHT
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordBestSelector
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordHistoryOrderer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordValueNormalizer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.defaultComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.defaultUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.CUSTOM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.POWER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.REPS
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.HOUR
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.METER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MINUTE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.POUND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.REP
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.WATT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.supportedUnits
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM as CUSTOM_UNIT

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

        AlertDialog(
            onDismissRequest = { deletingFamily = null },
            title = {
                Text(text = stringResource(R.string.personal_records_delete_family_title))
            },
            text = {
                Text(
                    text =
                        stringResource(
                            R.string.personal_records_delete_family_message,
                            entryCount,
                        ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFamily(family.id)
                        deletingFamily = null
                    },
                ) {
                    Text(text = stringResource(R.string.personal_records_delete_family_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFamily = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
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
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = {
                Text(text = stringResource(R.string.personal_records_delete_result_title))
            },
            text = {
                Text(text = stringResource(R.string.personal_records_delete_result_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry.id)
                        deletingEntry = null
                    },
                ) {
                    Text(text = stringResource(R.string.personal_records_delete_result_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
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
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
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

@Composable
private fun PersonalRecordsShelf(
    state: PersonalRecordsState,
    currentLocale: Locale,
    onFamilySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(state.categories, state.families) { buildFamilyGroups(state.categories, state.families) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.families.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Leaderboard,
                title = stringResource(R.string.personal_records_empty_title),
                body = stringResource(R.string.personal_records_empty_body),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                groups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                        Text(
                            text =
                                group.category?.name
                                    ?: stringResource(R.string.category_uncategorized),
                            style = typography.titleMedium,
                        )

                        group.families.forEach { family ->
                            val familyEntries = state.entries.filter { it.familyId == family.id }
                            val currentBest = PersonalRecordBestSelector.selectBest(family, familyEntries)
                            val entryCountText =
                                pluralStringResource(
                                    R.plurals.personal_records_family_entry_count,
                                    familyEntries.size,
                                    familyEntries.size,
                                )
                            val category = group.category
                            val accent = category?.colorId?.let(::categoryAccentColor) ?: colorScheme.surfaceVariant
                            val contentColor =
                                if (category == null) {
                                    colorScheme.onSurfaceVariant
                                } else {
                                    contentColorForBackground(accent)
                                }

                            Card(
                                onClick = { onFamilySelected(family.id) },
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                                border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
                                shape = shapes.medium,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .testTag(PERSONAL_RECORDS_FAMILY_CARD_TAG_PREFIX + family.id),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(SpacingLg),
                                    verticalArrangement = Arrangement.spacedBy(SpacingMd),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accent,
                                            tonalElevation = ElevationSm,
                                            shadowElevation = ElevationSm,
                                            modifier = Modifier.size(SmallIconSize + SpacingSm),
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize(),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Leaderboard,
                                                    contentDescription = null,
                                                    tint = contentColor,
                                                    modifier = Modifier.size(SmallIconSize),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(SpacingMd))

                                        Text(
                                            text = family.title,
                                            style = typography.titleMedium,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }

                                    FlowRow(
                                        modifier =
                                            Modifier.padding(
                                                start = SmallIconSize + SpacingSm + SpacingMd,
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                                        verticalArrangement = Arrangement.spacedBy(SpacingXs),
                                    ) {
                                        TitleChip(
                                            label = metricLabel(family.metricType),
                                            containerColor = colorScheme.surfaceVariant,
                                            contentColor = colorScheme.onSurfaceVariant,
                                        )
                                        TitleChip(
                                            label = comparisonLabel(family.comparisonRule),
                                            containerColor = colorScheme.surfaceVariant,
                                            contentColor = colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    val currentBestLabel =
                                        currentBest?.let {
                                            formatPersonalRecordValue(
                                                value = it.value,
                                                unit = it.unit,
                                                locale = currentLocale,
                                                unitLabel = unitLabelFor(it.unit, it.customUnitLabel, it.value),
                                            )
                                        }

                                    if (currentBestLabel == null) {
                                        val actionColors = personalRecordRelatedColors(category)
                                        AddActionPill(
                                            icon = Icons.Outlined.Add,
                                            label = stringResource(R.string.personal_records_add_first_result),
                                            containerColor = actionColors.container,
                                            contentColor = actionColors.content,
                                            onClick = { onFamilySelected(family.id) },
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom,
                                        ) {
                                            Text(text = currentBestLabel, style = typography.titleMedium)
                                            Text(
                                                text = entryCountText,
                                                style = typography.bodySmall,
                                                color = colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordDetail(
    state: PersonalRecordsState,
    family: PersonalRecordFamily,
    currentLocale: Locale,
    onEditEntry: (PersonalRecordEntry) -> Unit,
    onSetManualCurrentEntry: (familyId: Long, entryId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val familyEntries = remember(state.entries, family.id) { state.entries.filter { it.familyId == family.id } }
    val currentBest = PersonalRecordBestSelector.selectBest(family, familyEntries)
    val orderedHistory = PersonalRecordHistoryOrderer.order(family, familyEntries)
    val category = state.categories.firstOrNull { it.id == family.categoryId }
    val currentBestLabel =
        currentBest?.let {
            formatPersonalRecordValue(
                value = it.value,
                unit = it.unit,
                locale = currentLocale,
                unitLabel = unitLabelFor(it.unit, it.customUnitLabel, it.value),
            )
        }
    val currentBestDateLabel =
        currentBest?.let { formatWorkoutDate(it.recordDate, currentLocale) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonalRecordCategoryChip(category = category)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                TitleChip(
                    label = metricLabel(family.metricType),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
                TitleChip(
                    label = comparisonLabel(family.comparisonRule),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
            }
        }

        if (currentBest == null) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val emptyStateColors = personalRecordRelatedColors(category)
                EmptyStateCard(
                    icon = Icons.Outlined.Add,
                    title = stringResource(R.string.personal_records_add_first_result_title),
                    body = stringResource(R.string.personal_records_add_first_result_body),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colorScheme.surfaceContainerHigh,
                    iconContainerColor = emptyStateColors.container,
                    iconContentColor = emptyStateColors.content,
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
                shape = shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(SpacingLg),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    Text(text = stringResource(R.string.personal_records_current_best), style = typography.titleMedium)
                    Text(text = currentBestLabel.orEmpty(), style = typography.headlineMedium)
                    if (currentBestDateLabel != null) {
                        Text(
                            text = currentBestDateLabel,
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Text(text = stringResource(R.string.personal_records_history_title), style = typography.titleMedium)

                if (orderedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateCard(
                            icon = Icons.Outlined.History,
                            title = stringResource(R.string.personal_records_history_empty_title),
                            body = stringResource(R.string.personal_records_history_empty_body),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    orderedHistory.forEach { entry ->
                        PersonalRecordHistoryRow(
                            entry = entry,
                            currentLocale = currentLocale,
                            onClick = { onEditEntry(entry) },
                            isManualSelection = family.comparisonRule == MANUAL,
                            isCurrent = currentBest?.id == entry.id,
                            category = category,
                            onSetCurrent = { onSetManualCurrentEntry(family.id, entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordHistoryRow(
    entry: PersonalRecordEntry,
    currentLocale: Locale,
    onClick: () -> Unit,
    isManualSelection: Boolean,
    isCurrent: Boolean,
    category: Category?,
    onSetCurrent: () -> Unit,
) {
    val currentFlagColors = personalRecordRelatedColors(category)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
        shape = shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(PERSONAL_RECORDS_ENTRY_CARD_TAG_PREFIX + entry.id),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SpacingLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            formatPersonalRecordValue(
                                value = entry.value,
                                unit = entry.unit,
                                locale = currentLocale,
                                unitLabel = unitLabelFor(entry.unit, entry.customUnitLabel, entry.value),
                            ),
                        style = typography.titleMedium,
                    )
                    if (isCurrent) {
                        TitleChip(
                            label = stringResource(R.string.personal_records_current_selected),
                            containerColor = currentFlagColors.container,
                            contentColor = currentFlagColors.content,
                        )
                    }
                }
                Text(
                    text = formatWorkoutDate(entry.recordDate, currentLocale),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note.orEmpty(),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isManualSelection) {
                val selectionDescription =
                    stringResource(
                        if (isCurrent) {
                            R.string.personal_records_current_selected
                        } else {
                            R.string.personal_records_set_current
                        },
                    )
                RadioButton(
                    selected = isCurrent,
                    onClick = {
                        if (!isCurrent) onSetCurrent()
                    },
                    modifier =
                        Modifier
                            .testTag(PERSONAL_RECORDS_SET_CURRENT_TAG_PREFIX + entry.id)
                            .semantics { contentDescription = selectionDescription },
                )
            }
        }
    }
}

@Composable
private fun AddActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    Surface(
        onClick = onClick,
        shape = shapes.extraLarge,
        tonalElevation = ElevationSm,
        shadowElevation = ElevationSm,
        color = containerColor ?: colorScheme.surface,
        contentColor = contentColor ?: colorScheme.onSurface,
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = AddActionPillMinWidth),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AddActionPillHorizontalPadding,
                    vertical = SpacingLg,
                ),
            horizontalArrangement = Arrangement.spacedBy(SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = typography.titleSmall)
        }
    }
}

private data class PersonalRecordRelatedColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun personalRecordRelatedColors(category: Category?): PersonalRecordRelatedColors {
    val container =
        category?.colorId?.let(::categoryAccentColor)?.let { categoryColor ->
            val blend =
                if (isSystemInDarkTheme()) {
                    INDICATOR_EXTRA_BLEND_DARK
                } else {
                    INDICATOR_EXTRA_BLEND_LIGHT
                }
            lerp(categoryColor, colorScheme.surface, blend)
        } ?: colorScheme.primaryContainer
    val content =
        if (category == null) {
            colorScheme.onPrimaryContainer
        } else {
            contentColorForBackground(container)
        }
    return PersonalRecordRelatedColors(container = container, content = content)
}

@Composable
private fun PersonalRecordCategoryChip(category: Category?) {
    val accent = category?.colorId?.let(::categoryAccentColor) ?: colorScheme.surfaceVariant
    val contentColor =
        if (category == null) {
            colorScheme.onSurfaceVariant
        } else {
            contentColorForBackground(accent)
        }

    TitleChip(
        label = category?.name ?: stringResource(R.string.category_uncategorized),
        containerColor = accent,
        contentColor = contentColor,
    )
}

@Composable
private fun PersonalRecordTimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        TimeWheelColumn(
            label = stringResource(R.string.personal_records_unit_hour),
            values = 0..23,
            selectedValue = hours,
            onSelectedValueChange = onHoursChange,
            modifier = Modifier.weight(1f),
        )
        TimeWheelColumn(
            label = stringResource(R.string.personal_records_unit_minute),
            values = 0..59,
            selectedValue = minutes,
            onSelectedValueChange = onMinutesChange,
            modifier = Modifier.weight(1f),
        )
        TimeWheelColumn(
            label = stringResource(R.string.personal_records_unit_second),
            values = 0..59,
            selectedValue = seconds,
            onSelectedValueChange = onSecondsChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TimeWheelColumn(
    label: String,
    values: IntRange,
    selectedValue: Int,
    onSelectedValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(values) { values.toList() }
    val repeatCount = 31
    val wheelEntries =
        remember(entries) {
            buildList(entries.size * repeatCount) {
                repeat(repeatCount) {
                    addAll(entries)
                }
            }
        }
    val centerBlockStart = remember(entries) { (repeatCount / 2) * entries.size }
    val initialIndex =
        remember(selectedValue, entries, centerBlockStart) {
            val selectedIndex = entries.indexOf(selectedValue.coerceIn(values.first, values.last)).coerceAtLeast(0)
            centerBlockStart + selectedIndex
        }
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialIndex,
        )
    var centeredIndex by remember { mutableIntStateOf(initialIndex) }
    var lastHapticValue by remember(entries, selectedValue) { mutableIntStateOf(entries[initialIndex % entries.size]) }
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(listState, wheelEntries) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val visible = layoutInfo.visibleItemsInfo
                if (visible.isEmpty()) return@collect

                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val nearest =
                    visible.minByOrNull { itemInfo ->
                        abs((itemInfo.offset + (itemInfo.size / 2)) - viewportCenter)
                    } ?: return@collect

                centeredIndex = nearest.index.coerceIn(0, wheelEntries.lastIndex)
                val candidate = wheelEntries[centeredIndex]

                if (candidate != selectedValue) {
                    onSelectedValueChange(candidate)
                }

                if (candidate != lastHapticValue) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    lastHapticValue = candidate
                }

                val edgeBuffer = entries.size * 2
                if (centeredIndex < edgeBuffer || centeredIndex > wheelEntries.lastIndex - edgeBuffer) {
                    val candidateIndexInBlock = entries.indexOf(candidate).coerceAtLeast(0)
                    val recenterIndex = centerBlockStart + candidateIndexInBlock
                    if (recenterIndex != centeredIndex) {
                        centeredIndex = recenterIndex
                        coroutineScope.launch {
                            listState.scrollToItem(recenterIndex)
                        }
                    }
                }
            }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        Text(
            text = label,
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(min = PersonalRecordTimeWheelColumnMinWidth)
                    .height(PersonalRecordTimeWheelHeight),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = PersonalRecordTimeWheelContentPadding),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(wheelEntries) { index, value ->
                    val selected = index == centeredIndex
                    val distanceFromCenter = abs(index - centeredIndex).coerceAtMost(4)
                    val itemAlpha =
                        when (distanceFromCenter) {
                            0 -> 1f
                            1 -> 0.82f
                            2 -> 0.58f
                            3 -> 0.38f
                            else -> 0.22f
                        }
                    Surface(
                        onClick = {
                            onSelectedValueChange(value)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                        shape = shapes.small,
                        color =
                            if (selected) {
                                colorScheme.primaryContainer
                            } else {
                                colorScheme.surfaceVariant
                            },
                        contentColor =
                            if (selected) {
                                colorScheme.onPrimaryContainer
                            } else {
                                colorScheme.onSurfaceVariant
                            },
                        tonalElevation = if (selected) ElevationSm else Zero,
                        shadowElevation = if (selected) ElevationSm else Zero,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(PersonalRecordTimeWheelItemHeight)
                                .alpha(itemAlpha),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = value.toString().padStart(2, '0'),
                                style =
                                    if (selected) {
                                        typography.titleMedium
                                    } else {
                                        typography.bodyLarge
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TimeParts(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
)

private fun secondsToTimeParts(totalSeconds: Long): TimeParts {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    val hours = (safeSeconds / 3600L).toInt()
    val minutes = ((safeSeconds % 3600L) / 60L).toInt()
    val seconds = (safeSeconds % 60L).toInt()
    return TimeParts(hours = hours, minutes = minutes, seconds = seconds)
}

@Composable
internal fun PersonalRecordFamilyEditorDialog(
    categories: List<Category>,
    initialFamily: PersonalRecordFamily? = null,
    onDismiss: () -> Unit,
    onSave: (
        categoryId: Long?,
        title: String,
        metricType: PersonalRecordMetricType,
        comparisonRule: PersonalRecordComparisonRule,
    ) -> Unit,
) {
    val isEdit = initialFamily != null
    var title by rememberSaveable(initialFamily?.id) {
        mutableStateOf(initialFamily?.title.orEmpty().capitalizedFirstCharacter())
    }
    var categoryId by rememberSaveable(initialFamily?.id) { mutableStateOf(initialFamily?.categoryId) }
    var metricType by rememberSaveable(initialFamily?.id) { mutableStateOf(initialFamily?.metricType ?: DISTANCE) }
    var comparisonRule by rememberSaveable(initialFamily?.id) {
        mutableStateOf(initialFamily?.comparisonRule ?: metricType.defaultComparisonRule())
    }
    var metricMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var comparisonMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var didInitializeMetricRule by rememberSaveable(initialFamily?.id) { mutableStateOf(false) }

    LaunchedEffect(metricType) {
        if (didInitializeMetricRule) {
            comparisonRule = metricType.defaultComparisonRule()
        } else {
            didInitializeMetricRule = true
        }
    }

    AlertDialog(
        modifier =
            Modifier.testTag(
                if (isEdit) {
                    PERSONAL_RECORDS_EDIT_FAMILY_DIALOG_TAG
                } else {
                    PERSONAL_RECORDS_CREATE_FAMILY_DIALOG_TAG
                },
            ),
        onDismissRequest = onDismiss,
        title =
            {
                Text(
                    text =
                        if (isEdit) {
                            stringResource(R.string.personal_records_edit_family_title)
                        } else {
                            stringResource(R.string.personal_records_new_family_title)
                        },
                )
            },
        text = {
            KeyboardAwareDialogForm {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.capitalizedFirstCharacter() },
                    label = { Text(text = stringResource(R.string.personal_records_family_title)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                CategoryPickerField(
                    label = stringResource(R.string.personal_records_family_category),
                    categories =
                        remember(categories) {
                            categories.map { category ->
                                CategoryPickerOption(
                                    id = category.id,
                                    name = category.name,
                                    colorId = category.colorId,
                                )
                            }
                        },
                    selectedCategoryId = categoryId,
                    onCategorySelected = { categoryId = it },
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                if (isEdit) {
                    OutlinedTextField(
                        readOnly = true,
                        value = metricLabel(metricType),
                        onValueChange = {},
                        label = { Text(text = stringResource(R.string.personal_records_family_metric_type)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = metricMenuExpanded,
                        onExpandedChange = { metricMenuExpanded = !metricMenuExpanded },
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = metricLabel(metricType),
                            onValueChange = {},
                            label = {
                                Text(
                                    text = stringResource(R.string.personal_records_family_metric_type),
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = metricMenuExpanded)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    ),
                        )

                        DropdownMenu(
                            expanded = metricMenuExpanded,
                            onDismissRequest = { metricMenuExpanded = false },
                        ) {
                            PersonalRecordMetricType.entries.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(SpacingXxs)) {
                                            Text(text = metricLabel(option))
                                            Text(
                                                text = metricDescription(option),
                                                style = typography.bodySmall,
                                                color = colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    },
                                    onClick = {
                                        metricType = option
                                        metricMenuExpanded = false
                                    },
                                )
                                if (index != PersonalRecordMetricType.entries.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = SpacingXs),
                                        color = colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = metricDescription(metricType),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = SpacingXs),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                ExposedDropdownMenuBox(
                    expanded = comparisonMenuExpanded,
                    onExpandedChange = { comparisonMenuExpanded = !comparisonMenuExpanded },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = comparisonLabel(comparisonRule),
                        onValueChange = {},
                        label = { Text(text = stringResource(R.string.personal_records_family_comparison_rule)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = comparisonMenuExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    DropdownMenu(
                        expanded = comparisonMenuExpanded,
                        onDismissRequest = { comparisonMenuExpanded = false },
                    ) {
                        listOf(HIGHER_IS_BETTER, LOWER_IS_BETTER, MANUAL).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = comparisonLabel(option)) },
                                onClick = {
                                    comparisonRule = option
                                    comparisonMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        categoryId,
                        title.trim(),
                        metricType,
                        comparisonRule,
                    )
                },
            ) {
                Text(
                    text =
                        if (isEdit) {
                            stringResource(R.string.save_changes)
                        } else {
                            stringResource(R.string.personal_records_create)
                        },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )
}

@Composable
internal fun PersonalRecordEntryEditorDialog(
    families: List<PersonalRecordFamily>,
    entries: List<PersonalRecordEntry>,
    initialFamilyId: Long?,
    settingsDistanceUnit: DistanceUnit,
    settingsWeightUnit: WeightUnit,
    initialEntry: PersonalRecordEntry? = null,
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        familyId: Long,
        value: Double,
        unit: PersonalRecordUnit,
        recordDate: LocalDate,
        note: String?,
        customUnitLabel: String?,
    ) -> Unit,
    onDeleteRequested: (() -> Unit)? = null,
) {
    val initialDialogFamilyId = initialEntry?.familyId ?: initialFamilyId ?: families.firstOrNull()?.id
    val dialogKey = initialEntry?.id ?: initialDialogFamilyId ?: -1L
    var familyId by rememberSaveable(dialogKey) { mutableStateOf(initialDialogFamilyId) }
    var valueText by rememberSaveable(dialogKey) { mutableStateOf("") }
    var note by rememberSaveable(dialogKey) {
        mutableStateOf(initialEntry?.note.orEmpty().capitalizedFirstCharacter())
    }
    var customUnitLabel by rememberSaveable(dialogKey) { mutableStateOf(initialEntry?.customUnitLabel.orEmpty()) }
    var familyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var unitMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var recordDate by rememberSaveable(dialogKey) { mutableStateOf(initialEntry?.recordDate ?: LocalDate.now()) }
    var selectedUnit by rememberSaveable(dialogKey) { mutableStateOf(initialEntry?.unit ?: KILOMETER) }
    val selectedFamily = families.firstOrNull { it.id == familyId }
    val isTimeMetric = selectedFamily?.metricType == TIME
    val isDistanceOrWeightMetric = selectedFamily?.metricType == DISTANCE || selectedFamily?.metricType == WEIGHT
    val today = remember { LocalDate.now() }
    val currentEntry =
        remember(familyId, entries, initialEntry?.id, isEdit) {
            if (isEdit) {
                initialEntry
            } else {
                selectedFamily?.let { PersonalRecordBestSelector.selectBest(it, entries) }
            }
        }
    val initialTimeParts =
        remember(initialEntry?.id, selectedFamily?.id, currentEntry?.id) {
            when {
                isEdit && initialEntry != null && selectedFamily?.metricType == TIME ->
                    secondsToTimeParts(
                        PersonalRecordValueNormalizer.normalize(initialEntry.value, initialEntry.unit).toLong(),
                    )

                !isEdit && selectedFamily?.metricType == TIME && currentEntry != null ->
                    secondsToTimeParts(
                        PersonalRecordValueNormalizer.normalize(currentEntry.value, currentEntry.unit).toLong(),
                    )

                else -> TimeParts()
            }
        }
    var timeHours by rememberSaveable(dialogKey) { mutableStateOf(initialTimeParts.hours) }
    var timeMinutes by rememberSaveable(dialogKey) { mutableStateOf(initialTimeParts.minutes) }
    var timeSeconds by rememberSaveable(dialogKey) { mutableStateOf(initialTimeParts.seconds) }
    var hasLoadedInitialState by rememberSaveable(dialogKey) { mutableStateOf(false) }

    LaunchedEffect(familyId) {
        val family = selectedFamily ?: return@LaunchedEffect

        if (isEdit) {
            if (!hasLoadedInitialState && initialEntry != null) {
                hasLoadedInitialState = true
                selectedUnit = initialEntry.unit
                recordDate = initialEntry.recordDate
                note = initialEntry.note.orEmpty().capitalizedFirstCharacter()
                customUnitLabel = initialEntry.customUnitLabel.orEmpty()
                if (family.metricType == TIME) {
                    val parts =
                        secondsToTimeParts(
                            PersonalRecordValueNormalizer.normalize(initialEntry.value, initialEntry.unit).toLong(),
                        )
                    timeHours = parts.hours
                    timeMinutes = parts.minutes
                    timeSeconds = parts.seconds
                    valueText = ""
                } else {
                    valueText = formatEditablePersonalRecordValue(initialEntry.value)
                }
            }
            return@LaunchedEffect
        }

        hasLoadedInitialState = true
        recordDate = today
        note = ""
        customUnitLabel = currentEntry?.customUnitLabel.orEmpty()

        when (family.metricType) {
            TIME -> {
                selectedUnit = SECOND
                val parts =
                    currentEntry?.let {
                        secondsToTimeParts(
                            PersonalRecordValueNormalizer.normalize(it.value, it.unit).toLong(),
                        )
                    } ?: TimeParts()
                timeHours = parts.hours
                timeMinutes = parts.minutes
                timeSeconds = parts.seconds
                valueText = ""
            }

            DISTANCE -> {
                selectedUnit = settingsDistanceUnit.asPersonalRecordUnit()
                valueText =
                    currentEntry?.let {
                        formatEditablePersonalRecordValue(
                            PersonalRecordValueNormalizer.convert(it.value, it.unit, selectedUnit),
                        )
                    }.orEmpty()
            }

            WEIGHT -> {
                selectedUnit = settingsWeightUnit.asPersonalRecordUnit()
                valueText =
                    currentEntry?.let {
                        formatEditablePersonalRecordValue(
                            PersonalRecordValueNormalizer.convert(it.value, it.unit, selectedUnit),
                        )
                    }.orEmpty()
            }

            else -> {
                selectedUnit = currentEntry?.unit ?: family.defaultUnit
                valueText = currentEntry?.let { formatEditablePersonalRecordValue(it.value) }.orEmpty()
            }
        }
    }

    val canSave =
        familyId != null &&
            (if (isTimeMetric) true else parsePersonalRecordValue(valueText) != null) &&
            (!isDistanceOrWeightMetric || selectedUnit != CUSTOM_UNIT || customUnitLabel.isNotBlank()) &&
            !recordDate.isAfter(today)
    AlertDialog(
        modifier =
            Modifier.testTag(
                if (isEdit) {
                    PERSONAL_RECORDS_EDIT_ENTRY_DIALOG_TAG
                } else {
                    PERSONAL_RECORDS_ADD_ENTRY_DIALOG_TAG
                },
            ),
        onDismissRequest = onDismiss,
        title =
            {
                Text(
                    text =
                        if (isEdit) {
                            stringResource(R.string.personal_records_edit_result_title)
                        } else {
                            stringResource(R.string.personal_records_add_result_title)
                        },
                )
            },
        text = {
            KeyboardAwareDialogForm {
                ExposedDropdownMenuBox(
                    expanded = familyMenuExpanded,
                    onExpandedChange = {
                        if (!isEdit) {
                            familyMenuExpanded = !familyMenuExpanded
                        }
                    },
                ) {
                    OutlinedTextField(
                        enabled = !isEdit,
                        readOnly = true,
                        value = familyLabelFor(selectedFamily, entries),
                        onValueChange = {},
                        label = { Text(text = stringResource(R.string.personal_records_entry_family)) },
                        trailingIcon = {
                            if (!isEdit) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyMenuExpanded)
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(PERSONAL_RECORDS_ENTRY_FAMILY_FIELD_TAG)
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    DropdownMenu(
                        expanded = familyMenuExpanded,
                        onDismissRequest = { familyMenuExpanded = false },
                    ) {
                        families.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = familyLabelFor(option, entries)) },
                                onClick = {
                                    familyId = option.id
                                    familyMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingLg))

                if (isTimeMetric) {
                    key(selectedFamily?.id ?: -1L, isTimeMetric) {
                        PersonalRecordTimePicker(
                            hours = timeHours,
                            minutes = timeMinutes,
                            seconds = timeSeconds,
                            onHoursChange = { timeHours = it },
                            onMinutesChange = { timeMinutes = it },
                            onSecondsChange = { timeSeconds = it },
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SpacingSm),
                    ) {
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { valueText = it },
                            label = { Text(text = stringResource(R.string.personal_records_entry_value)) },
                            singleLine = true,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag(PERSONAL_RECORDS_ENTRY_VALUE_FIELD_TAG),
                        )

                        if (isDistanceOrWeightMetric) {
                            ExposedDropdownMenuBox(
                                expanded = unitMenuExpanded,
                                onExpandedChange = { unitMenuExpanded = !unitMenuExpanded },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = unitChoiceLabelFor(selectedUnit),
                                    onValueChange = {},
                                    label = { Text(text = stringResource(R.string.personal_records_entry_unit)) },
                                    singleLine = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded)
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                )

                                DropdownMenu(
                                    expanded = unitMenuExpanded,
                                    onDismissRequest = { unitMenuExpanded = false },
                                ) {
                                    selectedFamily?.metricType?.supportedUnits().orEmpty().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Column(verticalArrangement = Arrangement.spacedBy(SpacingXxs)) {
                                                    Text(text = unitChoiceLabelFor(option))
                                                    Text(
                                                        text = unitValueLabelFor(option),
                                                        style = typography.bodySmall,
                                                        color = colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            },
                                            onClick = {
                                                val previousUnit = selectedUnit
                                                selectedUnit = option
                                                unitMenuExpanded = false
                                                parsePersonalRecordValue(valueText)?.let { parsed ->
                                                    valueText =
                                                        formatEditablePersonalRecordValue(
                                                            PersonalRecordValueNormalizer.convert(
                                                                parsed,
                                                                previousUnit,
                                                                option,
                                                            ),
                                                        )
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isTimeMetric && selectedUnit == CUSTOM_UNIT) {
                    Spacer(modifier = Modifier.height(SpacingLg))

                    OutlinedTextField(
                        value = customUnitLabel,
                        onValueChange = { customUnitLabel = it },
                        label = { Text(text = stringResource(R.string.personal_records_entry_custom_unit_label)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(SpacingLg))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(PERSONAL_RECORDS_ENTRY_DATE_FIELD_TAG),
                ) {
                    OutlinedTextField(
                        value = formatWorkoutDate(recordDate, Locale.getDefault()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(R.string.personal_records_entry_date)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    showDatePicker = true
                                },
                    )
                }

                Spacer(modifier = Modifier.height(SpacingLg))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.capitalizedFirstCharacter() },
                    label = { Text(text = stringResource(R.string.personal_records_entry_note)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEdit && onDeleteRequested != null) {
                    IconButton(onClick = onDeleteRequested) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.personal_records_delete_result_confirm),
                            tint = colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }

                TextButton(
                    enabled = canSave,
                    onClick = {
                        val resolvedFamilyId = familyId ?: return@TextButton
                        val resolvedValue =
                            if (isTimeMetric) {
                                (timeHours * 3600 + timeMinutes * 60 + timeSeconds).toDouble()
                            } else {
                                parsePersonalRecordValue(valueText) ?: return@TextButton
                            }
                        val resolvedUnit = if (isTimeMetric) SECOND else selectedUnit
                        onSave(
                            resolvedFamilyId,
                            resolvedValue,
                            resolvedUnit,
                            recordDate,
                            note.trim().ifBlank { null },
                            customUnitLabel.trim().ifBlank { null },
                        )
                    },
                ) {
                    Text(
                        text =
                            if (isEdit) {
                                stringResource(R.string.save_changes)
                            } else {
                                stringResource(R.string.personal_records_save_result)
                            },
                    )
                }
            }
        },
    )

    if (showDatePicker) {
        val selectedDateMillis =
            remember(recordDate) {
                recordDate.toUtcEpochMillis()
            }
        val maximumSelectableDateMillis = remember(today) { today.toUtcEpochMillis() }
        val selectableDates =
            remember(maximumSelectableDateMillis) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= maximumSelectableDateMillis
                    }
                }
            }
        val datePickerState =
            remember(selectedDateMillis, selectableDates) {
                DatePickerState(
                    locale = Locale.getDefault(),
                    initialSelectedDateMillis = selectedDateMillis.coerceAtMost(maximumSelectableDateMillis),
                    selectableDates = selectableDates,
                )
            }

        HermesDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            recordDate = it.toUtcLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.personal_records_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private data class PersonalRecordFamilyGroup(
    val category: Category?,
    val families: List<PersonalRecordFamily>,
)

private fun buildFamilyGroups(
    categories: List<Category>,
    families: List<PersonalRecordFamily>,
): List<PersonalRecordFamilyGroup> {
    val grouped = families.groupBy { it.categoryId }
    val categoryGroups =
        categories
            .sortedBy { it.sortOrder }
            .mapNotNull { category ->
                val categoryFamilies = grouped[category.id].orEmpty().sortedWith(compareBy({ it.sortOrder }, { it.id }))
                if (categoryFamilies.isEmpty()) {
                    null
                } else {
                    PersonalRecordFamilyGroup(category = category, families = categoryFamilies)
                }
            }

    val uncategorizedFamilies = grouped[null].orEmpty().sortedWith(compareBy({ it.sortOrder }, { it.id }))
    val uncategorizedGroup =
        if (uncategorizedFamilies.isEmpty()) {
            emptyList()
        } else {
            listOf(
                PersonalRecordFamilyGroup(
                    category = null,
                    families = uncategorizedFamilies,
                ),
            )
        }

    return categoryGroups + uncategorizedGroup
}

@Composable
private fun categoryLabelFor(
    categoryId: Long?,
    categories: List<Category>,
): String {
    return if (categoryId == null) {
        stringResource(R.string.category_uncategorized)
    } else {
        categories.firstOrNull { it.id == categoryId }?.name ?: stringResource(R.string.category_uncategorized)
    }
}

@Composable
private fun familyLabelFor(
    family: PersonalRecordFamily?,
    entries: List<PersonalRecordEntry>,
): String {
    if (family == null) return stringResource(R.string.personal_records_entry_family_empty)
    val count = entries.count { it.familyId == family.id }
    return "${family.title} (${pluralStringResource(R.plurals.personal_records_family_entry_count, count, count)})"
}

@Composable
private fun metricLabel(metricType: PersonalRecordMetricType): String {
    return when (metricType) {
        DISTANCE -> stringResource(R.string.personal_records_metric_distance)
        TIME -> stringResource(R.string.personal_records_metric_time)
        WEIGHT -> stringResource(R.string.personal_records_metric_weight)
        POWER -> stringResource(R.string.personal_records_metric_power)
        REPS -> stringResource(R.string.personal_records_metric_reps)
        CUSTOM -> stringResource(R.string.personal_records_metric_custom)
    }
}

@Composable
private fun comparisonLabel(comparisonRule: PersonalRecordComparisonRule): String {
    return when (comparisonRule) {
        HIGHER_IS_BETTER -> stringResource(R.string.personal_records_comparison_higher)
        LOWER_IS_BETTER -> stringResource(R.string.personal_records_comparison_lower)
        MANUAL -> stringResource(R.string.personal_records_comparison_manual)
    }
}

@Composable
private fun metricDescription(metricType: PersonalRecordMetricType): String {
    return when (metricType) {
        DISTANCE -> stringResource(R.string.personal_records_metric_distance_help)
        TIME -> stringResource(R.string.personal_records_metric_time_help)
        WEIGHT -> stringResource(R.string.personal_records_metric_weight_help)
        POWER -> stringResource(R.string.personal_records_metric_power_help)
        REPS -> stringResource(R.string.personal_records_metric_reps_help)
        CUSTOM -> stringResource(R.string.personal_records_metric_custom_help)
    }
}

@Composable
private fun unitChoiceLabelFor(unit: PersonalRecordUnit): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.personal_records_unit_kilometer)
        MILE -> stringResource(R.string.personal_records_unit_mile)
        METER -> stringResource(R.string.personal_records_unit_meter)
        SECOND -> stringResource(R.string.personal_records_unit_second)
        MINUTE -> stringResource(R.string.personal_records_unit_minute)
        HOUR -> stringResource(R.string.personal_records_unit_hour)
        KILOGRAM -> stringResource(R.string.personal_records_unit_kilogram)
        POUND -> stringResource(R.string.personal_records_unit_pound)
        WATT -> stringResource(R.string.personal_records_unit_watt)
        REP -> stringResource(R.string.personal_records_unit_rep)
        CUSTOM_UNIT -> stringResource(R.string.personal_records_unit_custom)
    }
}

@Composable
private fun unitValueLabelFor(unit: PersonalRecordUnit): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.settings_unit_kilometers)
        MILE -> stringResource(R.string.settings_unit_miles)
        METER -> stringResource(R.string.personal_records_unit_meter_symbol)
        SECOND -> stringResource(R.string.personal_records_unit_second_symbol)
        MINUTE -> stringResource(R.string.personal_records_unit_minute_symbol)
        HOUR -> stringResource(R.string.personal_records_unit_hour_symbol)
        KILOGRAM -> stringResource(R.string.settings_unit_kilograms)
        POUND -> stringResource(R.string.settings_unit_pounds)
        WATT -> stringResource(R.string.personal_records_unit_watt_symbol)
        REP -> stringResource(R.string.personal_records_unit_rep_singular)
        CUSTOM_UNIT -> stringResource(R.string.personal_records_unit_custom)
    }
}

@Composable
private fun unitLabelFor(
    unit: PersonalRecordUnit,
    customLabel: String? = null,
    quantity: Double? = null,
): String {
    return when (unit) {
        KILOMETER -> stringResource(R.string.settings_unit_kilometers)
        MILE -> stringResource(R.string.settings_unit_miles)
        METER -> stringResource(R.string.personal_records_unit_meter_symbol)
        SECOND -> stringResource(R.string.personal_records_unit_second_symbol)
        MINUTE -> stringResource(R.string.personal_records_unit_minute_symbol)
        HOUR -> stringResource(R.string.personal_records_unit_hour_symbol)
        KILOGRAM -> stringResource(R.string.settings_unit_kilograms)
        POUND -> stringResource(R.string.settings_unit_pounds)
        WATT -> stringResource(R.string.personal_records_unit_watt_symbol)
        REP ->
            if (quantity == 1.0) {
                stringResource(R.string.personal_records_unit_rep_singular)
            } else {
                stringResource(R.string.personal_records_unit_rep_plural)
            }
        CUSTOM_UNIT -> customLabel?.ifBlank { null } ?: stringResource(R.string.personal_records_unit_custom)
    }
}

private fun DistanceUnit.asPersonalRecordUnit(): PersonalRecordUnit {
    return when (this) {
        DistanceUnit.KILOMETERS -> KILOMETER
        DistanceUnit.MILES -> MILE
    }
}

private fun WeightUnit.asPersonalRecordUnit(): PersonalRecordUnit {
    return when (this) {
        WeightUnit.KILOGRAMS -> KILOGRAM
        WeightUnit.POUNDS -> POUND
    }
}

internal fun parsePersonalRecordValue(valueText: String): Double? {
    return valueText
        .trim()
        .replace(',', '.')
        .toDoubleOrNull()
}
