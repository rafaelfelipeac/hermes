package com.rafaelfelipeac.hermes.features.settings.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    initialRoute: SettingsRoute? = null,
    onRouteConsumed: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val demoDataCreatedMessage = stringResource(R.string.settings_demo_data_created)
    val challengeDemoDataCreatedMessage = stringResource(R.string.settings_challenge_demo_data_created)
    val mixedTrophiesCreatedMessage = stringResource(R.string.settings_mixed_trophies_created)
    val lockedTrophiesCreatedMessage = stringResource(R.string.settings_locked_trophies_created)
    val completedTrophiesCreatedMessage = stringResource(R.string.settings_completed_trophies_created)
    val databaseClearedMessage = stringResource(R.string.settings_database_cleared)
    val feedbackUnavailableMessage = stringResource(R.string.settings_feedback_unavailable)
    val rateUnavailableMessage = stringResource(R.string.settings_rate_unavailable)
    val feedbackEmail = stringResource(R.string.settings_feedback_email)
    val mailtoTemplate = stringResource(R.string.settings_feedback_mailto_uri)
    val marketUrlTemplate = stringResource(R.string.settings_play_store_market_url)
    val webUrlTemplate = stringResource(R.string.settings_play_store_web_url)
    var route by rememberSaveable { mutableStateOf(SettingsRoute.MAIN) }
    var isSlotModeHelpVisible by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = route != SettingsRoute.MAIN || onBack != null) {
        if (route != SettingsRoute.MAIN) {
            route = SettingsRoute.MAIN
        } else {
            onBack?.invoke()
        }
    }

    LaunchedEffect(initialRoute) {
        if (initialRoute != null) {
            route = initialRoute
            onRouteConsumed()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.demoSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                demoDataCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.challengeDemoSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                challengeDemoDataCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.lockedTrophiesSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                lockedTrophiesCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.completedTrophiesSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                completedTrophiesCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.mixedTrophiesSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                mixedTrophiesCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.databaseClearCompletedEvents.collect {
            Toast.makeText(
                context,
                databaseClearedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    when (route) {
        SettingsRoute.MAIN ->
            SettingsContent(
                state = state,
                appVersion = VERSION_NAME,
                onBack = onBack,
                onThemeClick = { route = SettingsRoute.THEME },
                onLanguageClick = { route = SettingsRoute.LANGUAGE },
                onWeekStartClick = { route = SettingsRoute.START_OF_WEEK },
                onSlotModeClick = { route = SettingsRoute.SLOT_MODE },
                onUnitsClick = { route = SettingsRoute.UNITS },
                onFeedbackClick = { subject, body ->
                    context.launchSettingsFeedback(
                        subject = subject,
                        body = body,
                        feedbackEmail = feedbackEmail,
                        mailtoTemplate = mailtoTemplate,
                        feedbackUnavailableMessage = feedbackUnavailableMessage,
                    )
                },
                onRateClick = {
                    context.launchSettingsRating(marketUrlTemplate, webUrlTemplate, rateUnavailableMessage)
                },
                modifier = modifier,
            )
        SettingsRoute.THEME ->
            SettingsThemeScreen(
                themeMode = state.themeMode,
                useDynamicColor = state.useDynamicColor,
                onBack = { route = SettingsRoute.MAIN },
                onThemeSelected = viewModel::setThemeMode,
                onUseDynamicColorChanged = viewModel::setUseDynamicColor,
                modifier = modifier,
            )
        SettingsRoute.LANGUAGE ->
            SettingsLanguageScreen(
                language = state.language,
                onBack = { route = SettingsRoute.MAIN },
                onLanguageSelected = viewModel::setLanguage,
                modifier = modifier,
            )
        SettingsRoute.START_OF_WEEK ->
            SettingsWeekStartScreen(
                weekStartDay = state.weekStartDay,
                onBack = { route = SettingsRoute.MAIN },
                onWeekStartSelected = viewModel::setWeekStartDay,
                modifier = modifier,
            )
        SettingsRoute.SLOT_MODE ->
            SettingsSlotModeScreen(
                slotModePolicy = state.slotModePolicy,
                onBack = { route = SettingsRoute.MAIN },
                onHelpClick = { isSlotModeHelpVisible = true },
                onSlotModeSelected = viewModel::setSlotModePolicy,
                modifier = modifier,
            )
        SettingsRoute.UNITS ->
            SettingsUnitsScreen(
                distanceUnit = state.distanceUnit,
                paceUnit = state.paceUnit,
                weightUnit = state.weightUnit,
                onBack = { route = SettingsRoute.MAIN },
                onDistanceUnitSelected = viewModel::setDistanceUnit,
                onPaceUnitSelected = viewModel::setPaceUnit,
                onWeightUnitSelected = viewModel::setWeightUnit,
                modifier = modifier,
            )
    }

    if (isSlotModeHelpVisible) {
        AlertDialog(
            onDismissRequest = { isSlotModeHelpVisible = false },
            title = { Text(text = stringResource(R.string.settings_slot_mode_help_title)) },
            text = { Text(text = stringResource(R.string.settings_slot_mode_help_message)) },
            confirmButton = {
                Button(onClick = { isSlotModeHelpVisible = false }) {
                    Text(text = stringResource(R.string.weekly_training_tbd_help_confirm))
                }
            },
        )
    }
}
