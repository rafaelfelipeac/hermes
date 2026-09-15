package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.HermesDatePickerDialog
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.components.toUtcEpochMillis
import com.rafaelfelipeac.hermes.core.ui.components.toUtcLocalDate
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import java.time.LocalDate

@Composable
internal fun ChallengeProgressDialog(
    title: String,
    date: LocalDate,
    quantity: String,
    validationMessage: String?,
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
            KeyboardAwareDialogForm {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    validationMessage?.let { message ->
                        Text(text = message, color = colorScheme.error)
                    }
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = onQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_progress_quantity)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )
                    ChallengeDialogDateField(
                        label = stringResource(R.string.challenges_field_progress_date),
                        date = date,
                        onClick = { showDatePicker = true },
                    )
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
        ChallengeDatePickerDialog(
            date = date,
            onDateSelected = onDateChange,
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
internal fun ChallengeDialogDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLocale = currentLocale()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = date?.let { formatWorkoutDate(it, currentLocale) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChallengeDatePickerDialog(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedDateMillis = remember(date) { date.toUtcEpochMillis() }
    val currentLocale = currentLocale()
    val datePickerState =
        remember(selectedDateMillis, currentLocale) {
            DatePickerState(
                locale = currentLocale,
                initialSelectedDateMillis = selectedDateMillis,
            )
        }

    HermesDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it.toUtcLocalDate()) }
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
