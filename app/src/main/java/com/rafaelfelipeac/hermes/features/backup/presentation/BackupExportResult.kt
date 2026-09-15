package com.rafaelfelipeac.hermes.features.backup.presentation

internal fun backupExportResult(
    jsonResult: Result<String>,
    writeSucceeded: Boolean,
): Result<String> {
    return if (jsonResult.isFailure) {
        jsonResult
    } else if (writeSucceeded) {
        jsonResult
    } else {
        Result.failure(BackupExportWriteException())
    }
}

internal class BackupExportWriteException : IllegalStateException(EXPORT_WRITE_FAILED)

private const val EXPORT_WRITE_FAILED = "export_write_failed"
