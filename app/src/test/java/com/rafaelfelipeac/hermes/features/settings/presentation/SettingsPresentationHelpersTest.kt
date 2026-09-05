package com.rafaelfelipeac.hermes.features.settings.presentation

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class SettingsPresentationHelpersTest {
    @Test
    fun feedbackPreservesTokensAndExistingLineEndingBehavior() {
        assertEquals("Hello\n\nVersion 1.12", feedbackBodyText("Hello__NL____NL__Version 1.12"))
        assertEquals("Hello\r\nWorld", normalizeFeedbackBody("Hello\nWorld"))
        assertEquals("Hello\r\r\nWorld", normalizeFeedbackBody("Hello\r\nWorld"))
        assertEquals(EMPTY, feedbackBodyText(EMPTY))
        assertEquals("unchanged", normalizeFeedbackBody("unchanged"))
    }

    @Test
    fun storePackageOnlyStripsTrailingDevSuffixInDebug() {
        assertEquals("com.example", storePackageName("com.example.dev", true))
        assertEquals("com.example.dev", storePackageName("com.example.dev", false))
        assertEquals("com.dev.example", storePackageName("com.dev.example", true))
        assertEquals("com.example", storePackageName("com.example", true))
        assertEquals("com.example.dev", storePackageName("com.example.dev.dev", true))
    }

    @Test
    fun backupTimestampPreservesMissingAndMalformedFallbacks() {
        listOf(null, EMPTY, "   ").forEach { raw ->
            assertNull(formatBackupTimestamp(raw, Locale.US, ZoneId.of("UTC")))
        }
        listOf("invalid", "2026-09-05", " 2026-09-05T12:34:00Z ").forEach { raw ->
            assertEquals(raw, formatBackupTimestamp(raw, Locale.US, ZoneId.of("UTC")))
        }
    }

    @Test
    fun backupTimestampUsesExplicitLocaleAndZone() {
        val timestamp = "2026-09-05T12:34:00Z"
        assertEquals(
            "5 sept. 2026 12:34",
            formatBackupTimestamp(timestamp, Locale.FRANCE, ZoneId.of("UTC")),
        )
        assertEquals(
            "5 sept. 2026 09:34",
            formatBackupTimestamp(timestamp, Locale.FRANCE, ZoneId.of("America/Sao_Paulo")),
        )
        assertEquals(
            "05.09.2026, 12:34",
            formatBackupTimestamp(timestamp, Locale.GERMANY, ZoneId.of("UTC")),
        )
    }

    @Test
    fun backupFolderSummaryPreservesBlankHandling() {
        listOf(null, EMPTY, "  ").forEach { raw ->
            assertEquals(R.string.settings_backup_folder_default, backupFolderLabelRes(raw))
        }
        assertEquals(R.string.settings_backup_folder_selected, backupFolderLabelRes("content://folder"))
    }
}
