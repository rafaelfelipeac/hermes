package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

internal object BackupV7Decoder {
    fun decode(root: JsonObject): BackupDecodeResult {
        val useDynamicColor = decodeUseDynamicColor(root)
        if (useDynamicColor == DynamicColorDecode.Invalid) return Failure(INVALID_FIELD_VALUE)

        return when (val result = BackupV6Decoder.decode(root)) {
            is Failure -> result
            is Success ->
                result.copy(
                    snapshot =
                        result.snapshot.copy(
                            schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V7,
                            settings =
                                result.snapshot.settings?.copy(
                                    useDynamicColor =
                                        (useDynamicColor as? DynamicColorDecode.Present)?.value
                                            ?: false,
                                ),
                        ),
                )
        }
    }

    private fun decodeUseDynamicColor(root: JsonObject): DynamicColorDecode {
        val settingsElement = root[KEY_SETTINGS]
        val settings = settingsElement as? JsonObject
        val useDynamicColor = (settings?.get(KEY_USE_DYNAMIC_COLOR) as? JsonPrimitive)?.booleanOrNull

        return when {
            settingsElement == null -> DynamicColorDecode.Absent
            settings == null -> DynamicColorDecode.Invalid
            useDynamicColor == null -> DynamicColorDecode.Invalid
            else -> DynamicColorDecode.Present(useDynamicColor)
        }
    }

    private sealed interface DynamicColorDecode {
        data object Absent : DynamicColorDecode

        data object Invalid : DynamicColorDecode

        data class Present(val value: Boolean) : DynamicColorDecode
    }
}
