package com.rafaelfelipeac.hermes.features.app

import android.app.LocaleManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.rafaelfelipeac.hermes.core.strings.LocaleProvider
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import java.util.Locale

fun applyAppLanguage(
    context: Context,
    language: AppLanguage,
): Boolean {
    val languageTags = language.toApplicationLocaleTags()

    return if (SDK_INT >= TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val desired = LocaleList.forLanguageTags(languageTags)

        if (localeManager.applicationLocales != desired) {
            localeManager.applicationLocales = desired
            true
        } else {
            false
        }
    } else {
        val desired = LocaleListCompat.forLanguageTags(languageTags)

        if (AppCompatDelegate.getApplicationLocales() != desired) {
            AppCompatDelegate.setApplicationLocales(desired)
            true
        } else {
            false
        }
    }
}

internal fun AppLanguage.toApplicationLocaleTags(): String {
    return if (this == SYSTEM) EMPTY_LOCALE_TAGS else tag
}

internal fun AppLanguage.toLocale(localeProvider: LocaleProvider): Locale {
    return if (this == SYSTEM) localeProvider.current() else Locale.forLanguageTag(tag)
}

private const val EMPTY_LOCALE_TAGS = ""
