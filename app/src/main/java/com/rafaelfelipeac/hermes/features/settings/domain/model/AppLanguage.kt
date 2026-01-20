package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class AppLanguage(val tag: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    PORTUGUESE_BRAZIL("pt-BR"),
    ;

    companion object {
        fun fromTag(tag: String): AppLanguage {
            return entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}
