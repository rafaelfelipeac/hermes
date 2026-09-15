package com.rafaelfelipeac.hermes.features.app

import com.rafaelfelipeac.hermes.core.strings.LocaleProvider
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

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

    @Test
    fun toLocale_returnsEffectiveAppLocaleForSystemLanguage() {
        val provider = FixedLocaleProvider(Locale.JAPANESE)

        assertEquals(Locale.JAPANESE, SYSTEM.toLocale(provider))
    }

    @Test
    fun toLocale_returnsExplicitLocaleForSelectedLanguage() {
        val provider = FixedLocaleProvider(Locale.JAPANESE)

        assertEquals(Locale.ENGLISH, ENGLISH.toLocale(provider))
        assertEquals(Locale.forLanguageTag("pt-BR"), PORTUGUESE_BRAZIL.toLocale(provider))
    }

    private class FixedLocaleProvider(
        private val locale: Locale,
    ) : LocaleProvider {
        override fun current(): Locale = locale
    }
}
