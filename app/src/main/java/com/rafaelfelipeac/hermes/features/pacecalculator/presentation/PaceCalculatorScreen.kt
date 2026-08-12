package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculator
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_KM
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit.MIN_PER_MI
import com.rafaelfelipeac.hermes.features.settings.presentation.distanceUnitLabel
import com.rafaelfelipeac.hermes.features.settings.presentation.paceUnitLabel
import java.text.NumberFormat
import java.util.Locale

internal const val PACE_CALCULATOR_ROOT_TAG = "pace_calculator_root"

@Composable
fun PaceCalculatorScreen(
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    var mode by rememberSaveable { mutableStateOf(PaceCalculatorMode.PACE) }
    var distanceText by rememberSaveable { mutableStateOf("") }
    var timeHoursText by rememberSaveable { mutableStateOf("") }
    var timeMinutesText by rememberSaveable { mutableStateOf("") }
    var timeSecondsText by rememberSaveable { mutableStateOf("") }
    var paceMinutesText by rememberSaveable { mutableStateOf("") }
    var paceSecondsText by rememberSaveable { mutableStateOf("") }
    var selectedPresetMeters by rememberSaveable { mutableStateOf<Double?>(null) }
    val paceUnitMeters = paceUnitMeters(settingsPaceUnit)
    val distanceUnitMeters = distanceUnitMeters(settingsDistanceUnit)
    val result =
        remember(
            mode,
            distanceText,
            timeHoursText,
            timeMinutesText,
            timeSecondsText,
            paceMinutesText,
            paceSecondsText,
            paceUnitMeters,
            distanceUnitMeters,
        ) {
            calculatePaceCalculatorResult(
                mode = mode,
                distanceText = distanceText,
                timeHoursText = timeHoursText,
                timeMinutesText = timeMinutesText,
                timeSecondsText = timeSecondsText,
                paceMinutesText = paceMinutesText,
                paceSecondsText = paceSecondsText,
                paceUnitMeters = paceUnitMeters,
                distanceUnitMeters = distanceUnitMeters,
            )
        }

    BackHandler(onBack = onBack)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SpacingLg)
                .testTag(PACE_CALCULATOR_ROOT_TAG),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.settings_back),
                )
            }

            Text(
                text = stringResource(R.string.pace_calculator_title),
                style = typography.titleLarge,
            )
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PaceCalculatorMode.entries.forEachIndexed { index, option ->
                val selected = mode == option
                SegmentedButton(
                    selected = selected,
                    onClick = { mode = option },
                    shape = SegmentedButtonDefaults.itemShape(index, PaceCalculatorMode.entries.size),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = paceCalculatorModeLabel(option))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
            shape = shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(SpacingLg),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                Text(
                    text = stringResource(R.string.pace_calculator_distance_presets),
                    style = typography.titleMedium,
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    paceDistancePresets().forEach { preset ->
                        FilterChip(
                            selected = preset.valueMeters == selectedPresetMeters,
                            onClick = {
                                selectedPresetMeters = preset.valueMeters
                                distanceText =
                                    formatDistanceInput(
                                        preset.valueMeters,
                                        settingsDistanceUnit,
                                    )
                            },
                            label = { Text(text = preset.label) },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                    FilterChip(
                        selected = selectedPresetMeters == null,
                        onClick = { selectedPresetMeters = null },
                        label = { Text(text = stringResource(R.string.pace_calculator_distance_custom)) },
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
            }
        }

        when (mode) {
            PaceCalculatorMode.PACE -> {
                DistanceInputField(
                    distanceText = distanceText,
                    settingsDistanceUnit = settingsDistanceUnit,
                    onDistanceTextChange = {
                        distanceText = it
                        selectedPresetMeters = null
                    },
                )
                TimeInputFields(
                    hoursText = timeHoursText,
                    minutesText = timeMinutesText,
                    secondsText = timeSecondsText,
                    onHoursTextChange = { timeHoursText = it },
                    onMinutesTextChange = { timeMinutesText = it },
                    onSecondsTextChange = { timeSecondsText = it },
                )
            }

            PaceCalculatorMode.TIME -> {
                DistanceInputField(
                    distanceText = distanceText,
                    settingsDistanceUnit = settingsDistanceUnit,
                    onDistanceTextChange = {
                        distanceText = it
                        selectedPresetMeters = null
                    },
                )
                PaceInputFields(
                    paceMinutesText = paceMinutesText,
                    paceSecondsText = paceSecondsText,
                    settingsPaceUnit = settingsPaceUnit,
                    onMinutesChange = { paceMinutesText = it },
                    onSecondsChange = { paceSecondsText = it },
                )
            }

            PaceCalculatorMode.DISTANCE -> {
                TimeInputFields(
                    hoursText = timeHoursText,
                    minutesText = timeMinutesText,
                    secondsText = timeSecondsText,
                    onHoursTextChange = { timeHoursText = it },
                    onMinutesTextChange = { timeMinutesText = it },
                    onSecondsTextChange = { timeSecondsText = it },
                )
                PaceInputFields(
                    paceMinutesText = paceMinutesText,
                    paceSecondsText = paceSecondsText,
                    settingsPaceUnit = settingsPaceUnit,
                    onMinutesChange = { paceMinutesText = it },
                    onSecondsChange = { paceSecondsText = it },
                )
            }
        }

        ResultCard(
            mode = mode,
            result = result,
            settingsDistanceUnit = settingsDistanceUnit,
        )
    }
}

@Composable
private fun DistanceInputField(
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
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TimeInputFields(
    hoursText: String,
    minutesText: String,
    secondsText: String,
    onHoursTextChange: (String) -> Unit,
    onMinutesTextChange: (String) -> Unit,
    onSecondsTextChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(text = stringResource(R.string.pace_calculator_time), style = typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
            MiniNumberField(
                value = hoursText,
                onValueChange = onHoursTextChange,
                label = stringResource(R.string.pace_calculator_hours),
                modifier = Modifier.weight(1f),
            )
            MiniNumberField(
                value = minutesText,
                onValueChange = onMinutesTextChange,
                label = stringResource(R.string.pace_calculator_minutes),
                modifier = Modifier.weight(1f),
            )
            MiniNumberField(
                value = secondsText,
                onValueChange = onSecondsTextChange,
                label = stringResource(R.string.pace_calculator_seconds),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PaceInputFields(
    paceMinutesText: String,
    paceSecondsText: String,
    settingsPaceUnit: PaceUnit,
    onMinutesChange: (String) -> Unit,
    onSecondsChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(R.string.pace_calculator_pace),
            style = typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
            MiniNumberField(
                value = paceMinutesText,
                onValueChange = onMinutesChange,
                label = stringResource(R.string.pace_calculator_minutes),
                suffix = paceUnitLabel(settingsPaceUnit).substringAfter('/'),
                modifier = Modifier.weight(1f),
            )
            MiniNumberField(
                value = paceSecondsText,
                onValueChange = onSecondsChange,
                label = stringResource(R.string.pace_calculator_seconds),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MiniNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        trailingIcon = suffix?.let { { Text(text = it) } },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        modifier = modifier,
    )
}

@Composable
private fun ResultCard(
    mode: PaceCalculatorMode,
    result: PaceCalculatorResultUi,
    settingsDistanceUnit: DistanceUnit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        shape = shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(text = stringResource(R.string.pace_calculator_result), style = typography.titleMedium)
            Text(
                text = paceCalculatorResultLabel(mode),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    when (mode) {
                        PaceCalculatorMode.PACE -> {
                            result.paceSecondsPerUnit?.let { formatPaceSeconds(it) }
                                ?: stringResource(R.string.pace_calculator_result_empty)
                        }

                        PaceCalculatorMode.TIME -> {
                            result.finishTimeSeconds?.let { formatDuration(it) }
                                ?: stringResource(R.string.pace_calculator_result_empty)
                        }

                        PaceCalculatorMode.DISTANCE -> {
                            result.distanceMeters?.let {
                                formatDistance(
                                    distanceMeters = it,
                                    distanceUnitMeters = distanceUnitMeters(settingsDistanceUnit),
                                    unitLabel = distanceUnitLabel(settingsDistanceUnit),
                                )
                            } ?: stringResource(R.string.pace_calculator_result_empty)
                        }
                    },
                style = typography.headlineMedium,
            )
            val secondaryLabel =
                when (mode) {
                    PaceCalculatorMode.PACE -> result.finishTimeSeconds?.let(::formatDuration).orEmpty()
                    PaceCalculatorMode.TIME -> result.distanceMeters?.let {
                        formatDistance(
                            distanceMeters = it,
                            distanceUnitMeters = distanceUnitMeters(settingsDistanceUnit),
                            unitLabel = distanceUnitLabel(settingsDistanceUnit),
                        )
                    }.orEmpty()
                    PaceCalculatorMode.DISTANCE -> result.timeSeconds?.let(::formatDuration).orEmpty()
                }
            if (secondaryLabel.isNotBlank()) {
                Text(
                    text = secondaryLabel,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class PaceDistancePreset(
    val label: String,
    val valueMeters: Double,
)

private data class PaceCalculatorResultUi(
    val paceSecondsPerUnit: Double? = null,
    val finishTimeSeconds: Long? = null,
    val distanceMeters: Double? = null,
    val timeSeconds: Long? = null,
)

@Composable
private fun paceDistancePresets(): List<PaceDistancePreset> {
    return listOf(
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_1_km), valueMeters = 1_000.0),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_1_mile), valueMeters = 1_609.344),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_5k), valueMeters = 5_000.0),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_10k), valueMeters = 10_000.0),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_15k), valueMeters = 15_000.0),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_half_marathon), valueMeters = 21_097.5),
        PaceDistancePreset(label = stringResource(R.string.pace_calculator_preset_marathon), valueMeters = 42_195.0),
    )
}

private fun calculatePaceCalculatorResult(
    mode: PaceCalculatorMode,
    distanceText: String,
    timeHoursText: String,
    timeMinutesText: String,
    timeSecondsText: String,
    paceMinutesText: String,
    paceSecondsText: String,
    paceUnitMeters: Double,
    distanceUnitMeters: Double,
): PaceCalculatorResultUi {
    val distanceMeters = distanceText.toDoubleOrNull()?.times(distanceUnitMeters)
    val timeSeconds =
        listOf(timeHoursText.toLongOrNull(), timeMinutesText.toLongOrNull(), timeSecondsText.toLongOrNull())
            .let { parts -> (parts[0] ?: 0L) * 3600 + (parts[1] ?: 0L) * 60 + (parts[2] ?: 0L) }
    val paceSecondsPerUnit =
        (paceMinutesText.toLongOrNull() ?: 0L) * 60 + (paceSecondsText.toLongOrNull() ?: 0L)

    return when (mode) {
        PaceCalculatorMode.PACE -> {
            if (distanceMeters == null || timeSeconds == 0L) {
                PaceCalculatorResultUi()
            } else {
                val paceSeconds =
                    PaceCalculator.calculatePaceSecondsPerUnit(distanceMeters, timeSeconds.toDouble(), paceUnitMeters)
                PaceCalculatorResultUi(paceSecondsPerUnit = paceSeconds, finishTimeSeconds = timeSeconds)
            }
        }

        PaceCalculatorMode.TIME -> {
            if (distanceMeters == null || paceSecondsPerUnit == 0L) {
                PaceCalculatorResultUi()
            } else {
                val finishSeconds =
                    PaceCalculator.calculateFinishTimeSeconds(distanceMeters, paceSecondsPerUnit.toDouble(), paceUnitMeters)
                PaceCalculatorResultUi(finishTimeSeconds = finishSeconds.toLong(), distanceMeters = distanceMeters)
            }
        }

        PaceCalculatorMode.DISTANCE -> {
            if (paceSecondsPerUnit == 0L || timeSeconds == 0L) {
                PaceCalculatorResultUi()
            } else {
                val distance =
                    PaceCalculator.calculateDistanceMeters(timeSeconds.toDouble(), paceSecondsPerUnit.toDouble(), paceUnitMeters)
                PaceCalculatorResultUi(distanceMeters = distance, timeSeconds = timeSeconds)
            }
        }
    }
}

private fun formatDistance(
    distanceMeters: Double,
    distanceUnitMeters: Double,
    unitLabel: String,
): String {
    val value = distanceMeters / distanceUnitMeters
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.minimumFractionDigits = if (value % 1.0 == 0.0) 0 else 1
    formatter.maximumFractionDigits = 2
    return "${formatter.format(value)} $unitLabel"
}

private fun formatPaceSeconds(paceSecondsPerUnit: Double): String {
    val totalSeconds = paceSecondsPerUnit.toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun paceCalculatorModeLabel(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> stringResource(R.string.pace_calculator_mode_pace)
        PaceCalculatorMode.TIME -> stringResource(R.string.pace_calculator_mode_time)
        PaceCalculatorMode.DISTANCE -> stringResource(R.string.pace_calculator_mode_distance)
    }
}

@Composable
private fun paceCalculatorResultLabel(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> stringResource(R.string.pace_calculator_result_pace)
        PaceCalculatorMode.TIME -> stringResource(R.string.pace_calculator_result_time)
        PaceCalculatorMode.DISTANCE -> stringResource(R.string.pace_calculator_result_distance)
    }
}

private fun distanceUnitMeters(unit: DistanceUnit): Double {
    return when (unit) {
        KILOMETERS -> 1_000.0
        MILES -> 1_609.344
    }
}

private fun paceUnitMeters(unit: PaceUnit): Double {
    return when (unit) {
        MIN_PER_KM -> 1_000.0
        MIN_PER_MI -> 1_609.344
    }
}

private fun formatDistanceInput(
    meters: Double,
    distanceUnit: DistanceUnit,
): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(meters / distanceUnitMeters(distanceUnit))
}
