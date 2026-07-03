package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Report
import com.example.data.ReportResult
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SoftGrey
import kotlinx.coroutines.launch
import android.print.PrintAttributes
import android.print.PrintManager
import android.print.PrintDocumentAdapter
import android.os.ParcelFileDescriptor
import android.os.Bundle
import android.print.PageRange
import java.io.FileInputStream
import java.io.File
import java.io.FileOutputStream
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.print.PrintDocumentInfo
import android.content.Context
import androidx.compose.foundation.clickable
import com.example.data.MpWidalSerializer
import com.example.data.MpWidalData

@Composable
fun ReportPdfScreen(
    viewModel: LabViewModel,
    reportId: Int,
    reportsList: List<Report>,
    lang: String,
    currency: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val report = reportsList.find { it.id == reportId }

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Report not found!")
        }
        return
    }

    val patientsList by viewModel.patients.collectAsState()
    val currentPatient = patientsList.find { it.id == report.patientId }

    val reportResultsState = remember { mutableStateListOf<ReportResult>() }
    var isPreviewMode by remember { mutableStateOf(true) }
    val localDrafts = remember { mutableStateMapOf<Int, String>() }
    val localRemarks = remember { mutableStateMapOf<Int, String>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reportId) {
        viewModel.getResultsForReport(reportId).collect { results ->
            reportResultsState.clear()
            reportResultsState.addAll(results)
            results.forEach {
                localDrafts[it.id] = it.resultValue
                localRemarks[it.id] = it.remarks
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar with Back, Share, and Print actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = t("Report No: ", "रिपोर्ट संख्या: ", lang) + report.reportNo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ClinicalNavy
            )
            Row {
                IconButton(onClick = {
                    ToastHelper.showToast(context, "Printing Document...")
                }) {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = ClinicalNavy)
                }
                IconButton(onClick = {
                    ToastHelper.showToast(context, "Sharing PDF Report via WhatsApp...")
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = ClinicalNavy)
                }
            }
        }

        // A4 Paper Simulator Sheet (Scrollable)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (isPreviewMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    // Proper Top Margin to align perfectly when printed on pre-printed letterhead (1.5 - 2 inches)
                    Spacer(modifier = Modifier.height(140.dp))

                    // Patient and Doctor Meta Details Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            PatientMetaItem(label = t("Patient Name", "मरीज का नाम", lang), value = currentPatient?.name ?: report.patientName, isBold = true)
                            PatientMetaItem(label = t("Patient ID", "मरीज आईडी", lang), value = currentPatient?.patientId ?: report.patientId.toString())
                            PatientMetaItem(label = t("Age/Gender", "आयु/लिंग", lang), value = "${currentPatient?.age ?: report.patientAge} Yrs / ${currentPatient?.gender ?: report.patientGender}")
                        }
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.End
                        ) {
                            PatientMetaItem(label = t("Sample Date", "नमूना तारीख", lang), value = SimpleDateFormatEasy(report.collectionTime), alignRight = true)
                            PatientMetaItem(label = t("Report Date", "रिपोर्ट तारीख", lang), value = SimpleDateFormatEasy(report.dateCreated), alignRight = true)
                            PatientMetaItem(label = t("Referring Doctor", "संदर्भित डॉक्टर", lang), value = currentPatient?.referringDoctor ?: report.referringDoctor, isBold = true, alignRight = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Pathology Results Table Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ClinicalNavy)
                            .padding(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(t("TEST DESCRIPTION", "परीक्षण का नाम", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2.2f))
                            Text(t("RESULT", "परिणाम", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                            Text(t("NORMAL RANGE", "सामान्य सीमा", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test Results Rows with automatic High/Low flagging prefix
                    if (reportResultsState.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No results loaded.", color = SoftGrey, fontSize = 12.sp)
                        }
                    } else {
                        Column {
                            reportResultsState.forEach { result ->
                                if (result.testName == "MP + Widal Test") {
                                    MpWidalReportView(result = result, lang = lang)
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Test Name
                                        Text(
                                            text = result.testName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.weight(2.2f)
                                        )

                                        // Resolve patient-specific reference range (using age and gender)
                                        val resolvedRange = resolveReferenceRange(result.normalRange, report.patientAge, report.patientGender)
                                        val status = evaluateResultStatus(result.resultValue, resolvedRange)

                                        // Result value with High/Low indicators as prefix
                                        val isAbnormal = status != ValueStatus.NORMAL
                                        val resultColor = if (isAbnormal) AlertCrimson else Color.Black
                                        val textWeight = if (isAbnormal) FontWeight.Black else FontWeight.Medium
                                        val prefix = when (status) {
                                            ValueStatus.HIGH -> "↑ "
                                            ValueStatus.LOW -> "↓ "
                                            else -> ""
                                        }

                                        Row(
                                            modifier = Modifier.weight(1.2f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$prefix${result.resultValue} ${result.unit}",
                                                fontSize = 11.sp,
                                                fontWeight = textWeight,
                                                color = resultColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Normal range (resolved age-wise/gender-wise)
                                        Text(
                                            text = "$resolvedRange ${result.unit}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    if (result.remarks.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, bottom = 6.dp)
                                        ) {
                                            Text(
                                                text = "Remarks: ${result.remarks}",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }

                    // Proper Bottom Margin for Pre-printed Letterhead (no footer details, stamps, watermarks or QR codes)
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "✏ Edit Report Values",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ClinicalNavy,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    reportResultsState.forEach { result ->
                        var currentVal by remember(result.id) { mutableStateOf(localDrafts[result.id] ?: result.resultValue) }
                        var currentRemarks by remember(result.id) { mutableStateOf(localRemarks[result.id] ?: result.remarks) }

                        if (result.testName == "MP + Widal Test") {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(result.testName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ClinicalNavy)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MpWidalInputForm(
                                        initialValue = currentVal,
                                        onValueChange = { newVal ->
                                            currentVal = newVal
                                            localDrafts[result.id] = newVal
                                            viewModel.updateSingleReportResult(result, newVal, currentRemarks, report.patientAge, report.patientGender)
                                        },
                                        lang = lang
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(result.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ClinicalNavy)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = currentVal,
                                        onValueChange = {
                                            currentVal = it
                                            localDrafts[result.id] = it
                                            viewModel.updateSingleReportResult(result, it, currentRemarks, report.patientAge, report.patientGender)
                                        },
                                        label = { Text("Result Value") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = currentRemarks,
                                        onValueChange = {
                                            currentRemarks = it
                                            localRemarks[result.id] = it
                                            viewModel.updateSingleReportResult(result, currentVal, it, report.patientAge, report.patientGender)
                                        },
                                        label = { Text("Remarks") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Print, Download, Share, WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val file = ReportPdfGenerator.generatePdf(context, report, reportResultsState, viewModel.labName.value, currentPatient)
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        val jobName = "MPL Report ${report.reportNo}"
                        printManager.print(jobName, PdfPrintAdapter(file), PrintAttributes.Builder().build())
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🖨 Print", fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        val file = ReportPdfGenerator.generatePdf(context, report, reportResultsState, viewModel.labName.value, currentPatient)
                        ToastHelper.showToast(context, "Saved: ${file.absolutePath}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "📥 Download", fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        val file = ReportPdfGenerator.generatePdf(context, report, reportResultsState, viewModel.labName.value, currentPatient)
                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.fileprovider", file)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Report PDF"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "📤 Share", fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        val file = ReportPdfGenerator.generatePdf(context, report, reportResultsState, viewModel.labName.value, currentPatient)
                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.fileprovider", file)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            putExtra("jid", "${report.patientMobile}@s.whatsapp.net")
                            setPackage("com.whatsapp")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via WhatsApp"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🟢 WhatsApp", fontSize = 11.sp, maxLines = 1)
                }
            }

            // Row 2: Preview Report, Edit Report, Save Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { isPreviewMode = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "👁 Preview", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { isPreviewMode = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "✏ Edit", fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val updatedList = reportResultsState.map {
                                it.copy(
                                    resultValue = localDrafts[it.id] ?: it.resultValue,
                                    remarks = localRemarks[it.id] ?: it.remarks
                                )
                            }
                            viewModel.saveReportResults(report, updatedList, isFinalSubmit = true) {
                                ToastHelper.showToast(context, "Report saved successfully!")
                                isPreviewMode = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "💾 Save", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun PatientMetaItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    alignRight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start
    ) {
        Text(text = "$label: ", fontSize = 10.sp, color = Color.Gray, textAlign = if (alignRight) TextAlign.End else TextAlign.Start)
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = Color.Black,
            textAlign = if (alignRight) TextAlign.End else TextAlign.Start
        )
    }
}

fun SimpleDateFormatEasy(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}

@Composable
fun MpWidalReportView(result: ReportResult, lang: String) {
    val data = remember(result.resultValue) { MpWidalSerializer.deserialize(result.resultValue) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Section 1 - Malaria Antigen
        Text(
            text = "SECTION 1 – MALARIA ANTIGEN (Rapid Card)",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = ClinicalNavy,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray)
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("P. Falciparum", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(data.pf, modifier = Modifier.weight(1f), fontSize = 11.sp, color = if (data.pf == "Positive") AlertCrimson else Color.Black, fontWeight = FontWeight.Bold)
                Text("Negative", modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = Color.Gray)
            }
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("P. Vivax", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(data.pv, modifier = Modifier.weight(1f), fontSize = 11.sp, color = if (data.pv == "Positive") AlertCrimson else Color.Black, fontWeight = FontWeight.Bold)
                Text("Negative", modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2 - Widal Test
        Text(
            text = "SECTION 2 – WIDAL TEST (Slide Agglutination)",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = ClinicalNavy,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray)
        ) {
            val cols = listOf("1:20", "1:40", "1:80", "1:160", "1:320")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Antigen / Dilutions", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                cols.forEach { col ->
                    Text(col, modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Divider(color = Color.LightGray, thickness = 1.dp)

            WidalReportRow("Typhi (O)", data.typhiO)
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            WidalReportRow("Typhi (H)", data.typhiH)
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            WidalReportRow("Typhi (AH)", data.typhiAH)
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            WidalReportRow("Typhi (BH)", data.typhiBH)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.15f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FINAL RESULT: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ClinicalNavy)
                Text(
                    text = data.finalResult.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = if (data.finalResult == "Positive") AlertCrimson else SafeEmerald
                )
            }
            if (data.remarks.isNotBlank()) {
                Text(
                    text = "Remarks: ${data.remarks}",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun WidalReportRow(label: String, values: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        for (i in 0 until 5) {
            val cellVal = values.getOrElse(i) { "-" }
            Text(
                text = cellVal,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (cellVal == "+") AlertCrimson else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

class PdfPrintAdapter(private val pdfFile: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val pdi = PrintDocumentInfo.Builder("${pdfFile.name}")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback?.onLayoutFinished(pdi, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        var input: FileInputStream? = null
        var output: FileOutputStream? = null
        try {
            input = FileInputStream(pdfFile)
            output = FileOutputStream(destination?.fileDescriptor)
            val buf = ByteArray(16384)
            var bytesRead: Int
            while (input.read(buf).also { bytesRead = it } >= 0) {
                output.write(buf, 0, bytesRead)
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        } finally {
            input?.close()
            output?.close()
        }
    }
}

object ReportPdfGenerator {
    fun generatePdf(context: Context, report: Report, results: List<ReportResult>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        paint.isAntiAlias = true

        var y = 140f

        paint.color = android.graphics.Color.parseColor("#0F172A")
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("MICRO PATHOLOGY LABORATORY", 40f, y, paint)
        y += 20f

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Quality Diagnostics | Digital Health Records", 40f, y, paint)
        y += 20f

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 15f

        paint.textSize = 9f
        paint.color = android.graphics.Color.BLACK
        
        paint.isFakeBoldText = true
        canvas.drawText("Patient Name: ", 40f, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(report.patientName, 110f, y, paint)
        y += 14f

        paint.isFakeBoldText = true
        canvas.drawText("Patient ID: ", 40f, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(report.patientId.toString(), 110f, y, paint)
        y += 14f

        paint.isFakeBoldText = true
        canvas.drawText("Age/Gender: ", 40f, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText("${report.patientAge} Yrs / ${report.patientGender}", 110f, y, paint)

        var yRight = y - 28f
        paint.isFakeBoldText = true
        canvas.drawText("Sample Date: ", 340f, yRight, paint)
        paint.isFakeBoldText = false
        canvas.drawText(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(report.collectionTime)), 415f, yRight, paint)
        yRight += 14f

        paint.isFakeBoldText = true
        canvas.drawText("Report Date: ", 340f, yRight, paint)
        paint.isFakeBoldText = false
        canvas.drawText(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(report.dateCreated)), 415f, yRight, paint)
        yRight += 14f

        paint.isFakeBoldText = true
        canvas.drawText("Ref. Doctor: ", 340f, yRight, paint)
        paint.isFakeBoldText = false
        canvas.drawText(report.referringDoctor, 415f, yRight, paint)

        y += 15f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        paint.color = android.graphics.Color.parseColor("#0F172A")
        canvas.drawRect(40f, y - 12f, 555f, y + 8f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("TEST DESCRIPTION", 50f, y, paint)
        canvas.drawText("RESULT", 300f, y, paint)
        canvas.drawText("NORMAL RANGE", 440f, y, paint)
        y += 20f

        results.forEach { result ->
            if (result.testName == "MP + Widal Test") {
                val data = MpWidalSerializer.deserialize(result.resultValue)
                
                paint.color = android.graphics.Color.parseColor("#0F172A")
                paint.textSize = 10f
                paint.isFakeBoldText = true
                canvas.drawText("SECTION 1 – MALARIA ANTIGEN (Rapid Card)", 45f, y, paint)
                y += 15f

                paint.color = android.graphics.Color.BLACK
                paint.textSize = 9f
                
                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawRect(45f, y - 10f, 550f, y + 25f, paint)
                paint.style = Paint.Style.FILL

                paint.color = android.graphics.Color.BLACK
                paint.isFakeBoldText = true
                canvas.drawText("P. Falciparum", 55f, y, paint)
                paint.isFakeBoldText = false
                canvas.drawText("Negative", 450f, y, paint)
                if (data.pf == "Positive") {
                    paint.color = android.graphics.Color.RED
                    paint.isFakeBoldText = true
                }
                canvas.drawText(data.pf, 300f, y, paint)
                paint.color = android.graphics.Color.BLACK
                paint.isFakeBoldText = false
                y += 15f

                paint.isFakeBoldText = true
                canvas.drawText("P. Vivax", 55f, y, paint)
                paint.isFakeBoldText = false
                canvas.drawText("Negative", 450f, y, paint)
                if (data.pv == "Positive") {
                    paint.color = android.graphics.Color.RED
                    paint.isFakeBoldText = true
                }
                canvas.drawText(data.pv, 300f, y, paint)
                paint.color = android.graphics.Color.BLACK
                paint.isFakeBoldText = false
                y += 25f

                paint.color = android.graphics.Color.parseColor("#0F172A")
                paint.textSize = 10f
                paint.isFakeBoldText = true
                canvas.drawText("SECTION 2 – WIDAL TEST (Slide Agglutination)", 45f, y, paint)
                y += 15f

                paint.color = android.graphics.Color.parseColor("#F1F5F9")
                canvas.drawRect(45f, y - 10f, 550f, y + 5f, paint)
                paint.color = android.graphics.Color.BLACK
                paint.textSize = 8f
                paint.isFakeBoldText = true
                canvas.drawText("Antigen / Dilutions", 55f, y, paint)
                
                val dilutionHeaders = listOf("1:20", "1:40", "1:80", "1:160", "1:320")
                dilutionHeaders.forEachIndexed { i, dil ->
                    canvas.drawText(dil, 220f + (i * 65f), y, paint)
                }
                y += 15f

                paint.style = Paint.Style.STROKE
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawRect(45f, y - 25f, 550f, y + 60f, paint)
                paint.style = Paint.Style.FILL

                val rows = listOf(
                    "Typhi (O)" to data.typhiO,
                    "Typhi (H)" to data.typhiH,
                    "Typhi (AH)" to data.typhiAH,
                    "Typhi (BH)" to data.typhiBH
                )

                paint.color = android.graphics.Color.BLACK
                paint.textSize = 8f
                rows.forEach { (label, cellValues) ->
                    paint.isFakeBoldText = true
                    canvas.drawText(label, 55f, y, paint)
                    paint.isFakeBoldText = false
                    cellValues.forEachIndexed { i, valStr ->
                        if (valStr == "+") {
                            paint.color = android.graphics.Color.RED
                            paint.isFakeBoldText = true
                        } else {
                            paint.color = android.graphics.Color.BLACK
                        }
                        canvas.drawText(valStr, 230f + (i * 65f), y, paint)
                        paint.color = android.graphics.Color.BLACK
                        paint.isFakeBoldText = false
                    }
                    y += 14f
                }

                y += 10f
                paint.color = android.graphics.Color.parseColor("#F8FAFC")
                canvas.drawRect(45f, y - 10f, 550f, y + 10f, paint)
                paint.color = android.graphics.Color.parseColor("#0F172A")
                paint.textSize = 9f
                paint.isFakeBoldText = true
                canvas.drawText("FINAL RESULT: ", 55f, y, paint)
                
                if (data.finalResult == "Positive") paint.color = android.graphics.Color.RED else paint.color = android.graphics.Color.parseColor("#10B981")
                canvas.drawText(data.finalResult.uppercase(), 140f, y, paint)

                if (data.remarks.isNotBlank()) {
                    paint.color = android.graphics.Color.DKGRAY
                    paint.isFakeBoldText = false
                    canvas.drawText("Remarks: ${data.remarks}", 260f, y, paint)
                }
                y += 25f
            } else {
                paint.color = android.graphics.Color.BLACK
                paint.textSize = 9f
                paint.isFakeBoldText = true
                canvas.drawText(result.testName, 50f, y, paint)

                val resolvedRange = resolveReferenceRange(result.normalRange, report.patientAge, report.patientGender)
                val status = evaluateResultStatus(result.resultValue, resolvedRange)
                val isAbnormal = status != ValueStatus.NORMAL
                
                val prefix = when (status) {
                    ValueStatus.HIGH -> "↑ "
                    ValueStatus.LOW -> "↓ "
                    else -> ""
                }
                
                if (isAbnormal) {
                    paint.color = android.graphics.Color.RED
                    paint.isFakeBoldText = true
                } else {
                    paint.color = android.graphics.Color.BLACK
                    paint.isFakeBoldText = false
                }
                
                canvas.drawText("$prefix${result.resultValue} ${result.unit}", 300f, y, paint)
                
                paint.color = android.graphics.Color.DKGRAY
                paint.isFakeBoldText = false
                canvas.drawText("$resolvedRange ${result.unit}", 440f, y, paint)
                y += 14f

                if (result.remarks.isNotBlank()) {
                    paint.color = android.graphics.Color.GRAY
                    canvas.drawText("Remarks: ${result.remarks}", 60f, y, paint)
                    y += 14f
                }

                paint.color = android.graphics.Color.parseColor("#E2E8F0")
                canvas.drawLine(40f, y - 5f, 555f, y - 5f, paint)
                y += 10f
            }
        }

        y = 740f
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Digitally Signed By:", 400f, y, paint)
        y += 14f
        paint.isFakeBoldText = false
        canvas.drawText("Dr. S. K. Sharma, MD", 400f, y, paint)
        y += 12f
        canvas.drawText("Consultant Pathologist", 400f, y, paint)

        pdfDocument.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val mplDir = File(downloadsDir, "MPL Reports")
        if (!mplDir.exists()) mplDir.mkdirs()
        val file = File(mplDir, "${report.reportNo.replace("-", "_")}.pdf")
        val out = FileOutputStream(file)
        pdfDocument.writeTo(out)
        out.close()
        pdfDocument.close()

        return file
    }
}
