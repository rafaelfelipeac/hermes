package com.rafaelfelipeac.hermes.features.backup.domain.repository

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError

interface BackupRepository {
    suspend fun exportBackupJson(appVersion: String): Result<String>

    suspend fun importBackupJson(rawJson: String): ImportBackupResult

    suspend fun hasAnyData(): Boolean
}

sealed interface ImportBackupResult {
    data object Success : ImportBackupResult

    data class Failure(
        val error: ImportBackupError,
    ) : ImportBackupResult
}

enum class ImportBackupError {
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    MISSING_REQUIRED_SECTION,
    INVALID_FIELD_VALUE,
    INVALID_REFERENCE,
    WRITE_FAILED,
}

internal fun BackupDecodeError.toImportBackupError(): ImportBackupError {
    return when (this) {
        BackupDecodeError.INVALID_JSON -> ImportBackupError.INVALID_JSON
        BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION -> ImportBackupError.UNSUPPORTED_SCHEMA_VERSION
        BackupDecodeError.MISSING_REQUIRED_SECTION -> ImportBackupError.MISSING_REQUIRED_SECTION
        BackupDecodeError.INVALID_FIELD_VALUE -> ImportBackupError.INVALID_FIELD_VALUE
    }
}
