package com.antigravity.app.data.offline

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates warm, soothing ambient WAV files on first launch inside app-private offline storage
 * so starter Focus Songs play 100% offline immediately out-of-the-box without external assets.
 */
class OfflineAudioSeeder(private val context: Context) {

    private data class PadProfile(
        val trackId: String,
        val chordFreqsHz: DoubleArray,
        val durationSeconds: Int,
        val pulseHz: Double
    )

    private val starterSongProfiles = listOf(
        PadProfile("song_orbital_drift", doubleArrayOf(196.0, 246.94, 293.66, 369.99), 24, 0.25),
        PadProfile("song_emerald_rain", doubleArrayOf(174.61, 220.0, 261.63, 329.63), 24, 0.35),
        PadProfile("song_midnight_horizon", doubleArrayOf(164.81, 196.0, 246.94, 293.66), 24, 0.20),
        PadProfile("song_dawn_stillness", doubleArrayOf(220.0, 277.18, 329.63, 415.30), 24, 0.30)
    )

    fun offlineDir(): File {
        return File(context.filesDir, "offline_audio").apply { mkdirs() }
    }

    suspend fun ensureStarterSongsSeeded(): Map<String, String> = withContext(Dispatchers.IO) {
        val dir = offlineDir()
        val generatedPaths = mutableMapOf<String, String>()
        for (profile in starterSongProfiles) {
            val file = File(dir, "${profile.trackId}.wav")
            if (!file.exists() || file.length() < 1024L) {
                writeWarmAmbientWav(
                    targetFile = file,
                    chordFreqsHz = profile.chordFreqsHz,
                    durationSeconds = profile.durationSeconds,
                    pulseHz = profile.pulseHz
                )
            }
            generatedPaths[profile.trackId] = file.absolutePath
        }
        generatedPaths
    }

    suspend fun ensureAyatOfflineCompanionSeeded(): String = withContext(Dispatchers.IO) {
        val dir = offlineDir()
        val file = File(dir, "ayat_offline_companion.wav")
        if (!file.exists() || file.length() < 1024L) {
            // Peaceful, meditative D-major/fifth harmonic bell-pad for offline Ayat reflection
            writeWarmAmbientWav(
                targetFile = file,
                chordFreqsHz = doubleArrayOf(146.83, 220.0, 293.66, 440.0),
                durationSeconds = 18,
                pulseHz = 0.18
            )
        }
        file.absolutePath
    }

    private fun writeWarmAmbientWav(
        targetFile: File,
        chordFreqsHz: DoubleArray,
        durationSeconds: Int,
        pulseHz: Double
    ) {
        val sampleRate = 22_050
        val totalSamples = sampleRate * durationSeconds
        val dataSize = totalSamples * 2 // 16-bit mono
        val pcmBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)

        val fadeSamples = sampleRate * 2 // smooth 2-second fade-in / fade-out
        val twoPi = 2.0 * PI

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = when {
                i < fadeSamples -> i.toDouble() / fadeSamples
                i > totalSamples - fadeSamples -> (totalSamples - i).toDouble() / fadeSamples
                else -> 1.0
            }
            val breathingMod = 0.72 + 0.28 * sin(twoPi * pulseHz * t)

            var mix = 0.0
            for (idx in chordFreqsHz.indices) {
                val freq = chordFreqsHz[idx]
                // Gentle harmonic warmth (fundamental + soft octave overtone)
                val fundamental = sin(twoPi * freq * t)
                val overtone = 0.22 * sin(twoPi * (freq * 2.0) * t)
                mix += (fundamental + overtone) / chordFreqsHz.size
            }

            val sampleFloat = (mix * envelope * breathingMod * 0.42).coerceIn(-0.95, 0.95)
            val sampleShort = (sampleFloat * Short.MAX_VALUE).toInt().toShort()
            pcmBuffer.putShort(sampleShort)
        }

        FileOutputStream(targetFile).use { out ->
            out.write(buildWavHeader(dataSize, sampleRate, channels = 1, bitsPerSample = 16))
            out.write(pcmBuffer.array())
        }
    }

    private fun buildWavHeader(
        pcmDataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()
        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + pcmDataSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // PCM chunk size
            putShort(1) // PCM format
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign)
            putShort(bitsPerSample.toShort())
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(pcmDataSize)
        }.array()
    }
}
