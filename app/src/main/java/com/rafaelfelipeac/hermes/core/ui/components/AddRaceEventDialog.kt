@file:OptIn(ExperimentalMaterial3Api::class)

package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.ConfigurationCompat
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PlannedItemDialogContentMaxHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import java.time.LocalDate
import java.util.Locale

internal const val RACE_EVENT_DIALOG_TITLE_FIELD_TAG = "race_event_dialog_title_field"
internal const val RACE_EVENT_DIALOG_DESCRIPTION_FIELD_TAG = "race_event_dialog_description_field"

@Composable
fun AddRaceEventDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, categoryId: Long?, eventDate: LocalDate) -> Unit,
    onManageCategories: (title: String, description: String, categoryId: Long?, eventDate: LocalDate?) -> Unit,
    isEdit: Boolean,
    categories: List<CategoryUi>,
    selectedCategoryId: Long?,
    weekStartDay: WeekStartDay,
    selectedDate: LocalDate? = null,
    initialTitle: String = EMPTY,
    initialDescription: String = EMPTY,
) {
    val configuration = LocalConfiguration.current
    val currentLocale =
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle.capitalizedFirstCharacter()) }
    var description by rememberSaveable(initialDescription) {
        mutableStateOf(initialDescription.capitalizedFirstCharacter())
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var currentCategoryId by rememberSaveable(selectedCategoryId) { mutableStateOf(selectedCategoryId) }
    var eventDate by remember(selectedDate) { mutableStateOf(selectedDate) }
    val currentCategory = categories.firstOrNull { it.id == currentCategoryId }
    val currentCategoryAccent = currentCategory?.colorId?.let(::categoryAccentColor)
    val categoryLabel =
        currentCategory?.name ?: stringResource(R.string.category_uncategorized)
    val dateLabel = eventDate?.let { formatWorkoutDate(it, currentLocale) }.orEmpty()

    LaunchedEffect(categories, currentCategoryId) {
        if (currentCategoryId != null && categories.none { it.id == currentCategoryId }) {
            currentCategoryId = UNCATEGORIZED_ID
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    if (isEdit) {
                        stringResource(R.string.race_event_dialog_edit_race_event)
                    } else {
                        stringResource(R.string.race_event_dialog_add_race_event)
                    },
            )
        },
        text = {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .heightIn(max = PlannedItemDialogContentMaxHeight)
                        .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.capitalizedFirstCharacter() },
                    label = { Text(text = stringResource(R.string.workout_dialog_add_workout_title)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(RACE_EVENT_DIALOG_TITLE_FIELD_TAG),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.capitalizedFirstCharacter() },
                    label = { Text(text = stringResource(R.string.workout_dialog_add_workout_description)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(RACE_EVENT_DIALOG_DESCRIPTION_FIELD_TAG),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(R.string.race_event_dialog_date)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true },
                    )
                }

                Spacer(modifier = Modifier.height(SpacingLg))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = categoryLabel,
                        onValueChange = {},
                        label = { Text(text = stringResource(R.string.workout_dialog_add_workout_category)) },
                        textStyle = TextStyle(color = Color.Transparent),
                        prefix = {
                            TitleChip(
                                label = categoryLabel,
                                containerColor =
                                    currentCategoryAccent ?: colorScheme.surfaceVariant,
                                contentColor =
                                    if (currentCategoryAccent == null) {
                                        colorScheme.onSurfaceVariant
                                    } else {
                                        contentColorForBackground(currentCategoryAccent)
                                    },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { category ->
                            val accent = categoryAccentColor(category.colorId)
                            DropdownMenuItem(
                                text = {
                                    TitleChip(
                                        label = category.name,
                                        containerColor = accent,
                                        contentColor = contentColorForBackground(accent),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = {
                                    currentCategoryId = category.id
                                    expanded = false
                                },
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = SpacingSm))

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.workout_dialog_manage_categories),
                                    color = colorScheme.primary,
                                )
                            },
                            onClick = {
                                expanded = false
                                onManageCategories(title, description, currentCategoryId, eventDate)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    eventDate?.let {
                        onSave(title.trim(), description.trim(), currentCategoryId, it)
                    }
                },
                enabled = title.isNotBlank() && eventDate != null,
            ) {
                Text(
                    text =
                        if (isEdit) {
                            stringResource(R.string.save_changes)
                        } else {
                            stringResource(R.string.race_event_dialog_add_race_event_confirm)
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

    if (showDatePicker) {
        val selectedDateMillis = eventDate?.toUtcEpochMillis()
        val minimumSelectableDateMillis = remember { LocalDate.now().toUtcEpochMillis() }
        val selectableDates =
            remember(minimumSelectableDateMillis) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= minimumSelectableDateMillis
                    }
                }
            }
        val datePickerState =
            remember(selectedDateMillis, selectableDates) {
                DatePickerState(
                    locale = currentLocale,
                    initialSelectedDateMillis = selectedDateMillis,
                    selectableDates = selectableDates,
                )
            }
        datePickerState.applyWeekStartDayOverride(weekStartDay)

        DatePickerDialog(
            modifier = Modifier.padding(horizontal = SpacingXl),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            eventDate = millis.toUtcLocalDate()
                            showDatePicker = false
                        }
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
            DatePicker(state = datePickerState)
        }
    }
}
