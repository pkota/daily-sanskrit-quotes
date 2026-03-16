# Daily Sanskrit Quotes

An Android app that delivers one inspiring Sanskrit quote per day — with the original text, English translation, and attribution.

## Features

- **Daily Quote** — A new Sanskrit quote every day, deterministically selected so it stays the same if you reopen the app
- **Favorites** — Tap the heart to save quotes you love; browse them anytime in reverse chronological order
- **Share** — Send any quote to friends via the system share sheet (WhatsApp, Twitter, email, etc.)
- **Natural Language Search** — Find quotes by keyword or phrase with fast, case-insensitive partial matching
- **Cloud Sync** — Incremental delta updates keep your quote collection fresh without re-downloading everything
- **Daily Notifications** — Get reminded each morning (or whenever you choose) with the quote of the day
- **Customizable Themes** — Dark/light mode, color themes, adjustable text size, and orientation lock

## Screenshots

<!-- Add screenshots here -->
| Daily Quote | Favorites | Search | Settings |
|:-----------:|:---------:|:------:|:--------:|
| *coming soon* | *coming soon* | *coming soon* | *coming soon* |

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Database | Room |
| Networking | Ktor Client |
| Background Work | WorkManager |
| DI | Hilt |
| Serialization | kotlinx.serialization |
| Testing | JUnit 5, Kotest (property-based) |

## Getting Started

### Prerequisites

Follow the [macOS Development Environment Setup](docs/macos-dev-setup.md) guide to install:

- Android Studio
- JDK 17
- Android SDK (API 26–34)
- Command-line tools

### Build

```bash
# Clone the repo
git clone https://github.com/<your-username>/DailySanskritQuotes.git
cd DailySanskritQuotes

# Build debug APK
./gradlew :app:assembleDebug
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

### Run Tests

```bash
./gradlew :app:testDebugUnitTest
```

## Download

<!-- Replace with actual Play Store link once published -->
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80">](https://play.google.com/store/apps/details?id=com.dailysanskritquotes)

## License

All rights reserved.
