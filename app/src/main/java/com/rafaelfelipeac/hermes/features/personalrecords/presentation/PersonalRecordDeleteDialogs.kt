package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R

@Composable
internal fun PersonalRecordDeleteFamilyDialog(
    entryCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.personal_records_delete_family_confirm))
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
internal fun PersonalRecordDeleteEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.personal_records_delete_result_title))
        },
        text = {
            Text(text = stringResource(R.string.personal_records_delete_result_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.personal_records_delete_result_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )
}
