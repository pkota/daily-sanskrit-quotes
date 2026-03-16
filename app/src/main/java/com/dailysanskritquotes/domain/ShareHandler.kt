package com.dailysanskritquotes.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ShareHandler {

    /**
     * Formats a quote and launches the Android share sheet.
     * Handles ActivityNotFoundException with a toast.
     */
    fun share(context: Context, sanskritText: String, englishTranslation: String, attribution: String) {
        val text = formatQuoteForSharing(sanskritText, englishTranslation, attribution)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share Quote"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Unable to share. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Pure formatting function that can be tested without Android context.
         *
         * Format:
         * "<Sanskrit text>"
         *
         * "<English translation>"
         *
         * — <Attribution>
         *
         * Shared via Daily Sanskrit Quotes
         */
        fun formatQuoteForSharing(
            sanskritText: String,
            englishTranslation: String,
            attribution: String
        ): String {
            return buildString {
                append("\"$sanskritText\"")
                append("\n\n")
                append("\"$englishTranslation\"")
                append("\n\n")
                append("— $attribution")
                append("\n\n")
                append("Shared via Daily Sanskrit Quotes")
            }
        }
    }
}
