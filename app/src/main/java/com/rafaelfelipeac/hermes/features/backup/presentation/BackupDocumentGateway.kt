package com.rafaelfelipeac.hermes.features.backup.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.rafaelfelipeac.hermes.features.backup.BACKUP_IMPORT_LOG_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

internal interface BackupDocumentGateway {
    suspend fun writeText(
        uri: Uri,
        content: String,
    ): Boolean

    suspend fun readText(uri: Uri): String?

    suspend fun canWriteTree(treeUri: Uri): Boolean

    suspend fun writeTextToTree(
        treeUri: Uri,
        fileName: String,
        content: String,
    ): Boolean

    suspend fun saveTemporaryImportPayload(content: String): String?

    suspend fun readTemporaryImportPayload(token: String): String?

    suspend fun deleteTemporaryImportPayload(token: String)
}

internal class AndroidBackupDocumentGateway(
    private val context: Context,
) : BackupDocumentGateway {
    override suspend fun writeText(
        uri: Uri,
        content: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(content)
                    true
                } ?: false
            }.getOrDefault(false)
        }
    }

    override suspend fun readText(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e(BACKUP_IMPORT_LOG_TAG, LOG_BACKUP_DOCUMENT_STREAM_UNAVAILABLE)
                    null
                } else {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }.onFailure { throwable ->
                Log.e(BACKUP_IMPORT_LOG_TAG, LOG_BACKUP_DOCUMENT_READ_FAILED, throwable)
            }.getOrNull()
        }
    }

    override suspend fun canWriteTree(treeUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val root = DocumentFile.fromTreeUri(context, treeUri)
                root != null && root.exists() && root.canWrite()
            }.getOrDefault(false)
        }
    }

    override suspend fun writeTextToTree(
        treeUri: Uri,
        fileName: String,
        content: String,
    ): Boolean {
        val backupFile =
            withContext(Dispatchers.IO) {
                runCatching {
                    val root = DocumentFile.fromTreeUri(context, treeUri)

                    if (root == null || !root.canWrite()) {
                        null
                    } else {
                        root.createFile(BACKUP_MIME_TYPE, fileName)
                    }
                }.getOrNull()
            }

        return backupFile?.let { file -> writeText(file.uri, content) } ?: false
    }

    override suspend fun saveTemporaryImportPayload(content: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val token = UUID.randomUUID().toString()
                temporaryImportFile(token).writeText(content)
                token
            }.getOrNull()
        }
    }

    override suspend fun readTemporaryImportPayload(token: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val file = temporaryImportFile(token)

                if (file.exists()) {
                    file.readText()
                } else {
                    null
                }
            }.getOrNull()
        }
    }

    override suspend fun deleteTemporaryImportPayload(token: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                temporaryImportFile(token).delete()
            }
        }
    }

    private fun temporaryImportFile(token: String): File {
        return File(context.cacheDir, "$TEMPORARY_IMPORT_PREFIX$token$TEMPORARY_IMPORT_EXTENSION")
    }
}

internal const val BACKUP_MIME_TYPE = "application/json"

private const val TEMPORARY_IMPORT_PREFIX = "backup-import-"
private const val TEMPORARY_IMPORT_EXTENSION = ".json"
private const val LOG_BACKUP_DOCUMENT_READ_FAILED = "Could not read the selected backup document."
private const val LOG_BACKUP_DOCUMENT_STREAM_UNAVAILABLE =
    "The selected backup document did not provide a readable stream."
