@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE_TOKEN
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ReleaseNotesBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import java.util.Locale

private const val DEBUG_PACKAGE_SUFFIX = ".dev"
internal const val SETTINGS_THEME_ROW_TAG = "settings_theme_row"
internal const val SETTINGS_LANGUAGE_ROW_TAG = "settings_language_row"
internal const val SETTINGS_WEEK_START_ROW_TAG = "settings_week_start_row"
internal const val SETTINGS_UNITS_ROW_TAG = "settings_units_row"
internal const val SETTINGS_APP_VERSION_CARD_TAG = "settings_app_version_card"
internal const val SETTINGS_RELEASE_NOTES_SHEET_TAG = "settings_release_notes_sheet"
private const val SETTINGS_SCREEN_TAG = "SettingsScreen"
private const val LOG_FEEDBACK_INTENT_NOT_FOUND = "Feedback intent not found."
private const val LOG_FEEDBACK_INTENT_BLOCKED = "Feedback intent blocked by security policy."
private const val LOG_MARKET_INTENT_NOT_FOUND = "Market intent not found."
private const val LOG_MARKET_INTENT_BLOCKED = "Market intent blocked by security policy."
private const val LOG_WEB_INTENT_NOT_FOUND = "Web intent not found."
private const val LOG_WEB_INTENT_BLOCKED = "Web intent blocked by security policy."

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
                    val normalizedBody = body.replace("\n", "\r\n")
                    val mailToUri =
                        String.format(
                            Locale.ROOT,
                            mailtoTemplate,
                            feedbackEmail,
                            Uri.encode(subject),
                            Uri.encode(normalizedBody),
                        ).toUri()
                    val intent =
                        Intent(Intent.ACTION_SENDTO, mailToUri).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, normalizedBody)
                        }

                    try {
                        context.startActivity(intent)
                    } catch (error: ActivityNotFoundException) {
                        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_NOT_FOUND, error)

                        Toast.makeText(
                            context,
                            feedbackUnavailableMessage,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (error: SecurityException) {
                        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_BLOCKED, error)

                        Toast.makeText(
                            context,
                            feedbackUnavailableMessage,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                onRateClick = {
                    val packageName =
                        if (BuildConfig.DEBUG && context.packageName.endsWith(DEBUG_PACKAGE_SUFFIX)) {
                            context.packageName.removeSuffix(DEBUG_PACKAGE_SUFFIX)
                        } else {
                            context.packageName
                        }
                    val marketIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            String.format(
                                Locale.ROOT,
                                marketUrlTemplate,
                                packageName,
                            ).toUri(),
                        )
                    val webIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            String.format(
                                Locale.ROOT,
                                webUrlTemplate,
                                packageName,
                            ).toUri(),
                        )
                    val launchFailed =
                        try {
                            context.startActivity(marketIntent)
                            false
                        } catch (error: ActivityNotFoundException) {
                            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_NOT_FOUND, error)
                            true
                        } catch (error: SecurityException) {
                            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_BLOCKED, error)
                            true
                        }

                    if (launchFailed) {
                        val webLaunchFailed =
                            try {
                                context.startActivity(webIntent)
                                false
                            } catch (error: ActivityNotFoundException) {
                                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_NOT_FOUND, error)
                                true
                            } catch (error: SecurityException) {
                                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_BLOCKED, error)
                                true
                            }

                        if (webLaunchFailed) {
                            Toast.makeText(
                                context,
                                rateUnavailableMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                modifier = modifier,
            )
        SettingsRoute.THEME ->
            SettingsThemeScreen(
                themeMode = state.themeMode,
                onBack = { route = SettingsRoute.MAIN },
                onThemeSelected = viewModel::setThemeMode,
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

@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    appVersion: String,
    onBack: (() -> Unit)? = null,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onWeekStartClick: () -> Unit,
    onSlotModeClick: () -> Unit,
    onUnitsClick: () -> Unit,
    onFeedbackClick: (String, String) -> Unit,
    onRateClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val appName = stringResource(R.string.app_name)
    val releaseNotesDefinition = remember(appVersion) { releaseNotesForVersion(appVersion) }
    var isReleaseNotesVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(releaseNotesDefinition) {
        if (releaseNotesDefinition == null) {
            isReleaseNotesVisible = false
        }
    }

    val contentModifier =
        if (onBack != null) {
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = SpacingXl)
        } else {
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingXl)
        }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = contentModifier,
            verticalArrangement = Arrangement.spacedBy(SpacingXxl),
        ) {
            if (onBack != null) {
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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_title),
                        style = typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = typography.titleLarge,
                )
            }

            Column(
                modifier =
                    if (onBack != null) {
                        Modifier.padding(horizontal = SpacingXl)
                    } else {
                        Modifier
                    },
                verticalArrangement = Arrangement.spacedBy(SpacingXxl),
            ) {
                SettingsSection(title = stringResource(R.string.settings_workouts_title)) {
                    SettingsNavigationRow(
                        label = stringResource(R.string.settings_slot_mode_title),
                        onClick = onSlotModeClick,
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                    SettingsNavigationRow(
                        label = stringResource(R.string.settings_week_start_title),
                        detail = weekStartLabel(state.weekStartDay),
                        onClick = onWeekStartClick,
                        modifier = Modifier.testTag(SETTINGS_WEEK_START_ROW_TAG),
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                    SettingsNavigationRow(
                        label = stringResource(R.string.settings_units_title),
                        detail =
                            stringResource(
                                R.string.settings_units_summary,
                                distanceUnitLabel(state.distanceUnit),
                                paceUnitLabel(state.paceUnit),
                                weightUnitLabel(state.weightUnit),
                            ),
                        onClick = onUnitsClick,
                        modifier = Modifier.testTag(SETTINGS_UNITS_ROW_TAG),
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_theme_title)) {
                    SettingsNavigationRow(
                        label = themeLabel(state.themeMode),
                        onClick = onThemeClick,
                        modifier = Modifier.testTag(SETTINGS_THEME_ROW_TAG),
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_language_title)) {
                    SettingsNavigationRow(
                        label = languageLabel(state.language),
                        onClick = onLanguageClick,
                        modifier = Modifier.testTag(SETTINGS_LANGUAGE_ROW_TAG),
                    )
                }

                val feedbackSubject =
                    stringResource(
                        R.string.settings_feedback_subject,
                        appName,
                    )
                val feedbackBody =
                    stringResource(
                        R.string.settings_feedback_email_body,
                        appVersion,
                    ).replace(NEW_LINE_TOKEN, NEW_LINE)

                Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        style = typography.titleMedium,
                    )

                    SettingsCard {
                        SettingsInfoRow(
                            icon = Icons.Outlined.Email,
                            title = stringResource(R.string.settings_feedback_title),
                            body = stringResource(R.string.settings_feedback_body),
                            onClick = { onFeedbackClick(feedbackSubject, feedbackBody) },
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                        SettingsInfoRow(
                            icon = Icons.Outlined.Star,
                            title = stringResource(R.string.settings_rate_title),
                            body = stringResource(R.string.settings_rate_body),
                            onClick = onRateClick,
                        )
                    }

                    SettingsVersionCard(
                        appVersion = appVersion,
                        hasReleaseNotes = releaseNotesDefinition != null,
                        onClick = { isReleaseNotesVisible = true },
                    )
                }
            }
        }

        if (isReleaseNotesVisible && releaseNotesDefinition != null) {
            ReleaseNotesBottomSheet(
                definition = releaseNotesDefinition,
                onDismiss = { isReleaseNotesVisible = false },
            )
        }
    }
}

@Composable
private fun SettingsVersionCard(
    appVersion: String,
    hasReleaseNotes: Boolean,
    onClick: () -> Unit,
) {
    SettingsCard(
        modifier =
            Modifier
                .testTag(SETTINGS_APP_VERSION_CARD_TAG)
                .then(
                    if (hasReleaseNotes) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingSm),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = stringResource(R.string.settings_app_version, appVersion),
                style = typography.bodySmall,
                color = if (hasReleaseNotes) colorScheme.primary else colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            if (hasReleaseNotes) {
                Text(
                    text = stringResource(R.string.settings_release_notes_available),
                    style = typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReleaseNotesBottomSheet(
    definition: ReleaseNotesDefinition,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag(SETTINGS_RELEASE_NOTES_SHEET_TAG),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingXl)
                    .padding(bottom = ReleaseNotesBottomPadding)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.settings_release_notes_title,
                        definition.normalizedVersion,
                    ),
                style = typography.titleMedium,
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                definition.sections.forEach { section ->
                    ReleaseNotesSection(
                        title = stringResource(section.titleRes),
                        items = stringArrayResource(section.itemsRes).toList(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReleaseNotesSection(
    title: String,
    items: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = title,
            style =
                typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
            color = colorScheme.primary,
        )

        Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    Text(
                        text = stringResource(R.string.settings_release_notes_bullet),
                        style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colorScheme.primary,
                    )
                    Text(
                        text = item,
                        style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
