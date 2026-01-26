package com.rafaelfelipeac.hermes.features.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun fromTag_returnsMatchingLanguage() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.PORTUGUESE_BRAZIL, AppLanguage.fromTag("pt-BR"))
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromTag("de"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromTag("fr"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromTag("es"))
        assertEquals(AppLanguage.ITALIAN, AppLanguage.fromTag("it"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromTag("ar"))
        assertEquals(AppLanguage.HINDI, AppLanguage.fromTag("hi"))
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromTag("ja"))
    }

    @Test
    fun fromTag_returnsEnglishByDefault() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("unknown"))
    }
}
