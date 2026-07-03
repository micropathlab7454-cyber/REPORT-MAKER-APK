package com.example.data

import org.json.JSONObject
import org.json.JSONArray

data class MpWidalData(
    val pf: String = "Negative", // "Positive" or "Negative"
    val pv: String = "Negative", // "Positive" or "Negative"
    val typhiO: List<String> = listOf("-", "-", "-", "-", "-"),
    val typhiH: List<String> = listOf("-", "-", "-", "-", "-"),
    val typhiAH: List<String> = listOf("-", "-", "-", "-", "-"),
    val typhiBH: List<String> = listOf("-", "-", "-", "-", "-"),
    val finalResult: String = "Negative", // "Positive" or "Negative"
    val remarks: String = ""
)

object MpWidalSerializer {
    fun serialize(data: MpWidalData): String {
        val obj = JSONObject()
        obj.put("pf", data.pf)
        obj.put("pv", data.pv)
        obj.put("typhiO", JSONArray(data.typhiO))
        obj.put("typhiH", JSONArray(data.typhiH))
        obj.put("typhiAH", JSONArray(data.typhiAH))
        obj.put("typhiBH", JSONArray(data.typhiBH))
        obj.put("finalResult", data.finalResult)
        obj.put("remarks", data.remarks)
        return obj.toString()
    }

    fun deserialize(jsonStr: String): MpWidalData {
        if (jsonStr.isBlank() || !jsonStr.startsWith("{")) return MpWidalData()
        return try {
            val obj = JSONObject(jsonStr)
            val pf = obj.optString("pf", "Negative")
            val pv = obj.optString("pv", "Negative")
            
            val typhiO = mutableListOf<String>()
            val arrO = obj.optJSONArray("typhiO")
            if (arrO != null) {
                for (i in 0 until arrO.length()) typhiO.add(arrO.optString(i, "-"))
            } else {
                repeat(5) { typhiO.add("-") }
            }

            val typhiH = mutableListOf<String>()
            val arrH = obj.optJSONArray("typhiH")
            if (arrH != null) {
                for (i in 0 until arrH.length()) typhiH.add(arrH.optString(i, "-"))
            } else {
                repeat(5) { typhiH.add("-") }
            }

            val typhiAH = mutableListOf<String>()
            val arrAH = obj.optJSONArray("typhiAH")
            if (arrAH != null) {
                for (i in 0 until arrAH.length()) typhiAH.add(arrAH.optString(i, "-"))
            } else {
                repeat(5) { typhiAH.add("-") }
            }

            val typhiBH = mutableListOf<String>()
            val arrBH = obj.optJSONArray("typhiBH")
            if (arrBH != null) {
                for (i in 0 until arrBH.length()) typhiBH.add(arrBH.optString(i, "-"))
            } else {
                repeat(5) { typhiBH.add("-") }
            }

            val finalResult = obj.optString("finalResult", "Negative")
            val remarks = obj.optString("remarks", "")

            MpWidalData(pf, pv, typhiO, typhiH, typhiAH, typhiBH, finalResult, remarks)
        } catch (e: Exception) {
            MpWidalData()
        }
    }
}

data class ParameterTemplate(
    val name: String,
    val unit: String,
    val normalRange: String,
    val method: String
)

object PathologyTestTemplates {
    val templates = mapOf(
        "CBC" to listOf(
            ParameterTemplate("Hemoglobin", "g/dL", "Male: 13.5 - 17.5 | Female: 12.0 - 15.5 | Child: 11.0 - 14.5 | General: 12.0 - 16.0", "Automated Cell Counter"),
            ParameterTemplate("Total Leukocyte Count (TLC)", "/cu mm", "4000 - 11000", "Automated Cell Counter"),
            ParameterTemplate("Neutrophils", "%", "40 - 75", "Automated Cell Counter"),
            ParameterTemplate("Lymphocytes", "%", "20 - 45", "Automated Cell Counter"),
            ParameterTemplate("Monocytes", "%", "2 - 10", "Automated Cell Counter"),
            ParameterTemplate("Eosinophils", "%", "1 - 6", "Automated Cell Counter"),
            ParameterTemplate("Basophils", "%", "0 - 1", "Automated Cell Counter"),
            ParameterTemplate("RBC Count", "million/cu mm", "Male: 4.5 - 5.9 | Female: 4.1 - 5.1", "Automated Cell Counter"),
            ParameterTemplate("HCT/PCV", "%", "Male: 40 - 50 | Female: 36 - 46", "Automated Cell Counter"),
            ParameterTemplate("MCV", "fL", "80 - 100", "Automated Cell Counter"),
            ParameterTemplate("MCH", "pg", "27 - 32", "Automated Cell Counter"),
            ParameterTemplate("MCHC", "g/dL", "32 - 36", "Automated Cell Counter"),
            ParameterTemplate("RDW", "%", "11.5 - 14.5", "Automated Cell Counter"),
            ParameterTemplate("Platelet Count", "lakhs/cu mm", "1.50 - 4.50", "Automated Cell Counter"),
            ParameterTemplate("MPV", "fL", "7.4 - 10.4", "Automated Cell Counter"),
            ParameterTemplate("PCT", "%", "0.16 - 0.36", "Automated Cell Counter"),
            ParameterTemplate("PDW", "fL", "9.0 - 17.0", "Automated Cell Counter")
        ),
        "LFT" to listOf(
            ParameterTemplate("Serum Bilirubin Total", "mg/dL", "0.2 - 1.2", "Diazo"),
            ParameterTemplate("Serum Bilirubin Direct", "mg/dL", "0.0 - 0.3", "Diazo"),
            ParameterTemplate("Serum Bilirubin Indirect", "mg/dL", "0.2 - 0.8", "Calculated"),
            ParameterTemplate("SGOT / AST", "U/L", "5 - 40", "IFCC"),
            ParameterTemplate("SGPT / ALT", "U/L", "5 - 45", "IFCC"),
            ParameterTemplate("Alkaline Phosphatase (ALP)", "U/L", "40 - 129", "pNPP"),
            ParameterTemplate("Total Protein", "g/dL", "6.0 - 8.3", "Biuret"),
            ParameterTemplate("Serum Albumin", "g/dL", "3.5 - 5.0", "BCG"),
            ParameterTemplate("Serum Globulin", "g/dL", "2.0 - 3.5", "Calculated"),
            ParameterTemplate("A:G Ratio", "Ratio", "1.0 - 2.1", "Calculated")
        ),
        "KFT" to listOf(
            ParameterTemplate("Blood Urea", "mg/dL", "15 - 45", "Urease GLDH"),
            ParameterTemplate("Serum Creatinine", "mg/dL", "General: 0.6 - 1.2 | Male: 0.6 - 1.2 | Female: 0.5 - 1.1 | Child: 0.3 - 0.7", "Modified Jaffe's"),
            ParameterTemplate("Serum Uric Acid", "mg/dL", "General: 3.5 - 7.2 | Male: 3.5 - 7.2 | Female: 2.6 - 6.0 | Child: 2.0 - 5.5", "Uricase"),
            ParameterTemplate("Blood Urea Nitrogen (BUN)", "mg/dL", "7 - 20", "Calculated"),
            ParameterTemplate("Serum Sodium (Na+)", "mmol/L", "135 - 145", "ISE"),
            ParameterTemplate("Serum Potassium (K+)", "mmol/L", "3.5 - 5.0", "ISE"),
            ParameterTemplate("Serum Chloride (Cl-)", "mmol/L", "96 - 106", "ISE")
        ),
        "LIPID" to listOf(
            ParameterTemplate("Total Cholesterol", "mg/dL", "150 - 200", "CHOD-PAP"),
            ParameterTemplate("Serum Triglycerides", "mg/dL", "60 - 150", "GPO-PAP"),
            ParameterTemplate("HDL Cholesterol (Good)", "mg/dL", "40 - 60", "Direct Homogeneous"),
            ParameterTemplate("LDL Cholesterol (Bad)", "mg/dL", "80 - 130", "Calculated"),
            ParameterTemplate("VLDL Cholesterol", "mg/dL", "10 - 30", "Calculated"),
            ParameterTemplate("Cholesterol / HDL Ratio", "Ratio", "3.3 - 5.0", "Calculated")
        ),
        "THYROID" to listOf(
            ParameterTemplate("Triiodothyronine (Total T3)", "ng/dL", "80 - 200", "CLIA"),
            ParameterTemplate("Thyroxine (Total T4)", "μg/dL", "5.1 - 14.1", "CLIA"),
            ParameterTemplate("Thyroid Stimulating Hormone (TSH)", "μIU/mL", "0.4 - 4.5", "CLIA")
        ),
        "URINE" to listOf(
            ParameterTemplate("Urine Color", "", "Pale Yellow", "Physical Exam"),
            ParameterTemplate("Urine Transparency", "", "Clear", "Physical Exam"),
            ParameterTemplate("Specific Gravity", "", "1.005 - 1.030", "Physical Exam"),
            ParameterTemplate("pH", "", "4.5 - 8.0", "Physical Exam"),
            ParameterTemplate("Urine Glucose", "", "Nil", "Chemical Exam"),
            ParameterTemplate("Urine Protein (Albumin)", "", "Nil", "Chemical Exam"),
            ParameterTemplate("Ketone Bodies", "", "Nil", "Chemical Exam"),
            ParameterTemplate("Urine Bilirubin", "", "Nil", "Chemical Exam"),
            ParameterTemplate("Blood / Hemoglobin", "", "Nil", "Chemical Exam"),
            ParameterTemplate("Urobilinogen", "", "Normal", "Chemical Exam"),
            ParameterTemplate("Nitrite", "", "Negative", "Chemical Exam"),
            ParameterTemplate("Pus Cells", "/hpf", "0 - 5", "Microscopy"),
            ParameterTemplate("Epithelial Cells", "/hpf", "0 - 5", "Microscopy"),
            ParameterTemplate("Red Blood Cells (RBCs)", "/hpf", "Nil", "Microscopy"),
            ParameterTemplate("Casts", "", "Nil", "Microscopy"),
            ParameterTemplate("Crystals", "", "Nil", "Microscopy"),
            ParameterTemplate("Bacteria", "", "Nil", "Microscopy")
        ),
        "STOOL" to listOf(
            ParameterTemplate("Stool Color", "", "Brownish", "Physical Exam"),
            ParameterTemplate("Stool Consistency", "", "Semi-Solid", "Physical Exam"),
            ParameterTemplate("Stool Mucus", "", "Absent", "Physical Exam"),
            ParameterTemplate("Stool Blood", "", "Absent", "Physical Exam"),
            ParameterTemplate("Stool pH", "", "Neutral", "Chemical Exam"),
            ParameterTemplate("Stool Occult Blood", "", "Negative", "Chemical Exam"),
            ParameterTemplate("Reducing Sugars", "", "Absent", "Chemical Exam"),
            ParameterTemplate("Protozoa / Amoeba", "", "Absent", "Microscopy"),
            ParameterTemplate("Ova / Cyst", "", "Absent", "Microscopy"),
            ParameterTemplate("Stool Pus Cells", "/hpf", "Absent", "Microscopy"),
            ParameterTemplate("Stool Red Blood Cells", "/hpf", "Absent", "Microscopy")
        ),
        "HbA1c" to listOf(
            ParameterTemplate("Glycated Hemoglobin (HbA1c)", "%", "4.5 - 5.6 (Non-Diabetic)", "HPLC"),
            ParameterTemplate("Mean Plasma Glucose", "mg/dL", "70 - 100", "Calculated")
        )
    )

    fun getTemplateForCode(code: String): List<ParameterTemplate>? {
        val upperCode = code.trim().uppercase()
        
        // Direct matching
        if (templates.containsKey(upperCode)) {
            return templates[upperCode]
        }
        
        // Semantic/fuzzy matching
        if (upperCode.contains("CBC") || upperCode.contains("HEMOGLOBIN")) {
            return templates["CBC"]
        }
        if (upperCode.contains("LFT") || upperCode.contains("LIVER") || upperCode.contains("BIL") || upperCode.contains("SGPT") || upperCode.contains("SGOT") || upperCode.contains("ALP") || upperCode.contains("PROT")) {
            return templates["LFT"]
        }
        if (upperCode.contains("KFT") || upperCode.contains("KIDNEY") || upperCode.contains("UREA") || upperCode.contains("CRE") || upperCode.contains("UA") || upperCode.contains("RENAL")) {
            return templates["KFT"]
        }
        if (upperCode.contains("LIPID") || upperCode.contains("CHOL") || upperCode.contains("TRIG") || upperCode.contains("HDL") || upperCode.contains("LDL") || upperCode.contains("HEART")) {
            return templates["LIPID"]
        }
        if (upperCode.contains("TSH") || upperCode.contains("T3") || upperCode.contains("T4") || upperCode.contains("THYROID")) {
            return templates["THYROID"]
        }
        if (upperCode.contains("URINE")) {
            return templates["URINE"]
        }
        if (upperCode.contains("STOOL")) {
            return templates["STOOL"]
        }
        if (upperCode.contains("HBA1C") || upperCode.contains("GLYCATED")) {
            return templates["HbA1c"]
        }
        
        return null
    }
}
