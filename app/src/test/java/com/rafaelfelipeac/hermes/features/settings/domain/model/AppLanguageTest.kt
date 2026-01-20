package com.rafaelfelipeac.hermes.features.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun fromTag_returnsMatchingLanguage() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.PORTUGUESE_BRAZIL, AppLanguage.fromTag("pt-BR"))
    }

    @Test
    fun fromTag_returnsEnglishByDefault() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("unknown"))
    }
}
