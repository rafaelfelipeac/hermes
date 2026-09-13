package com.rafaelfelipeac.hermes.features.app

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.DARK
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.LIGHT
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel

@Composable
fun HermesAppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(settingsState.language, settingsState.isLoaded) {
        if (settingsState.isLoaded) {
            val applied = applyAppLanguage(context, settingsState.language)

            if (applied && activity != null) {
                activity.recreate()
            }
        }
    }

    DisposableEffect(context, lifecycleOwner, settingsViewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    settingsViewModel.syncLanguageFromPlatform(currentPlatformAppLanguage(context))
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
