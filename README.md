# Antigravity (Android — Kotlin + Jetpack Compose + Media3 + Room)

All-in-one personal productivity and mindfulness Android application combining a smart To-Do manager with a dual-mode offline-ready audio player for **Songs** and **Quranic Ayats**.

## Key Features

1. **Dual-Mode Audio Player (Songs & Quranic Ayats)**
   - Segmented switcher between **Songs** and **Quranic Ayats**
   - Full transport controls: play, pause, next, previous, seek bar with timestamps, shuffle, and repeat (`OFF`, `ALL`, `ONE`)
   - Background playback & media notification controls via Media3 `MediaSessionService` (`AntigravityPlaybackService`)
   - Ayat mode displays Surah name (English & Arabic), Ayat number, toggleable Arabic RTL script, and English translation
   - **Offline-first audio support**:
     - Built-in `OfflineAudioSeeder` generates starter ambient Lo-Fi Focus tracks and an offline Ayat contemplation tone in app-private storage so playback works immediately offline
     - `OfflineDownloadManager` + Media3 `SimpleCache` lets you save Quranic Ayats offline with 1 tap (`Download All Ayats`)
     - `LocalAudioImporter` lets you import local `.mp3`, `.m4a`, `.wav`, or `.flac` files via the Android file picker or `MediaStore` device scan (with optional Surah/Ayat metadata tagging)

2. **Smart To-Do List Manager**
   - Create, edit, complete (with animated strikethrough), and delete tasks (with Snackbar Undo)
   - Priority tags (`High`, `Medium`, `Low`), due dates & times (Material Date/Time pickers), categories (`Work`, `Study`, `Personal`, `Spiritual`), search, and filters
   - **Focus Mode Integration**: launch Lo-Fi Focus music or calming Ayats directly from the To-Do screen

3. **Home Dashboard**
   - Daily productivity progress summary
   - **Daily Ayat** inspiration card with 1-tap "Listen to Ayat"
   - **Quick Resume** card for the last played Song or Surah/Ayat
   - Today's pending tasks with interactive completion checkboxes

4. **Dark Glassmorphic UI & Offline Persistence**
   - Deep navy/slate background with translucent glassmorphic cards and cyan/emerald accents
   - 3-tab bottom navigation (**Home**, **To-Do**, **Player**) + persistent floating mini-player
   - Room SQLite database (`antigravity.db`) + Jetpack DataStore (`antigravity_playback_prefs`)

## Opening & Running in Android Studio

1. Open `C:\Users\LENOVO\.gemini\antigravity\scratch\Antigravity` in **Android Studio** (Ladybug or newer recommended).
2. Let Android Studio sync Gradle (it uses `gradle/libs.versions.toml` and will automatically configure the Gradle wrapper/SDK if not already cached).
3. Run the `:app` configuration on an Android emulator or physical device (Android 8.0 / API 26+).
4. Run unit tests via Android Studio or `./gradlew testDebugUnitTest`.
