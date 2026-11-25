package com.antigravity.app.data.repository

import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.DailyAyat
import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.model.TaskPriority
import java.util.Calendar

object SeedCatalog {

    val starterSongs: List<AudioTrack> = listOf(
        AudioTrack(
            id = "song_orbital_drift",
            mode = AudioMode.SONG,
            title = "Orbital Drift (Lo-Fi Pad)",
            subtitle = "Antigravity Studio • Deep Focus",
            durationMs = 24_000L,
            isFocusTrack = true,
            isDownloadedOffline = true,
            sortOrder = 1
        ),
        AudioTrack(
            id = "song_emerald_rain",
            mode = AudioMode.SONG,
            title = "Emerald Rain (Ambient)",
            subtitle = "Antigravity Studio • Study Calm",
            durationMs = 24_000L,
            isFocusTrack = true,
            isDownloadedOffline = true,
            sortOrder = 2
        ),
        AudioTrack(
            id = "song_midnight_horizon",
            mode = AudioMode.SONG,
            title = "Midnight Horizon",
            subtitle = "Antigravity Studio • Warm Synth",
            durationMs = 24_000L,
            isFocusTrack = true,
            isDownloadedOffline = true,
            sortOrder = 3
        ),
        AudioTrack(
            id = "song_dawn_stillness",
            mode = AudioMode.SONG,
            title = "Dawn Stillness",
            subtitle = "Antigravity Studio • Mindful Breath",
            durationMs = 24_000L,
            isFocusTrack = false,
            isDownloadedOffline = true,
            sortOrder = 4
        )
    )

    val starterAyats: List<AudioTrack> = listOf(
        AudioTrack(
            id = "ayat_1_1",
            mode = AudioMode.AYAT,
            title = "Al-Fatihah • Ayat 1",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 6_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3",
            surahNumber = 1,
            surahName = "Surah Al-Fatihah",
            surahNameArabic = "سورة الفاتحة",
            ayatNumber = 1,
            arabicText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
            translationText = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
            isFocusTrack = true,
            sortOrder = 101
        ),
        AudioTrack(
            id = "ayat_1_2",
            mode = AudioMode.AYAT,
            title = "Al-Fatihah • Ayat 2",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 6_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2.mp3",
            surahNumber = 1,
            surahName = "Surah Al-Fatihah",
            surahNameArabic = "سورة الفاتحة",
            ayatNumber = 2,
            arabicText = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
            translationText = "[All] praise is [due] to Allah, Lord of the worlds.",
            sortOrder = 102
        ),
        AudioTrack(
            id = "ayat_1_5",
            mode = AudioMode.AYAT,
            title = "Al-Fatihah • Ayat 5",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 6_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5.mp3",
            surahNumber = 1,
            surahName = "Surah Al-Fatihah",
            surahNameArabic = "سورة الفاتحة",
            ayatNumber = 5,
            arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            translationText = "It is You we worship and You we ask for help.",
            isFocusTrack = true,
            sortOrder = 103
        ),
        AudioTrack(
            id = "ayat_2_255",
            mode = AudioMode.AYAT,
            title = "Ayat al-Kursi • Ayat 255",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 45_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3",
            surahNumber = 2,
            surahName = "Surah Al-Baqarah",
            surahNameArabic = "سورة البقرة",
            ayatNumber = 255,
            arabicText = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَـٰوَٰتِ وَمَا فِى ٱلْأَرْضِ",
            translationText = "Allah — there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth.",
            isFocusTrack = true,
            sortOrder = 104
        ),
        AudioTrack(
            id = "ayat_112_1",
            mode = AudioMode.AYAT,
            title = "Al-Ikhlas • Ayat 1",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 5_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6222.mp3",
            surahNumber = 112,
            surahName = "Surah Al-Ikhlas",
            surahNameArabic = "سورة الإخلاص",
            ayatNumber = 1,
            arabicText = "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
            translationText = "Say, \"He is Allah, [who is] One.\"",
            sortOrder = 105
        ),
        AudioTrack(
            id = "ayat_112_2",
            mode = AudioMode.AYAT,
            title = "Al-Ikhlas • Ayat 2",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 4_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6223.mp3",
            surahNumber = 112,
            surahName = "Surah Al-Ikhlas",
            surahNameArabic = "سورة الإخلاص",
            ayatNumber = 2,
            arabicText = "ٱللَّهُ ٱلصَّمَدُ",
            translationText = "Allah, the Eternal Refuge.",
            sortOrder = 106
        ),
        AudioTrack(
            id = "ayat_113_1",
            mode = AudioMode.AYAT,
            title = "Al-Falaq • Ayat 1",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 5_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6226.mp3",
            surahNumber = 113,
            surahName = "Surah Al-Falaq",
            surahNameArabic = "سورة الفلق",
            ayatNumber = 1,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ",
            translationText = "Say, \"I seek refuge in the Lord of daybreak.\"",
            sortOrder = 107
        ),
        AudioTrack(
            id = "ayat_114_1",
            mode = AudioMode.AYAT,
            title = "An-Nas • Ayat 1",
            subtitle = "Mishary Rashid Alafasy",
            durationMs = 5_000L,
            streamUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6231.mp3",
            surahNumber = 114,
            surahName = "Surah An-Nas",
            surahNameArabic = "سورة الناس",
            ayatNumber = 1,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ",
            translationText = "Say, \"I seek refuge in the Lord of mankind.\"",
            sortOrder = 108
        )
    )

    val dailyAyats: List<DailyAyat> = listOf(
        DailyAyat(
            trackId = "ayat_2_255",
            surahName = "Surah Al-Baqarah",
            surahNameArabic = "سورة البقرة",
            surahNumber = 2,
            ayatNumber = 255,
            arabicText = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ",
            translationText = "Allah — there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep.",
            reflectionNote = "Anchor your day in stillness knowing everything is sustained."
        ),
        DailyAyat(
            trackId = "ayat_1_5",
            surahName = "Surah Al-Fatihah",
            surahNameArabic = "سورة الفاتحة",
            surahNumber = 1,
            ayatNumber = 5,
            arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            translationText = "It is You we worship and You we ask for help.",
            reflectionNote = "Begin focused work by renewing intention and seeking strength."
        ),
        DailyAyat(
            trackId = "ayat_1_1",
            surahName = "Surah Al-Fatihah",
            surahNameArabic = "سورة الفاتحة",
            surahNumber = 1,
            ayatNumber = 1,
            arabicText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
            translationText = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
            reflectionNote = "Start every meaningful task with mercy and clarity."
        ),
        DailyAyat(
            trackId = "ayat_112_1",
            surahName = "Surah Al-Ikhlas",
            surahNameArabic = "سورة الإخلاص",
            surahNumber = 112,
            ayatNumber = 1,
            arabicText = "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
            translationText = "Say, \"He is Allah, [who is] One.\"",
            reflectionNote = "Simplify your focus to what truly matters today."
        )
    )

    fun dailyAyatForToday(dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)): DailyAyat {
        val index = (dayOfYear.coerceAtLeast(0)) % dailyAyats.size
        return dailyAyats[index]
    }

    fun starterTasks(nowMillis: Long = System.currentTimeMillis()): List<TaskItem> {
        val oneHour = 3_600_000L
        return listOf(
            TaskItem(
                id = "seed_task_1",
                title = "Morning Quran reflection & mindful breathing",
                notes = "Listen to Surah Al-Fatihah or Ayat al-Kursi in Focus mode",
                priority = TaskPriority.HIGH,
                category = TaskCategory.SPIRITUAL,
                dueAtMillis = nowMillis + oneHour * 2
            ),
            TaskItem(
                id = "seed_task_2",
                title = "Deep focus sprint on priority project",
                notes = "Pair with Orbital Drift Lo-Fi audio for 45 minutes",
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                dueAtMillis = nowMillis + oneHour * 4
            ),
            TaskItem(
                id = "seed_task_3",
                title = "Review study notes & summarize key concepts",
                notes = "Organize takeaways into concise bullets",
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.STUDY,
                dueAtMillis = nowMillis + oneHour * 6
            ),
            TaskItem(
                id = "seed_task_4",
                title = "Evening walk & offline playlist check",
                notes = "Import favorite local MP3s for offline listening",
                priority = TaskPriority.LOW,
                category = TaskCategory.PERSONAL,
                dueAtMillis = nowMillis + oneHour * 9
            )
        )
    }
}
