package com.rafaelfelipeac.hermes.core.useraction.metadata

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

object UserActionMetadataSerializer {
    private val json = Json { ignoreUnknownKeys = true }

    fun toJson(metadata: Map<String, String>): String {
        return buildJsonObject {
            metadata.forEach { (key, value) ->
                put(key, JsonPrimitive(value))
            }
        }.toString()
    }

    fun fromJson(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()

        val element = runCatching { json.parseToJsonElement(raw) }.getOrNull() ?: return emptyMap()
        val obj = element as? JsonObject ?: return emptyMap()

        return obj.entries.associate { (key, value) ->
            key to value.jsonPrimitive.content
        }
    }
}
