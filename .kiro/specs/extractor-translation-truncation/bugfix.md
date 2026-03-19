# Bugfix Requirements Document

## Introduction

The `stripHkPrefix()` function in `QuoteBlockExtractor.kt` incorrectly strips the beginning of English translation sentences, mistaking them for Harvard-Kyoto (HK) transliteration prefixes. This causes multi-sentence English translations to be truncated, losing the opening sentences entirely.

The root cause is twofold:
1. `stripHkPrefix()` uses an overly broad heuristic — it looks for any capitalized word at position > 0 and checks if the preceding text is "all letters/whitespace/punctuation," which matches normal English prose. It then strips everything before that capitalized word.
2. `isHarvardKyoto()` has a threshold of only 1 match for the `[a-z][A-Z]` pattern, which is too sensitive and can match normal English words.

Concrete example (quote q0004): the full English translation "Penance is the best way to live in the Kruta Yuga. In Treta yuga, attaining knowledge was the best way..." is truncated to "Kruta Yuga. In Treta yuga..." because `stripHkPrefix` sees "Kruta" (capitalized, at position > 0) and strips everything before it.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN an English translation line contains multiple sentences with mid-line capitalized words (e.g., proper nouns like "Kruta Yuga", "Treta", "Dwapara") THEN the system strips the text preceding the first such capitalized word, treating it as a Harvard-Kyoto transliteration prefix

1.2 WHEN an English translation line begins with ordinary English words that satisfy the loose prefix check (all letters, whitespace, and basic punctuation) followed by a capitalized word at position > 0 THEN the system incorrectly identifies the beginning of the translation as HK transliteration and removes it

1.3 WHEN `isHarvardKyoto()` evaluates a line containing even a single occurrence of a lowercase letter followed by an uppercase letter (e.g., normal English words near proper nouns) THEN the system may incorrectly classify the line as HK transliteration and filter it out entirely

### Expected Behavior (Correct)

2.1 WHEN an English translation line contains multiple sentences with mid-line capitalized words (proper nouns, sentence starts after periods) THEN the system SHALL preserve the entire translation text without stripping any portion

2.2 WHEN an English translation line begins with ordinary English words and contains no actual HK transliteration patterns (no words with internal lowercase-to-uppercase transitions like "nItidoShAH") THEN the system SHALL return the line unchanged

2.3 WHEN `isHarvardKyoto()` evaluates a line THEN the system SHALL require a sufficiently high density of HK-characteristic patterns (multiple `[a-z][A-Z]` mid-word transitions) before classifying the line as HK transliteration, to avoid false positives on normal English text

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a line is genuine Harvard-Kyoto transliteration (containing multiple HK-characteristic patterns like `nItidoShAH`, `mRutyuH`, `prIyate` with mid-word lowercase-to-uppercase transitions) THEN the system SHALL CONTINUE TO filter it out via `isHarvardKyoto`

3.2 WHEN a line is a true mixed HK+English line where actual HK transliteration words (with `[a-z][A-Z]` patterns) precede English text THEN the system SHALL CONTINUE TO strip the genuine HK prefix portion and retain the English portion

3.3 WHEN an English translation line contains only a single sentence with no ambiguous capitalization patterns THEN the system SHALL CONTINUE TO extract it correctly as it does today

3.4 WHEN Devanagari text, Sanskrit verse lines, and attribution lines are processed THEN the system SHALL CONTINUE TO extract them correctly without any change in behavior

3.5 WHEN the extractor re-runs after the fix THEN all existing quote IDs (q0001, q0002, ...) SHALL remain unchanged — the fix must only affect `englishTranslation` content, not quote identity, ordering, or count

### Verification Requirements

4.1 A backup of the pre-fix output has been saved at `extractor/output/quotes-full.backup.json`

4.2 AFTER the fix is applied and the extractor is re-run THEN a diff report SHALL be generated comparing `quotes-full.backup.json` (before) with the new `quotes-full.json` (after), listing each quote ID where the `englishTranslation` changed and showing the old vs new text

4.3 The diff report SHALL confirm that no quote IDs were added, removed, or reordered — only `englishTranslation` values may differ
