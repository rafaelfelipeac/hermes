@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.time.DurationParts
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.capitalizedFirstCharacter
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelColumnMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelContentPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PersonalRecordTimeWheelItemHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordBestSelector
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.supportedUnits
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.CUSTOM as CUSTOM_UNIT

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
    var valueText by rememberSaveable(dialogKey) { mutableStateOf(EMPTY) }
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
    val currentLocale = currentLocale()
    val currentEntry =
        remember(familyId, entries, initialEntry?.id, isEdit) {
            if (isEdit) {
                initialEntry
            } else {
                selectedFamily?.let { PersonalRecordBestSelector.selectBest(it, entries) }
            }
        }
    val initialDefaults =
        remember(initialEntry?.id, selectedFamily?.id, currentEntry?.id, isEdit) {
            personalRecordEntryEditorDefaults(
                PersonalRecordEntryEditorSource(
                    family = selectedFamily,
                    entries = entries,
                    initialEntry = initialEntry,
                    isEdit = isEdit,
                    settingsDistanceUnit = settingsDistanceUnit,
                    settingsWeightUnit = settingsWeightUnit,
                ),
            )
        }
    var timeHours by rememberSaveable(dialogKey) { mutableIntStateOf(initialDefaults.time.hours) }
    var timeMinutes by rememberSaveable(dialogKey) { mutableIntStateOf(initialDefaults.time.minutes) }
    var timeSeconds by rememberSaveable(dialogKey) { mutableIntStateOf(initialDefaults.time.seconds) }
    var hasLoadedInitialState by rememberSaveable(dialogKey) { mutableStateOf(false) }

    LaunchedEffect(familyId) {
        selectedFamily ?: return@LaunchedEffect
        if (isEdit && hasLoadedInitialState) return@LaunchedEffect
        hasLoadedInitialState = true
        selectedUnit = initialDefaults.unit
        valueText = initialDefaults.valueText
        timeHours = initialDefaults.time.hours
        timeMinutes = initialDefaults.time.minutes
        timeSeconds = initialDefaults.time.seconds
        customUnitLabel = initialDefaults.customUnitLabel
        recordDate = if (isEdit) initialEntry?.recordDate ?: today else today
        note = if (isEdit) initialEntry?.note.orEmpty().capitalizedFirstCharacter() else EMPTY
    }

    val canSave =
        canSavePersonalRecordEntry(
            family = selectedFamily,
            fields =
                PersonalRecordEntryEditorFields(
                    valueText = valueText,
                    selectedUnit = selectedUnit,
                    time = DurationParts(timeHours, timeMinutes, timeSeconds),
                    recordDate = recordDate,
                    note = note,
                    customUnitLabel = customUnitLabel,
                ),
            today = today,
        )
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
                    key(selectedFamily.id, true) {
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
                                KeyboardOptions(
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
                                    selectedFamily.metricType.supportedUnits().forEach { option ->
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
                                                valueText =
                                                    convertPersonalRecordEditorValue(
                                                        valueText = valueText,
                                                        fromUnit = previousUnit,
                                                        toUnit = option,
                                                    )
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
                        value = formatWorkoutDate(recordDate, currentLocale),
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
                        val input =
                            buildPersonalRecordEntryInput(
                                family = selectedFamily,
                                fields =
                                    PersonalRecordEntryEditorFields(
                                        valueText = valueText,
                                        selectedUnit = selectedUnit,
                                        time =
                                            DurationParts(
                                                hours = timeHours,
                                                minutes = timeMinutes,
                                                seconds = timeSeconds,
                                            ),
                                        recordDate = recordDate,
                                        note = note,
                                        customUnitLabel = customUnitLabel,
                                    ),
                            ) ?: return@TextButton
                        onSave(
                            input.familyId,
                            input.value,
                            input.unit,
                            input.recordDate,
                            input.note,
                            input.customUnitLabel,
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
            remember(selectedDateMillis, selectableDates, currentLocale) {
                DatePickerState(
                    locale = currentLocale,
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
