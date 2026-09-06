package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

internal class ActivityUiFormatterTrophy(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    fun buildTrophyTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label =
            metadata[UserActionMetadataKeys.TROPHY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: stringProvider.get(R.string.activity_value_unknown)

        return when (actionType) {
            UserActionType.SHARE_TROPHY ->
                stringProvider.get(
                    R.string.activity_action_share_trophy,
                    shared.quoteValue(label) ?: label,
                )

            else -> null
        }
    }
}
