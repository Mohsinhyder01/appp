package com.antigravity.app

import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.repository.SeedCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedCatalogTest {

    @Test
    fun `starter songs are marked offline ready and include focus tracks`() {
        assertTrue(SeedCatalog.starterSongs.isNotEmpty())
        assertTrue(SeedCatalog.starterSongs.all { it.mode == AudioMode.SONG && it.isDownloadedOffline })
        assertTrue(SeedCatalog.starterSongs.any { it.isFocusTrack })
    }

    @Test
    fun `starter ayats include surah metadata, arabic text, and translation`() {
        assertTrue(SeedCatalog.starterAyats.isNotEmpty())
        assertTrue(
            SeedCatalog.starterAyats.all {
                it.mode == AudioMode.AYAT &&
                    !it.surahName.isNullOrBlank() &&
                    it.ayatNumber != null &&
                    !it.arabicText.isNullOrBlank() &&
                    !it.translationText.isNullOrBlank()
            }
        )
    }

    @Test
    fun `dailyAyatForToday wraps safely across day numbers`() {
        val day0 = SeedCatalog.dailyAyatForToday(0)
        val wrapped = SeedCatalog.dailyAyatForToday(SeedCatalog.dailyAyats.size)
        assertEquals(day0.trackId, wrapped.trackId)
    }
}
