# Implementation Plan: Subhashitani Enhancements

## Overview

Incremental implementation of app enhancements: data layer first (entities, DAOs, migration, DTOs), then domain logic, resources, theme/UI updates, and property-based tests at each layer. Each task builds on the previous, ensuring no orphaned code.

## Tasks

- [x] 1. Data layer: Update QuoteEntity and add CustomTagEntity
  - [x] 1.1 Add `transliteration` and `tags` fields to `QuoteEntity`
    - Add `val transliteration: String = ""` and `val tags: String = "[]"` to `QuoteEntity`
    - _Requirements: 5.1, 6.1, 6.4_

  - [x] 1.2 Create `CustomTagEntity` data class
    - Create `app/src/main/java/com/dailysanskritquotes/data/db/CustomTagEntity.kt`
    - Composite primary key `(tagName, quoteId)`, foreign key to `quotes(id)` with CASCADE delete, index on `quoteId`
    - _Requirements: 7.3_

  - [x] 1.3 Create `CustomTagDao` interface
    - Create `app/src/main/java/com/dailysanskritquotes/data/db/CustomTagDao.kt`
    - Methods: `insert`, `delete`, `getTagsForQuote`, `getAllDistinctTagNames`, `searchQuoteIdsByTag`
    - _Requirements: 7.2, 7.3, 7.5, 7.6, 7.9_

  - [x] 1.4 Update `QuoteDao.searchQuotes` to include `tags` column
    - Extend the LIKE query to also match against the `tags` column
    - Add `getById` if not present (already exists)
    - _Requirements: 8.1_

  - [x] 1.5 Update `QuoteDatabase` to version 3 with migration
    - Register `CustomTagEntity` in `@Database` entities list
    - Add `customTagDao()` abstract method
    - Increment version to 3
    - Add `MIGRATION_2_3`: ALTER TABLE to add `transliteration` and `tags` columns, CREATE TABLE `custom_tags` with index
    - Replace `fallbackToDestructiveMigration()` with `.addMigrations(MIGRATION_2_3)`
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ]* 1.6 Write property test: Migration preserves favorites (Property 8)
    - **Property 8: Migration preserves favorites**
    - Use Room `MigrationTestHelper` to create v2 database with random quotes (some favorited), run migration, verify favorites and favoritedAt preserved
    - Use 25 iterations
    - **Validates: Requirements 11.3**

- [x] 2. Data layer: Update DTOs and mapping
  - [x] 2.1 Add `transliteration` and `tags` fields to `QuoteDto`
    - Add `val transliteration: String = ""` and `val tags: List<String> = emptyList()` with defaults for backward compatibility
    - _Requirements: 5.2, 6.2_

  - [x] 2.2 Update `QuoteDto.toEntity` mapping function
    - Create or update the mapping extension to serialize `tags` list to JSON string and pass `transliteration` through
    - _Requirements: 5.1, 5.2, 6.1, 6.2_

  - [x]* 2.3 Write property test: QuoteDto serialization round-trip (Property 2)
    - **Property 2: QuoteDto serialization round-trip**
    - Generate random `QuoteDto` instances with arbitrary transliteration and tags, serialize to JSON, deserialize, assert equality
    - Use 25 iterations
    - **Validates: Requirements 5.1, 5.2, 6.1, 6.2, 6.4**

- [x] 3. Checkpoint - Data layer verification
  - Ensure all tests pass (`./gradlew :app:testDebugUnitTest`), ask the user if questions arise.
  - Verify build succeeds (`./gradlew :app:assembleDebug`)

- [x] 4. Domain layer: CustomTagManager and SearchEngine updates
  - [x] 4.1 Create `CustomTagManager` class
    - Create `app/src/main/java/com/dailysanskritquotes/domain/CustomTagManager.kt`
    - Methods: `getTagsForQuote`, `getAllTagNames`, `addTag` (with validation: trim, reject blank, reject >30 chars), `removeTag`
    - _Requirements: 7.1, 7.2, 7.5, 7.7, 7.8, 7.9_

  - [x]* 4.2 Write property test: Custom tag add/query round-trip (Property 4)
    - **Property 4: Custom tag add/query round-trip**
    - Use Room in-memory database. Generate random valid tag names (non-blank, ≤30 chars) and quote IDs, add tags, query, verify presence
    - Use 25 iterations
    - **Validates: Requirements 7.2, 7.3, 7.6, 7.9**

  - [x]* 4.3 Write property test: Custom tag removal (Property 5)
    - **Property 5: Custom tag removal**
    - Use Room in-memory database. Add a random tag, remove it, verify absence
    - Use 25 iterations
    - **Validates: Requirements 7.5**

  - [x]* 4.4 Write property test: Invalid tag name rejection (Property 6)
    - **Property 6: Invalid tag name rejection**
    - Generate random whitespace-only strings and strings >30 chars, verify `addTag` returns false and tag is not persisted
    - Use 25 iterations
    - **Validates: Requirements 7.7, 7.8**

  - [x] 4.5 Update `SearchEngine` to merge custom tag results
    - Update constructor to accept `CustomTagDao`
    - Combine `quoteDao.searchQuotes` (now includes predefined tags) with `customTagDao.searchQuoteIdsByTag` results, deduplicating by quote ID
    - _Requirements: 8.1, 8.2, 8.3, 8.5_

  - [x] 4.6 Update `AppModule` Hilt DI wiring
    - Add `provideCustomTagDao` provider
    - Add `provideCustomTagManager` provider
    - Update `provideSearchEngine` to inject `CustomTagDao`
    - _Requirements: 7.3, 8.2_

  - [x]* 4.7 Write property test: Tag search inclusion (Property 7)
    - **Property 7: Tag search inclusion**
    - Use Room in-memory database. Create quotes with random tags, search by tag substring, verify matching quotes are in results
    - Use 25 iterations
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.5**

- [x] 5. Checkpoint - Domain layer verification
  - Ensure all tests pass (`./gradlew :app:testDebugUnitTest`), ask the user if questions arise.
  - Verify build succeeds (`./gradlew :app:assembleDebug`)

- [x] 6. Resources: Font, icon, and strings
  - [x] 6.1 Add Vesper Libre font files to `res/font/`
    - Add `vesper_libre_regular.ttf` and `vesper_libre_bold.ttf` to `app/src/main/res/font/`
    - _Requirements: 10.1_

  - [x] 6.2 Update `strings.xml` app name to "Subhashitani"
    - Change `<string name="app_name">` value from "Daily Sanskrit Quotes" to "Subhashitani"
    - _Requirements: 1.1, 1.2, 1.3_

  - [x] 6.3 Create new adaptive app icon with "सं" letter
    - Create foreground vector/PNG with Devanagari "सं" in dark amber/saffron color
    - Create background layer with warm cream/off-white color
    - Provide `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` pointing to adaptive icon layers
    - Provide density-specific fallback PNGs (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 7. Theme and typography updates
  - [x] 7.1 Define `VesperLibre` FontFamily and `scaledTypography` in `Theme.kt`
    - Create `VesperLibre` FontFamily with Regular and Bold weights from `R.font.vesper_libre_regular` and `R.font.vesper_libre_bold`
    - Create `scaledTypography(scale: Float)` function that applies VesperLibre to `headlineMedium` and `bodyLarge` with scaled font sizes
    - Pass constructed `Typography` to `MaterialTheme(typography = ...)` in `DailySanskritQuotesTheme`
    - _Requirements: 10.2, 10.3, 10.4, 10.5, 10.6, 4.1_

  - [x]* 7.2 Write property test: Typography scaling correctness (Property 3)
    - **Property 3: Typography scaling correctness**
    - Generate random `TextSizeOption` values, call `scaledTypography`, verify font sizes are `base * scale`
    - Use 25 iterations
    - **Validates: Requirements 4.1**

  - [x]* 7.3 Write property test: Settings persistence round-trip (Property 1)
    - **Property 1: Settings persistence round-trip**
    - Generate random combinations of dark mode, color theme, and text size, persist via SharedPreferences, load back, verify equality
    - Use 25 iterations
    - **Validates: Requirements 2.3, 3.2, 4.2**

- [x] 8. UI screens: Transliteration, tags, and custom tag controls
  - [x] 8.1 Update `DailyQuoteScreen` to display transliteration and tags
    - Add transliteration `Text` below Sanskrit text (conditionally hidden when empty), styled with `bodyLarge` + VesperLibre + italic
    - Add `FlowRow` of `AssistChip` for predefined tags (surfaceVariant) and custom tags (primaryContainer + close icon)
    - Add "Add Tag" chip that opens a tag input dialog
    - Wire to `CustomTagManager` via ViewModel
    - _Requirements: 5.3, 5.6, 6.3, 6.5, 7.1, 7.4_

  - [x] 8.2 Update `SearchScreen` to display transliteration and tags
    - Same transliteration and tag chip display as DailyQuoteScreen
    - _Requirements: 5.4, 8.1, 8.5_

  - [x] 8.3 Update `FavoritesScreen` to display transliteration and tags
    - Same transliteration and tag chip display as DailyQuoteScreen
    - _Requirements: 5.5, 7.1_

  - [x] 8.4 Create Add Tag dialog composable
    - `AlertDialog` with `TextField` (max 30 chars), autocomplete dropdown suggesting existing tag names from `CustomTagManager.getAllTagNames()`
    - Confirm/cancel buttons
    - _Requirements: 7.1, 7.2, 7.7, 7.8, 7.9_

  - [x] 8.5 Update ViewModels to expose transliteration, tags, and custom tag operations
    - Update `DailyQuoteViewModel`, `SearchViewModel`, `FavoritesViewModel` to expose custom tag flows and add/remove tag methods
    - Inject `CustomTagManager` where needed
    - _Requirements: 7.2, 7.4, 7.5_

- [x] 9. Final checkpoint - Full verification
  - Ensure all existing and new tests pass (`./gradlew :app:testDebugUnitTest`), ask the user if questions arise.
  - Verify build succeeds (`./gradlew :app:assembleDebug`)
  - Verify no regressions in existing functionality

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- All property-based tests use 25 iterations (project convention)
- Checkpoints ensure incremental validation at each layer boundary
- Build command: `./gradlew :app:assembleDebug`
- Test command: `./gradlew :app:testDebugUnitTest`
