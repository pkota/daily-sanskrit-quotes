# Requirements Document

## Introduction

The Blog Quote Extractor is a standalone command-line tool that parses blog posts from `subhashitani-en.blogspot.com` containing Sanskrit quotes (subhashitas) and generates a `quotes-full.json` file compatible with the existing Subhashitani Android app. The blog contains approximately 1500 quotes hosted on Blogger. The tool reads exported HTML content, extracts structured quote data (Sanskrit text, English translation, attribution), generates uniform IAST transliteration from the Devanagari text, deduplicates quotes, assigns unique IDs, and outputs a valid `QuoteFile` JSON file that the app can consume directly.

Each blog post follows a consistent structure:
1. Post title in the format `{Source} - {Topic}` in Devanagari (e.g., "सूक्तिमुक्तावली - सज्जन सहवासः")
2. Sanskrit verse in Devanagari script (with danda ।and double-danda ॥ markers)
3. Attribution in Devanagari (prefixed with `- `, e.g., "- सूक्तिमुक्तावली")
4. Transliteration in Harvard-Kyoto romanization
5. Attribution in Roman script (e.g., "- sUktimuktAvalii")
6. English translation paragraph
7. Attribution in English (e.g., "- Suktimuktavali")
8. Blogger labels (e.g., "Labels: Education, Manu Smriti, Subhashita") — present on individual post pages as metadata

The blog was primarily active from 2010–2013. Each post contains exactly one quote. Blogger labels are attached as post metadata and typically include the source text name and a thematic category (e.g., "Education", "Desire"), along with a generic "Subhashita" label.

## Glossary

- **Extractor**: The blog-quote-extractor command-line tool
- **QuoteFile**: The top-level JSON structure containing a version number and a list of quotes, as defined by the Subhashitani app's `QuoteFile` data model
- **QuoteRecord**: A single extracted quote object containing id, sanskritText, englishTranslation, attribution, transliteration, and tags fields, matching the app's `QuoteDto` schema
- **Blog_Post**: A single blog post HTML file containing one Sanskrit quote with associated metadata
- **Quote_Block**: A contiguous section within a Blog_Post that contains a single Sanskrit quote and its associated translation, attribution, transliteration, and tags
- **Deduplication**: The process of identifying and removing duplicate quotes based on normalized Sanskrit text comparison
- **IAST**: International Alphabet of Sanskrit Transliteration, a romanization scheme for Sanskrit

## Requirements

### Requirement 1: Blog Post Input

**User Story:** As a content curator, I want to point the Extractor at a directory of blog post files, so that all quotes across my posts are processed in one run.

#### Acceptance Criteria

1. THE Extractor SHALL accept a command-line argument specifying the input directory path containing Blog_Post files
2. THE Extractor SHALL accept a command-line argument specifying the output file path for the generated QuoteFile JSON
3. WHEN the input directory does not exist, THE Extractor SHALL exit with a non-zero exit code and print an error message indicating the directory was not found
4. WHEN the input directory contains no parseable Blog_Post files, THE Extractor SHALL exit with a non-zero exit code and print an error message indicating no blog posts were found
5. THE Extractor SHALL recursively scan the input directory for files with `.html` extensions

### Requirement 2: Blog Post Parsing

**User Story:** As a content curator, I want the Extractor to parse the exported Blogger HTML, so that all quotes from my blog are extracted correctly.

#### Acceptance Criteria

1. THE Extractor SHALL parse HTML files exported from the Blogger platform at `subhashitani-en.blogspot.com`
2. THE Extractor SHALL identify individual Quote_Blocks by detecting post sections containing a Devanagari Sanskrit verse followed by a Harvard-Kyoto transliteration and an English translation
3. THE Extractor SHALL extract the post title (in the format `{Source} - {Topic}`) and use the source portion as the attribution for the quote
4. IF a Blog_Post file cannot be parsed due to encoding errors or malformed content, THEN THE Extractor SHALL log a warning with the file name and continue processing remaining files
5. THE Extractor SHALL support Blog_Post files encoded in UTF-8

### Requirement 3: Quote Block Extraction

**User Story:** As a content curator, I want the Extractor to identify and extract individual quotes from within blog posts, so that each quote becomes a separate record in the output.

#### Acceptance Criteria

1. THE Extractor SHALL extract the Sanskrit text in Devanagari script from each Quote_Block and store it in the sanskritText field of the QuoteRecord
2. THE Extractor SHALL extract the English translation from each Quote_Block and store it in the englishTranslation field of the QuoteRecord
3. WHEN a Quote_Block contains an attribution line (prefixed with `- ` or `—` in Devanagari or English), THE Extractor SHALL extract the attribution and store it in the attribution field of the QuoteRecord
4. WHEN a Quote_Block does not contain an attribution, THE Extractor SHALL set the attribution field to "Unknown"
5. THE Extractor SHALL generate IAST transliteration from the Devanagari sanskritText for each QuoteRecord and store it in the transliteration field
6. THE Extractor SHALL NOT use transliterations found in the blog post content (e.g., Harvard-Kyoto from the English blog), to ensure uniform transliteration quality across all quotes
7. THE Extractor SHALL handle Devanagari punctuation marks (danda ।, double danda ॥) and numerals during transliteration
8. THE Extractor SHALL populate the tags field from Blogger labels (or generated from translation text when labels are absent) as described in Requirement 4, limited to 2 tags per quote

### Requirement 4: Quote Block Detection

**User Story:** As a content curator, I want the Extractor to reliably detect quote boundaries in my blog's HTML, so that each quote is extracted as a separate record.

#### Acceptance Criteria

1. THE Extractor SHALL detect Quote_Block boundaries by identifying Devanagari text segments (Unicode range U+0900–U+097F) that are followed by a transliteration segment (Latin characters) and an English translation segment
2. THE Extractor SHALL extract the attribution from the Devanagari line prefixed with `- ` (e.g., "- सूक्तिमुक्तावली") and use the English attribution line (e.g., "- Suktimuktavali") as the attribution field value
3. WHEN a blog post contains a "Labels:" metadata section, THE Extractor SHALL extract the Blogger labels and store them in the tags field of the QuoteRecord
4. THE Extractor SHALL exclude the generic "Subhashita" label from the tags list, as it applies to all quotes and provides no distinguishing value
5. THE Extractor SHALL also exclude labels that duplicate the attribution (source text name) from the tags list, since attribution is already stored separately
6. WHEN a blog post does not contain a "Labels:" section or all labels are excluded, THE Extractor SHALL generate tags by analyzing the English translation text to identify thematic categories (e.g., "Wisdom", "Virtue", "Education", "Devotion")
7. THE Extractor SHALL limit the tags field to a maximum of 2 tags per QuoteRecord; when more than 2 candidate tags exist, the Extractor SHALL keep the 2 most specific thematic tags

### Requirement 5: Unique ID Generation

**User Story:** As a content curator, I want each extracted quote to have a stable unique ID, so that the app can track quotes consistently across regenerations.

#### Acceptance Criteria

1. THE Extractor SHALL generate a unique id for each QuoteRecord using the format `q` followed by a zero-padded four-digit sequential number (e.g., "q0001", "q0002")
2. WHEN the number of quotes exceeds 9999, THE Extractor SHALL extend the zero-padding to accommodate the total count (e.g., "q10000", "q10001")
3. THE Extractor SHALL assign IDs in the order quotes are encountered during processing, with files processed in alphabetical order by file path

### Requirement 6: Deduplication

**User Story:** As a content curator, I want duplicate quotes appearing across multiple blog posts to be merged into a single record, so that the output file contains no redundant entries.

#### Acceptance Criteria

1. THE Extractor SHALL identify duplicate quotes by comparing normalized sanskritText values
2. THE Extractor SHALL normalize sanskritText by removing leading and trailing whitespace and collapsing consecutive whitespace characters into a single space before comparison
3. WHEN a duplicate quote is detected, THE Extractor SHALL keep the first occurrence and discard subsequent duplicates
4. WHEN a duplicate quote is detected, THE Extractor SHALL log an informational message identifying the duplicate and the source Blog_Post files
5. WHEN a duplicate quote has different tags across occurrences, THE Extractor SHALL merge the tags from all occurrences into a single deduplicated list

### Requirement 7: QuoteFile JSON Output

**User Story:** As a content curator, I want the output JSON to be directly usable by the Subhashitani app, so that I can drop it into the app's assets without manual editing.

#### Acceptance Criteria

1. THE Extractor SHALL output a JSON file conforming to the QuoteFile schema with a top-level "version" field set to 1 and a "quotes" array of QuoteRecord objects
2. THE Extractor SHALL serialize each QuoteRecord with the fields: id (string), sanskritText (string), englishTranslation (string), attribution (string), transliteration (string), and tags (array of strings)
3. THE Extractor SHALL output the JSON file with UTF-8 encoding and pretty-printed formatting (indented with 2 or 4 spaces)
4. WHEN the transliteration field is an empty string, THE Extractor SHALL omit the transliteration field from the JSON output to match the existing app convention
5. WHEN the tags field is an empty list, THE Extractor SHALL omit the tags field from the JSON output to match the existing app convention

### Requirement 8: QuoteFile Round-Trip Compatibility

**User Story:** As a developer, I want to verify that the generated JSON can be parsed by the app's existing parser and re-printed without data loss, so that I have confidence in format correctness.

#### Acceptance Criteria

1. FOR ALL valid QuoteFile JSON outputs produced by the Extractor, parsing the JSON with the app's QuoteFileParser and then printing with QuoteFilePrinter SHALL produce a semantically equivalent JSON document (round-trip property)
2. THE Extractor SHALL validate the generated JSON against the QuoteFile schema before writing the output file
3. IF the generated JSON fails schema validation, THEN THE Extractor SHALL exit with a non-zero exit code and print an error message describing the validation failure

### Requirement 9: Processing Summary

**User Story:** As a content curator, I want a summary of what the Extractor processed, so that I can verify the extraction was complete and correct.

#### Acceptance Criteria

1. WHEN processing completes successfully, THE Extractor SHALL print a summary to standard output containing: the number of Blog_Post files processed, the total number of Quote_Blocks found, the number of duplicates removed, and the number of QuoteRecords written to the output file
2. WHEN processing completes successfully, THE Extractor SHALL exit with exit code 0
