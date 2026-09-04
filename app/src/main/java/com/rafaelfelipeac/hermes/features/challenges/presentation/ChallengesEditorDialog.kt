@file:Suppress("ArgumentListWrapping", "ImportOrdering", "LongMethod", "MaximumLineLength", "MaxLineLength", "Wrapping")

package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerField
import com.rafaelfelipeac.hermes.core.ui.components.CategoryPickerOption
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal const val CHALLENGES_TAG_EDITOR = "challenges_editor"

@Composable
internal fun ChallengesEditorDialog(
    editorState: ChallengeEditorState,
    categories: List<Category>,
    validationMessage: String?,
    onManageCategories: (ChallengeEditorState) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTargetTypeChange: (ChallengeTargetType) -> Unit,
    onTargetQuantityChange: (String) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val dialogKey = editorState.challengeId ?: -1L
    var showStartDatePicker by rememberSaveable(dialogKey) { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable(dialogKey) { mutableStateOf(false) }
    val currentLocale = currentLocale()
    val startDate = editorState.startDate
    val endDate = editorState.endDate
    val visibleCategories =
        remember(categories, editorState.categoryId) {
            categories
                .filter { !it.isHidden || it.id == editorState.categoryId }
                .map { category ->
                    CategoryPickerOption(
                        id = category.id,
                        name = category.name,
                        colorId = category.colorId,
                    )
                }
        }
    val inclusiveDays =
        startDate?.let { start ->
            endDate?.let { end ->
                if (end.isBefore(start)) null else (ChronoUnit.DAYS.between(start, end) + 1)
            }
        }
    val previewText =
        remember(editorState.targetType, editorState.targetQuantityText, startDate, endDate) {
            val quantity = ChallengeQuantity.parseLocalized(editorState.targetQuantityText, currentLocale)
            if (quantity == null || inclusiveDays == null) {
                null
            } else {
                val planned =
                    when (editorState.targetType) {
                        ChallengeTargetType.DAILY ->
                            runCatching {
                                ChallengeQuantity.multiply(quantity, inclusiveDays)
                            }.getOrNull()
                        ChallengeTargetType.TOTAL -> quantity
                    }
                planned?.let { ChallengeQuantity.format(it, currentLocale) }
            }
        }

    AlertDialog(
        modifier = Modifier.testTag(CHALLENGES_TAG_EDITOR),
        onDismissRequest = onCancel,
        title = {
            Text(
                text =
                    if (editorState.challengeId == null) {
                        stringResource(R.string.challenges_create)
                    } else {
                        stringResource(R.string.challenges_editor_title)
                    },
            )
        },
        text = {
            KeyboardAwareDialogForm {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
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
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )

                    CategoryPickerField(
                        label = stringResource(R.string.challenges_field_category),
                        categories = visibleCategories,
                        selectedCategoryId = editorState.categoryId,
                        onCategorySelected = onCategoryChange,
                        onManageCategories = { onManageCategories(editorState) },
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ChallengeTargetType.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = editorState.targetType == option,
                                onClick = { onTargetTypeChange(option) },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index,
                                        ChallengeTargetType.entries.size,
                                    ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = challengeTargetTypeLabel(option))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editorState.targetQuantityText,
                        onValueChange = onTargetQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.challenges_field_target_quantity)) },
                        keyboardOptions = DefaultTextFieldKeyboardOptions,
                    )

                    if (inclusiveDays != null && previewText != null) {
                        Text(
                            text =
                                when (editorState.targetType) {
                                    ChallengeTargetType.DAILY ->
                                        stringResource(
                                            R.string.challenges_daily_preview,
                                            ChallengeQuantity.format(
                                                ChallengeQuantity.parseLocalized(editorState.targetQuantityText, currentLocale)
                                                    ?: 0L,
                                                currentLocale,
                                            ),
                                            inclusiveDays,
                                            previewText,
                                        )
                                    ChallengeTargetType.TOTAL ->
                                        stringResource(
                                            R.string.challenges_total_preview,
                                            previewText,
                                        )
                                },
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd), modifier = Modifier.fillMaxWidth()) {
                        ChallengeDialogDateField(
                            label = stringResource(R.string.challenges_field_start_date),
                            date = startDate,
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                        ChallengeDialogDateField(
                            label = stringResource(R.string.challenges_field_end_date),
                            date = endDate,
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )

    if (showStartDatePicker) {
        ChallengeDatePickerDialog(
            date = editorState.startDate ?: LocalDate.now(),
            onDateSelected = onStartDateChange,
            onDismiss = { showStartDatePicker = false },
        )
    }

    if (showEndDatePicker) {
        ChallengeDatePickerDialog(
            date = editorState.endDate ?: editorState.startDate ?: LocalDate.now(),
            onDateSelected = onEndDateChange,
            onDismiss = { showEndDatePicker = false },
        )
    }
}
