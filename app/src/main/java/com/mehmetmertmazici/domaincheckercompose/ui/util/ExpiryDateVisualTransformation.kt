package com.mehmetmertmazici.domaincheckercompose.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that shows a masked expiry date input.
 * Always displays the template "AA/YY" and fills in digits as user types.
 * Example:
 *   Input: ""     -> Display: "AA/YY"
 *   Input: "1"    -> Display: "1A/YY"
 *   Input: "12"   -> Display: "12/YY"
 *   Input: "123"  -> Display: "12/3Y"
 *   Input: "1225" -> Display: "12/25"
 */
class ExpiryDateVisualTransformation : VisualTransformation {
    
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)
        val originalLength = digits.length
        
        val formatted = buildString {
            // Month part (positions 0-1)
            append(if (digits.isNotEmpty()) digits[0] else 'A')
            append(if (digits.length > 1) digits[1] else 'A')
            append('/')
            // Year part (positions 3-4)
            append(if (digits.length > 2) digits[2] else 'Y')
            append(if (digits.length > 3) digits[3] else 'Y')
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Map original cursor position to transformed position
                // The '/' is at index 2 in transformed text
                return when {
                    offset <= 0 -> 0
                    offset == 1 -> 1
                    offset == 2 -> 2
                    offset == 3 -> 4 // After '/', skip it
                    offset >= 4 -> 5
                    else -> 0
                }.coerceIn(0, 5)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Map transformed cursor position back to original
                return when {
                    offset <= 0 -> 0
                    offset == 1 -> 1
                    offset == 2 -> 2
                    offset == 3 -> 2 // '/' maps back to position 2
                    offset == 4 -> 3
                    offset >= 5 -> 4
                    else -> 0
                }.coerceIn(0, originalLength)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}
