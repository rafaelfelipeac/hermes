package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl

internal const val SETTINGS_THEME_ROW_TAG = "settings_theme_row"
internal const val SETTINGS_LANGUAGE_ROW_TAG = "settings_language_row"
internal const val SETTINGS_WEEK_START_ROW_TAG = "settings_week_start_row"
internal const val SETTINGS_UNITS_ROW_TAG = "settings_units_row"
internal const val SETTINGS_APP_VERSION_CARD_TAG = "settings_app_version_card"
internal const val SETTINGS_RELEASE_NOTES_SHEET_TAG = "settings_release_notes_sheet"

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
                SettingsWorkoutSection(
                    weekStartDay = state.weekStartDay,
                    distanceUnit = state.distanceUnit,
                    paceUnit = state.paceUnit,
                    weightUnit = state.weightUnit,
                    onSlotModeClick = onSlotModeClick,
                    onWeekStartClick = onWeekStartClick,
                    onUnitsClick = onUnitsClick,
                )
                SettingsThemeSection(themeMode = state.themeMode, onThemeClick = onThemeClick)
                SettingsLanguageSection(language = state.language, onLanguageClick = onLanguageClick)
                SettingsAboutSection(
                    appVersion = appVersion,
                    hasReleaseNotes = releaseNotesDefinition != null,
                    onReleaseNotesClick = { isReleaseNotesVisible = true },
                    onFeedbackClick = onFeedbackClick,
                    onRateClick = onRateClick,
                )
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
