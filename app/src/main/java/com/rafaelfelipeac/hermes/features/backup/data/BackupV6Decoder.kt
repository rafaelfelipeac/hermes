package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import kotlinx.serialization.json.JsonObject

internal object BackupV6Decoder {
    fun decode(root: JsonObject): BackupDecodeResult {
        return BackupV5Decoder.decode(
            root = root,
            schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V6,
        )
    }
}
