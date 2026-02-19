package com.rafaelfelipeac.hermes.core.strings

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface StringProvider {
    fun get(
        id: Int,
        vararg args: Any,
    ): String

    fun getForLanguage(
        languageTag: String?,
        id: Int,
        vararg args: Any,
    ): String
}

@Singleton
class AndroidStringProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String {
            return context.getString(id, *args)
        }

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String {
            if (languageTag.isNullOrBlank()) {
                return context.getString(id, *args)
            }

            val locale = Locale.forLanguageTag(languageTag)
            val configuration = context.resources.configuration
            val localized = Configuration(configuration).apply {
                setLocale(locale)
            }
            val localizedContext = context.createConfigurationContext(localized)
            return localizedContext.getString(id, *args)
        }
    }
