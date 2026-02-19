package com.rafaelfelipeac.hermes.features.app

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel

@Composable
fun HermesAppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val lastAppliedLanguage = remember { mutableStateOf<AppLanguage?>(null) }

    LaunchedEffect(settingsState.language) {
        if (lastAppliedLanguage.value == null) {
            lastAppliedLanguage.value = settingsState.language
            return@LaunchedEffect
        }

        if (lastAppliedLanguage.value == settingsState.language) {
            return@LaunchedEffect
        }

        lastAppliedLanguage.value = settingsState.language

        val applied = applyAppLanguage(context, settingsState.language)

        if (applied && activity != null) {
            activity.recreate()
        }
    }

    val darkTheme =
        when (settingsState.themeMode) {
            DARK -> true
            LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    HermesTheme(darkTheme = darkTheme) {
        HermesAppContent()
    }
}

@PreviewScreenSizes
@Composable
private fun HermesAppPreview(
    @PreviewParameter(HermesAppPreviewProvider::class)
    preview: HermesAppPreviewData,
) {
    HermesTheme(darkTheme = preview.darkTheme) {
        HermesAppContent()
    }
}
