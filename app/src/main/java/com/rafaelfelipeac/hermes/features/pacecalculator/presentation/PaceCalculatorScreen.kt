@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.meters

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
internal const val DEFAULT_NUMBER_TEXT = "0"

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

    val currentLocale = currentLocale()
    var mode by rememberSaveable { mutableStateOf(PaceCalculatorMode.PACE) }
    var distanceText by rememberSaveable { mutableStateOf(EMPTY) }
    var timeHoursText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var timeMinutesText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var timeSecondsText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var paceMinutesText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var paceSecondsText by rememberSaveable { mutableStateOf(DEFAULT_NUMBER_TEXT) }
    var selectedPresetMeters by rememberSaveable { mutableStateOf<Double?>(null) }
    var hasLoggedValidCalculation by rememberSaveable { mutableStateOf(false) }
    val paceUnitMeters = settingsPaceUnit.meters()
    val distanceUnitMeters = settingsDistanceUnit.meters()
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
                        distanceText = formatDistanceInput(preset.valueMeters, settingsDistanceUnit, currentLocale)
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
