package com.rafaelfelipeac.hermes.features.settings.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseNotesTest {
    @Test
    fun normalizedReleaseNotesVersionRemovesDevSuffix() {
        assertEquals(
            "1.10.1",
            normalizedReleaseNotesVersion("1.10.1-dev"),
        )
    }

    @Test
    fun releaseNotesForVersionReturnsCurrentVersionNotes() {
        assertNotNull(releaseNotesForVersion("1.10.1"))
    }

    @Test
    fun releaseNotesForVersionReturnsOnlySectionsPresentInCurrentChangelog() {
        assertEquals(
            1,
            releaseNotesForVersion("1.10.1")?.sections?.size,
        )
    }

    @Test
    fun releaseNotesForVersionReturnsCurrentVersionNotesForDevBuilds() {
        assertNotNull(releaseNotesForVersion("1.10.1-dev"))
    }

    @Test
    fun releaseNotesForVersionReturnsNullWhenVersionIsMissing() {
        assertNull(releaseNotesForVersion("0.0.0-test"))
    }
}
