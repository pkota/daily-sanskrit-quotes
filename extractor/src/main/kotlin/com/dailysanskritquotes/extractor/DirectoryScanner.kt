package com.dailysanskritquotes.extractor

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.streams.toList

object DirectoryScanner {

    private val SUPPORTED_EXTENSIONS = setOf("html", "atom", "xml")

    fun findHtmlFiles(inputDir: Path): List<Path> {
        return findInputFiles(inputDir).filter { it.extension == "html" }
    }

    fun findInputFiles(inputDir: Path): List<Path> {
        return Files.walk(inputDir)
            .filter { Files.isRegularFile(it) && it.extension in SUPPORTED_EXTENSIONS }
            .toList()
            .sortedBy { it.toString() }
    }
}
