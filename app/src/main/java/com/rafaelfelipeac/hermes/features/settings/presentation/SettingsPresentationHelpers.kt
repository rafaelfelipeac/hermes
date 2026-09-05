package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.annotation.StringRes
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE_TOKEN
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val DEBUG_PACKAGE_SUFFIX = ".dev"
private const val EMAIL_NEW_LINE = "\r\n"

internal fun feedbackBodyText(localizedBody: String): String = localizedBody.replace(NEW_LINE_TOKEN, NEW_LINE)

internal fun normalizeFeedbackBody(body: String): String = body.replace(NEW_LINE, EMAIL_NEW_LINE)

internal fun storePackageName(packageName: String, isDebug: Boolean): String =
    if (isDebug && packageName.endsWith(DEBUG_PACKAGE_SUFFIX)) {
        packageName.removeSuffix(DEBUG_PACKAGE_SUFFIX)
    } else {
        packageName
    }

@StringRes
internal fun backupFolderLabelRes(rawUri: String?): Int =
    if (rawUri.isNullOrBlank()) {
        R.string.settings_backup_folder_default
    } else {
        R.string.settings_backup_folder_selected
    }

internal fun formatBackupTimestamp(
    rawTimestamp: String?,
    locale: Locale,
    zoneId: ZoneId,
): String? {
    if (rawTimestamp.isNullOrBlank()) return null

    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(zoneId)

    return runCatching {
        formatter.format(Instant.parse(rawTimestamp))
    }.getOrDefault(rawTimestamp)
}
