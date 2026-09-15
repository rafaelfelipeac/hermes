package com.rafaelfelipeac.hermes.features.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.rafaelfelipeac.hermes.BuildConfig
import java.util.Locale

private const val SETTINGS_SCREEN_TAG = "SettingsScreen"
private const val LOG_FEEDBACK_INTENT_NOT_FOUND = "Feedback intent not found."
private const val LOG_FEEDBACK_INTENT_BLOCKED = "Feedback intent blocked by security policy."
private const val LOG_MARKET_INTENT_NOT_FOUND = "Market intent not found."
private const val LOG_MARKET_INTENT_BLOCKED = "Market intent blocked by security policy."
private const val LOG_WEB_INTENT_NOT_FOUND = "Web intent not found."
private const val LOG_WEB_INTENT_BLOCKED = "Web intent blocked by security policy."

internal fun Context.launchSettingsFeedback(
    subject: String,
    body: String,
    feedbackEmail: String,
    mailtoTemplate: String,
    feedbackUnavailableMessage: String,
) {
    val normalizedBody = normalizeFeedbackBody(body)
    val mailToUri =
        String.format(
            Locale.ROOT,
            mailtoTemplate,
            feedbackEmail,
            Uri.encode(subject),
            Uri.encode(normalizedBody),
        ).toUri()
    val intent =
        Intent(Intent.ACTION_SENDTO, mailToUri).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, normalizedBody)
        }

    try {
        startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_NOT_FOUND, error)

        Toast.makeText(
            this,
            feedbackUnavailableMessage,
            Toast.LENGTH_SHORT,
        ).show()
    } catch (error: SecurityException) {
        Log.e(SETTINGS_SCREEN_TAG, LOG_FEEDBACK_INTENT_BLOCKED, error)

        Toast.makeText(
            this,
            feedbackUnavailableMessage,
            Toast.LENGTH_SHORT,
        ).show()
    }
}

internal fun Context.launchSettingsRating(
    marketUrlTemplate: String,
    webUrlTemplate: String,
    rateUnavailableMessage: String,
) {
    val targetPackageName =
        storePackageName(packageName, BuildConfig.DEBUG)
    val marketIntent =
        Intent(
            Intent.ACTION_VIEW,
            String.format(
                Locale.ROOT,
                marketUrlTemplate,
                targetPackageName,
            ).toUri(),
        )
    val webIntent =
        Intent(
            Intent.ACTION_VIEW,
            String.format(
                Locale.ROOT,
                webUrlTemplate,
                targetPackageName,
            ).toUri(),
        )
    val launchFailed =
        try {
            startActivity(marketIntent)
            false
        } catch (error: ActivityNotFoundException) {
            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_NOT_FOUND, error)
            true
        } catch (error: SecurityException) {
            Log.e(SETTINGS_SCREEN_TAG, LOG_MARKET_INTENT_BLOCKED, error)
            true
        }

    if (launchFailed) {
        val webLaunchFailed =
            try {
                startActivity(webIntent)
                false
            } catch (error: ActivityNotFoundException) {
                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_NOT_FOUND, error)
                true
            } catch (error: SecurityException) {
                Log.e(SETTINGS_SCREEN_TAG, LOG_WEB_INTENT_BLOCKED, error)
                true
            }

        if (webLaunchFailed) {
            Toast.makeText(
                this,
                rateUnavailableMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}
