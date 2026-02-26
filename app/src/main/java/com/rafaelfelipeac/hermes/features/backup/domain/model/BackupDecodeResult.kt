package com.rafaelfelipeac.hermes.features.backup.domain.model

sealed interface BackupDecodeResult {
    data class Success(
        val snapshot: BackupSnapshot,
    ) : BackupDecodeResult

    data class Failure(
        val error: BackupDecodeError,
    ) : BackupDecodeResult
}

enum class BackupDecodeError {
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    MISSING_REQUIRED_SECTION,
    INVALID_FIELD_VALUE,
}
