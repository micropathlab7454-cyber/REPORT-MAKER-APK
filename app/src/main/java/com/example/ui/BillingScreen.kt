package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabTest
import com.example.data.Patient
import com.example.data.TestPackage
import com.example.ui.theme.SoftGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: LabViewModel,
    patientsList: List<Patient>,
    testsList: List<LabTest>,
    packagesList: List<TestPackage>,
    lang: String,
    currency: String,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current

    // Selection States
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var testSearchText by remember { mutableStateOf("") }
    
    // Sample inputs
    var sampleTypeInput by remember { mutableStateOf("Blood") }
    var techInput by remember { mutableStateOf("Rahul Varma") }
    var approverInput by remember { mutableStateOf("Dr. Sameer Kapoor, MD") }

    val billingTests = viewModel.selectedTestsForBill
    val billingPackages = viewModel.selectedPackagesForBill

    // Calculated fields
    val subTotal = viewModel.getSubTotal()
    val gstAmt = viewModel.getGSTAmount()
    val finalTotal = viewModel.getFinalTotal()
    val dueAmt = viewModel.getDueAmount()

    val filteredTests = testsList.filter { t ->
        t.name.lowercase().contains(testSearchText.lowercase()) ||
        t.code.lowercase().contains(testSearchText.lowercase()) ||
        t.category.lowercase().contains(testSearchText.lowercase())
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT COLUMN: Search & Selector pane (60% width on tablet, 100% on mobile if simplified)
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp)
        ) {
            Text(
                text = t("Select Patient & Diagnostics", "मरीज और टेस्ट चुनें", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Patient dropdown/selector
            var patientExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedButton(
                    onClick = { patientExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedPatient?.let { "${it.name} (${it.patientId})" }
                                ?: t("Choose Patient (Required)...", "मरीज चुनें...", lang),
                            fontSize = 13.sp
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = patientExpanded,
                    onDismissRequest = { patientExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
                ) {
                    patientsList.forEach { patient ->
                        DropdownMenuItem(
                            text = { Text("${patient.name} (${patient.mobile})", fontSize = 13.sp) },
                            onClick = {
                                selectedPatient = patient
                                patientExpanded = false
                            }
                        )
                    }
                }
            }

            // Tabs / Selector search
            OutlinedTextField(
                value = testSearchText,
                onValueChange = { testSearchText = it },
                placeholder = { Text(t("Search Pathology Tests or Packages...", "पैथोलॉजी टेस्ट या पैकेज खोजें...", lang), fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            Text(
                text = t("Pathology Tests List", "पैथोलॉजी टेस्ट सूची", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SoftGrey,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                // Seed Packages in search results if matched
                val matchedPackages = packagesList.filter { p -> p.name.lowercase().contains(testSearchText.lowercase()) }
                if (matchedPackages.isNotEmpty()) {
                    item {
                        Text(t("Test Packages", "टेस्ट पैकेज", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    items(matchedPackages) { pkg ->
                        PackageSelectRow(
                            pkg = pkg,
                            currency = currency,
                            isSelected = billingPackages.contains(pkg),
                            onSelect = {
                                if (billingPackages.contains(pkg)) {
                                    viewModel.removePackageFromBilling(pkg)
                                } else {
                                    viewModel.selectPackageForBilling(pkg)
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }

                item {
                    Text(t("Individual Tests", "व्यक्तिगत टेस्ट", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                items(filteredTests) { test ->
                    TestSelectRow(
                        test = test,
                        currency = currency,
                        isSelected = billingTests.contains(test),
                        onSelect = {
                            if (billingTests.contains(test)) {
                                viewModel.removeTestFromBilling(test)
                            } else {
                                viewModel.selectTestForBilling(test)
                            }
                        }
                    )
                }
            }
        }

        // RIGHT COLUMN: Cart Summary, pricing calculations, sample collection fields
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = t("Billing Invoice Summary", "बिलिंग सारांश", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Display selected items list
            if (billingTests.isEmpty() && billingPackages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        t("Cart is empty. Select tests/packages.", "कार्ट खाली है। टेस्ट चुनें।", lang),
                        fontSize = 12.sp,
                        color = SoftGrey
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        billingPackages.forEach { pkg ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("[Pkg] ${pkg.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("$currency${pkg.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.removePackageFromBilling(pkg) }
                                )
                            }
                        }
                        billingTests.forEach { test ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(test.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("$currency${test.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.removeTestFromBilling(test) }
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Pricing calculator inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t("Apply Discount ($currency)", "छूट जोड़ें", lang), fontSize = 12.sp)
                var discountText by remember { mutableStateOf("0") }
                OutlinedTextField(
                    value = discountText,
                    onValueChange = {
                        discountText = it
                        viewModel.billDiscount.value = it.toDoubleOrNull() ?: 0.0
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t("Include 18% GST", "18% जीएसटी शामिल करें", lang), fontSize = 12.sp)
                Switch(
                    checked = viewModel.billGstEnabled.value,
                    onCheckedChange = { viewModel.billGstEnabled.value = it }
                )
            }

            // Total Amounts Breakdowns
            PriceRow(t("Subtotal", "उप-योग", lang), "$currency$subTotal")
            if (viewModel.billGstEnabled.value) PriceRow("GST (18%)", "$currency$gstAmt")
            if (viewModel.billDiscount.value > 0) PriceRow(t("Discount", "छूट", lang), "- $currency${viewModel.billDiscount.value}")
            PriceRow(t("Final Net Amount", "कुल देय राशि", lang), "$currency$finalTotal", isBold = true)

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Paid and payment modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t("Amount Paid ($currency)", "भुगतान की गई राशि", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                var paidText by remember { mutableStateOf("0") }
                OutlinedTextField(
                    value = paidText,
                    onValueChange = {
                        paidText = it
                        viewModel.billPaidAmount.value = it.toDoubleOrNull() ?: 0.0
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
            }
            PriceRow(t("Remaining Due Balance", "बकाया राशि", lang), "$currency$dueAmt", isBold = true, textColor = MaterialTheme.colorScheme.error)

            // Payment Mode Select
            Text(text = t("Payment Mode", "भुगतान का प्रकार", lang), fontSize = 11.sp, color = SoftGrey, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Cash", "Card", "UPI").forEach { mode ->
                    FilterChip(
                        selected = viewModel.billPaymentMode.value == mode,
                        onClick = { viewModel.billPaymentMode.value = mode },
                        label = { Text(t(mode, if (mode=="Cash") "नकद" else if (mode=="Card") "कार्ड" else "यूपीआई", lang), fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Sample collection inputs
            Text(t("Sample Collection Info", "नमूना संग्रह जानकारी", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
            
            var sampleExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                OutlinedButton(onClick = { sampleExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(t(sampleTypeInput, when(sampleTypeInput) {
                            "Blood" -> "रक्त"
                            "Urine" -> "मूत्र"
                            "Stool" -> "मल"
                            "Sputum" -> "बलगम"
                            "Semen" -> "वीर्य"
                            "Swab" -> "स्वाब"
                            else -> "अन्य"
                        }, lang), fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = sampleExpanded, onDismissRequest = { sampleExpanded = false }) {
                    listOf("Blood", "Urine", "Stool", "Sputum", "Semen", "Swab", "Other").forEach { sm ->
                        DropdownMenuItem(
                            text = { Text(sm) },
                            onClick = {
                                sampleTypeInput = sm
                                sampleExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = techInput,
                onValueChange = { techInput = it },
                label = { Text(t("Technician Name", "तकनीशियन", lang)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            OutlinedTextField(
                value = approverInput,
                onValueChange = { approverInput = it },
                label = { Text(t("Approver Pathologist", "अनुमोदक रोगविज्ञानी", lang)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            Button(
                onClick = {
                    if (selectedPatient == null) {
                        ToastHelper.showToast(context, "Please select a Patient first!")
                        return@Button
                    }
                    if (billingTests.isEmpty() && billingPackages.isEmpty()) {
                        ToastHelper.showToast(context, "Please add tests or packages to bill!")
                        return@Button
                    }
                    viewModel.generateBillAndReport(
                        patient = selectedPatient!!,
                        sampleType = sampleTypeInput,
                        technician = techInput,
                        approvedBy = approverInput,
                        onComplete = { reportId ->
                            ToastHelper.showToast(context, "Invoice and Report Entry Generated!", Toast.LENGTH_LONG)
                            viewModel.activeReportId.value = reportId
                            onNavigate(Screen.Reports)
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("Complete Registration & Bill", "पंजीकरण और बिल पूरा करें", lang))
            }
        }
    }
}

@Composable
fun TestSelectRow(
    test: LabTest,
    currency: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onSelect() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(test.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${test.code} | ${test.category}", fontSize = 10.sp, color = SoftGrey)
            }
            Text("$currency${test.price}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PackageSelectRow(
    pkg: TestPackage,
    currency: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onSelect() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(pkg.testCodes, fontSize = 9.sp, color = SoftGrey, maxLines = 1)
            }
            Text("$currency${pkg.price}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PriceRow(label: String, value: String, isBold: Boolean = false, textColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 11.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = textColor)
    }
}
