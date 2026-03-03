@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE_TOKEN
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.presentation.CategoriesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val DEBUG_PACKAGE_SUFFIX = ".dev"
internal const val SETTINGS_THEME_ROW_TAG = "settings_theme_row"
internal const val SETTINGS_LANGUAGE_ROW_TAG = "settings_language_row"
internal const val SETTINGS_WEEK_START_ROW_TAG = "settings_week_start_row"
private const val SETTINGS_SCREEN_TAG = "SettingsScreen"
private const val BACKUP_MIME_TYPE = "application/json"
private const val BACKUP_EXTENSION = ".json"
private const val BACKUP_FILE_NAME_PREFIX = "hermes-backup-"
private const val ISO_TIME_SEPARATOR = ":"
private const val FILE_SAFE_TIME_SEPARATOR = "-"
private const val LOG_FEEDBACK_INTENT_NOT_FOUND = "Feedback intent not found."
private const val LOG_FEEDBACK_INTENT_BLOCKED = "Feedback intent blocked by security policy."
private const val LOG_MARKET_INTENT_NOT_FOUND = "Market intent not found."
private const val LOG_MARKET_INTENT_BLOCKED = "Market intent blocked by security policy."
private const val LOG_WEB_INTENT_NOT_FOUND = "Web intent not found."
private const val LOG_WEB_INTENT_BLOCKED = "Web intent blocked by security policy."
private const val EXPORT_WRITE_FAILED = "export_write_failed"
private const val EXPORT_DESTINATION_SAVE_AS = "save_as"
private const val EXPORT_DESTINATION_FOLDER = "folder"

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
    val demoDataCreatedMessage = stringResource(R.string.settings_demo_data_created)
    val feedbackUnavailableMessage = stringResource(R.string.settings_feedback_unavailable)
    val rateUnavailableMessage = stringResource(R.string.settings_rate_unavailable)
    val feedbackEmail = stringResource(R.string.settings_feedback_email)
    val mailtoTemplate = stringResource(R.string.settings_feedback_mailto_uri)
    val marketUrlTemplate = stringResource(R.string.settings_play_store_market_url)
    val webUrlTemplate = stringResource(R.string.settings_play_store_web_url)
    var route by rememberSaveable { mutableStateOf(SettingsRoute.MAIN) }
    var isSlotModeHelpVisible by rememberSaveable { mutableStateOf(false) }
    var isBackupHelpVisible by rememberSaveable { mutableStateOf(false) }
    var isImportReplaceDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingImportPayload by remember { mutableStateOf<String?>(null) }
    var pendingSaveAsDestinationConfigured by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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

    BackHandler(enabled = route != SettingsRoute.MAIN) {
        if (route == SettingsRoute.CATEGORIES) {
            onExitCategories()
        }

        route = SettingsRoute.MAIN
    }

    LaunchedEffect(initialRoute) {
        if (initialRoute != null) {
            route = initialRoute
            onRouteConsumed()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.demoSeedCompletedEvents.collect {
            Toast.makeText(
                context,
                demoDataCreatedMessage,
                Toast.LENGTH_SHORT,
            ).show()
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

    when (route) {
        SettingsRoute.MAIN ->
            SettingsContent(
                state = state,
                appVersion = VERSION_NAME,
                onThemeClick = { route = SettingsRoute.THEME },
                onLanguageClick = { route = SettingsRoute.LANGUAGE },
                onWeekStartClick = { route = SettingsRoute.START_OF_WEEK },
                onSlotModeClick = { route = SettingsRoute.SLOT_MODE },
                onFeedbackClick = { subject, body ->
                    val normalizedBody = body.replace("\n", "\r\n")
                    val mailToUri =
                        String.format(
                            Locale.ROOT,
                            mailtoTemplate,
                            feedbackEmail,
                            Uri.encode(subject),
                            Uri.encode(normalizedBody),
                        ).toUri()
                    val intent =
                        Intent(Intent.ACTION_SENDTO, mailToUri).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, normalizedBody)
                        }

                    try {
                        context.startActivity(intent)
                    } catch (error: ActivityNotFoundException) {
                        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_NOT_FOUND, error)

                        Toast.makeText(
                            context,
                            feedbackUnavailableMessage,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (error: SecurityException) {
                        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_BLOCKED, error)

                        Toast.makeText(
                            context,
                            feedbackUnavailableMessage,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                onRateClick = {
                    val packageName =
                        if (BuildConfig.DEBUG && context.packageName.endsWith(DEBUG_PACKAGE_SUFFIX)) {
                            context.packageName.removeSuffix(DEBUG_PACKAGE_SUFFIX)
                        } else {
                            context.packageName
                        }
                    val marketIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            String.format(
                                Locale.ROOT,
                                marketUrlTemplate,
                                packageName,
                            ).toUri(),
                        )
                    val webIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            String.format(
                                Locale.ROOT,
                                webUrlTemplate,
                                packageName,
                            ).toUri(),
                        )
                    val launchFailed =
                        try {
                            context.startActivity(marketIntent)
                            false
                        } catch (error: ActivityNotFoundException) {
                            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_NOT_FOUND, error)
                            true
                        } catch (error: SecurityException) {
                            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_BLOCKED, error)
                            true
                        }

                    if (launchFailed) {
                        val webLaunchFailed =
                            try {
                                context.startActivity(webIntent)
                                false
                            } catch (error: ActivityNotFoundException) {
                                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_NOT_FOUND, error)
                                true
                            } catch (error: SecurityException) {
                                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_BLOCKED, error)
                                true
                            }

                        if (webLaunchFailed) {
                            Toast.makeText(
                                context,
                                rateUnavailableMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                onSeedDemoData = {
                    viewModel.seedDemoData()
                },
                onCategoriesClick = { route = SettingsRoute.CATEGORIES },
                onBackupClick = { route = SettingsRoute.BACKUP },
                modifier = modifier,
            )
        SettingsRoute.THEME ->
            SettingsThemeScreen(
                themeMode = state.themeMode,
                onBack = { route = SettingsRoute.MAIN },
                onThemeSelected = viewModel::setThemeMode,
                modifier = modifier,
            )
        SettingsRoute.LANGUAGE ->
            SettingsLanguageScreen(
                language = state.language,
                onBack = { route = SettingsRoute.MAIN },
                onLanguageSelected = viewModel::setLanguage,
                modifier = modifier,
            )
        SettingsRoute.START_OF_WEEK ->
            SettingsWeekStartScreen(
                weekStartDay = state.weekStartDay,
                onBack = { route = SettingsRoute.MAIN },
                onWeekStartSelected = viewModel::setWeekStartDay,
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
        SettingsRoute.SLOT_MODE ->
            SettingsSlotModeScreen(
                slotModePolicy = state.slotModePolicy,
                onBack = { route = SettingsRoute.MAIN },
                onHelpClick = { isSlotModeHelpVisible = true },
                onSlotModeSelected = viewModel::setSlotModePolicy,
                modifier = modifier,
            )
        SettingsRoute.BACKUP ->
            SettingsBackupScreen(
                state = state,
                onBack = { route = SettingsRoute.MAIN },
                onHelpClick = { isBackupHelpVisible = true },
                modifier = modifier,
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
                                        destinationConfigured = true,
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
    }

    if (isSlotModeHelpVisible) {
        AlertDialog(
            onDismissRequest = { isSlotModeHelpVisible = false },
            title = { Text(text = stringResource(R.string.settings_slot_mode_help_title)) },
            text = { Text(text = stringResource(R.string.settings_slot_mode_help_message)) },
            confirmButton = {
                Button(onClick = { isSlotModeHelpVisible = false }) {
                    Text(text = stringResource(R.string.weekly_training_tbd_help_confirm))
                }
            },
        )
    }

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
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    appVersion: String,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onWeekStartClick: () -> Unit,
    onSlotModeClick: () -> Unit,
    onFeedbackClick: (String, String) -> Unit,
    onRateClick: () -> Unit,
    onSeedDemoData: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBackupClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val appName = stringResource(R.string.app_name)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(SpacingXl),
            verticalArrangement = Arrangement.spacedBy(SpacingXxl),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = typography.titleLarge,
            )

            SettingsSection(title = stringResource(R.string.settings_workouts_title)) {
                SettingsNavigationRow(
                    label = stringResource(R.string.settings_categories),
                    onClick = onCategoriesClick,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                SettingsNavigationRow(
                    label = stringResource(R.string.settings_slot_mode_title),
                    onClick = onSlotModeClick,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                SettingsNavigationRow(
                    label = stringResource(R.string.settings_week_start_title),
                    detail = weekStartLabel(state.weekStartDay),
                    onClick = onWeekStartClick,
                    modifier = Modifier.testTag(SETTINGS_WEEK_START_ROW_TAG),
                )
            }

            SettingsSection(title = stringResource(R.string.settings_data_title)) {
                SettingsNavigationRow(
                    label = stringResource(R.string.settings_backup_title),
                    onClick = onBackupClick,
                )
            }

            SettingsSection(title = stringResource(R.string.settings_theme_title)) {
                SettingsNavigationRow(
                    label = themeLabel(state.themeMode),
                    onClick = onThemeClick,
                    modifier = Modifier.testTag(SETTINGS_THEME_ROW_TAG),
                )
            }

            SettingsSection(title = stringResource(R.string.settings_language_title)) {
                SettingsNavigationRow(
                    label = languageLabel(state.language),
                    onClick = onLanguageClick,
                    modifier = Modifier.testTag(SETTINGS_LANGUAGE_ROW_TAG),
                )
            }

            if (BuildConfig.DEBUG) {
                SettingsSection(title = stringResource(R.string.settings_developer_title)) {
                    SettingsActionButton(
                        label = stringResource(R.string.settings_seed_demo_data),
                        onClick = onSeedDemoData,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val feedbackSubject =
                stringResource(
                    R.string.settings_feedback_subject,
                    appName,
                )
            val feedbackBody =
                stringResource(
                    R.string.settings_feedback_email_body,
                    appVersion,
                ).replace(NEW_LINE_TOKEN, NEW_LINE)

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

                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_app_version, appVersion),
                        style = typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = SpacingSm),
                    )
                }
            }
        }
    }
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
