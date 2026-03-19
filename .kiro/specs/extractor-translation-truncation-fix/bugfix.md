# Bugfix Requirements Document

## Introduction

The blog quote extractor's `QuoteBlockExtractor` truncates multi-sentence English translations by incorrectly stripping leading text it misidentifies as Harvard-Kyoto (HK) transliteration. The `stripHkPrefix` function in `QuoteBlockExtractor.kt` looks for a "transition point" where a capitalized English word follows preceding words, and if the preceding words pass a loose heuristic check, it discards everything before that point. This causes the extractor to retain only the tail portion of many English translations, losing the beginning sentences entirely.

For example, the translation "Penance is the best way to live in the Kruta Yuga. In Treta yuga, attaining knowledge was the best way." gets truncated to "Kruta Yuga. In Treta yuga, attaining knowledge was the best way." because "In" is detected as a sentence-starting capitalized word and the text before it is incorrectly classified as an HK prefix.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN an English translation line contains multiple sentences and a mid-line capitalized word (e.g., "...the Kruta Yuga. In Treta yuga...") THEN the system strips the text preceding the capitalized word, treating it as Harvard-Kyoto transliteration prefix

1.2 WHEN an English translation line begins with words that pass the loose HK-prefix heuristic (letters, whitespace, and basic punctuation only) followed by a capitalized word THEN the system incorrectly identifies the beginning of the translation as HK transliteration and removes it

1.3 WHEN a pure English translation line (containing no actual HK transliteration) is processed by `stripHkPrefix` THEN the system may still strip leading text if the line happens to contain a capitalized word after position 0 that the heuristic misidentifies as an English sentence start following HK text

### Expected Behavior (Correct)

2.1 WHEN an English translation line contains multiple sentences with mid-line capitalized words THEN the system SHALL preserve the entire translation text without stripping any portion

2.2 WHEN an English translation line begins with ordinary English words (no HK patterns like lowercase-followed-by-uppercase within a word) THEN the system SHALL return the line unchanged

2.3 WHEN a line contains no actual Harvard-Kyoto transliteration patterns (no `[a-z][A-Z]` mid-word transitions) THEN the system SHALL preserve the full line content

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a line is genuine Harvard-Kyoto transliteration (containing patterns like `nItidoShAH`, `mRutyuH`, `prIyate` with mid-word lowercase-to-uppercase transitions) THEN the system SHALL CONTINUE TO filter it out via `isHarvardKyoto`

3.2 WHEN a line is a mixed HK+English line where actual HK transliteration words precede English text THEN the system SHALL CONTINUE TO strip the HK prefix portion and retain the English portion

3.3 WHEN an English translation line contains only a single sentence with no mid-line capitalized words beyond proper nouns THEN the system SHALL CONTINUE TO extract it correctly as it does today

3.4 WHEN Devanagari text, Sanskrit verse lines, and attribution lines are processed THEN the system SHALL CONTINUE TO extract them correctly without any change in behavior
