package com.mehmetmertmazici.domaincheckercompose.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that formats credit card numbers into groups of 4 digits.
 * Example: "1234567812345678" -> "1234 5678 1234 5678"
 */
class CreditCardVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(16)
        val formatted = StringBuilder()

        for (i in trimmed.indices) {
            formatted.append(trimmed[i])
            // Add space after every 4th digit, except at the end
            if ((i + 1) % 4 == 0 && i != trimmed.lastIndex) {
                formatted.append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                // Count spaces added before this offset
                val spacesAdded = (offset - 1) / 4
                return (offset + spacesAdded).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                // Remove space counts to get original position
                val groups = offset / 5 // 4 digits + 1 space = 5 chars per group
                val remainder = offset % 5
                val originalOffset = groups * 4 + remainder.coerceAtMost(4)
                return originalOffset.coerceAtMost(trimmed.length)
            }
        }

        return TransformedText(
            AnnotatedString(formatted.toString()),
            offsetMapping
        )
    }
}
