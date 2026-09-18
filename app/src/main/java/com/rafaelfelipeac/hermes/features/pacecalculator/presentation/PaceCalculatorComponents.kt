@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.presentation.distanceUnitLabel

@Composable
internal fun DistancePresets(
    settingsDistanceUnit: DistanceUnit,
    selectedPresetMeters: Double?,
    onPresetSelected: (PaceDistancePreset) -> Unit,
    onCustomSelected: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderThin, colorScheme.outlineVariant),
        shape = shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag(PACE_CALCULATOR_PRESETS_TAG),
    ) {
        Column(
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(
                text = stringResource(R.string.pace_calculator_distance_presets),
                style = typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                paceDistancePresets(settingsDistanceUnit).forEach { preset ->
                    FilterChip(
                        selected = preset.valueMeters == selectedPresetMeters,
                        onClick = { onPresetSelected(preset) },
                        label = { Text(text = stringResource(preset.labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
                FilterChip(
                    selected = selectedPresetMeters == null,
                    onClick = onCustomSelected,
                    label = { Text(text = stringResource(R.string.pace_calculator_distance_custom)) },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }
    }
}

@Composable
internal fun DistanceInputField(
    distanceText: String,
    settingsDistanceUnit: DistanceUnit,
    onDistanceTextChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = distanceText,
        onValueChange = onDistanceTextChange,
        label = { Text(text = stringResource(R.string.pace_calculator_distance)) },
        trailingIcon = { Text(text = distanceUnitLabel(settingsDistanceUnit)) },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
            ),
        modifier = Modifier.fillMaxWidth().testTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG),
    )
}

@Composable
internal fun TimeInputFields(
    hoursText: String,
    minutesText: String,
    secondsText: String,
    onHoursTextChange: (String) -> Unit,
    onMinutesTextChange: (String) -> Unit,
    onSecondsTextChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(text = stringResource(R.string.pace_calculator_time), style = typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MiniNumberField(
                value = hoursText,
                onValueChange = onHoursTextChange,
                label = stringResource(R.string.pace_calculator_hours),
                maxValue = MAX_TIME_HOURS.toInt(),
                modifier = Modifier.weight(1f).testTag(PACE_CALCULATOR_TIME_HOURS_INPUT_TAG),
            )
            MiniNumberField(
                value = minutesText,
                onValueChange = onMinutesTextChange,
                label = stringResource(R.string.pace_calculator_minutes),
                maxValue = MAX_MINUTES_OR_SECONDS.toInt(),
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG),
            )
            MiniNumberField(
                value = secondsText,
                onValueChange = onSecondsTextChange,
                label = stringResource(R.string.pace_calculator_seconds),
                maxValue = MAX_MINUTES_OR_SECONDS.toInt(),
                modifier = Modifier.weight(1f).testTag(PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG),
            )
        }
    }
}

@Composable
internal fun PaceInputFields(
    paceMinutesText: String,
    paceSecondsText: String,
    onMinutesChange: (String) -> Unit,
    onSecondsChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(R.string.pace_calculator_pace),
            style = typography.titleMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MiniNumberField(
                value = paceMinutesText,
                onValueChange = onMinutesChange,
                label = stringResource(R.string.pace_calculator_minutes),
                maxValue = MAX_PACE_MINUTES,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(PACE_CALCULATOR_PACE_MINUTES_INPUT_TAG),
            )
            MiniNumberField(
                value = paceSecondsText,
                onValueChange = onSecondsChange,
                label = stringResource(R.string.pace_calculator_seconds),
                maxValue = MAX_MINUTES_OR_SECONDS.toInt(),
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(PACE_CALCULATOR_PACE_SECONDS_INPUT_TAG),
            )
        }
    }
}

@Composable
private fun MiniNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxValue: Int,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = { updatedValue ->
            sanitizedWholeNumberInput(updatedValue.text, maxValue)?.let { sanitizedText ->
                fieldValue = updatedValue.copy(text = sanitizedText)
                onValueChange(sanitizedText)
            }
        },
        label = { Text(text = label) },
        singleLine = true,
        trailingIcon = suffix?.let { { Text(text = it) } },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        modifier =
            modifier.onFocusChanged { focusState ->
                when {
                    focusState.isFocused && fieldValue.text == DEFAULT_NUMBER_TEXT -> {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    }
                    !focusState.isFocused && fieldValue.text.isEmpty() -> {
                        fieldValue = TextFieldValue(DEFAULT_NUMBER_TEXT)
                        onValueChange(DEFAULT_NUMBER_TEXT)
                    }
                }
            },
    )
}

@Composable
internal fun ResultCard(
    mode: PaceCalculatorMode,
    result: PaceCalculatorResultUi,
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
) {
    val hasResult = result.hasResult()
    val containerColor = if (hasResult) colorScheme.primaryContainer else colorScheme.surfaceContainerLow
    val contentColor = if (hasResult) colorScheme.onPrimaryContainer else colorScheme.onSurface
    val supportingContentColor = if (hasResult) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(BorderThin, if (hasResult) colorScheme.primary else colorScheme.outlineVariant),
        shape = shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(
                text = stringResource(R.string.pace_calculator_result),
                style = typography.titleMedium,
                color = contentColor,
            )
            if (hasResult) {
                val labels = result.labels(mode, settingsDistanceUnit, settingsPaceUnit)
                Text(
                    text = paceCalculatorResultLabel(mode),
                    style = typography.bodyMedium,
                    color = supportingContentColor,
                )
                Text(
                    text = labels.primary,
                    style = typography.headlineMedium,
                    color = contentColor,
                    modifier = Modifier.testTag(PACE_CALCULATOR_RESULT_TAG),
                )
                if (labels.secondary.isNotBlank()) {
                    Text(
                        text = labels.secondary,
                        style = typography.bodyMedium,
                        color = supportingContentColor,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.pace_calculator_result_empty),
                    style = typography.bodyMedium,
                    color = supportingContentColor,
                    modifier = Modifier.testTag(PACE_CALCULATOR_RESULT_TAG),
                )
            }
        }
    }
}
