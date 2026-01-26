package com.rafaelfelipeac.hermes.features.app

import android.app.LocaleManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM

fun applyAppLanguage(
    context: Context,
    language: AppLanguage,
): Boolean {
    return if (SDK_INT >= TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val desired = if (language == SYSTEM) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(language.tag)
        }

        if (localeManager.applicationLocales != desired) {
            localeManager.applicationLocales = desired
            true
        } else {
            false
        }
    } else {
        val desired = if (language == SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }

        if (AppCompatDelegate.getApplicationLocales() != desired) {
            AppCompatDelegate.setApplicationLocales(desired)
            true
        } else {
            false
        }
    }
}
