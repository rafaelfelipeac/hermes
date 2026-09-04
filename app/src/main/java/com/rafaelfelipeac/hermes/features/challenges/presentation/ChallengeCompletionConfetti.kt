package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

private const val CHALLENGE_CONFETTI_CENTER_X = 0.5
private const val CHALLENGE_CONFETTI_CENTER_Y = 0.34
private const val CHALLENGE_CONFETTI_LEFT_ANGLE = 180
private const val CHALLENGE_CONFETTI_RIGHT_ANGLE = 0
private const val CHALLENGE_CONFETTI_SPREAD = 52
private const val CHALLENGE_CONFETTI_EMITTER_DURATION_MS = 250L
private const val CHALLENGE_CONFETTI_PARTICLE_COUNT = 42
private const val CHALLENGE_CONFETTI_VISIBLE_DURATION_MS = 2_000L

@Composable
internal fun ChallengeCompletionConfetti(
    burstKey: Int,
    category: Category?,
    modifier: Modifier = Modifier,
) {
    var parties by remember { mutableStateOf(emptyList<Party>()) }
    val hapticFeedback = LocalHapticFeedback.current
    val categoryAccent = category?.let { categoryAccentColor(it.colorId) }
    val palette =
        listOf(
            (categoryAccent ?: colorScheme.primary).toArgb(),
            colorScheme.primary.toArgb(),
            colorScheme.secondary.toArgb(),
            colorScheme.tertiary.toArgb(),
        ).distinct()
    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
        parties =
            listOf(
                Party(
                    angle = CHALLENGE_CONFETTI_LEFT_ANGLE,
                    spread = CHALLENGE_CONFETTI_SPREAD,
                    colors = palette,
                    position = Position.Relative(CHALLENGE_CONFETTI_CENTER_X, CHALLENGE_CONFETTI_CENTER_Y),
                    emitter =
                        Emitter(
                            duration = CHALLENGE_CONFETTI_EMITTER_DURATION_MS,
                            TimeUnit.MILLISECONDS,
                        ).max(CHALLENGE_CONFETTI_PARTICLE_COUNT),
                ),
                Party(
                    angle = CHALLENGE_CONFETTI_RIGHT_ANGLE,
                    spread = CHALLENGE_CONFETTI_SPREAD,
                    colors = palette,
                    position = Position.Relative(CHALLENGE_CONFETTI_CENTER_X, CHALLENGE_CONFETTI_CENTER_Y),
                    emitter =
                        Emitter(
                            duration = CHALLENGE_CONFETTI_EMITTER_DURATION_MS,
                            TimeUnit.MILLISECONDS,
                        ).max(CHALLENGE_CONFETTI_PARTICLE_COUNT),
                ),
            )
        kotlinx.coroutines.delay(CHALLENGE_CONFETTI_VISIBLE_DURATION_MS.milliseconds)
        parties = emptyList()
    }
    if (parties.isNotEmpty()) {
        KonfettiView(
            modifier = modifier.testTag(CHALLENGES_TAG_COMPLETION_CONFETTI),
            parties = parties,
        )
    }
}
