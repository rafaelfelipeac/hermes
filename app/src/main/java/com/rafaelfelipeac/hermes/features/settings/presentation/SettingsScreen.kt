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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.features.categories.presentation.CategoriesScreen
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
    initialRoute: SettingsRoute? = null,
    onRouteConsumed: () -> Unit = {},
    onExitCategories: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val demoDataCreatedMessage = stringResource(R.string.demo_data_created)
    var route by rememberSaveable { mutableStateOf(SettingsRoute.MAIN) }

    LaunchedEffect(initialRoute) {
        if (initialRoute != null) {
            route = initialRoute
            onRouteConsumed()
        }
    }

    when (route) {
        SettingsRoute.MAIN ->
            SettingsContent(
                state = state,
                appVersion = VERSION_NAME,
                onThemeSelected = viewModel::setThemeMode,
                onLanguageSelected = viewModel::setLanguage,
                onSeedDemoData = {
                    viewModel.seedDemoData()
                    android.widget.Toast.makeText(
                        context,
                        demoDataCreatedMessage,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
                onCategoriesClick = { route = SettingsRoute.CATEGORIES },
                modifier = modifier,
            )
        SettingsRoute.CATEGORIES ->
            CategoriesScreen(
                onBack = {
                    route = SettingsRoute.MAIN
                    onExitCategories()
                },
                modifier = modifier,
            )
    }
}

@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    appVersion: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSeedDemoData: () -> Unit,
    onCategoriesClick: () -> Unit,
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
            style = typography.titleLarge,
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

        SettingsSection(title = stringResource(R.string.settings_workouts_title)) {
            SettingsNavigationRow(
                label = stringResource(R.string.settings_categories),
                onClick = onCategoriesClick,
            )
        }

        if (BuildConfig.DEBUG) {
            SettingsSection(title = stringResource(R.string.settings_developer_title)) {
                SettingsActionButton(
                    label = stringResource(R.string.seed_demo_data),
                    onClick = onSeedDemoData,
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingLg))

        HorizontalDivider()

        Text(
            text = stringResource(R.string.settings_app_version, appVersion),
            style = typography.bodySmall,
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
            style = typography.titleMedium,
        )

        Surface(
            tonalElevation = ElevationSm,
            shape = shapes.medium,
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
            style = typography.bodyLarge,
        )
    }
}

@Composable
private fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onClick) {
            Text(
                text = label,
                style = typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    label: String,
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
        Text(
            text = label,
            style = typography.bodyLarge,
        )
    }
}
