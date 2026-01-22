package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class AppLanguage(val tag: String) {
    SYSTEM(AppLanguageConstants.SYSTEM_LANGUAGE_TAG),
    ENGLISH(AppLanguageConstants.ENGLISH_LANGUAGE_TAG),
    PORTUGUESE_BRAZIL(AppLanguageConstants.PORTUGUESE_BRAZIL_LANGUAGE_TAG),
    GERMAN(AppLanguageConstants.GERMAN_LANGUAGE_TAG),
    FRENCH(AppLanguageConstants.FRENCH_LANGUAGE_TAG),
    SPANISH(AppLanguageConstants.SPANISH_LANGUAGE_TAG),
    ITALIAN(AppLanguageConstants.ITALIAN_LANGUAGE_TAG),
    ARABIC(AppLanguageConstants.ARABIC_LANGUAGE_TAG),
    HINDI(AppLanguageConstants.HINDI_LANGUAGE_TAG),
    JAPANESE(AppLanguageConstants.JAPANESE_LANGUAGE_TAG),
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
    const val GERMAN_LANGUAGE_TAG = "de"
    const val FRENCH_LANGUAGE_TAG = "fr"
    const val SPANISH_LANGUAGE_TAG = "es"
    const val ITALIAN_LANGUAGE_TAG = "it"
    const val ARABIC_LANGUAGE_TAG = "ar"
    const val HINDI_LANGUAGE_TAG = "hi"
    const val JAPANESE_LANGUAGE_TAG = "ja"
}
