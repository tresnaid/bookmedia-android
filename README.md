# Archie Gallery (Android Companion App)

A focused Android gallery companion for Archie, built with native Jetpack Compose and Material 3.

## Features
- **Archive Grid**: Compact three-column poster grid with icon-only video, saved, and failed states.
- **Browse Options**: Filter by media type and platform, show only media saved to the server, and change sorting without blocking the grid.
- **Keyword Search**: Instant client-side search across author handle (`@username`), keywords, archive ID, and post URL.
- **Fullscreen Swipe Viewer (`HorizontalPager`)**:
  - Horizontal swipe to flick between previous and next media items.
  - Native video playback via **AndroidX Media3 ExoPlayer**.
  - Tap-to-toggle bottom controls with the seeker, description, server-save state, and source link.
- **Server Profiles**: Switch between local LAN, private-network, and public Archie hosts.

## Project Structure
```
bookmedia-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/bookmedia/gallery/
│   │       ├── MainActivity.kt
│   │       ├── model/ArchiveItem.kt
│   │       ├── data/BookmediaRepository.kt
│   │       └── ui/
│   │           ├── Theme.kt
│   │           ├── GalleryViewModel.kt
│   │           └── GalleryScreen.kt
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## How to Build & Run
1. Open [`/home/tresnaid/project/bookmedia-android`](file:///home/tresnaid/project/bookmedia-android) in **Android Studio**.
2. Sync Gradle dependencies.
3. Run on your Android emulator or physical device.
4. Open Browse options, then Server profiles, to configure an Archie host URL (for example `http://192.168.1.x:8080`, or `http://10.0.2.2:8080` in the Android emulator).
