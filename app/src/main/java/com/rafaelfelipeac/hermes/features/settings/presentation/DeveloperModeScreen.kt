package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SettingsDeveloperSectionSpacing
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl

@Composable
internal fun DeveloperModeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var pendingDeveloperAction by rememberSaveable { mutableStateOf<DeveloperSeedAction?>(null) }

    SettingsDetailScreen(
        title = stringResource(R.string.settings_developer_title),
        onBack = onBack,
        modifier = modifier,
        contentInsideCard = false,
    ) {
        Column(
            modifier = Modifier.padding(bottom = SpacingXl),
            verticalArrangement = Arrangement.spacedBy(SettingsDeveloperSectionSpacing),
        ) {
            SettingsSection(title = stringResource(R.string.settings_developer_data_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(SettingsDeveloperSectionSpacing)) {
                    Text(
                        text = stringResource(R.string.settings_developer_data_body),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )

                    SettingsActionButton(
                        label = stringResource(R.string.settings_seed_demo_data),
                        onClick = { pendingDeveloperAction = DeveloperSeedAction.DEMO_DATA },
                    )

                    SettingsActionButton(
                        label = stringResource(R.string.settings_clear_database),
                        onClick = { pendingDeveloperAction = DeveloperSeedAction.CLEAR_DATABASE },
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.settings_developer_trophies_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(SettingsDeveloperSectionSpacing)) {
                    Text(
                        text = stringResource(R.string.settings_developer_trophies_body),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )

                    SettingsActionButton(
                        label = stringResource(R.string.settings_seed_mixed_trophies),
                        onClick = { pendingDeveloperAction = DeveloperSeedAction.MIXED_TROPHIES },
                    )

                    SettingsActionButton(
                        label = stringResource(R.string.settings_seed_locked_trophies),
                        onClick = { pendingDeveloperAction = DeveloperSeedAction.LOCKED_TROPHIES },
                    )

                    SettingsActionButton(
                        label = stringResource(R.string.settings_seed_completed_trophies),
                        onClick = { pendingDeveloperAction = DeveloperSeedAction.UNLOCKED_TROPHIES },
                    )
                }
            }
        }

        pendingDeveloperAction?.let { action ->
            AlertDialog(
                onDismissRequest = { pendingDeveloperAction = null },
                title = {
                    Text(
                        text =
                            stringResource(
                                if (action == DeveloperSeedAction.CLEAR_DATABASE) {
                                    R.string.settings_clear_database_confirm_title
                                } else {
                                    R.string.settings_developer_confirm_title
                                },
                            ),
                    )
                },
                text = {
                    Text(
                        text =
                            stringResource(
                                if (action == DeveloperSeedAction.CLEAR_DATABASE) {
                                    R.string.settings_clear_database_confirm_message
                                } else {
                                    R.string.settings_developer_confirm_message
                                },
                            ),
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (action) {
                                DeveloperSeedAction.DEMO_DATA -> viewModel.seedDemoData()
                                DeveloperSeedAction.MIXED_TROPHIES -> viewModel.seedMixedTrophies()
                                DeveloperSeedAction.LOCKED_TROPHIES -> viewModel.seedLockedTrophies()
                                DeveloperSeedAction.UNLOCKED_TROPHIES -> viewModel.seedCompletedTrophies()
                                DeveloperSeedAction.CLEAR_DATABASE -> viewModel.clearDatabase()
                            }
                            pendingDeveloperAction = null
                        },
                    ) {
                        Text(text = stringResource(R.string.settings_developer_confirm_continue))
                    }
                },
                dismissButton = {
                    Button(onClick = { pendingDeveloperAction = null }) {
                        Text(text = stringResource(R.string.settings_developer_confirm_cancel))
                    }
                },
            )
        }
    }
}

private enum class DeveloperSeedAction {
    DEMO_DATA,
    MIXED_TROPHIES,
    LOCKED_TROPHIES,
    UNLOCKED_TROPHIES,
    CLEAR_DATABASE,
}
