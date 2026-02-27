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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.BuildConfig.VERSION_NAME
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE_TOKEN
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconGlyphSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
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
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val DEBUG_PACKAGE_SUFFIX = ".dev"
internal const val SETTINGS_THEME_ROW_TAG = "settings_theme_row"
internal const val SETTINGS_LANGUAGE_ROW_TAG = "settings_language_row"
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
    var pendingImportPayload by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exportFailedMessage = stringResource(R.string.settings_export_backup_error)
    val importFailedMessage = stringResource(R.string.settings_import_backup_error)
    val importSuccessMessage = stringResource(R.string.settings_import_backup_success)

    val exportDocumentLauncher =
        rememberLauncherForActivityResult(CreateDocument(BACKUP_MIME_TYPE)) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            scope.launch {
                val jsonResult = viewModel.exportBackupJson(VERSION_NAME)
                val writeSucceeded =
                    jsonResult.getOrNull()?.let { payload -> writeTextToUri(context, uri, payload) } ?: false
                val message = if (writeSucceeded) null else exportFailedMessage

                message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
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

    when (route) {
        SettingsRoute.MAIN ->
            SettingsContent(
                state = state,
                appVersion = VERSION_NAME,
                onThemeClick = { route = SettingsRoute.THEME },
                onLanguageClick = { route = SettingsRoute.LANGUAGE },
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
        SettingsRoute.SLOT_MODE ->
            SettingsDetailScreen(
                title = stringResource(R.string.settings_slot_mode_title),
                onBack = { route = SettingsRoute.MAIN },
                onHelpClick = { isSlotModeHelpVisible = true },
                helpContentDescription = stringResource(R.string.settings_slot_mode_help_title),
                modifier = modifier,
            ) {
                SettingsOptionRow(
                    label = stringResource(R.string.settings_slot_mode_auto),
                    selected = state.slotModePolicy == AUTO_WHEN_MULTIPLE,
                    onClick = { viewModel.setSlotModePolicy(AUTO_WHEN_MULTIPLE) },
                )
                SettingsOptionRow(
                    label = stringResource(R.string.settings_slot_mode_always),
                    selected = state.slotModePolicy == ALWAYS_SHOW,
                    onClick = { viewModel.setSlotModePolicy(ALWAYS_SHOW) },
                )
            }
        SettingsRoute.BACKUP ->
            SettingsDetailScreen(
                title = stringResource(R.string.settings_backup_title),
                onBack = { route = SettingsRoute.MAIN },
                onHelpClick = { isBackupHelpVisible = true },
                helpContentDescription = stringResource(R.string.settings_backup_help_title),
                modifier = modifier,
            ) {
                SettingsBackupActionRow(
                    label = stringResource(R.string.settings_export_backup_title),
                    detail = backupExportLabel(state.lastBackupExportedAt),
                    onClick = { exportDocumentLauncher.launch(backupFileName()) },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                SettingsBackupActionRow(
                    label = stringResource(R.string.settings_import_backup_title),
                    detail = backupImportLabel(state.lastBackupImportedAt),
                    onClick = { importDocumentLauncher.launch(arrayOf(BACKUP_MIME_TYPE)) },
                )
            }
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

private fun writeTextToUri(
    context: Context,
    uri: Uri,
    content: String,
): Boolean {
    return runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(content)
            true
        } ?: false
    }.getOrDefault(false)
}

private fun readTextFromUri(
    context: Context,
    uri: Uri,
): String? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }.getOrNull()
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
    onHelpClick: (() -> Unit)? = null,
    helpContentDescription: String? = null,
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
            SettingsCard(content = content)
        }
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
private fun SettingsBackupActionRow(
    label: String,
    detail: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SpacingXxs),
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
            )
            Text(
                text = detail,
                style = typography.bodySmall,
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
        )
    }
}

@Composable
private fun backupExportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp) ?: never
    return stringResource(R.string.settings_backup_last_exported, formatted)
}

@Composable
private fun backupImportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp) ?: never
    return stringResource(R.string.settings_backup_last_imported, formatted)
}

@Composable
private fun formatBackupTimestamp(rawTimestamp: String?): String? {
    if (rawTimestamp.isNullOrBlank()) return null
    val locale = Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(zoneId)

    return runCatching {
        formatter.format(Instant.parse(rawTimestamp))
    }.getOrDefault(rawTimestamp)
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
