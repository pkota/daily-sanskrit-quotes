# Implementation Plan: Daily Sanskrit Quotes

## Overview

Incremental implementation of a Kotlin + Jetpack Compose Android app following MVVM architecture. Tasks build from data layer (Room, serialization, parsing) through domain logic (quote selection, favorites, search, sync) to UI and notifications, with property-based tests (Kotest) validating correctness at each layer.

## Tasks

- [x] 1. Project scaffolding and core data models
  - [x] 1.1 Create Android project with Kotlin, Jetpack Compose, and configure Gradle dependencies
    - Set up Hilt, Room, Ktor Client, WorkManager, kotlinx.serialization, Material 3, Kotest, JUnit 5
    - Configure minimum and target SDK versions
    - _Requirements: 9.1, 9.3_
  - [x] 1.2 Define Room entities and database
    - Create `QuoteEntity`, `ShownQuoteEntity`, `QuoteFtsEntity` (FTS4 virtual table)
    - Create `QuoteDatabase` with DAOs for quotes, shown quotes, and FTS
    - _Requirements: 2.2, 2.3, 2.4_
  - [x] 1.3 Define serialization models and file formats
    - Create `Manifest`, `QuoteFile`, `QuoteDto`, `QuoteDelta`, `DeltaChange` data classes with kotlinx.serialization annotations
    - Create `SyncMetadata` data class for SharedPreferences
    - _Requirements: 7.1_

- [x] 2. Quote file parser, printer, and bundled data import
  - [x] 2.1 Implement `QuoteFileParser` and `QuoteFilePrinter`
    - `QuoteFileParser.parseQuoteFile(json)`, `parseManifest(json)`, `parseDelta(json)` returning `Result<T>`
    - `QuoteFilePrinter.print(quotes, version)` producing pretty-printed JSON
    - Handle invalid/unparseable JSON by returning `Result.failure` with descriptive error message
    - _Requirements: 7.1, 7.2, 7.3, 7.4_
  - [x] 2.2 Write property test: Quote file round-trip (Property 12)
    - **Property 12: Quote file round-trip (parse ↔ print)**
    - For any valid list of `QuoteDto`, printing to JSON then parsing produces an equivalent list
    - **Validates: Requirements 7.1, 7.2, 7.4, 7.5**
  - [x] 2.3 Write property test: Invalid JSON produces descriptive error (Property 13)
    - **Property 13: Invalid JSON produces descriptive error**
    - For any string that is not valid JSON or doesn't conform to schema, parser returns failure Result with non-empty error message
    - **Validates: Requirements 7.3**
  - [x] 2.4 Write property test: Manifest round-trip (Property 14)
    - **Property 14: Manifest round-trip (parse ↔ print)**
    - For any valid `Manifest`, serializing then deserializing produces an equivalent object
    - **Validates: Requirements 7.1**
  - [x] 2.5 Write property test: Delta file round-trip (Property 15)
    - **Property 15: Delta file round-trip (parse ↔ print)**
    - For any valid `QuoteDelta`, serializing then deserializing produces an equivalent object
    - **Validates: Requirements 7.1**
  - [x] 2.6 Implement bundled quote file import
    - Bundle `quotes-full.json` in assets
    - On first launch, parse and insert all quotes into Room database
    - _Requirements: 2.1, 2.2_

- [x] 3. QuoteRepository and storage layer
  - [x] 3.1 Implement `QuoteRepository`
    - `getQuoteById`, `getAllQuotes`, `getUnshownQuotes`, `markAsShown`, `resetShownQuotes`
    - `searchQuotes` using FTS virtual table
    - `getLocalVersion`, `applyDelta`, `rebuildFromFull` with transaction safety
    - Wire Room DAOs and SharedPreferences for sync metadata
    - _Requirements: 2.3, 2.4, 6.3, 6.4_
  - [x] 3.2 Write property test: Quote storage round-trip (Property 3)
    - **Property 3: Quote storage round-trip**
    - For any valid Quote entity stored in DB, retrieving by ID returns identical fields
    - Use Room in-memory database
    - **Validates: Requirements 2.2, 2.3, 3.4**

- [x] 4. Checkpoint - Ensure data layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. DailyQuoteSelector
  - [x] 5.1 Implement `DailyQuoteSelector`
    - `getQuoteForDate(date)`: deterministically select one unshown quote per day using `shown_quotes` table
    - `resetCycleIfNeeded()`: clear shown quotes when all have been displayed
    - Same date always returns same quote; new day selects a different unshown quote
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  - [x] 5.2 Write property test: Daily quote determinism (Property 1)
    - **Property 1: Daily quote determinism**
    - For any date and non-empty DB, `getQuoteForDate(date)` called multiple times returns the same quote
    - **Validates: Requirements 1.1, 1.3**
  - [x] 5.3 Write property test: Quote uniqueness within a display cycle (Property 2)
    - **Property 2: Quote uniqueness within a display cycle**
    - For a DB of size N, selecting quotes for N consecutive days produces N distinct IDs; cycle resets and next selection succeeds
    - **Validates: Requirements 1.4, 1.5**

- [x] 6. FavoritesManager
  - [x] 6.1 Implement `FavoritesManager`
    - `toggleFavorite(quoteId)`: toggle `isFavorite` and set/clear `favoritedAt` timestamp
    - `getFavorites()`: return `Flow<List<Quote>>` sorted by `favoritedAt` descending
    - `isFavorite(quoteId)`: check favorite status
    - Persist across app restarts via Room
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  - [x] 6.2 Write property test: Favorite toggle is self-inverse (Property 4)
    - **Property 4: Favorite toggle is self-inverse**
    - For any quote, toggling favorite twice returns to original state
    - **Validates: Requirements 3.1, 3.2**
  - [x] 6.3 Write property test: Favorites are reverse-chronologically ordered (Property 5)
    - **Property 5: Favorites are reverse-chronologically ordered**
    - For any set of favorited quotes with distinct timestamps, `getFavorites()` returns strictly descending order
    - **Validates: Requirements 3.3**

- [x] 7. SearchEngine and ShareHandler
  - [x] 7.1 Implement `SearchEngine`
    - Case-insensitive partial match against `sanskritText` and `englishTranslation` using Room FTS4
    - Return results within 500ms for up to 5000 quotes
    - _Requirements: 5.1, 5.2, 5.3, 5.5_
  - [x] 7.2 Write property test: Search completeness (Property 7)
    - **Property 7: Search completeness (case-insensitive, partial match)**
    - For any quote and any substring of its text (length ≥ 1), searching returns a result set including that quote
    - **Validates: Requirements 5.1, 5.2**
  - [x] 7.3 Implement `ShareHandler`
    - Format quote as: Sanskrit text, English translation, attribution, "Shared via Daily Sanskrit Quotes"
    - Launch Android system share sheet via Intent
    - Handle share failure with toast error message
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  - [x] 7.4 Write property test: Share formatting contains all fields (Property 6)
    - **Property 6: Share formatting contains all fields**
    - For any Quote with non-empty fields, formatted share string contains all three values as substrings
    - **Validates: Requirements 4.2**

- [x] 8. Checkpoint - Ensure domain layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. QuoteUpdater (Incremental Delta Sync)
  - [x] 9.1 Implement `QuoteUpdater` with manifest + delta sync protocol
    - Fetch `manifest.json` from configurable base URL via Ktor Client
    - Compare remote version with local `db_version` from SharedPreferences
    - If versions match → `AlreadyUpToDate`
    - If `localVersion >= minDeltaVersion` → fetch and apply cumulative delta file
    - If `localVersion < minDeltaVersion` → fetch `quotes-full.json` for full rebuild
    - Wrap all DB mutations in Room transactions for rollback on failure
    - Enforce 24-hour minimum interval between sync attempts
    - Preserve favorite status during delta apply and full rebuild
    - Return appropriate `SyncResult` variant
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_
  - [x] 9.2 Set up WorkManager periodic background sync
    - Schedule periodic work request (24-hour interval) for background sync
    - Ensure network operations run on background thread
    - _Requirements: 6.2, 6.7_
  - [x] 9.3 Write property test: Sync timing gate (Property 8)
    - **Property 8: Sync timing gate**
    - If `lastSyncTimestamp` < 24h ago → `AlreadyUpToDate` without network requests; if ≥ 24h → attempts manifest fetch
    - **Validates: Requirements 6.2**
  - [x] 9.4 Write property test: Delta application correctness (Property 9)
    - **Property 9: Delta application correctness**
    - For any local DB at version V and valid delta V→W: all adds exist, all updates reflected, local version = W
    - **Validates: Requirements 6.3**
  - [x] 9.5 Write property test: Favorites preserved during sync (Property 10)
    - **Property 10: Favorites preserved during sync**
    - For any favorited quotes and any valid sync operation, `isFavorite` and `favoritedAt` remain unchanged
    - **Validates: Requirements 6.4**
  - [x] 9.6 Write property test: Failed sync preserves local state (Property 11)
    - **Property 11: Failed sync preserves local state**
    - If sync fails (network error, invalid data), local DB contents and version remain identical to pre-sync state
    - **Validates: Requirements 6.5, 6.6**

- [x] 10. Checkpoint - Ensure sync layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. NotificationManager
  - [x] 11.1 Implement `QuoteNotificationManager`
    - `scheduleDailyNotification(hour, minute)` using `AlarmManager.setExactAndAllowWhileIdle`
    - `cancelAllNotifications()` to remove all scheduled alarms
    - `updateNotificationTime(hour, minute)` to reschedule
    - Create `BroadcastReceiver` to handle alarm and post notification with quote text
    - Default time: 8:00 AM local time zone
    - Handle Android 13+ notification permission request
    - Tapping notification opens app and displays daily quote
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 12. Hilt dependency injection setup
  - [x] 12.1 Configure Hilt modules and wiring
    - Create Hilt modules providing `QuoteDatabase`, `QuoteRepository`, `DailyQuoteSelector`, `FavoritesManager`, `SearchEngine`, `ShareHandler`, `QuoteUpdater`, `QuoteNotificationManager`
    - Annotate `Application` class with `@HiltAndroidApp`
    - _Requirements: All (cross-cutting)_

- [x] 13. ViewModels
  - [x] 13.1 Implement `DailyQuoteViewModel`
    - Expose daily quote state via `StateFlow`
    - Handle favorite toggle and share actions
    - _Requirements: 1.1, 1.2, 1.3, 3.1, 4.1_
  - [x] 13.2 Implement `FavoritesViewModel`
    - Expose favorites list as `StateFlow` from `FavoritesManager.getFavorites()`
    - Handle unfavorite action
    - _Requirements: 3.1, 3.2, 3.3_
  - [x] 13.3 Implement `SearchViewModel`
    - Expose search results as `StateFlow`
    - Debounce search input, handle empty/cleared query
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 14. Jetpack Compose UI screens
  - [x] 14.1 Implement `DailyQuoteScreen`
    - Display Sanskrit text, English translation, attribution
    - Favorite button (toggle), share button
    - _Requirements: 1.1, 1.2, 3.1, 4.1_
  - [x] 14.2 Implement `FavoritesScreen`
    - Scrollable list of favorited quotes in reverse chronological order
    - Tap to view, unfavorite action
    - _Requirements: 3.3_
  - [x] 14.3 Implement `SearchScreen`
    - Search text field with real-time results
    - "No results found" message for empty results
    - Clear query returns to previous state
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  - [x] 14.4 Implement `SettingsScreen`
    - Notification time picker, notification enable/disable toggle
    - Dark/light mode toggle, color theme selection, text size adjustment, orientation lock
    - _Requirements: 8.4, 8.5_
  - [x] 14.5 Implement navigation and `MainActivity`
    - Single-activity architecture with Compose Navigation
    - Bottom navigation: Daily Quote, Favorites, Search, Settings
    - Handle notification tap deep link to daily quote
    - _Requirements: 8.3_

- [x] 15. Checkpoint - Ensure full app builds and UI renders
  - Ensure all tests pass, ask the user if questions arise.

- [x] 16. Documentation: macOS dev setup and Play Store publishing
  - [x] 16.1 Create macOS development environment setup guide
    - List required software: Android Studio, JDK, Android SDK, command-line tools
    - Provide Homebrew installation commands for all dependencies
    - Specify minimum and target SDK versions
    - Include Gradle command to build debug APK
    - _Requirements: 9.1, 9.2, 9.3, 9.4_
  - [x] 16.2 Create Google Play Store publishing guide
    - Steps to generate signed release APK/App Bundle
    - Steps to create Google Play Developer account
    - Steps to create store listing (title, description, screenshots, privacy policy)
    - Steps to upload App Bundle and submit for review
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [ ] 17. GitHub version control setup
  - [-] 17.1 Initialize Git repository and create `.gitignore`
    - Initialize local Git repo
    - Create `.gitignore` for Android/Kotlin (exclude `build/`, `.gradle/`, `.idea/`, `local.properties`, `*.apk`, `*.aab`, keystore files)
    - Make initial commit with all project files
  - [ ] 17.2 Create GitHub repository and push code
    - Create a new repository on GitHub (via `gh` CLI or manually)
    - Add GitHub remote and push initial commit to `main` branch
    - Set up branch protection rules (optional: require PR reviews for `main`)
  - [ ] 17.3 Create a `README.md` for the GitHub repository
    - App description, features overview, screenshots placeholder
    - Build instructions (reference the macOS dev setup guide from task 16.1)
    - Link to Play Store listing (placeholder)

- [ ] 18. Final checkpoint - Ensure all tests pass and app is complete
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at data, domain, sync, and UI layers
- Property tests (Kotest) validate universal correctness properties from the design document
- All sync operations use Room transactions for atomicity and rollback safety
- Implementation language: Kotlin with Jetpack Compose (as specified in design)
