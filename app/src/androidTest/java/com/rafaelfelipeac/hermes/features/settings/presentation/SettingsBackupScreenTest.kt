package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsBackupScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun backupActionsDispatchAndFolderClearVisibilityReacts() {
        val state =
            mutableStateOf(
                SettingsState(
                    themeMode = ThemeMode.SYSTEM,
                    language = AppLanguage.SYSTEM,
                    slotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE,
                    weekStartDay = WeekStartDay.MONDAY,
                    lastBackupExportedAt = null,
                    lastBackupImportedAt = null,
                    backupFolderUri = null,
                ),
            )
        val actions = mutableListOf<String>()
        composeRule.setContent {
            SettingsBackupScreen(
                state = state.value,
                onBack = {},
                onHelpClick = {},
                onExportClick = { actions += EXPORT },
                onImportClick = { actions += IMPORT },
                onSelectFolderClick = { actions += SELECT },
                onClearFolderClick = { actions += CLEAR },
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNodeWithText(context.getString(R.string.settings_backup_folder_clear)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.settings_export_backup_title)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_import_backup_title)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_backup_folder_title)).performClick()
        composeRule.runOnIdle { state.value = state.value.copy(backupFolderUri = TEST_FOLDER) }
        composeRule.onNodeWithText(context.getString(R.string.settings_backup_folder_clear)).performClick()
        composeRule.runOnIdle { assertEquals(listOf(EXPORT, IMPORT, SELECT, CLEAR), actions) }
    }
}

private const val EXPORT = "export"
private const val IMPORT = "import"
private const val SELECT = "select"
private const val CLEAR = "clear"
private const val TEST_FOLDER = "content://test/folder"
