# Extractor Translation Truncation Bugfix Design

## Overview

The `stripHkPrefix()` and `isHarvardKyoto()` functions in `QuoteBlockExtractor.kt` use overly broad heuristics that misidentify normal English prose as Harvard-Kyoto (HK) transliteration. This causes multi-sentence English translations to be truncated — the opening portion is stripped because a capitalized proper noun (e.g., "Kruta") is mistaken for the start of the "real" English text after an HK prefix.

The fix tightens both functions: raise the `isHarvardKyoto` threshold so normal English text with incidental `[a-z][A-Z]` patterns is not classified as HK, and make `stripHkPrefix` require genuine HK-characteristic words in the prefix before stripping.

## Glossary

- **Bug_Condition (C)**: An English translation line that contains mid-line capitalized words (proper nouns, new sentences) and no genuine HK transliteration, yet is truncated by `stripHkPrefix` or filtered by `isHarvardKyoto`
- **Property (P)**: English translation lines without genuine HK patterns must pass through `stripHkPrefix` and `isHarvardKyoto` unchanged
- **Preservation**: Genuine HK transliteration lines must continue to be filtered, genuine mixed HK+English lines must continue to have their HK prefix stripped, and all non-`englishTranslation` fields (IDs, Sanskrit text, attribution, ordering) must remain identical
- **`stripHkPrefix(text)`**: Function in `QuoteBlockExtractor.kt` that attempts to remove a leading Harvard-Kyoto transliteration prefix from a mixed line, returning only the English portion
- **`isHarvardKyoto(text)`**: Function in `QuoteBlockExtractor.kt` that classifies a line as HK transliteration based on `[a-z][A-Z]` mid-word pattern count and danda presence
- **HK Pattern**: A `[a-z][A-Z]` transition within a single word, characteristic of Harvard-Kyoto transliteration (e.g., `nItidoShAH` has transitions `n→I`, `o→S`, `h→A`, `A→H`)

## Bug Details

### Bug Condition

The bug manifests when an English translation line contains mid-line capitalized words (proper nouns like "Kruta Yuga", sentence starts after periods) but no genuine HK transliteration patterns. The `stripHkPrefix()` function finds the first capitalized word at position > 0, checks if the preceding text is "all letters/whitespace/punctuation" (which normal English satisfies), and strips everything before it. Additionally, `isHarvardKyoto()` can filter out entire lines if even a single incidental `[a-z][A-Z]` match occurs.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type String (a Latin-script line from the post body)
  OUTPUT: boolean

  hkWordCount := count of words in input that contain [a-z][A-Z] mid-word transitions
  hasCapitalizedWordAfterStart := EXISTS word at position i > 0 WHERE word[0].isUpperCase
                                  AND word has no [a-z][A-Z] pattern AND word.length > 1
  prefixIsPlainText := all characters before that word are letters, whitespace, or punctuation

  RETURN (hkWordCount == 0 AND hasCapitalizedWordAfterStart AND prefixIsPlainText)
         OR (hkWordCount <= 1 AND isHarvardKyoto_current(input) returns true)
END FUNCTION
```

### Examples

- **q0004**: Input `"Penance is the best way to live in the Kruta Yuga. In Treta yuga, attaining knowledge was the best way..."` → `stripHkPrefix` sees "Kruta" at position > 0, prefix is plain English, strips to `"Kruta Yuga. In Treta yuga..."` — loses "Penance is the best way to live in the"
- **Multi-sentence**: Input `"The wise person seeks truth. Knowledge leads to liberation."` → `stripHkPrefix` sees "Knowledge" at position > 0, prefix is plain text, strips to `"Knowledge leads to liberation."` — loses the first sentence
- **Single incidental match**: Input `"He went to McDonald's for lunch"` → `isHarvardKyoto` finds 1 match (`cD`) and returns true, filtering the entire line
- **Edge case (correct)**: Input `"nItidoShAH mRutyuH The wise seek truth"` → this IS a genuine mixed line; the HK prefix should be stripped, yielding `"The wise seek truth"`

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Genuine HK transliteration lines (multiple `[a-z][A-Z]` mid-word transitions like `nItidoShAH`, `mRutyuH`, `prIyate`) must continue to be filtered by `isHarvardKyoto`
- Genuine mixed HK+English lines where actual HK words precede English text must continue to have the HK prefix stripped by `stripHkPrefix`
- Lines containing Devanagari dandas (`।`, `॥`) must continue to be filtered
- Single-sentence English translations with no ambiguous patterns must continue to be extracted correctly
- Devanagari text extraction, attribution detection, and all non-translation fields must remain unchanged
- All existing quote IDs (q0001, q0002, ...) must remain identical — no additions, removals, or reordering

**Scope:**
All inputs that do NOT involve the `isHarvardKyoto` or `stripHkPrefix` code paths are completely unaffected. The fix is scoped to these two private functions only. This includes:
- Devanagari line detection (`containsDevanagari`)
- Attribution line detection (`isAttributionLine`)
- ID assignment, deduplication, JSON serialization
- Atom feed and HTML parsing

## Hypothesized Root Cause

Based on the bug description and code analysis, the issues are:

1. **`isHarvardKyoto` threshold too low**: The function returns `true` when `hkMatches >= 1`. A single incidental `[a-z][A-Z]` occurrence in normal English (e.g., "McDonald" → `cD`) triggers a false positive. Genuine HK lines typically have 3+ such transitions per line. The threshold should be raised (e.g., >= 3) or switched to a per-word density check.

2. **`stripHkPrefix` prefix check too broad**: The condition `prefix.all { it.isLetter() || it.isWhitespace() || it in ".,;:!?()" }` matches any normal English text. This means ANY capitalized word at position > 0 triggers stripping if the prefix is plain text. The function should only strip when the prefix actually contains words with HK-characteristic patterns (`[a-z][A-Z]` transitions).

3. **No word-level HK detection in `stripHkPrefix`**: The function checks if the prefix as a whole contains `[a-z][A-Z]` OR is all-letters/whitespace/punctuation. The `OR` branch is the problem — it should be removed or replaced with a requirement that the prefix contains actual HK words.

4. **No minimum word count or density check**: Neither function considers how many words exhibit HK patterns relative to total words. A density-based approach would be more robust.

## Correctness Properties

Property 1: Bug Condition - English translations without HK patterns are preserved intact

_For any_ English translation line that contains zero words with `[a-z][A-Z]` mid-word transitions (i.e., no genuine HK transliteration words), the fixed `stripHkPrefix` function SHALL return the line unchanged, and the fixed `isHarvardKyoto` function SHALL return `false`.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Genuine HK transliteration continues to be detected and stripped

_For any_ line that contains 3 or more words with `[a-z][A-Z]` mid-word transitions (genuine HK transliteration), the fixed `isHarvardKyoto` function SHALL return `true`, preserving the existing filtering behavior. For mixed lines where genuine HK words precede English text, `stripHkPrefix` SHALL continue to strip the HK prefix.

**Validates: Requirements 3.1, 3.2, 3.3**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `extractor/src/main/kotlin/com/dailysanskritquotes/extractor/QuoteBlockExtractor.kt`

**Function**: `isHarvardKyoto`

**Specific Changes**:
1. **Raise HK match threshold**: Change `hkMatches >= 1` to `hkMatches >= 3` (or use a per-word density metric). Genuine HK lines like `nItidoShAH mRutyuH prIyate` have many transitions; normal English rarely has more than 1-2 incidental matches.
2. **Keep danda check**: The `Regex("[।॥]").containsMatchIn(text)` check is correct and should remain unchanged.

**Function**: `stripHkPrefix`

**Specific Changes**:
3. **Require HK words in prefix**: Remove the `prefix.all { ... }` fallback branch. Only strip when the prefix actually contains words with `[a-z][A-Z]` patterns (i.e., `Regex("[a-z][A-Z]").containsMatchIn(prefix)` is the sole condition).
4. **Add word-level HK check**: Before stripping, verify that at least one word in the prefix contains a `[a-z][A-Z]` transition. This prevents stripping when the prefix is just normal English.
5. **Preserve the transition-point logic**: The logic for finding the first "English sentence start" word (uppercase, no mid-word HK pattern, length > 1, not "I") is sound and should be kept.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior. The project uses JUnit 5 and Kotest for property-based testing.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write unit tests that call `stripHkPrefix` and `isHarvardKyoto` directly with English translation strings that trigger the bug. Run these tests on the UNFIXED code to observe failures and confirm the root cause.

**Test Cases**:
1. **Multi-sentence with proper nouns**: Call `stripHkPrefix("Penance is the best way to live in the Kruta Yuga.")` — expect full string returned, will fail on unfixed code (returns truncated)
2. **Single incidental HK match**: Call `isHarvardKyoto("He went to McDonald's for lunch")` — expect `false`, will fail on unfixed code (returns `true`)
3. **Two sentences with capitalized start**: Call `stripHkPrefix("The wise seek truth. Knowledge leads to liberation.")` — expect full string, will fail on unfixed code
4. **Line with proper noun only**: Call `stripHkPrefix("This is about Dharma and its meaning")` — expect full string, will fail on unfixed code

**Expected Counterexamples**:
- `stripHkPrefix` returns truncated strings for all multi-sentence English inputs with mid-line capitalization
- `isHarvardKyoto` returns `true` for English lines with even 1 incidental `[a-z][A-Z]` match
- Root cause confirmed: the `prefix.all { ... }` branch in `stripHkPrefix` and the `>= 1` threshold in `isHarvardKyoto`

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed functions produce the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result_strip := stripHkPrefix_fixed(input)
  result_hk := isHarvardKyoto_fixed(input)
  ASSERT result_strip == input  // English text returned unchanged
  ASSERT result_hk == false     // Not classified as HK
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed functions produce the same result as the original functions.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT isHarvardKyoto_original(input) == isHarvardKyoto_fixed(input)
  ASSERT stripHkPrefix_original(input) == stripHkPrefix_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing using Kotest is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for genuine HK inputs

**Test Plan**: Observe behavior on UNFIXED code first for genuine HK inputs, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Genuine HK filtering preserved**: Generate strings with 3+ HK-pattern words, verify `isHarvardKyoto` returns `true` on both old and new code
2. **Genuine mixed line stripping preserved**: Construct lines with HK words followed by English, verify `stripHkPrefix` strips correctly on both old and new code
3. **Danda-based filtering preserved**: Verify lines with `।` or `॥` continue to be classified as HK
4. **Full pipeline ID preservation**: Run extractor on input data, compare output IDs with backup to verify no changes

### Unit Tests

- Test `isHarvardKyoto` with genuine HK lines (expect `true`), English lines (expect `false`), and edge cases (1-2 incidental matches)
- Test `stripHkPrefix` with pure English lines (expect unchanged), genuine mixed lines (expect stripped), and edge cases
- Test full `extract()` pipeline with sample posts to verify `englishTranslation` output

### Property-Based Tests

- Generate random English sentences with proper nouns and verify `stripHkPrefix` returns them unchanged (fix checking)
- Generate random strings with 3+ HK-pattern words and verify `isHarvardKyoto` returns `true` (preservation checking)
- Generate random English lines with 0 HK-pattern words and verify `isHarvardKyoto` returns `false` (fix checking)

### Integration Tests

- Run the full extractor pipeline on the actual input data and compare output with `quotes-full.backup.json`
- Verify all quote IDs match between backup and new output
- Generate a diff report showing only `englishTranslation` changes
