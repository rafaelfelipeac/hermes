package com.rafaelfelipeac.hermes.features.backup.domain.repository

enum class ImportBackupError {
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    MISSING_REQUIRED_SECTION,
    INVALID_FIELD_VALUE,
    INVALID_REFERENCE,
    WRITE_FAILED,
}
