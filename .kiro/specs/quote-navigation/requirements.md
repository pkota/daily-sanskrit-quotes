# Requirements Document

## Introduction

This feature adds three capabilities to the Daily Quote screen in the Subhashitani app: a hidden developer button to reveal additional quotes on demand, horizontal swipe navigation between previously shown quotes, and a date label on the quote card showing when each quote was revealed. Together these let the developer test quote cycling without waiting for a new day, let users browse their quote history via swipe gestures, and provide temporal context for each quote.

## Glossary

- **Daily_Quote_Screen**: The main Composable screen (`DailyQuoteScreen.kt`) that displays the current quote card with actions such as favorite, share, and tag.
- **Quote_Card**: The `Card` Composable inside Daily_Quote_Screen that renders the Sanskrit text, transliteration, English translation, attribution, tags, and action buttons for a single quote.
- **DailyQuote_ViewModel**: The `DailyQuoteViewModel` class that manages UI state for the Daily_Quote_Screen, loads quotes via DailyQuote_Selector, and exposes a `StateFlow<DailyQuoteUiState>`.
- **DailyQuote_Selector**: The `DailyQuoteSelector` domain class that deterministically picks a quote for a given date, marks it as shown, and handles cycle resets.
- **Shown_Quotes_Table**: The Room `shown_quotes` table storing `ShownQuoteEntity` records, each with a `quoteId` (PK) and `shownDate` (ISO date string).
- **Shown_Quote_Dao**: The Room DAO (`ShownQuoteDao`) providing queries against the Shown_Quotes_Table, including `getQuoteIdForDate()`, `getShownQuoteIds()`, `markAsShown()`, and `resetAll()`.
- **Quote_Repository**: The `QuoteRepository` class that wraps database access and exposes methods such as `getQuoteShownOnDate()`, `markAsShown()`, `getUnshownQuotes()`, and `getAllQuotes()`.
- **Reveal_Next_Button**: A hidden UI button on the Daily_Quote_Screen that, when activated, selects and displays a new unshown quote as if a new day had arrived.
- **Swipe_Navigator**: The horizontal swipe gesture handler on the Quote_Card that allows navigation between previously shown quotes.
- **Shown_Quote_History**: The ordered list of all ShownQuoteEntity records from the Shown_Quotes_Table, sorted chronologically by shownDate.
- **Search_Screen**: The Composable screen (`SearchScreen.kt`) that displays search results as a scrollable list of quote cards.
- **Search_ViewModel**: The `SearchViewModel` class that manages search query state and exposes search results as a `StateFlow<List<QuoteEntity>>`.
- **Search_Result_Pager**: The horizontal swipe gesture handler on the Search_Screen that allows navigation between search result quotes in a full-screen pager view.

## Requirements

### Requirement 1: Hidden Reveal Next Quote Button

**User Story:** As a developer, I want a hidden button on the daily quote screen that reveals the next unshown quote on demand, so that I can test quote cycling without waiting for a new calendar day.

#### Acceptance Criteria

1. THE Daily_Quote_Screen SHALL render the Reveal_Next_Button in a hidden state by default, with no visual indicator of its presence.
2. WHEN the user performs a triple-tap gesture on the Quote_Card, THE Daily_Quote_Screen SHALL toggle the visibility of the Reveal_Next_Button.
3. WHILE the Reveal_Next_Button is visible, THE Daily_Quote_Screen SHALL display the Reveal_Next_Button at the bottom of the Quote_Card.
4. WHEN the user presses the Reveal_Next_Button, THE DailyQuote_ViewModel SHALL select a new unshown quote using DailyQuote_Selector, mark the selected quote as shown with today's date in the Shown_Quotes_Table, and update the UI state to display the newly selected quote.
5. IF no unshown quotes remain when the Reveal_Next_Button is pressed, THEN THE DailyQuote_Selector SHALL reset the shown-quote cycle and select from the full quote pool.
6. WHEN a new quote is revealed via the Reveal_Next_Button, THE DailyQuote_ViewModel SHALL add the new quote to the Shown_Quote_History so that the Swipe_Navigator can navigate to the new quote.

### Requirement 2: Swipe Navigation Between Shown Quotes

**User Story:** As a user, I want to swipe left and right on the quote card to browse previously revealed quotes, so that I can revisit quotes I have seen on earlier days.

#### Acceptance Criteria

1. WHEN the Daily_Quote_Screen loads, THE DailyQuote_ViewModel SHALL retrieve the Shown_Quote_History sorted by shownDate in ascending order.
2. WHEN the user swipes right on the Quote_Card, THE Swipe_Navigator SHALL navigate to the previous quote in the Shown_Quote_History relative to the currently displayed quote.
3. WHEN the user swipes left on the Quote_Card, THE Swipe_Navigator SHALL navigate to the next quote in the Shown_Quote_History relative to the currently displayed quote.
4. WHILE the currently displayed quote is the earliest entry in the Shown_Quote_History, THE Swipe_Navigator SHALL not respond to a right-swipe gesture.
5. WHILE the currently displayed quote is the latest entry in the Shown_Quote_History, THE Swipe_Navigator SHALL not respond to a left-swipe gesture.
6. WHEN the Swipe_Navigator navigates to a different quote, THE DailyQuote_ViewModel SHALL load the full QuoteEntity for that quote from the database and update the UI state to display the quote along with its correct favorite status and custom tags.
7. THE Swipe_Navigator SHALL use Compose Foundation's `HorizontalPager` or equivalent swipe-based Composable to provide smooth, animated page transitions between quotes.

### Requirement 3: Date Display on Quote Card

**User Story:** As a user, I want to see the date a quote was revealed displayed on the quote card, so that I know when each quote was first shown to me.

#### Acceptance Criteria

1. THE Quote_Card SHALL display the shownDate of the currently displayed quote in the top-left corner of the card.
2. THE Quote_Card SHALL render the shownDate using `MaterialTheme.typography.labelSmall` to keep the date visually unobtrusive.
3. WHEN the DailyQuote_ViewModel loads a quote, THE DailyQuote_ViewModel SHALL include the shownDate from the Shown_Quotes_Table in the UI state exposed to the Daily_Quote_Screen.
4. WHEN the user navigates to a different quote via the Swipe_Navigator, THE Quote_Card SHALL update the displayed date to reflect the shownDate of the newly displayed quote.
5. THE Quote_Card SHALL format the shownDate as a formal, human-readable date string in the format "dd MMM, yyyy" (e.g., "15 Jan, 2025") rather than the raw ISO format stored in the database.

### Requirement 4: Swipe Navigation in Search Results

**User Story:** As a user, I want to swipe left and right through search results, so that I can browse matching quotes without returning to the list view.

#### Acceptance Criteria

1. WHEN the user taps a search result card on the Search_Screen, THE Search_Screen SHALL open a full-screen pager view (Search_Result_Pager) displaying the tapped quote.
2. THE Search_Result_Pager SHALL use the same `HorizontalPager` component as the Daily_Quote_Screen Swipe_Navigator to provide consistent swipe behavior.
3. WHEN the user swipes left on the Search_Result_Pager, THE Search_Result_Pager SHALL navigate to the next quote in the search results list.
4. WHEN the user swipes right on the Search_Result_Pager, THE Search_Result_Pager SHALL navigate to the previous quote in the search results list.
5. WHILE the currently displayed quote is the first search result, THE Search_Result_Pager SHALL not respond to a right-swipe gesture.
6. WHILE the currently displayed quote is the last search result, THE Search_Result_Pager SHALL not respond to a left-swipe gesture.
7. THE Search_Result_Pager SHALL display each quote using the same Quote_Card layout as the Daily_Quote_Screen, including favorite, share, and tag actions.
8. THE Search_Result_Pager SHALL provide a back navigation mechanism (back gesture or button) to return to the search results list.
