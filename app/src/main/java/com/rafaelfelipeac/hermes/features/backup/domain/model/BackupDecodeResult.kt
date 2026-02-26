package com.rafaelfelipeac.hermes.features.backup.domain.model

sealed interface BackupDecodeResult {
    data class Success(
        val snapshot: BackupSnapshot,
    ) : BackupDecodeResult

    data class Failure(
        val error: BackupDecodeError,
    ) : BackupDecodeResult
}
