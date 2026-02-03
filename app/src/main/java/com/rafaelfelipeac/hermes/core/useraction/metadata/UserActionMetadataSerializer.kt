package com.rafaelfelipeac.hermes.core.useraction.metadata

object UserActionMetadataSerializer {

    fun toJson(metadata: Map<String, String>): String {
        return metadata.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${escape(key)}\":\"${escape(value)}\""
        }
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
