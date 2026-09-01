package com.rafaelfelipeac.hermes.features.activity.presentation

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityActionFilterContextTest {
    private val formatter =
        ActivityUiFormatter(
            object : StringProvider {
                override fun get(
                    id: Int,
                    vararg args: Any,
                ): String = id.toString()

                override fun getForLanguage(
                    languageTag: String?,
                    id: Int,
                    vararg args: Any,
                ): String = get(id, *args)
            },
        )

    @Test
    fun completionFilterIncludesChallengeProgressThatCompletesChallenge() {
        val completedProgress =
            challengeProgressRecord(
                metadata =
                    mapOf(
                        WAS_COMPLETED to false.toString(),
                        IS_COMPLETED to true.toString(),
                    ),
            )
        val regularProgress =
            challengeProgressRecord(
                id = 2L,
                metadata =
                    mapOf(
                        WAS_COMPLETED to false.toString(),
                        IS_COMPLETED to false.toString(),
                    ),
            )

        val filtered =
            filterActions(
                actions = listOf(completedProgress, regularProgress),
                context =
                    ActivityActionFilterContext(
                        primaryFilter = ActivityPrimaryFilter.COMPLETIONS,
                        categoryId = null,
                        categories = emptyList(),
                        categoryAliasesById = emptyMap(),
                        weekStartDate = null,
                    ),
                formatter = formatter,
            )

        assertEquals(listOf(completedProgress), filtered)
    }

    private fun challengeProgressRecord(
        id: Long = 1L,
        metadata: Map<String, String>,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY.name,
            entityType = UserActionEntityType.CHALLENGE.name,
            entityId = id,
            metadata = UserActionMetadataSerializer.toJson(metadata),
            timestamp = 0L,
        )
    }
}
