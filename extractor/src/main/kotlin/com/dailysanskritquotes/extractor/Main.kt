package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.RawQuote
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // Parse arguments
    val inputDir = parseArg(args, "--input")
    val outputFile = parseArg(args, "--output")

    if (inputDir == null || outputFile == null) {
        System.err.println("Usage: extractor --input <directory> --output <file>")
        exitProcess(1)
    }

    val inputPath = Paths.get(inputDir)
    val outputPath = Paths.get(outputFile)

    // Validate input directory
    if (!inputPath.exists() || !inputPath.isDirectory()) {
        System.err.println("ERROR: Input directory not found: $inputDir")
        exitProcess(1)
    }

    // Step 1: Scan for input files (HTML, Atom XML)
    val inputFiles = DirectoryScanner.findInputFiles(inputPath)
    if (inputFiles.isEmpty()) {
        System.err.println("ERROR: No .html/.atom/.xml files found in $inputDir")
        exitProcess(1)
    }

    // Step 2-3: Parse files and extract quotes
    val rawQuotes = mutableListOf<RawQuote>()
    var parsedFileCount = 0
    var skippedFileCount = 0

    for (file in inputFiles) {
        val posts = when (file.toString().substringAfterLast('.')) {
            "atom", "xml" -> AtomFeedParser.parse(file)
            else -> listOfNotNull(HtmlParser.parse(file))
        }

        for (post in posts) {
            val quote = QuoteBlockExtractor.extract(post, file.toString())
            if (quote == null) {
                skippedFileCount++
                continue
            }
            rawQuotes.add(quote)
            parsedFileCount++
        }

        if (posts.isEmpty()) skippedFileCount++
    }

    // Step 4: Deduplicate
    val deduplicationResult = Deduplicator.deduplicate(rawQuotes)
    for (logEntry in deduplicationResult.duplicateLog) {
        System.err.println("INFO: $logEntry")
    }

    // Step 5: Assign IDs (also generates IAST transliteration and tags)
    val quoteDtos = IdAssigner.assignIds(deduplicationResult.uniqueQuotes)

    // Step 6: Write JSON
    JsonWriter.write(quoteDtos, outputPath)

    // Step 7: Validate round-trip
    val jsonContent = outputPath.readText(Charsets.UTF_8)
    val validationResult = RoundTripValidator.validate(jsonContent)
    if (validationResult.isFailure) {
        System.err.println("ERROR: ${validationResult.exceptionOrNull()?.message}")
        exitProcess(1)
    }

    // Step 8: Print summary
    println("Processing complete!")
    println("  Files processed: $parsedFileCount")
    println("  Files skipped:   $skippedFileCount")
    println("  Quotes found:    ${rawQuotes.size}")
    println("  Duplicates:      ${deduplicationResult.duplicateCount}")
    println("  Quotes written:  ${quoteDtos.size}")
    println("  Output file:     $outputFile")
}

private fun parseArg(args: Array<String>, flag: String): String? {
    val index = args.indexOf(flag)
    return if (index >= 0 && index + 1 < args.size) args[index + 1] else null
}
