package com.antigravity.app.data.model

data class DailyAyat(
    val trackId: String,
    val surahName: String,
    val surahNameArabic: String,
    val surahNumber: Int,
    val ayatNumber: Int,
    val arabicText: String,
    val translationText: String,
    val reflectionNote: String
)
