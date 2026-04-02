package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconGlyphSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SettingsDeveloperButtonContentHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SettingsDeveloperButtonContentVerticalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SettingsDeveloperButtonVerticalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
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
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.FRIDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.MONDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.SATURDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.SUNDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.THURSDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.TUESDAY
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.WEDNESDAY

@Composable
internal fun SettingsSection(
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
internal fun SettingsDetailScreen(
    title: String,
    onBack: () -> Unit,
    onHelpClick: (() -> Unit)? = null,
    helpContentDescription: String? = null,
    contentInsideCard: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    val resolvedHelpContentDescription =
        if (onHelpClick != null) {
            requireNotNull(helpContentDescription) {
                "helpContentDescription is required when onHelpClick is provided."
            }
        } else {
            null
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.settings_back),
                )
            }

            Text(
                text = title,
                style = typography.titleLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (onHelpClick != null) {
                Surface(
                    shape = CircleShape,
                    color = colorScheme.surfaceVariant,
                    tonalElevation = ElevationSm,
                    shadowElevation = ElevationSm,
                    modifier = Modifier.size(HelpIconSize),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clickable(onClick = onHelpClick),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = resolvedHelpContentDescription,
                            modifier = Modifier.size(HelpIconGlyphSize),
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = SpacingXl)) {
            if (contentInsideCard) {
                SettingsCard(content = content)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingLg)) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
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
internal fun SettingsOptionRow(
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
internal fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SettingsDeveloperButtonVerticalPadding),
        contentPadding =
            PaddingValues(
                horizontal = SettingsDeveloperButtonContentHorizontalPadding,
                vertical = SettingsDeveloperButtonContentVerticalPadding,
            ),
        elevation = ButtonDefaults.buttonElevation(),
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
        )
    }
}

@Composable
internal fun SettingsNavigationRow(
    label: String,
    detail: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SpacingXxs),
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = typography.bodySmall,
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
        )
    }
}

@Composable
internal fun SettingsInfoRow(
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
internal fun themeLabel(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
        LIGHT -> stringResource(R.string.settings_theme_light)
        DARK -> stringResource(R.string.settings_theme_dark)
    }
}

@Composable
internal fun languageLabel(language: AppLanguage): String {
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

@Composable
internal fun weekStartLabel(weekStartDay: WeekStartDay): String {
    return when (weekStartDay) {
        MONDAY -> stringResource(R.string.day_monday)
        TUESDAY -> stringResource(R.string.day_tuesday)
        WEDNESDAY -> stringResource(R.string.day_wednesday)
        THURSDAY -> stringResource(R.string.day_thursday)
        FRIDAY -> stringResource(R.string.day_friday)
        SATURDAY -> stringResource(R.string.day_saturday)
        SUNDAY -> stringResource(R.string.day_sunday)
    }
}
