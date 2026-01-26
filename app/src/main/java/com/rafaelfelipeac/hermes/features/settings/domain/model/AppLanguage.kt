package com.rafaelfelipeac.hermes.features.settings.domain.model

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.ARABIC_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.ENGLISH_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.FRENCH_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.GERMAN_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.HINDI_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.ITALIAN_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.JAPANESE_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.PORTUGUESE_BRAZIL_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.SPANISH_LANGUAGE_TAG
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguageConstants.SYSTEM_LANGUAGE_TAG

enum class AppLanguage(val tag: String) {
    SYSTEM(SYSTEM_LANGUAGE_TAG),
    ENGLISH(ENGLISH_LANGUAGE_TAG),
    PORTUGUESE_BRAZIL(PORTUGUESE_BRAZIL_LANGUAGE_TAG),
    GERMAN(GERMAN_LANGUAGE_TAG),
    FRENCH(FRENCH_LANGUAGE_TAG),
    SPANISH(SPANISH_LANGUAGE_TAG),
    ITALIAN(ITALIAN_LANGUAGE_TAG),
    ARABIC(ARABIC_LANGUAGE_TAG),
    HINDI(HINDI_LANGUAGE_TAG),
    JAPANESE(JAPANESE_LANGUAGE_TAG),
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
