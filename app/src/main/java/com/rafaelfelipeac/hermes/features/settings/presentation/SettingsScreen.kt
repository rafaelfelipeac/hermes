package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsContent(
        state = state,
        appVersion = BuildConfig.VERSION_NAME,
        onThemeSelected = viewModel::setThemeMode,
        onLanguageSelected = viewModel::setLanguage,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    appVersion: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxl),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
            )

            SettingsSection(title = stringResource(R.string.settings_theme_title)) {
                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_system),
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                )
                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_light),
                    selected = state.themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) },
                )
                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = state.themeMode == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_language_title)) {
                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_system),
                    selected = state.language == AppLanguage.SYSTEM,
                    onClick = { onLanguageSelected(AppLanguage.SYSTEM) },
                )
                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_english),
                    selected = state.language == AppLanguage.ENGLISH,
                    onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
                )
                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_portuguese_brazil),
                    selected = state.language == AppLanguage.PORTUGUESE_BRAZIL,
                    onClick = { onLanguageSelected(AppLanguage.PORTUGUESE_BRAZIL) },
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingXl, vertical = Dimens.SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider()
            Text(
                text = stringResource(R.string.settings_app_version, appVersion),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Dimens.SpacingMd),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Surface(
            tonalElevation = Dimens.ElevationSm,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = Dimens.SpacingLg,
                        vertical = Dimens.SpacingMd,
                    ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = Dimens.SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Spacer(modifier = Modifier.width(Dimens.SpacingLg))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
