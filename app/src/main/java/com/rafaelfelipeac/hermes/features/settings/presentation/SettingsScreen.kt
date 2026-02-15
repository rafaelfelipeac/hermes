package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
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
import androidx.core.net.toUri

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
    val feedbackEmail = stringResource(R.string.settings_feedback_email)
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
                onThemeClick = { route = SettingsRoute.THEME },
                onLanguageClick = { route = SettingsRoute.LANGUAGE },
                onFeedbackClick = { subject, body ->
                    val normalizedBody = body.replace("\n", "\r\n")
                    val mailToUri =
                        Uri.parse(
                            "mailto:$feedbackEmail" +
                                "?subject=${Uri.encode(subject)}" +
                                "&body=${Uri.encode(normalizedBody)}",
                        )
                    val intent =
                        Intent(Intent.ACTION_SENDTO, mailToUri).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, normalizedBody)
                        }
                    context.startActivity(intent)
                },
                onRateClick = {
                    val packageName = context.packageName
                    val marketIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            "market://details?id=$packageName".toUri(),
                        )
                    val webIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$packageName".toUri(),
                        )

                    if (marketIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(marketIntent)
                    } else {
                        context.startActivity(webIntent)
                    }
                },
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
        SettingsRoute.THEME ->
            SettingsDetailScreen(
                title = stringResource(R.string.settings_theme_title),
                onBack = { route = SettingsRoute.MAIN },
                modifier = modifier,
            ) {
                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_system),
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_light),
                    selected = state.themeMode == LIGHT,
                    onClick = { viewModel.setThemeMode(LIGHT) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = state.themeMode == DARK,
                    onClick = { viewModel.setThemeMode(DARK) },
                )
            }
        SettingsRoute.LANGUAGE ->
            SettingsDetailScreen(
                title = stringResource(R.string.settings_language_title),
                onBack = { route = SettingsRoute.MAIN },
                modifier = modifier,
            ) {
                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_system),
                    selected = state.language == SYSTEM,
                    onClick = { viewModel.setLanguage(SYSTEM) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_english),
                    selected = state.language == ENGLISH,
                    onClick = { viewModel.setLanguage(ENGLISH) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_portuguese_brazil),
                    selected = state.language == PORTUGUESE_BRAZIL,
                    onClick = { viewModel.setLanguage(PORTUGUESE_BRAZIL) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_german),
                    selected = state.language == GERMAN,
                    onClick = { viewModel.setLanguage(GERMAN) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_french),
                    selected = state.language == FRENCH,
                    onClick = { viewModel.setLanguage(FRENCH) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_spanish),
                    selected = state.language == SPANISH,
                    onClick = { viewModel.setLanguage(SPANISH) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_italian),
                    selected = state.language == ITALIAN,
                    onClick = { viewModel.setLanguage(ITALIAN) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_arabic),
                    selected = state.language == ARABIC,
                    onClick = { viewModel.setLanguage(ARABIC) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_hindi),
                    selected = state.language == HINDI,
                    onClick = { viewModel.setLanguage(HINDI) },
                )

                SettingsOptionRow(
                    label = stringResource(R.string.settings_language_japanese),
                    selected = state.language == JAPANESE,
                    onClick = { viewModel.setLanguage(JAPANESE) },
                )
            }
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
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onFeedbackClick: (String, String) -> Unit,
    onRateClick: () -> Unit,
    onSeedDemoData: () -> Unit,
    onCategoriesClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val appName = stringResource(R.string.app_name)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(SpacingXl)
                    .padding(bottom = SpacingXl + SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingXxl),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = typography.titleLarge,
            )

            SettingsSection(title = stringResource(R.string.settings_theme_title)) {
                SettingsNavigationRow(
                    label = themeLabel(state.themeMode),
                    onClick = onThemeClick,
                )
            }

            SettingsSection(title = stringResource(R.string.settings_language_title)) {
                SettingsNavigationRow(
                    label = languageLabel(state.language),
                    onClick = onLanguageClick,
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SettingsCard {
                val feedbackSubject =
                    stringResource(
                        R.string.settings_feedback_subject,
                        appName,
                    )
                val feedbackBody =
                    stringResource(
                        R.string.settings_feedback_email_body,
                        appVersion,
                    ).replace("__NL__", "\n")
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
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = SpacingXl, vertical = SpacingXs),
        ) {
            HorizontalDivider(modifier = Modifier.padding(bottom = SpacingSm))

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

        SettingsCard(content = content)
    }
}

@Composable
private fun SettingsDetailScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.categories_back),
            )
        }

        Text(
            text = title,
            style = typography.titleLarge,
        )

        SettingsCard(content = content)
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.padding(vertical = SpacingXxs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClick,
        ) {
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
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = SpacingXs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(top = SpacingXxs),
        )

        Spacer(modifier = Modifier.width(SpacingLg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(SpacingXxs))

            Text(
                text = body,
                style = typography.bodySmall,
            )
        }
    }
}

@Composable
private fun themeLabel(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
        LIGHT -> stringResource(R.string.settings_theme_light)
        DARK -> stringResource(R.string.settings_theme_dark)
    }
}

@Composable
private fun languageLabel(language: AppLanguage): String {
    return when (language) {
        SYSTEM -> stringResource(R.string.settings_language_system)
        ENGLISH -> stringResource(R.string.settings_language_english)
        PORTUGUESE_BRAZIL ->
            stringResource(R.string.settings_language_portuguese_brazil)
        GERMAN -> stringResource(R.string.settings_language_german)
        FRENCH -> stringResource(R.string.settings_language_french)
        SPANISH -> stringResource(R.string.settings_language_spanish)
        ITALIAN -> stringResource(R.string.settings_language_italian)
        ARABIC -> stringResource(R.string.settings_language_arabic)
        HINDI -> stringResource(R.string.settings_language_hindi)
        JAPANESE -> stringResource(R.string.settings_language_japanese)
    }
}
