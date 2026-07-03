package com.example.ui

import android.content.Context
import android.widget.Toast
import java.util.Locale

data class TestReferenceRanges(
    val general: String = "",
    val male: String = "",
    val female: String = "",
    val child: String = ""
) {
    fun toSerializedString(): String {
        val parts = mutableListOf<String>()
        if (general.isNotBlank()) parts.add("General: ${general.trim()}")
        if (male.isNotBlank()) parts.add("Male: ${male.trim()}")
        if (female.isNotBlank()) parts.add("Female: ${female.trim()}")
        if (child.isNotBlank()) parts.add("Child: ${child.trim()}")
        return if (parts.isEmpty()) "" else parts.joinToString(" | ")
    }

    companion object {
        fun fromSerializedString(str: String): TestReferenceRanges {
            if (str.isBlank()) return TestReferenceRanges()
            var general = ""
            var male = ""
            var female = ""
            var child = ""
            
            val parts = str.split("|").map { it.trim() }
            for (part in parts) {
                if (part.startsWith("General:", ignoreCase = true)) {
                    general = part.substringAfter("General:").trim()
                } else if (part.startsWith("Male:", ignoreCase = true)) {
                    male = part.substringAfter("Male:").trim()
                } else if (part.startsWith("Female:", ignoreCase = true)) {
                    female = part.substringAfter("Female:").trim()
                } else if (part.startsWith("Child:", ignoreCase = true)) {
                    child = part.substringAfter("Child:").trim()
                }
            }
            
            // If none of the prefixes are found, treat the whole string as General
            if (general.isEmpty() && male.isEmpty() && female.isEmpty() && child.isEmpty()) {
                general = str.trim()
            }
            
            return TestReferenceRanges(general, male, female, child)
        }
    }
}

enum class RangeComparison {
    NORMAL, HIGH, LOW
}

enum class ValueStatus {
    NORMAL, HIGH, LOW
}

fun resolveReferenceRange(normalRangeStr: String, patientAge: Int, patientGender: String): String {
    val ranges = TestReferenceRanges.fromSerializedString(normalRangeStr)
    
    // 1. If child range is available and patient is age < 18, use child range
    if (patientAge < 18 && ranges.child.isNotBlank()) {
        return ranges.child
    }
    
    // 2. If gender is Male and male range is available, use male range
    if (patientGender.trim().lowercase(Locale.ROOT).startsWith("m") && ranges.male.isNotBlank()) {
        return ranges.male
    }
    
    // 3. If gender is Female and female range is available, use female range
    if (patientGender.trim().lowercase(Locale.ROOT).startsWith("f") && ranges.female.isNotBlank()) {
        return ranges.female
    }
    
    // 4. Fallback to general range, if general is empty, return whatever is first non-blank, or the original string
    return if (ranges.general.isNotBlank()) {
        ranges.general
    } else if (ranges.male.isNotBlank()) {
        ranges.male
    } else if (ranges.female.isNotBlank()) {
        ranges.female
    } else if (ranges.child.isNotBlank()) {
        ranges.child
    } else {
        normalRangeStr
    }
}

fun compareValueWithRange(enteredValue: String, rangeStr: String): RangeComparison {
    val numVal = enteredValue.trim().toDoubleOrNull() ?: return RangeComparison.NORMAL
    
    val cleanRange = rangeStr.trim().lowercase(Locale.ROOT)
    if (cleanRange.isEmpty() || cleanRange == "n/a" || cleanRange == "normal" || cleanRange == "negative") return RangeComparison.NORMAL
    
    try {
        // Find all decimal numbers in the string
        val numberRegex = """\d+(\.\d+)?""".toRegex()
        val matches = numberRegex.findAll(cleanRange).toList()
        
        if (matches.size >= 2) {
            val minVal = matches[0].value.toDoubleOrNull()
            val maxVal = matches[1].value.toDoubleOrNull()
            if (minVal != null && maxVal != null) {
                return if (numVal < minVal) {
                    RangeComparison.LOW
                } else if (numVal > maxVal) {
                    RangeComparison.HIGH
                } else {
                    RangeComparison.NORMAL
                }
            }
        }
        
        // Starts with < or less than
        if (cleanRange.contains("<") || cleanRange.contains("less than")) {
            val limit = numberRegex.find(cleanRange)?.value?.toDoubleOrNull()
            if (limit != null) {
                return if (numVal >= limit) RangeComparison.HIGH else RangeComparison.NORMAL
            }
        }
        
        // Starts with > or greater than
        if (cleanRange.contains(">") || cleanRange.contains("greater than")) {
            val limit = numberRegex.find(cleanRange)?.value?.toDoubleOrNull()
            if (limit != null) {
                return if (numVal <= limit) RangeComparison.LOW else RangeComparison.NORMAL
            }
        }
    } catch (e: Exception) {
        // ignore
    }
    
    return RangeComparison.NORMAL
}

fun evaluateResultStatus(valueStr: String, rangeStr: String): ValueStatus {
    val comparison = compareValueWithRange(valueStr, rangeStr)
    return when (comparison) {
        RangeComparison.HIGH -> ValueStatus.HIGH
        RangeComparison.LOW -> ValueStatus.LOW
        else -> ValueStatus.NORMAL
    }
}

object ToastHelper {
    private var currentToast: Toast? = null

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        val appContext = context.applicationContext
        currentToast?.cancel()
        val toast = Toast.makeText(appContext, message, duration)
        currentToast = toast
        toast.show()
    }
}

