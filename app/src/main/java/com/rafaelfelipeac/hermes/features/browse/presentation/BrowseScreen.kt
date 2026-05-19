@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.browse.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.features.activity.presentation.ActivityScreen
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.presentation.CategoriesScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.DeveloperModeScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsBackupScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsState
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophiesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val BROWSE_ROOT_TAG = "browse_root"
private const val BROWSE_CARD_TAG_PREFIX = "browse_card_"
private const val BACKUP_MIME_TYPE = "application/json"
private const val BACKUP_EXTENSION = ".json"
private const val BACKUP_FILE_NAME_PREFIX = "hermes-backup-"
private const val ISO_TIME_SEPARATOR = ":"
private const val FILE_SAFE_TIME_SEPARATOR = "-"
private const val EXPORT_WRITE_FAILED = "export_write_failed"
private const val EXPORT_DESTINATION_SAVE_AS = "save_as"
private const val EXPORT_DESTINATION_FOLDER = "folder"

@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    route: BrowseDestination = BrowseDestination.ROOT,
    settingsState: SettingsState,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    requestedActivityId: Long? = null,
    onRequestedActivityConsumed: () -> Unit = {},
    requestedTrophyStableId: String? = null,
    onRequestedTrophyConsumed: () -> Unit = {},
    onNavigateTo: (BrowseDestination) -> Unit,
    onBack: () -> Unit,
) {
    when (route) {
        BrowseDestination.ROOT ->
            BrowseHome(
                modifier = modifier,
                onNavigateTo = onNavigateTo,
            )

        BrowseDestination.CATEGORIES ->
            CategoriesScreen(
                onBack = onBack,
                modifier = modifier,
            )

        BrowseDestination.TROPHIES ->
            TrophiesScreen(
                modifier = modifier,
                onBack = onBack,
                requestedTrophyStableId = requestedTrophyStableId,
                onRequestedTrophyConsumed = onRequestedTrophyConsumed,
                onOpenActivities = { onNavigateTo(BrowseDestination.ACTIVITIES) },
            )

        BrowseDestination.ACTIVITIES ->
            ActivityScreen(
                modifier = modifier,
                onBack = onBack,
                requestedActivityId = requestedActivityId,
                onRequestedActivityConsumed = onRequestedActivityConsumed,
            )

        BrowseDestination.BACKUP ->
            BrowseBackupScreen(
                modifier = modifier,
                state = settingsState,
                viewModel = settingsViewModel,
                onBack = onBack,
            )

        BrowseDestination.SETTINGS ->
            SettingsScreen(
                modifier = modifier,
                onBack = onBack,
            )

        BrowseDestination.DEVELOPER ->
            if (BuildConfig.DEBUG) {
                DeveloperModeScreen(
                    modifier = modifier,
                    onBack = onBack,
                    viewModel = settingsViewModel,
                )
            } else {
                BrowseHome(
                    modifier = modifier,
                    onNavigateTo = onNavigateTo,
                )
            }
    }
}

@Composable
private fun BrowseHome(
    modifier: Modifier = Modifier,
    onNavigateTo: (BrowseDestination) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Text(
            text = stringResource(R.string.browse_title),
            style = typography.titleLarge,
        )

        BrowseDestinationCard(
            title = stringResource(R.string.categories_title),
            icon = Icons.Outlined.Category,
            onClick = { onNavigateTo(BrowseDestination.CATEGORIES) },
            modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "categories"),
        )

        BrowseDestinationCard(
            title = stringResource(R.string.trophies_title),
            icon = Icons.Outlined.EmojiEvents,
            onClick = { onNavigateTo(BrowseDestination.TROPHIES) },
            modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "trophies"),
        )

        BrowseDestinationCard(
            title = stringResource(R.string.trophies_activities_action),
            icon = Icons.Default.History,
            onClick = { onNavigateTo(BrowseDestination.ACTIVITIES) },
            modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "activities"),
        )

        BrowseDestinationCard(
            title = stringResource(R.string.browse_backup_import_title),
            icon = Icons.Outlined.Inventory2,
            onClick = { onNavigateTo(BrowseDestination.BACKUP) },
            modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "backup"),
        )

        Spacer(modifier = Modifier.height(SpacingXxl))

        BrowseDestinationCard(
            title = stringResource(R.string.settings_title),
            icon = Icons.Default.Settings,
            onClick = { onNavigateTo(BrowseDestination.SETTINGS) },
            modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "settings"),
        )

        if (BuildConfig.DEBUG) {
            BrowseDestinationCard(
                title = stringResource(R.string.settings_developer_title),
                icon = Icons.Outlined.Construction,
                onClick = { onNavigateTo(BrowseDestination.DEVELOPER) },
                modifier = Modifier.testTag(BROWSE_CARD_TAG_PREFIX + "developer"),
            )
        }
    }
}

@Composable
private fun BrowseDestinationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SmallIconSize),
                tint = colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(SpacingMd))
            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BrowseBackupScreen(
    modifier: Modifier = Modifier,
    state: SettingsState,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBackupHelpVisible by rememberSaveable { mutableStateOf(false) }
    var isImportReplaceDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingImportPayload by remember { mutableStateOf<String?>(null) }
    var pendingSaveAsDestinationConfigured by rememberSaveable { mutableStateOf(false) }
    val exportFailedMessage = stringResource(R.string.settings_export_backup_error)
    val exportSuccessMessage = stringResource(R.string.settings_export_backup_success)
    val exportFallbackMessage = stringResource(R.string.settings_export_backup_fallback_save_as)
    val importFailedMessage = stringResource(R.string.settings_import_backup_error)
    val importSuccessMessage = stringResource(R.string.settings_import_backup_success)
    val backupFolderUnavailableMessage = stringResource(R.string.settings_backup_folder_unavailable)

    val exportDocumentLauncher =
        rememberLauncherForActivityResult(CreateDocument(BACKUP_MIME_TYPE)) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            scope.launch {
                val jsonResult = viewModel.exportBackupJson(VERSION_NAME)
                val writeSucceeded =
                    jsonResult.getOrNull()?.let { payload -> writeTextToUri(context, uri, payload) } ?: false
                val message = if (writeSucceeded) exportSuccessMessage else exportFailedMessage
                val exportResult =
                    if (jsonResult.isFailure) {
                        jsonResult
                    } else if (writeSucceeded) {
                        jsonResult
                    } else {
                        Result.failure(IllegalStateException(EXPORT_WRITE_FAILED))
                    }

                viewModel.logExportBackupResult(
                    exportResult = exportResult,
                    destinationType = EXPORT_DESTINATION_SAVE_AS,
                    destinationConfigured = pendingSaveAsDestinationConfigured,
                )
                pendingSaveAsDestinationConfigured = false

                message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

    val backupFolderLauncher =
        rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            }

            scope.launch {
                viewModel.setBackupFolderUri(uri.toString())
            }
        }

    fun importPayload(raw: String) {
        scope.launch {
            when (val result = viewModel.importBackupJson(raw)) {
                is ImportBackupResult.Success -> {
                    Toast.makeText(context, importSuccessMessage, Toast.LENGTH_SHORT).show()
                }

                is ImportBackupResult.Failure -> {
                    val message =
                        when (result.error) {
                            ImportBackupError.INVALID_JSON,
                            ImportBackupError.UNSUPPORTED_SCHEMA_VERSION,
                            ImportBackupError.MISSING_REQUIRED_SECTION,
                            ImportBackupError.INVALID_FIELD_VALUE,
                            ImportBackupError.INVALID_REFERENCE,
                            ImportBackupError.WRITE_FAILED,
                            -> importFailedMessage
                        }

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importDocumentLauncher =
        rememberLauncherForActivityResult(OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val payload = readTextFromUri(context, uri)
                if (payload == null) {
                    Toast.makeText(context, importFailedMessage, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (viewModel.hasBackupData()) {
                    pendingImportPayload = payload
                    isImportReplaceDialogVisible = true
                } else {
                    importPayload(payload)
                }
            }
        }

    LaunchedEffect(state.backupFolderUri) {
        val rawUri = state.backupFolderUri ?: return@LaunchedEffect
        val folderUri = rawUri.toUri()
        val isAccessible =
            runCatching {
                val root = DocumentFile.fromTreeUri(context, folderUri)
                root != null && root.exists() && root.canWrite()
            }.getOrDefault(false)

        if (!isAccessible) {
            viewModel.clearBackupFolderUri(logUserAction = false)

            Toast.makeText(context, backupFolderUnavailableMessage, Toast.LENGTH_SHORT).show()
        }
    }

    BrowseBackupContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onHelpClick = { isBackupHelpVisible = true },
        onExportClick = {
            scope.launch {
                val configuredUri = state.backupFolderUri

                if (configuredUri == null) {
                    pendingSaveAsDestinationConfigured = false
                    exportDocumentLauncher.launch(backupFileName())
                } else {
                    val jsonResult = viewModel.exportBackupJson(VERSION_NAME)

                    if (jsonResult.isFailure) {
                        Toast.makeText(
                            context,
                            exportFallbackMessage,
                            Toast.LENGTH_SHORT,
                        ).show()

                        viewModel.logExportBackupResult(
                            exportResult = jsonResult,
                            destinationType = EXPORT_DESTINATION_FOLDER,
                            destinationConfigured = false,
                        )
                    } else {
                        val writeSucceeded =
                            jsonResult.getOrNull()?.let { payload ->
                                writeTextToBackupFolder(
                                    context = context,
                                    treeUri = configuredUri.toUri(),
                                    content = payload,
                                )
                            } ?: false

                        if (writeSucceeded) {
                            Toast.makeText(
                                context,
                                exportSuccessMessage,
                                Toast.LENGTH_SHORT,
                            ).show()

                            viewModel.logExportBackupResult(
                                exportResult = jsonResult,
                                destinationType = EXPORT_DESTINATION_FOLDER,
                                destinationConfigured = true,
                            )
                        } else {
                            Toast.makeText(
                                context,
                                exportFallbackMessage,
                                Toast.LENGTH_SHORT,
                            ).show()

                            pendingSaveAsDestinationConfigured = true
                            exportDocumentLauncher.launch(backupFileName())

                            viewModel.logExportBackupResult(
                                exportResult = jsonResult,
                                destinationType = EXPORT_DESTINATION_FOLDER,
                                destinationConfigured = false,
                            )
                        }
                    }
                }
            }
        },
        onImportClick = { importDocumentLauncher.launch(arrayOf(BACKUP_MIME_TYPE)) },
        onSelectFolderClick = { backupFolderLauncher.launch(null) },
        onClearFolderClick = {
            scope.launch {
                viewModel.clearBackupFolderUri()
            }
        },
    )

    if (isBackupHelpVisible) {
        AlertDialog(
            onDismissRequest = { isBackupHelpVisible = false },
            title = { Text(text = stringResource(R.string.settings_backup_help_title)) },
            text = { Text(text = stringResource(R.string.settings_backup_help_message)) },
            confirmButton = {
                Button(onClick = { isBackupHelpVisible = false }) {
                    Text(text = stringResource(R.string.weekly_training_tbd_help_confirm))
                }
            },
        )
    }

    if (isImportReplaceDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isImportReplaceDialogVisible = false
                pendingImportPayload = null
            },
            title = { Text(text = stringResource(R.string.settings_import_backup_replace_title)) },
            text = { Text(text = stringResource(R.string.settings_import_backup_replace_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        val payload = pendingImportPayload
                        if (payload != null) {
                            importPayload(payload)
                        }
                        isImportReplaceDialogVisible = false
                        pendingImportPayload = null
                    },
                ) {
                    Text(text = stringResource(R.string.settings_import_backup_replace_confirm))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        isImportReplaceDialogVisible = false
                        pendingImportPayload = null
                    },
                ) {
                    Text(text = stringResource(R.string.settings_import_backup_replace_cancel))
                }
            },
        )
    }
}

@Composable
private fun BrowseBackupContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onSelectFolderClick: () -> Unit,
    onClearFolderClick: () -> Unit,
) {
    SettingsBackupScreen(
        state = state,
        onBack = onBack,
        onHelpClick = onHelpClick,
        modifier = modifier,
        onExportClick = onExportClick,
        onImportClick = onImportClick,
        onSelectFolderClick = onSelectFolderClick,
        onClearFolderClick = onClearFolderClick,
    )
}

private fun backupFileName(): String {
    val timestamp = java.time.LocalDateTime.now().toString().replace(ISO_TIME_SEPARATOR, FILE_SAFE_TIME_SEPARATOR)
    return "$BACKUP_FILE_NAME_PREFIX$timestamp$BACKUP_EXTENSION"
}

private suspend fun writeTextToUri(
    context: Context,
    uri: Uri,
    content: String,
): Boolean {
    return withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(content)
                true
            } ?: false
        }.getOrDefault(false)
    }
}

private suspend fun readTextFromUri(
    context: Context,
    uri: Uri,
): String? {
    return withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }
}

private suspend fun writeTextToBackupFolder(
    context: Context,
    treeUri: Uri,
    content: String,
): Boolean {
    val backupFile =
        withContext(Dispatchers.IO) {
            runCatching {
                val root = DocumentFile.fromTreeUri(context, treeUri)

                if (root == null || !root.canWrite()) {
                    null
                } else {
                    root.createFile(BACKUP_MIME_TYPE, backupFileName())
                }
            }.getOrNull()
        }

    return backupFile?.let { file -> writeTextToUri(context, file.uri, content) } ?: false
}
