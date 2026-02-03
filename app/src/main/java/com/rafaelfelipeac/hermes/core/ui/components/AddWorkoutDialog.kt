package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.preview.AddWorkoutDialogPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.AddWorkoutDialogPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg

@Composable
fun AddWorkoutDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (type: String, description: String) -> Unit,
    isEdit: Boolean,
    initialType: String = EMPTY,
    initialDescription: String = EMPTY,
) {
    var type by rememberSaveable { mutableStateOf(initialType) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }

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
                    label = { Text(text = stringResource(R.string.add_workout_type)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(R.string.add_workout_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(type.trim(), description.trim()) },
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

@Preview(showBackground = true)
@Composable
private fun AddWorkoutDialogPreview(
    @PreviewParameter(AddWorkoutDialogPreviewProvider::class)
    preview: AddWorkoutDialogPreviewData,
) {
    AddWorkoutDialog(
        onDismiss = {},
        onSave = { _, _ -> },
        isEdit = preview.isEdit,
        initialType = preview.initialType,
        initialDescription = preview.initialDescription,
    )
}
