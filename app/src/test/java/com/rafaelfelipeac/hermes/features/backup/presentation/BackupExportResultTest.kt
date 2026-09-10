package com.rafaelfelipeac.hermes.features.backup.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupExportResultTest {
    @Test
    fun backupExportResult_returnsOriginalSuccessWhenWriteSucceeds() {
        val result = backupExportResult(Result.success(BACKUP_JSON), writeSucceeded = true)

        assertEquals(BACKUP_JSON, result.getOrNull())
    }

    @Test
    fun backupExportResult_returnsFailureWhenJsonExportFails() {
        val failure = IllegalStateException("json failed")

        val result = backupExportResult(Result.failure(failure), writeSucceeded = true)

        assertEquals(failure, result.exceptionOrNull())
    }

    @Test
    fun backupExportResult_returnsFailureWhenWriteFailsAfterJsonExport() {
        val result = backupExportResult(Result.success(BACKUP_JSON), writeSucceeded = false)

        assertTrue(result.exceptionOrNull() is BackupExportWriteException)
    }
}

private const val BACKUP_JSON = "{}"
