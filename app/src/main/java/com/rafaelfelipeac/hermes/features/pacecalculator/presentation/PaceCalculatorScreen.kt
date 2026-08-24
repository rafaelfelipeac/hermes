@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.strings.formatElapsedTime
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
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
import kotlin.math.roundToLong

internal const val PACE_CALCULATOR_ROOT_TAG = "pace_calculator_root"
internal const val PACE_CALCULATOR_BACK_BUTTON_TAG = "pace_calculator_back_button"
internal const val PACE_CALCULATOR_TITLE_TAG = "pace_calculator_title"
internal const val PACE_CALCULATOR_BODY_TAG = "pace_calculator_body"
internal const val PACE_CALCULATOR_PRESETS_TAG = "pace_calculator_presets"
internal const val PACE_CALCULATOR_DISTANCE_INPUT_TAG = "pace_calculator_distance_input"
internal const val PACE_CALCULATOR_TIME_MINUTES_INPUT_TAG = "pace_calculator_time_minutes_input"
internal const val PACE_CALCULATOR_TIME_HOURS_INPUT_TAG = "pace_calculator_time_hours_input"
internal const val PACE_CALCULATOR_TIME_SECONDS_INPUT_TAG = "pace_calculator_time_seconds_input"
internal const val PACE_CALCULATOR_PACE_MINUTES_INPUT_TAG = "pace_calculator_pace_minutes_input"
internal const val PACE_CALCULATOR_PACE_SECONDS_INPUT_TAG = "pace_calculator_pace_seconds_input"
internal const val PACE_CALCULATOR_MODE_PACE_TAG = "pace_calculator_mode_pace"
internal const val PACE_CALCULATOR_MODE_TIME_TAG = "pace_calculator_mode_time"
internal const val PACE_CALCULATOR_MODE_DISTANCE_TAG = "pace_calculator_mode_distance"
internal const val PACE_CALCULATOR_RESULT_TAG = "pace_calculator_result"
private const val DEFAULT_NUMBER_TEXT = "0"

@Composable
fun PaceCalculatorRoute(
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaceCalculatorViewModel = hiltViewModel(),
) {
    PaceCalculatorScreen(
        settingsDistanceUnit = settingsDistanceUnit,
        settingsPaceUnit = settingsPaceUnit,
        onBack = onBack,
        modifier = modifier,
        onValidCalculation = viewModel::logCalculation,
    )
}

@Composable
fun PaceCalculatorScreen(
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onValidCalculation: (PaceCalculatorMode) -> Unit = {},
) {
    BackHandler(onBack = onBack)

    var mode by rememberSaveable { mutableStateOf(PaceCalculatorMode.PACE) }
    var distanceText by rememberSaveable { mutableStateOf(EMPTY) }
    var timeHoursText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var timeMinutesText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var timeSecondsText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var paceMinutesText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var paceSecondsText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var selectedPresetMeters by rememberSaveable { mutableStateOf<Double?>(null) }
    var hasLoggedValidCalculation by rememberSaveable { mutableStateOf(false) }
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
                PaceCalculatorInput(
                    mode = mode,
                    distanceText = distanceText,
                    timeHoursText = timeHoursText,
                    timeMinutesText = timeMinutesText,
                    timeSecondsText = timeSecondsText,
                    paceMinutesText = paceMinutesText,
                    paceSecondsText = paceSecondsText,
                    paceUnitMeters = paceUnitMeters,
                    distanceUnitMeters = distanceUnitMeters,
                ),
            )
        }

    LaunchedEffect(result, mode, hasLoggedValidCalculation) {
        if (!hasLoggedValidCalculation && result.hasResult()) {
            hasLoggedValidCalculation = true
            onValidCalculation(mode)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(PACE_CALCULATOR_ROOT_TAG),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SpacingSm,
                        end = SpacingXl,
                        top = SpacingSm,
                        bottom = SpacingSm,
                    ),
        ) {
            IconButton(
                onClick = onBack,
                modifier =
                    Modifier
                        .testTag(PACE_CALCULATOR_BACK_BUTTON_TAG),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.settings_back),
                )
            }

            Text(
                text = stringResource(R.string.pace_calculator_title),
                style = typography.titleLarge,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).testTag(PACE_CALCULATOR_TITLE_TAG),
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .testTag(PACE_CALCULATOR_BODY_TAG)
                    .padding(
                        start = SpacingXl,
                        end = SpacingXl,
                        bottom = SpacingXxl,
                    ),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PaceCalculatorMode.entries.forEachIndexed { index, option ->
                    val selected = mode == option
                    SegmentedButton(
                        selected = selected,
                        onClick = { mode = option },
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index,
                                PaceCalculatorMode.entries.size,
                            ),
                        modifier =
                            Modifier
                                .weight(1f)
                                .testTag(paceCalculatorModeTestTag(option)),
                    ) {
                        Text(text = paceCalculatorModeLabel(option))
                    }
                }
            }

            if (mode != PaceCalculatorMode.DISTANCE) {
                DistancePresets(
                    settingsDistanceUnit = settingsDistanceUnit,
                    selectedPresetMeters = selectedPresetMeters,
                    onPresetSelected = { preset ->
                        selectedPresetMeters = preset.valueMeters
                        distanceText = formatDistanceInput(preset.valueMeters, settingsDistanceUnit)
                    },
                    onCustomSelected = { selectedPresetMeters = null },
                )
            }

            when (mode) {
                PaceCalculatorMode.PACE -> {
                    DistanceInputField(
                        distanceText = distanceText,
                        settingsDistanceUnit = settingsDistanceUnit,
                        onDistanceTextChange = {
                            if (validDistanceInput(it)) {
                                distanceText = it
                                selectedPresetMeters = null
                            }
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
                            if (validDistanceInput(it)) {
                                distanceText = it
                                selectedPresetMeters = null
                            }
                        },
                    )
                    PaceInputFields(
                        paceMinutesText = paceMinutesText,
                        paceSecondsText = paceSecondsText,
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
                        onMinutesChange = { paceMinutesText = it },
                        onSecondsChange = { paceSecondsText = it },
                    )
                }
            }

            ResultCard(
                mode = mode,
                result = result,
                settingsDistanceUnit = settingsDistanceUnit,
                settingsPaceUnit = settingsPaceUnit,
            )
        }
    }
}

private fun PaceCalculatorResultUi.hasResult(): Boolean {
    return paceSecondsPerUnit != null ||
        finishTimeSeconds != null ||
        distanceMeters != null
}

@Composable
private fun DistancePresets(
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
                        label = { Text(text = preset.label) },
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
        modifier = Modifier.fillMaxWidth().testTag(PACE_CALCULATOR_DISTANCE_INPUT_TAG),
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
private fun PaceInputFields(
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
private fun ResultCard(
    mode: PaceCalculatorMode,
    result: PaceCalculatorResultUi,
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
) {
    val hasResult = result.hasResult()
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderThin, colorScheme.outlineVariant),
        shape = shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(text = stringResource(R.string.pace_calculator_result), style = typography.titleMedium)
            if (hasResult) {
                val labels = result.labels(mode, settingsDistanceUnit, settingsPaceUnit)
                Text(
                    text = paceCalculatorResultLabel(mode),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = labels.primary,
                    style = typography.headlineMedium,
                    modifier = Modifier.testTag(PACE_CALCULATOR_RESULT_TAG),
                )
                if (labels.secondary.isNotBlank()) {
                    Text(
                        text = labels.secondary,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.pace_calculator_result_empty),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(PACE_CALCULATOR_RESULT_TAG),
                )
            }
        }
    }
}

private data class PaceCalculatorResultLabels(
    val primary: String,
    val secondary: String,
)

@Composable
private fun PaceCalculatorResultUi.labels(
    mode: PaceCalculatorMode,
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
): PaceCalculatorResultLabels =
    when (mode) {
        PaceCalculatorMode.PACE ->
            PaceCalculatorResultLabels(
                primary =
                    paceSecondsPerUnit?.let {
                        formatPaceSeconds(
                            paceSecondsPerUnit = it,
                            unitLabel = paceUnitLabel(settingsPaceUnit),
                        )
                    }.orEmpty(),
                secondary = finishTimeSeconds?.let(::formatElapsedTime).orEmpty(),
            )
        PaceCalculatorMode.TIME ->
            PaceCalculatorResultLabels(
                primary = finishTimeSeconds?.let(::formatElapsedTime).orEmpty(),
                secondary =
                    distanceMeters?.let {
                        formatDistance(
                            distanceMeters = it,
                            distanceUnitMeters = distanceUnitMeters(settingsDistanceUnit),
                            unitLabel = distanceUnitLabel(settingsDistanceUnit),
                        )
                    }.orEmpty(),
            )
        PaceCalculatorMode.DISTANCE ->
            PaceCalculatorResultLabels(
                primary =
                    distanceMeters?.let {
                        formatDistance(
                            distanceMeters = it,
                            distanceUnitMeters = distanceUnitMeters(settingsDistanceUnit),
                            unitLabel = distanceUnitLabel(settingsDistanceUnit),
                        )
                    }.orEmpty(),
                secondary = timeSeconds?.let(::formatElapsedTime).orEmpty(),
            )
    }

private data class PaceDistancePreset(
    val label: String,
    val valueMeters: Double,
)

@Composable
private fun paceDistancePresets(distanceUnit: DistanceUnit): List<PaceDistancePreset> {
    val halfMarathon = PaceDistancePreset(stringResource(R.string.pace_calculator_preset_half_marathon), 21_097.5)
    val marathon = PaceDistancePreset(stringResource(R.string.pace_calculator_preset_marathon), 42_195.0)
    return when (distanceUnit) {
        KILOMETERS ->
            listOf(
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_1_km), 1_000.0),
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_5k), 5_000.0),
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_10k), 10_000.0),
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_15k), 15_000.0),
                halfMarathon,
                marathon,
            )

        MILES ->
            listOf(
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_1_mile), 1_609.344),
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_5_miles), 8_046.72),
                PaceDistancePreset(stringResource(R.string.pace_calculator_preset_10_miles), 16_093.44),
                halfMarathon,
                marathon,
            )
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

private fun formatPaceSeconds(
    paceSecondsPerUnit: Double,
    unitLabel: String,
): String {
    val totalSeconds = paceSecondsPerUnit.roundToLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d %s".format(minutes, seconds, unitLabel)
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

private fun paceCalculatorModeTestTag(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> PACE_CALCULATOR_MODE_PACE_TAG
        PaceCalculatorMode.TIME -> PACE_CALCULATOR_MODE_TIME_TAG
        PaceCalculatorMode.DISTANCE -> PACE_CALCULATOR_MODE_DISTANCE_TAG
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
