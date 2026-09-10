package com.rafaelfelipeac.hermes.features.app

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLocaleTest {
    @Test
    fun toApplicationLocaleTags_returnsEmptyTagsForSystemLanguage() {
        assertEquals("", SYSTEM.toApplicationLocaleTags())
    }

    @Test
    fun toApplicationLocaleTags_returnsLanguageTagForExplicitLanguage() {
        assertEquals("en", ENGLISH.toApplicationLocaleTags())
        assertEquals("pt-BR", PORTUGUESE_BRAZIL.toApplicationLocaleTags())
    }
}
