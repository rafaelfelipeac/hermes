package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.rafaelfelipeac.hermes.R

private const val DEV_VERSION_SUFFIX_SEPARATOR = "-"
private const val RELEASE_NOTES_VERSION_1_7_0 = "1.7.0"

internal data class ReleaseNotesDefinition(
    val normalizedVersion: String,
    val sections: List<ReleaseNotesSectionDefinition>,
)

internal data class ReleaseNotesSectionDefinition(
    @StringRes val titleRes: Int,
    @ArrayRes val itemsRes: Int,
)

internal fun normalizedReleaseNotesVersion(appVersion: String): String {
    return appVersion.substringBefore(DEV_VERSION_SUFFIX_SEPARATOR)
}

internal fun releaseNotesForVersion(appVersion: String): ReleaseNotesDefinition? {
    return when (val normalizedVersion = normalizedReleaseNotesVersion(appVersion)) {
        RELEASE_NOTES_VERSION_1_7_0 ->
            ReleaseNotesDefinition(
                normalizedVersion = normalizedVersion,
                sections =
                    listOf(
                        ReleaseNotesSectionDefinition(
                            titleRes = R.string.settings_release_notes_added,
                            itemsRes = R.array.settings_release_notes_current_added,
                        ),
                        ReleaseNotesSectionDefinition(
                            titleRes = R.string.settings_release_notes_changed,
                            itemsRes = R.array.settings_release_notes_current_changed,
                        ),
                        ReleaseNotesSectionDefinition(
                            titleRes = R.string.settings_release_notes_fixed,
                            itemsRes = R.array.settings_release_notes_current_fixed,
                        ),
                    ),
            )
        else -> null
    }
}
