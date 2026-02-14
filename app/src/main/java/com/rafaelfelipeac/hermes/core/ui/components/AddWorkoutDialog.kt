@file:OptIn(ExperimentalMaterial3Api::class)

package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.preview.AddWorkoutDialogPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.AddWorkoutDialogPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi

@Composable
fun AddWorkoutDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (type: String, description: String, categoryId: Long?) -> Unit,
    onManageCategories: (type: String, description: String, categoryId: Long?) -> Unit,
    isEdit: Boolean,
    categories: List<CategoryUi>,
    selectedCategoryId: Long?,
    initialType: String = EMPTY,
    initialDescription: String = EMPTY,
) {
    var type by rememberSaveable(initialType) { mutableStateOf(initialType) }
    var description by rememberSaveable(initialDescription) { mutableStateOf(initialDescription) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var currentCategoryId by rememberSaveable(selectedCategoryId) { mutableStateOf(selectedCategoryId) }
    val currentCategory = categories.firstOrNull { it.id == currentCategoryId }
    val categoryLabel =
        currentCategory?.name ?: stringResource(R.string.category_uncategorized)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    if (isEdit) {
                        stringResource(R.string.edit_workout)
                    } else {
                        stringResource(R.string.add_workout)
                    },
            )
        },
        text = {
            Column(modifier = modifier) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text(text = stringResource(R.string.add_workout_title)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(R.string.add_workout_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = categoryLabel,
                        onValueChange = {},
                        label = { Text(text = stringResource(R.string.add_workout_category)) },
                        leadingIcon = {
                            CategoryColorDot(colorId = currentCategory?.colorId)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryColorDot(colorId = category.colorId)
                                        Spacer(modifier = Modifier.width(SpacingSm))
                                        Text(text = category.name)
                                    }
                                },
                                onClick = {
                                    currentCategoryId = category.id
                                    expanded = false
                                },
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = SpacingSm))

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.manage_categories)) },
                            onClick = {
                                expanded = false
                                onManageCategories(type, description, currentCategoryId)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(type.trim(), description.trim(), currentCategoryId) },
                enabled = type.isNotBlank(),
            ) {
                Text(
                    text =
                        if (isEdit) {
                            stringResource(R.string.save_changes)
                        } else {
                            stringResource(R.string.add_workout_confirm)
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
private fun CategoryColorDot(colorId: String?) {
    val dotColor =
        if (colorId == null) {
            Color.Transparent
        } else {
            categoryAccentColor(colorId)
        }

    Box(
        modifier =
            Modifier
                .size(SpacingMd)
                .background(dotColor, CircleShape),
    )
}

@Preview(showBackground = true)
@Composable
private fun AddWorkoutDialogPreview(
    @PreviewParameter(AddWorkoutDialogPreviewProvider::class)
    preview: AddWorkoutDialogPreviewData,
) {
    AddWorkoutDialog(
        onDismiss = {},
        onSave = { _, _, _ -> },
        onManageCategories = { _, _, _ -> },
        isEdit = preview.isEdit,
        categories = emptyList(),
        selectedCategoryId = null,
        initialType = preview.initialType,
        initialDescription = preview.initialDescription,
    )
}
