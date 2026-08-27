package com.rafaelfelipeac.hermes.features.backup.domain.repository

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_JSON
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.MISSING_REQUIRED_SECTION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION

internal fun BackupDecodeError.toImportBackupError(): ImportBackupError {
    return when (this) {
        INVALID_JSON -> ImportBackupError.INVALID_JSON
        UNSUPPORTED_SCHEMA_VERSION -> ImportBackupError.UNSUPPORTED_SCHEMA_VERSION
        MISSING_REQUIRED_SECTION -> ImportBackupError.MISSING_REQUIRED_SECTION
        INVALID_FIELD_VALUE -> ImportBackupError.INVALID_FIELD_VALUE
    }
}
