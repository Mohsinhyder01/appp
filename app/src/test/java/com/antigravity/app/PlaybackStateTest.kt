package com.antigravity.app

import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.RepeatMode
import com.antigravity.app.player.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun `progressFraction clamps between 0 and 1`() {
        val state = PlaybackState(positionMs = 15_000L, durationMs = 30_000L)
        assertEquals(0.5f, state.progressFraction, 0.001f)

        val zeroDuration = PlaybackState(positionMs = 5_000L, durationMs = 0L)
        assertEquals(0f, zeroDuration.progressFraction, 0.001f)

        val overDuration = PlaybackState(positionMs = 45_000L, durationMs = 30_000L)
        assertEquals(1f, overDuration.progressFraction, 0.001f)
    }

    @Test
    fun `repeatMode cycles OFF -> ALL -> ONE -> OFF`() {
        assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
        assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
        assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())
    }

    @Test
    fun `ayat track displays surah and ayat number reference`() {
        val ayat = AudioTrack(
            id = "ayat_2_255",
            mode = AudioMode.AYAT,
            title = "Ayat al-Kursi",
            subtitle = "Mishary Rashid Alafasy",
            surahName = "Surah Al-Baqarah",
            ayatNumber = 255
        )
        assertEquals("Surah Al-Baqarah • Ayat 255", ayat.displayReference)
        assertFalse(ayat.isAvailableOffline)

        val downloaded = ayat.copy(localPath = "/data/user/0/com.antigravity.app/files/ayat.mp3", isDownloadedOffline = true)
        assertTrue(downloaded.isAvailableOffline)
    }
}
