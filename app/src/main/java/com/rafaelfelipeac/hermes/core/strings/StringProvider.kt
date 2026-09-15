package com.rafaelfelipeac.hermes.core.strings

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.appcompat.app.AppCompatDelegate
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

interface LocaleProvider {
    fun current(): Locale
}

@Singleton
class AndroidStringProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val localeProvider: LocaleProvider,
    ) : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String {
            return getForLocale(localeProvider.current(), id, *args)
        }

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String {
            val locale =
                languageTag
                    ?.takeIf(String::isNotBlank)
                    ?.let(Locale::forLanguageTag)
                    ?: localeProvider.current()

            return getForLocale(locale, id, *args)
        }

        private fun getForLocale(
            locale: Locale,
            id: Int,
            vararg args: Any,
        ): String {
            val configuration = context.resources.configuration
            val localized =
                Configuration(configuration).apply {
                    setLocale(locale)
                }
            val localizedContext = context.createConfigurationContext(localized)
            return localizedContext.getString(id, *args)
        }
    }

@Singleton
class AndroidLocaleProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : LocaleProvider {
        override fun current(): Locale {
            return applicationLocale() ?: Locale.getDefault()
        }

        private fun applicationLocale(): Locale? {
            return if (SDK_INT >= TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales.firstLocaleOrNull()
            } else {
                AppCompatDelegate.getApplicationLocales().firstLocaleOrNull()
            }
        }
    }

private fun android.os.LocaleList.firstLocaleOrNull(): Locale? {
    return if (isEmpty) null else get(0)
}

private fun androidx.core.os.LocaleListCompat.firstLocaleOrNull(): Locale? {
    return if (isEmpty) null else get(0)
}
