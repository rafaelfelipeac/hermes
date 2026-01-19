package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.settings.AppLanguage
import com.rafaelfelipeac.hermes.core.settings.ThemeMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    SettingsContent(
        state = state,
        onThemeSelected = viewModel::setThemeMode,
        onLanguageSelected = viewModel::setLanguage,
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge
        )

        SettingsSection(title = stringResource(R.string.settings_theme_title)) {
            SettingsOptionRow(
                label = stringResource(R.string.settings_theme_system),
                selected = state.themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) }
            )
            SettingsOptionRow(
                label = stringResource(R.string.settings_theme_light),
                selected = state.themeMode == ThemeMode.LIGHT,
                onClick = { onThemeSelected(ThemeMode.LIGHT) }
            )
            SettingsOptionRow(
                label = stringResource(R.string.settings_theme_dark),
                selected = state.themeMode == ThemeMode.DARK,
                onClick = { onThemeSelected(ThemeMode.DARK) }
            )
        }

        SettingsSection(title = stringResource(R.string.settings_language_title)) {
            SettingsOptionRow(
                label = stringResource(R.string.settings_language_system),
                selected = state.language == AppLanguage.SYSTEM,
                onClick = { onLanguageSelected(AppLanguage.SYSTEM) }
            )
            SettingsOptionRow(
                label = stringResource(R.string.settings_language_english),
                selected = state.language == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
            )
            SettingsOptionRow(
                label = stringResource(R.string.settings_language_portuguese_brazil),
                selected = state.language == AppLanguage.PORTUGUESE_BRAZIL,
                onClick = { onLanguageSelected(AppLanguage.PORTUGUESE_BRAZIL) }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
