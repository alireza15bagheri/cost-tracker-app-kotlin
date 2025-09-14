package fr.alirezabagheri.simplecosttracker.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)

    fun format(number: Double): String {
        return formatter.format(number)
    }

    fun parse(formattedString: String): Double? {
        return try {
            formatter.parse(formattedString)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }
}

class NumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.replace(",", "")
        val formattedText = if (originalText.isNotEmpty()) {
            try {
                NumberFormatter.format(originalText.toDouble())
            } catch (e: NumberFormatException) {
                // Handle cases where the input is not a valid number, e.g., just a "."
                originalText
            }
        } else {
            ""
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val commas = formattedText.count { it == ',' }
                return offset + commas
            }

            override fun transformedToOriginal(offset: Int): Int {
                val commas = formattedText.substring(0, offset).count { it == ',' }
                return offset - commas
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}