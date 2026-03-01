package com.rafaelfelipeac.hermes.features.backup.domain.model

enum class BackupDecodeError {
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    MISSING_REQUIRED_SECTION,
    INVALID_FIELD_VALUE,
}
