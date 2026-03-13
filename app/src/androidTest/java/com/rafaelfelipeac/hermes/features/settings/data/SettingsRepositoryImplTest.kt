package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            repository = SettingsRepositoryImpl(context)
            context.settingsDataStore.edit { it.clear() }
        }

    @Test
    fun themeMode_defaultsToSystem_whenPreferenceIsMissing() =
        runTest {
            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
            assertEquals(ThemeMode.SYSTEM, repository.initialThemeMode())
        }

    @Test
    fun themeMode_defaultsToSystem_whenStoredValueIsInvalid() =
        runTest {
            context.settingsDataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = "BROKEN_VALUE"
            }

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }
}
