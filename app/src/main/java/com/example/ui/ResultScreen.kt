package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Report
import com.example.data.ReportResult
import com.example.data.MpWidalData
import com.example.data.MpWidalSerializer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SoftGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: LabViewModel,
    reportsList: List<Report>,
    lang: String,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.reportSearchQuery.collectAsState()

    // Screen-level navigation: list reports vs enter results for one report
    var activeEditingReport by remember { mutableStateOf<Report?>(null) }
    val resultsListState = remember { mutableStateListOf<ReportResult>() }
    var isViewingPdf by remember { mutableStateOf(false) }

    // Synchronize selected report ID
    val activeReportIdFromVm by viewModel.activeReportId

    if (activeReportIdFromVm != null && isViewingPdf) {
        ReportPdfScreen(
            viewModel = viewModel,
            reportId = activeReportIdFromVm!!,
            reportsList = reportsList,
            lang = lang,
            currency = "₹",
            onBack = {
                viewModel.activeReportId.value = null
                isViewingPdf = false
            }
        )
        return
    }

    LaunchedEffect(activeReportIdFromVm, reportsList, isViewingPdf) {
        if (activeReportIdFromVm != null && !isViewingPdf) {
            val rep = reportsList.find { it.id == activeReportIdFromVm }
            if (rep != null) {
                activeEditingReport = rep
                // Fetch existing results
                viewModel.getResultsForReport(rep.id).collect { results ->
                    resultsListState.clear()
                    resultsListState.addAll(results)
                    viewModel.prepareResultsEntry(rep.id, results)
                }
            }
        }
    }

    val filteredReports = reportsList.filter { r ->
        r.reportNo.lowercase().contains(searchQuery.lowercase()) ||
        r.patientName.lowercase().contains(searchQuery.lowercase()) ||
        r.sampleBarcode.lowercase().contains(searchQuery.lowercase())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (activeEditingReport == null) {
            // LIST REPORTS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.reportSearchQuery.value = it },
                    placeholder = { Text(t("Search Reports (Patient Name, Report No, Barcode)", "रिपोर्ट खोजें (मरीज का नाम, संख्या, बारकोड)", lang), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }

            if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(t("No Lab Reports Generated", "कोई लैब रिपोर्ट नहीं मिली", lang), color = SoftGrey, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    items(filteredReports) { report ->
                        ReportListItem(
                            report = report,
                            lang = lang,
                            onEnterResults = {
                                isViewingPdf = false
                                viewModel.activeReportId.value = report.id
                            },
                            onViewPdf = {
                                isViewingPdf = true
                                viewModel.activeReportId.value = report.id
                            }
                        )
                    }
                }
            }
        } else {
            // ENTER RESULTS FORM FOR SINGLE REPORT
            val report = activeEditingReport!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(t("Report Results Entry", "परिणाम प्रविष्टि", lang), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text("${report.patientName} (${report.patientId}) | ${report.reportNo}", fontSize = 11.sp, color = SoftGrey)
                    }
                    IconButton(onClick = {
                        activeEditingReport = null
                        viewModel.activeReportId.value = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient metadata banner
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = t("Age/Gender: ", "आयु/लिंग: ", lang) + "${report.patientAge}/${report.patientGender}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = t("Sample: ", "नमूना: ", lang) + report.sampleType, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = t("Barcode: ", "बारकोड: ", lang) + report.sampleBarcode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Results list inputs
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(resultsListState, key = { it.id }) { result ->
                        var localVal by remember(result.id) {
                            val existing = viewModel.resultsDraftMap[result.id] ?: ""
                            val initial = if (result.testName == "MP + Widal Test" && (existing.isBlank() || !existing.startsWith("{"))) {
                                MpWidalSerializer.serialize(MpWidalData())
                            } else {
                                existing
                            }
                            if (result.testName == "MP + Widal Test") {
                                viewModel.resultsDraftMap[result.id] = initial
                            }
                            mutableStateOf(initial)
                        }
                        var localRemarks by remember(result.id) { mutableStateOf(viewModel.resultsRemarksMap[result.id] ?: "") }

                        if (result.testName == "MP + Widal Test") {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(result.testName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MpWidalInputForm(
                                        initialValue = localVal,
                                        onValueChange = { newVal ->
                                            localVal = newVal
                                            viewModel.resultsDraftMap[result.id] = newVal
                                            viewModel.updateSingleReportResult(result, newVal, localRemarks, report.patientAge, report.patientGender)
                                        },
                                        lang = lang
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(result.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    val resolvedRange = resolveReferenceRange(result.normalRange, report.patientAge, report.patientGender)
                                    val status = evaluateResultStatus(localVal, resolvedRange)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = localVal,
                                            onValueChange = {
                                                localVal = it
                                                viewModel.resultsDraftMap[result.id] = it
                                                viewModel.updateSingleReportResult(result, it, localRemarks, report.patientAge, report.patientGender)
                                            },
                                            placeholder = { Text(t("Result Value", "मूल्य दर्ज करें", lang), fontSize = 11.sp) },
                                            modifier = Modifier.weight(1.0f),
                                            shape = MaterialTheme.shapes.small,
                                            singleLine = true
                                        )
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(text = "${t("Unit", "इकाई", lang)}: ${result.unit}", fontSize = 10.sp, color = SoftGrey)
                                            Text(text = "${t("Range", "सीमा", lang)}: $resolvedRange", fontSize = 10.sp, color = SoftGrey, fontWeight = FontWeight.Bold)
                                            if (localVal.trim().isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val prefix = when (status) {
                                                        ValueStatus.HIGH -> "↑ "
                                                        ValueStatus.LOW -> "↓ "
                                                        else -> ""
                                                    }
                                                    val statusText = when (status) {
                                                        ValueStatus.HIGH -> t("Above Range", "सीमा से ऊपर", lang)
                                                        ValueStatus.LOW -> t("Below Range", "सीमा से नीचे", lang)
                                                        else -> t("Normal", "सामान्य", lang)
                                                    }
                                                    val statusColor = when (status) {
                                                        ValueStatus.HIGH, ValueStatus.LOW -> AlertCrimson
                                                        else -> SafeEmerald
                                                    }
                                                    Text(
                                                        text = "$prefix$statusText",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = localRemarks,
                                        onValueChange = {
                                            localRemarks = it
                                            viewModel.resultsRemarksMap[result.id] = it
                                            viewModel.updateSingleReportResult(result, localVal, it, report.patientAge, report.patientGender)
                                        },
                                        placeholder = { Text(t("Test Remarks/Observations", "टिप्पणियां", lang), fontSize = 10.sp) },
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                        shape = MaterialTheme.shapes.small,
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveReportResults(report, resultsListState, isFinalSubmit = false) {
                                ToastHelper.showToast(context, "Draft saved successfully!")
                                activeEditingReport = null
                                viewModel.activeReportId.value = null
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(t("Save Draft", "ड्राफ्ट सुरक्षित करें", lang))
                    }

                    Button(
                        onClick = {
                            viewModel.saveReportResults(report, resultsListState, isFinalSubmit = true) {
                                ToastHelper.showToast(context, "Report Approved and Signed Digitally!")
                                activeEditingReport = null
                                viewModel.activeReportId.value = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(t("Approve Report", "अनुमोदित करें", lang))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportListItem(
    report: Report,
    lang: String,
    onEnterResults: () -> Unit,
    onViewPdf: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(report.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${report.reportNo} | ${report.patientId} | Barcode: ${report.sampleBarcode}", fontSize = 11.sp, color = SoftGrey)
                }
                val statColor = when (report.sampleStatus) {
                    "Completed" -> SafeEmerald
                    "In Process" -> PendingAmber
                    else -> SoftGrey
                }
                Box(
                    modifier = Modifier
                        .background(statColor.copy(alpha = 0.15f), MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(t(report.sampleStatus, if (report.sampleStatus=="Completed") "पूर्ण" else if (report.sampleStatus=="In Process") "प्रक्रिया में" else "संग्रहित", lang), color = statColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${t("Sample", "नमूना", lang)}: ${report.sampleType}", fontSize = 11.sp)
                    Text("${t("Referring Doctor", "डॉक्टर", lang)}: ${report.referringDoctor}", fontSize = 11.sp, color = SoftGrey)
                }

                Row {
                    Button(
                        onClick = onEnterResults,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(t("Results", "परिणाम दर्ज करें", lang), fontSize = 11.sp)
                    }
                    if (report.sampleStatus == "Completed") {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onViewPdf,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(t("View PDF", "रिपोर्ट देखें", lang), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MpWidalInputForm(
    initialValue: String,
    onValueChange: (String) -> Unit,
    lang: String
) {
    val data = remember(initialValue) { MpWidalSerializer.deserialize(initialValue) }
    
    fun updateData(updated: MpWidalData) {
        onValueChange(MpWidalSerializer.serialize(updated))
    }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = "Section 1 – Malaria Antigen",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("P. Falciparum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                DropdownSelector(
                    selected = data.pf,
                    options = listOf("Negative", "Positive"),
                    onSelected = { updateData(data.copy(pf = it)) }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("P. Vivax", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                DropdownSelector(
                    selected = data.pv,
                    options = listOf("Negative", "Positive"),
                    onSelected = { updateData(data.copy(pv = it)) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Section 2 – Widal Test",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        val columns = listOf("1:20", "1:40", "1:80", "1:160", "1:320")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            columns.forEach { col ->
                Text(col, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
        
        WidalRowInput("Typhi (O)", data.typhiO, columns.size) { index, valStr ->
            val newList = data.typhiO.toMutableList()
            newList[index] = valStr
            updateData(data.copy(typhiO = newList))
        }
        WidalRowInput("Typhi (H)", data.typhiH, columns.size) { index, valStr ->
            val newList = data.typhiH.toMutableList()
            newList[index] = valStr
            updateData(data.copy(typhiH = newList))
        }
        WidalRowInput("Typhi (AH)", data.typhiAH, columns.size) { index, valStr ->
            val newList = data.typhiAH.toMutableList()
            newList[index] = valStr
            updateData(data.copy(typhiAH = newList))
        }
        WidalRowInput("Typhi (BH)", data.typhiBH, columns.size) { index, valStr ->
            val newList = data.typhiBH.toMutableList()
            newList[index] = valStr
            updateData(data.copy(typhiBH = newList))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text("Final Result", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                DropdownSelector(
                    selected = data.finalResult,
                    options = listOf("Negative", "Positive"),
                    onSelected = { updateData(data.copy(finalResult = it)) }
                )
            }
            Column(modifier = Modifier.weight(1.5f)) {
                Text("Interpretation/Remarks (Optional)", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = data.remarks,
                    onValueChange = { updateData(data.copy(remarks = it)) },
                    placeholder = { Text("Enter remarks", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun WidalRowInput(
    rowLabel: String,
    cellValues: List<String>,
    colCount: Int,
    onCellChange: (Int, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rowLabel, modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        for (i in 0 until colCount) {
            val currentVal = cellValues.getOrElse(i) { "-" }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (currentVal == "+") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        val nextVal = if (currentVal == "+") "-" else "+"
                        onCellChange(i, nextVal)
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentVal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (currentVal == "+") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(text = selectionOption) },
                    onClick = {
                        onSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}
