package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsExternalActionsTest {
    @Test
    fun feedbackPreservesEncodedUriAndExtras() {
        val context = RecordingContext()
        val resources = context.resources
        context.launchSettingsFeedback(
            subject = "Feedback & details",
            body = "Hello\nWorld",
            feedbackEmail = "test@example.com",
            mailtoTemplate = resources.getString(R.string.settings_feedback_mailto_uri),
            feedbackUnavailableMessage = resources.getString(R.string.settings_feedback_unavailable),
        )
        val intent = context.launched.single()
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals(
            "mailto:test@example.com?subject=Feedback%20%26%20details&body=Hello%0D%0AWorld",
            intent.data.toString(),
        )
        assertArrayEquals(arrayOf("test@example.com"), intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Feedback & details", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Hello\r\nWorld", intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun ratingUsesMarketWithoutLaunchingWebWhenAvailable() {
        val context = RecordingContext()
        launchRating(context)
        assertEquals(listOf("market://details?id=com.example"), context.launched.map { it.data.toString() })
        assertEquals(Intent.ACTION_VIEW, context.launched.single().action)
    }

    @Test
    fun ratingFallsBackToWebWhenMarketIsMissingOrBlocked() {
        listOf(ActivityNotFoundException(), SecurityException()).forEach { failure ->
            val context = RecordingContext { intent -> if (intent.data?.scheme == "market") throw failure }
            launchRating(context)
            assertEquals(
                listOf("market://details?id=com.example", "https://play.google.com/store/apps/details?id=com.example"),
                context.launched.map { it.data.toString() },
            )
        }
    }

    @Test
    fun unavailableExternalAppsDoNotPropagateExpectedFailures() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            listOf(ActivityNotFoundException(), SecurityException()).forEach { failure ->
                val context = RecordingContext { throw failure }
                launchRating(context)
                assertEquals(2, context.launched.size)
                context.launchSettingsFeedback(
                    subject = "Feedback",
                    body = "Body",
                    feedbackEmail = "test@example.com",
                    mailtoTemplate = context.getString(R.string.settings_feedback_mailto_uri),
                    feedbackUnavailableMessage = context.getString(R.string.settings_feedback_unavailable),
                )
                assertEquals(Intent.ACTION_SENDTO, context.launched.last().action)
            }
        }
    }

    private fun launchRating(context: Context) {
        context.launchSettingsRating(
            marketUrlTemplate = context.getString(R.string.settings_play_store_market_url),
            webUrlTemplate = context.getString(R.string.settings_play_store_web_url),
            rateUnavailableMessage = context.getString(R.string.settings_rate_unavailable),
        )
    }
}

private class RecordingContext(
    private val onLaunch: (Intent) -> Unit = {},
) : ContextWrapper(ApplicationProvider.getApplicationContext<Context>()) {
    val launched = mutableListOf<Intent>()

    override fun getPackageName(): String = "com.example.dev"

    override fun startActivity(intent: Intent) {
        launched += intent
        onLaunch(intent)
    }
}
