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
        return raw
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() }
            ?.let { it as? JsonObject }
            ?.entries
            ?.associate { (key, value) ->
                key to value.jsonPrimitive.content
            }
            ?: emptyMap()
    }
}
