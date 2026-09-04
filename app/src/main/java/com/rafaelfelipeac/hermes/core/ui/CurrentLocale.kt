package com.rafaelfelipeac.hermes.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import java.util.Locale

@Composable
internal fun currentLocale(): Locale = LocalLocale.current.platformLocale
