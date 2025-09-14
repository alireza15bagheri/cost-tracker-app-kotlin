package fr.alirezabagheri.simplecosttracker.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

object NumberFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)

    fun format(number: Double): String {
        return formatter.format(number)
    }

    fun parse(formattedString: String): Double? {
        return try {
            formatter.parse(formattedString.ifEmpty { "0" })?.toDouble()
        } catch (e: Exception) {
            null
        }
    }
}

class NumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.replace(",", "")
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = try {
            val number = originalText.toDouble()
            NumberFormatter.format(number)
        } catch (e: NumberFormatException) {
            // In case of invalid input like "1.2.3", just show the original text
            return TransformedText(text, OffsetMapping.Identity)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                val commasBeforeOffset = formattedText.take(offset + (formattedText.length - originalText.length)).count { it == ',' }
                return (offset + commasBeforeOffset).coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formattedText.isEmpty()) return 0
                val commasBeforeOffset = formattedText.take(offset).count { it == ',' }
                return (offset - commasBeforeOffset).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}