package com.rafaelfelipeac.hermes.features.settings.domain.model

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.*
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun fromTag_returnsMatchingLanguage() {
        assertEquals(ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(PORTUGUESE_BRAZIL, AppLanguage.fromTag("pt-BR"))
        assertEquals(GERMAN, AppLanguage.fromTag("de"))
        assertEquals(FRENCH, AppLanguage.fromTag("fr"))
        assertEquals(SPANISH, AppLanguage.fromTag("es"))
        assertEquals(ITALIAN, AppLanguage.fromTag("it"))
        assertEquals(ARABIC, AppLanguage.fromTag("ar"))
        assertEquals(HINDI, AppLanguage.fromTag("hi"))
        assertEquals(JAPANESE, AppLanguage.fromTag("ja"))
    }

    @Test
    fun fromTag_returnsEnglishByDefault() {
        assertEquals(ENGLISH, AppLanguage.fromTag("unknown"))
    }
}
