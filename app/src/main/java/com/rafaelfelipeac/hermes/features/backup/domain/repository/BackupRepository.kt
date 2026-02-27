package com.rafaelfelipeac.hermes.features.backup.domain.repository

interface BackupRepository {
    suspend fun exportBackupJson(appVersion: String): Result<String>

    suspend fun importBackupJson(rawJson: String): ImportBackupResult

    suspend fun getDataStats(): BackupDataStats

    suspend fun hasAnyData(): Boolean
}
