@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerField
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerOption
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.capitalizedFirstCharacter
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.defaultComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE

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
            comparisonRule = comparisonRuleAfterMetricSelection(metricType)
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
