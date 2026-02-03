package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.*
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ARABIC
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.FRENCH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.GERMAN
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.HINDI
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ITALIAN
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.JAPANESE
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SPANISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        appVersion = VERSION_NAME,
        onThemeSelected = viewModel::setThemeMode,
        onLanguageSelected = viewModel::setLanguage,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    appVersion: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingXxl),
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
                selected = state.themeMode == LIGHT,
                onClick = { onThemeSelected(LIGHT) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_theme_dark),
                selected = state.themeMode == DARK,
                onClick = { onThemeSelected(DARK) },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_language_title)) {
            SettingsOptionRow(
                label = stringResource(R.string.settings_language_system),
                selected = state.language == SYSTEM,
                onClick = { onLanguageSelected(SYSTEM) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_english),
                selected = state.language == ENGLISH,
                onClick = { onLanguageSelected(ENGLISH) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_portuguese_brazil),
                selected = state.language == PORTUGUESE_BRAZIL,
                onClick = { onLanguageSelected(PORTUGUESE_BRAZIL) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_german),
                selected = state.language == GERMAN,
                onClick = { onLanguageSelected(GERMAN) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_french),
                selected = state.language == FRENCH,
                onClick = { onLanguageSelected(FRENCH) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_spanish),
                selected = state.language == SPANISH,
                onClick = { onLanguageSelected(SPANISH) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_italian),
                selected = state.language == ITALIAN,
                onClick = { onLanguageSelected(ITALIAN) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_arabic),
                selected = state.language == ARABIC,
                onClick = { onLanguageSelected(ARABIC) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_hindi),
                selected = state.language == HINDI,
                onClick = { onLanguageSelected(HINDI) },
            )

            SettingsOptionRow(
                label = stringResource(R.string.settings_language_japanese),
                selected = state.language == JAPANESE,
                onClick = { onLanguageSelected(JAPANESE) },
            )
        }

        Spacer(modifier = Modifier.height(SpacingLg))

        HorizontalDivider()

        Text(
            text = stringResource(R.string.settings_app_version, appVersion),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingXs, bottom = SpacingXs),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Surface(
            tonalElevation = ElevationSm,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = SpacingLg,
                        vertical = SpacingMd,
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
                .padding(vertical = SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )

        Spacer(modifier = Modifier.width(SpacingLg))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
