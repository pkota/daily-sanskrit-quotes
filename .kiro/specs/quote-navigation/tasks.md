# Implementation Plan: Quote Navigation

## Overview

Add swipe navigation between shown quotes, a hidden "Reveal Next Quote" button, date display on quote cards, and swipe navigation in search results. Implementation proceeds bottom-up: data layer → domain → ViewModel → UI, with a shared QuoteCardContent composable extracted to avoid duplication.

## Tasks

- [x] 1. Data layer: Add shown-quote history query and repository method
  - [x] 1.1 Add `getAllShownQuotesSorted()` query to `ShownQuoteDao`
    - Add `@Query("SELECT * FROM shown_quotes ORDER BY shownDate ASC") suspend fun getAllShownQuotesSorted(): List<ShownQuoteEntity>` to `ShownQuoteDao.kt`
    - _Requirements: 2.1_

  - [x] 1.2 Add `getShownQuoteHistory()` method to `QuoteRepository`
    - Add `suspend fun getShownQuoteHistory(): List<Pair<QuoteEntity, String>>` that calls `shownQuoteDao.getAllShownQuotesSorted()`, joins each entry with `quoteDao.getById(quoteId)`, and filters out null (deleted) quotes
    - _Requirements: 2.1, 2.6, 3.3_

  - [ ]* 1.3 Write property test for history sort order
    - **Property 3: Shown quote history is sorted by shownDate ascending**
    - **Validates: Requirements 2.1**

- [x] 2. Domain layer: Add `selectNextUnshownQuote()` to `DailyQuoteSelector`
  - [x] 2.1 Implement `selectNextUnshownQuote()` in `DailyQuoteSelector.kt`
    - Get unshown quotes from repository; if empty, reset cycle and get all quotes
    - Select deterministically using a varied seed (e.g. `today.plusDays(candidates.size.toLong())`) so it doesn't collide with the day's existing quote
    - Mark selected quote as shown with today's date and return it
    - _Requirements: 1.4, 1.5, 1.6_

  - [ ]* 2.2 Write property test for reveal grows history
    - **Property 2: Revealing a quote selects an unshown quote and grows the history**
    - **Validates: Requirements 1.4, 1.6**

- [x] 3. Checkpoint - Verify data and domain layers
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. ViewModel layer: Refactor `DailyQuoteViewModel` to history-based state
  - [x] 4.1 Create `ShownQuoteItem` data class and update `DailyQuoteUiState`
    - Add `ShownQuoteItem` data class with `quote: QuoteEntity`, `shownDate: String`, `isFavorite: Boolean`, `customTags: List<CustomTagEntity>` in the viewmodel file
    - Change `DailyQuoteUiState.Success` to hold `quoteHistory: List<ShownQuoteItem>` and `currentPageIndex: Int` instead of a single quote
    - _Requirements: 2.1, 2.6, 3.3_

  - [x] 4.2 Refactor `DailyQuoteViewModel` to load full quote history
    - Replace `loadTodayQuote()` with `loadQuoteHistory()` that fetches all shown quotes via `repository.getShownQuoteHistory()`, ensures today's quote exists via `dailyQuoteSelector.getQuoteForDate(today)`, builds `ShownQuoteItem` list with favorite status and custom tags, and sets `currentPageIndex` to the last item
    - Add `onPageChanged(index: Int)` to update `currentPageIndex` in the UI state
    - Add `revealNextQuote()` that calls `dailyQuoteSelector.selectNextUnshownQuote()` then reloads history and scrolls to end
    - Update `toggleFavorite()`, `shareQuote()`, `addTag()`, `removeTag()` to operate on `quoteHistory[currentPageIndex]`
    - Update `customTags` StateFlow to derive from the current page's quote ID
    - Update `refreshIfNewDay()` to call `loadQuoteHistory()` instead
    - _Requirements: 1.4, 1.6, 2.1, 2.6, 3.3_

  - [ ]* 4.3 Write property test for navigation direction correctness
    - **Property 4: Page navigation direction correctness**
    - **Validates: Requirements 2.2, 2.3**

  - [ ]* 4.4 Write property test for data consistency
    - **Property 5: Each history page contains correct quote data and shownDate**
    - **Validates: Requirements 2.6, 3.3, 3.4**

- [x] 5. Add `selectedResultIndex` state to `SearchViewModel`
  - [x] 5.1 Add pager selection state to `SearchViewModel`
    - Add `val selectedResultIndex = MutableStateFlow<Int?>(null)`
    - Add `fun selectResult(index: Int)` and `fun clearSelection()` methods
    - _Requirements: 4.1, 4.8_

- [x] 6. Checkpoint - Verify ViewModel changes
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. UI layer: Extract shared `QuoteCardContent` composable
  - [x] 7.1 Create `QuoteCardContent` composable
    - Extract the quote card body (date label, Sanskrit text, transliteration, translation, attribution, tags, favorite/share actions) into a shared `QuoteCardContent` composable in a new file (e.g. `ui/screens/QuoteCardContent.kt`)
    - Accept parameters: `quote: QuoteEntity`, `shownDate: String?` (null hides date), `isFavorite: Boolean`, `customTags: List<CustomTagEntity>`, `predefinedTags: List<String>`, action callbacks
    - Display `shownDate` in top-left corner using `MaterialTheme.typography.labelSmall` formatted as "dd MMM, yyyy"
    - _Requirements: 3.1, 3.2, 3.5, 4.7_

  - [ ]* 7.2 Write property test for date formatting round trip
    - **Property 6: Date formatting round trip**
    - **Validates: Requirements 3.5**

- [x] 8. UI layer: Refactor `DailyQuoteScreen` with HorizontalPager and triple-tap
  - [x] 8.1 Add `HorizontalPager` to `DailyQuoteScreen`
    - Replace the single `Card` with a `HorizontalPager` where each page renders `QuoteCardContent` for the corresponding `ShownQuoteItem`
    - Wire `pagerState.currentPage` changes to `viewModel.onPageChanged()`
    - Set initial page to `currentPageIndex` (today's quote, last in list)
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 2.7_

  - [x] 8.2 Add triple-tap gesture and Reveal Next Quote button
    - Add `Modifier.pointerInput` with `detectTapGestures` using a tap counter + 500ms timeout to detect triple-tap
    - Toggle local `revealButtonVisible` state on triple-tap
    - Render a "Reveal Next Quote" button below the card when visible, calling `viewModel.revealNextQuote()` on press
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ]* 8.3 Write property test for toggle involution
    - **Property 1: Reveal button visibility toggle is an involution**
    - **Validates: Requirements 1.2**

- [x] 9. UI layer: Add search result pager to `SearchScreen`
  - [x] 9.1 Add pager overlay to `SearchScreen`
    - When `selectedResultIndex` is non-null, render a full-screen `HorizontalPager` overlay over the search results list, starting at the selected index
    - Each page uses `QuoteCardContent` with `shownDate = null`
    - Add back navigation (back gesture or button) that calls `viewModel.clearSelection()`
    - Wire tap on search result cards to call `viewModel.selectResult(index)`
    - Close pager if search query changes (reset `selectedResultIndex` to null)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [ ]* 9.2 Write property test for search pager bounds
    - **Property 7: Search pager navigation stays within bounds**
    - **Validates: Requirements 4.5, 4.6**

- [-] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use Kotest property testing (`io.kotest:kotest-property`)
- The shared `QuoteCardContent` composable is created before the screens that use it to avoid duplication
- `HorizontalPager` comes from `androidx.compose.foundation.pager`
