# Requirements Document

## Introduction

This document specifies the requirements for enhancing the existing Daily Sanskrit Quotes Android app. The enhancements include renaming the app to "Subhashitani", fixing settings UI to properly apply visual changes, adding new quote data fields (transliteration and tags), enabling user custom tags, creating a new app icon featuring the Sanskrit letter "सं", and implementing the Vesper Libre custom font for Sanskrit text display.

## Glossary

- **App**: The Subhashitani Android application
- **Quote_Entity**: The Room database entity representing a quote with all its fields
- **Quote_DTO**: The data transfer object for parsing quotes from JSON
- **Settings_Screen**: The Compose screen where users configure app preferences
- **Theme_System**: The Material 3 theming infrastructure that applies colors and typography
- **Tag**: A categorization label attached to a quote (predefined or user-created)
- **Predefined_Tag**: A tag that comes bundled with the quote data from the server
- **Custom_Tag**: A tag created by the user and stored locally
- **Transliteration**: The romanized (Latin alphabet) representation of Sanskrit text
- **Subhashitam**: A Sanskrit quote (singular form of Subhashitani); used interchangeably with "quote" throughout the app
- **Vesper_Libre_Font**: A Google Font used for displaying Sanskrit text

## Requirements

### Requirement 1: App Rename

**User Story:** As a user, I want the app to be named "Subhashitani", so that the app name reflects its Sanskrit heritage.

#### Acceptance Criteria

1. THE App SHALL display "Subhashitani" as the application name in the device launcher
2. THE App SHALL display "Subhashitani" in the system app settings and app info screens
3. THE App SHALL use "Subhashitani" in the string resource `app_name` replacing "Daily Sanskrit Quotes"

### Requirement 2: Dark Mode Setting Applies Visually

**User Story:** As a user, I want the dark mode toggle in settings to change the app's color scheme, so that I can use the app comfortably in low-light environments.

#### Acceptance Criteria

1. WHEN the user enables dark mode in the Settings_Screen, THE Theme_System SHALL apply the dark color scheme to all screens immediately
2. WHEN the user disables dark mode in the Settings_Screen, THE Theme_System SHALL apply the light color scheme to all screens immediately
3. THE Theme_System SHALL persist the dark mode preference across app restarts
4. THE Theme_System SHALL pass the dark mode setting from the Settings_Screen to the root `DailySanskritQuotesTheme` composable via the SettingsViewModel state

### Requirement 3: Color Theme Setting Applies Visually

**User Story:** As a user, I want the color theme selection in settings to change the app's color palette, so that I can personalize the app's appearance.

#### Acceptance Criteria

1. WHEN the user selects a color theme (Default, Warm, Cool, or Forest) in the Settings_Screen, THE Theme_System SHALL apply the corresponding color scheme to all screens immediately
2. THE Theme_System SHALL persist the selected color theme across app restarts
3. THE Theme_System SHALL pass the color theme setting from the Settings_Screen to the root `DailySanskritQuotesTheme` composable via the SettingsViewModel state

### Requirement 4: Text Size Setting Applies Visually

**User Story:** As a user, I want the text size slider in settings to change the size of quote text, so that I can read quotes comfortably based on my visual preferences.

#### Acceptance Criteria

1. WHEN the user adjusts the text size slider in the Settings_Screen, THE App SHALL scale the Sanskrit text and English translation text on quote display screens according to the selected TextSizeOption scale factor
2. THE App SHALL persist the text size preference across app restarts
3. THE App SHALL apply the text size scale to the DailyQuoteScreen, SearchScreen, and FavoritesScreen quote displays
4. THE Theme_System SHALL provide the text size scale factor to quote display composables via the SettingsViewModel state

### Requirement 5: Transliteration Field for Quotes

**User Story:** As a user, I want to see a romanized version of Sanskrit text, so that I can learn to pronounce the quotes even if I cannot read Devanagari script.

#### Acceptance Criteria

1. THE Quote_Entity SHALL include a `transliteration` field of type String that stores the romanized Sanskrit text
2. THE Quote_DTO SHALL include a `transliteration` field for parsing from JSON data
3. WHEN a quote is displayed on the DailyQuoteScreen, THE App SHALL display the transliteration text below the Sanskrit text
4. WHEN a quote is displayed in search results, THE App SHALL display the transliteration text below the Sanskrit text
5. WHEN a quote is displayed in favorites, THE App SHALL display the transliteration text below the Sanskrit text
6. IF a quote has an empty or null transliteration, THEN THE App SHALL not display the transliteration section for that quote

### Requirement 6: Predefined Tags for Quotes

**User Story:** As a user, I want quotes to have predefined tags, so that I can understand the theme or category of each quote at a glance.

#### Acceptance Criteria

1. THE Quote_Entity SHALL include a `tags` field that stores a list of predefined tag strings
2. THE Quote_DTO SHALL include a `tags` field for parsing predefined tags from JSON data
3. WHEN a quote is displayed, THE App SHALL render the predefined tags as visual chips below the quote content
4. THE App SHALL store predefined tags as a serialized list in the Room database
5. IF a quote has no predefined tags, THEN THE App SHALL not display the tags section for that quote

### Requirement 7: User Custom Tags

**User Story:** As a user, I want to add my own custom tags to any quote, so that I can organize and categorize quotes according to my personal preferences.

#### Acceptance Criteria

1. THE App SHALL provide an "Add Tag" control on the quote detail display allowing users to create custom tags
2. WHEN the user enters a custom tag name and confirms, THE App SHALL save the custom tag associated with the specific quote
3. THE App SHALL store custom tags separately from predefined tags in a dedicated Room database table
4. THE App SHALL display custom tags alongside predefined tags on the quote display, visually distinguishing custom tags from predefined tags
5. WHEN the user taps a remove action on a custom tag, THE App SHALL delete the custom tag from the quote
6. THE App SHALL persist custom tags across app restarts
7. THE App SHALL limit custom tag names to 30 characters
8. IF the user enters an empty or whitespace-only tag name, THEN THE App SHALL not create the custom tag

### Requirement 7: User Custom Tags

**User Story:** As a user, I want to add and remove my own custom tags on any subhashitam (quote), so that I can organize and categorize quotes according to my personal preferences.

#### Acceptance Criteria

1. THE App SHALL provide an "Add Tag" control on every subhashitam display (DailyQuoteScreen, FavoritesScreen, SearchScreen) allowing users to create custom tags
2. WHEN the user enters a custom tag name and confirms, THE App SHALL save the custom tag associated with the specific subhashitam
3. THE App SHALL store custom tags in a dedicated Room database table with a many-to-many relationship to quotes (one subhashitam can have multiple tags, one tag can be on multiple subhashitams)
4. THE App SHALL display custom tags alongside predefined tags on the subhashitam display, visually distinguishing custom tags from predefined tags (e.g., different chip color or icon)
5. WHEN the user taps a remove action on a custom tag chip, THE App SHALL remove that tag from the subhashitam
6. THE App SHALL persist custom tags across app restarts
7. THE App SHALL limit custom tag names to 30 characters
8. IF the user enters an empty or whitespace-only tag name, THEN THE App SHALL not create the custom tag
9. THE App SHALL allow the same custom tag to be reused across multiple subhashitams without re-typing (suggest existing tags)

### Requirement 8: Search by Tags

**User Story:** As a user, I want to search for subhashitams by their tags, so that I can quickly find quotes on specific themes or topics.

#### Acceptance Criteria

1. WHEN the user enters a search query, THE SearchEngine SHALL match against predefined tags in addition to sanskritText and englishTranslation
2. WHEN the user enters a search query, THE SearchEngine SHALL match against custom tags in addition to predefined tags
3. THE SearchEngine SHALL return subhashitams where any associated tag (predefined or custom) contains the search query as a case-insensitive substring
4. THE App SHALL include tag matches in the FTS (Full-Text Search) index for fast retrieval
5. IF a subhashitam matches via tag but not via text content, THE App SHALL still include it in search results

### Requirement 9: App Icon with Sanskrit Letter

**User Story:** As a user, I want the app icon to feature the Sanskrit letter "सं" (saM), so that the app is visually identifiable and reflects its Sanskrit focus.

#### Acceptance Criteria

1. THE App SHALL use an adaptive icon featuring the Sanskrit letter "सं" as the foreground element
2. THE App SHALL provide the icon in all required Android density buckets (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
3. THE App SHALL use a complementary background color that provides good contrast with the "सं" letter
4. THE App SHALL provide both regular and round launcher icon variants
5. THE App icon foreground SHALL use a color that is visible in both light and dark launcher themes

### Requirement 10: Vesper Libre Custom Font for Sanskrit Text

**User Story:** As a user, I want Sanskrit text displayed in the Vesper Libre font, so that the text has an aesthetically pleasing and readable appearance.

#### Acceptance Criteria

1. THE App SHALL bundle the Vesper Libre font files in the app resources
2. THE App SHALL apply the Vesper Libre font to all Sanskrit text displays (sanskritText field)
3. THE App SHALL apply the Vesper Libre font to all transliteration text displays
4. THE Theme_System SHALL define a custom FontFamily for Vesper Libre
5. THE App SHALL use the Vesper Libre Regular weight for body text and Medium or Bold weights for headings where available
6. IF the Vesper Libre font fails to load, THEN THE App SHALL fall back to the system default font

### Requirement 11: Database Migration for New Fields

**User Story:** As a developer, I want the database to migrate cleanly when adding new fields, so that existing user data (favorites, shown quotes) is preserved.

#### Acceptance Criteria

1. WHEN the app is updated with the new schema, THE App SHALL execute a Room database migration that adds the `transliteration` and `tags` columns to the quotes table
2. THE App SHALL set default values for `transliteration` (empty string) and `tags` (empty list) for existing quotes during migration
3. THE App SHALL preserve all existing favorite statuses and favoritedAt timestamps during migration
4. THE App SHALL increment the database version number to trigger the migration
