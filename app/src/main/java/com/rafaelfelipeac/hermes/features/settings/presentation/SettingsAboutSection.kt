package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs

@Composable
internal fun SettingsAboutSection(
    appVersion: String,
    hasReleaseNotes: Boolean,
    onReleaseNotesClick: () -> Unit,
    onFeedbackClick: (String, String) -> Unit,
    onRateClick: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val feedbackSubject =
        stringResource(
            R.string.settings_feedback_subject,
            appName,
        )
    val feedbackBody =
        feedbackBodyText(
            stringResource(R.string.settings_feedback_email_body, appVersion),
        )

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
            hasReleaseNotes = hasReleaseNotes,
            onClick = onReleaseNotesClick,
        )
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
