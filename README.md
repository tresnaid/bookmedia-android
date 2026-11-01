# Bookmedia Gallery (Android Companion App)

A focused, high-performance Android gallery companion app for [Bookmedia](file:///home/tresnaid/project/bookmedia). Built with native **Jetpack Compose**, **Material 3**, and strict adherence to **antislop-ui** principles (no purple neon gradients, zero decorative metric bloat, instant gesture response).

## Features
- **Google Photos-Style Grid**: 3-column media grid with pinch-ratio thumbnails, play indicators on video items, and edge-to-edge layout.
- **Media Kind Filter**: Filter by `All`, `Photos`, and `Videos`.
- **Platform Badges**: One-tap toggles for source platforms (`TIKTOK`, `X`, `INSTAGRAM`, `DIRECT`, `THREADS`).
- **Topic Filter**: Filter by archive topics / tags (`#unsorted`, `#thread`, `#analysis`, custom types).
- **Keyword Search**: Instant client-side search across author handle (`@username`), keywords, archive ID, and post URL.
- **Fullscreen Swipe Viewer (`HorizontalPager`)**:
  - Horizontal swipe to flick between previous and next media items.
  - Native video playback via **AndroidX Media3 ExoPlayer**.
  - Tap-to-toggle overlay controls with counter (`X of Y`), metadata, and direct source linkout.
- **Network Configuration**: In-app server URL switcher (supports local LAN, Tailscale, or public host).

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
4. Tap the Settings icon in the top right to configure your Bookmedia host URL (e.g. `http://192.168.1.x:8080` or `http://10.0.2.2:8080` for Android emulator).
