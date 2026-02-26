package com.rafaelfelipeac.hermes.features.backup.domain.repository

sealed interface ImportBackupResult {
    data object Success : ImportBackupResult

    data class Failure(
        val error: ImportBackupError,
    ) : ImportBackupResult
}
