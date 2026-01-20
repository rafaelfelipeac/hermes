package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class AppLanguage(val tag: String) {
    SYSTEM(AppLanguageConstants.SYSTEM_LANGUAGE_TAG),
    ENGLISH(AppLanguageConstants.ENGLISH_LANGUAGE_TAG),
    PORTUGUESE_BRAZIL(AppLanguageConstants.PORTUGUESE_BRAZIL_LANGUAGE_TAG),
    ;

    companion object {
        fun fromTag(tag: String): AppLanguage {
            return entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}

private object AppLanguageConstants {
    const val SYSTEM_LANGUAGE_TAG = "system"
    const val ENGLISH_LANGUAGE_TAG = "en"
    const val PORTUGUESE_BRAZIL_LANGUAGE_TAG = "pt-BR"
}
