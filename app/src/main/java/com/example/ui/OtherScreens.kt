package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SoftGrey

// --- 1. LOGIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LabViewModel,
    lang: String
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.img_lab_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = viewModel.labName.value.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ClinicalNavy,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = t("Authorized Portal Login", "प्राधिकृत पोर्टल लॉगिन", lang),
                    fontSize = 11.sp,
                    color = SoftGrey
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(t("Username", "उपयोगकर्ता नाम", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(t("Password", "पासवर्ड", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.login(username, password) { success ->
                            if (!success) {
                                ToastHelper.showToast(context, "Invalid Credentials!")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(t("LOGIN", "लॉगिन करें", lang), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Quick Login bypass buttons for testing
                Text(text = t("Quick Demo Login", "त्वरित डेमो लॉगिन", lang), fontSize = 11.sp, color = SoftGrey, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DemoBypassButton(label = "Admin", modifier = Modifier.weight(1f)) {
                        username = "admin"; password = "admin"
                    }
                    DemoBypassButton(label = "Reception", modifier = Modifier.weight(1.2f)) {
                        username = "recep"; password = "123"
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DemoBypassButton(label = "Technician", modifier = Modifier.weight(1.2f)) {
                        username = "tech"; password = "123"
                    }
                    DemoBypassButton(label = "Pathologist", modifier = Modifier.weight(1.2f)) {
                        username = "patho"; password = "123"
                    }
                }
            }
        }
    }
}

@Composable
fun DemoBypassButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalNavy)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}


// --- 2. DOCTOR MANAGEMENT SCREEN ---
@Composable
fun DoctorScreen(
    viewModel: LabViewModel,
    doctorsList: List<Doctor>,
    commissionsList: List<DoctorCommission>,
    lang: String,
    currency: String
) {
    val context = LocalContext.current
    var showAddDoc by remember { mutableStateOf(false) }
    var docName by remember { mutableStateOf("") }
    var docSpecialization by remember { mutableStateOf("") }
    var docMobile by remember { mutableStateOf("") }
    var docCommission by remember { mutableStateOf("10") }

    val activeId by viewModel.activeDoctorId
    LaunchedEffect(activeId) {
        if (activeId != null) {
            val d = doctorsList.find { it.id == activeId }
            if (d != null) {
                docName = d.name
                docSpecialization = d.specialization
                docMobile = d.mobile
                docCommission = d.commissionPercentage.toString()
                showAddDoc = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (!showAddDoc) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("Referring Doctors list", "डॉक्टरों की सूची", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy)
                Button(onClick = {
                    viewModel.activeDoctorId.value = null
                    docName = ""
                    docSpecialization = ""
                    docMobile = ""
                    docCommission = "10"
                    showAddDoc = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("Add Doctor", "डॉक्टर जोड़ें", lang))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(doctorsList) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${doc.specialization} | Mobile: ${doc.mobile}", fontSize = 11.sp, color = SoftGrey)
                                }
                                Box(modifier = Modifier.background(ClinicalNavy.copy(alpha = 0.1f), MaterialTheme.shapes.small).padding(6.dp)) {
                                    Text("${doc.commissionPercentage}% Commission", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ClinicalNavy)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { viewModel.activeDoctorId.value = doc.id }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(t("Edit", "संपादित करें", lang), fontSize = 11.sp)
                                }
                                if (doc.name != "Self / Direct Walk-in") {
                                    TextButton(onClick = { viewModel.deleteDoctor(doc) }, colors = ButtonDefaults.textButtonColors(contentColor = AlertCrimson)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(t("Delete", "हटाएं", lang), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Form Add Doctor
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(t("Add/Edit Referring Doctor", "डॉक्टर विवरण जोड़ें/संपादित करें", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text(t("Doctor Name", "डॉक्टर का नाम", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = docSpecialization, onValueChange = { docSpecialization = it }, label = { Text(t("Specialization", "विशेषज्ञता", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = docMobile, onValueChange = { docMobile = it }, label = { Text(t("Mobile Number", "मोबाइल नंबर", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(
                    value = docCommission,
                    onValueChange = { docCommission = it },
                    label = { Text(t("Commission Percentage (%)", "कमीशन प्रतिशत (%)", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showAddDoc = false }, modifier = Modifier.weight(1f)) {
                        Text(t("Cancel", "रद्द करें", lang))
                    }
                    Button(
                        onClick = {
                            if (docName.trim().isEmpty()) return@Button
                            val commPct = docCommission.toDoubleOrNull() ?: 10.0
                            viewModel.saveDoctor(docName, docSpecialization, docMobile, commPct) {
                                showAddDoc = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(t("Save Doctor", "सुरक्षित करें", lang))
                    }
                }
            }
        }
    }
}


// --- 3. LABORATORY TEST MANAGEMENT SCREEN ---
@Composable
fun TestsScreen(
    viewModel: LabViewModel,
    testsList: List<LabTest>,
    lang: String,
    currency: String
) {
    var categoryFilter by remember { mutableStateOf("All") }
    var showAddTest by remember { mutableStateOf(false) }

    // Form states
    var tCode by remember { mutableStateOf("") }
    var tName by remember { mutableStateOf("") }
    var tCategory by remember { mutableStateOf("Hematology") }
    var tPrice by remember { mutableStateOf("") }
    var tUnit by remember { mutableStateOf("") }
    var tRangeGeneral by remember { mutableStateOf("") }
    var tRangeMale by remember { mutableStateOf("") }
    var tRangeFemale by remember { mutableStateOf("") }
    var tRangeChild by remember { mutableStateOf("") }
    var tMethod by remember { mutableStateOf("") }

    val activeId by viewModel.activeTestId
    LaunchedEffect(activeId) {
        if (activeId != null) {
            val t = testsList.find { it.id == activeId }
            if (t != null) {
                tCode = t.code
                tName = t.name
                tCategory = t.category
                tPrice = t.price.toString()
                tUnit = t.unit
                val ranges = TestReferenceRanges.fromSerializedString(t.normalRange)
                tRangeGeneral = ranges.general
                tRangeMale = ranges.male
                tRangeFemale = ranges.female
                tRangeChild = ranges.child
                tMethod = t.method
                showAddTest = true
            }
        }
    }

    val categories = listOf("All", "Hematology", "Clinical Pathology", "Biochemistry", "Serology", "Immunology", "Thyroid Profile", "Diabetes Profile", "Kidney Function Tests", "Liver Function Tests", "Lipid Profile", "Electrolytes", "Vitamin Tests", "Infectious Disease Tests", "Urine Tests", "Stool Tests", "Tumor Markers")

    val searchVal by viewModel.testSearchQuery.collectAsState()
    val filteredTests = testsList.filter { t ->
        (categoryFilter == "All" || t.category == categoryFilter) &&
        (t.name.lowercase().contains(searchVal.lowercase()) || t.code.lowercase().contains(searchVal.lowercase()))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (!showAddTest) {
            // Search & Add
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.testSearchQuery.value = it },
                    placeholder = { Text(t("Search 5000+ Pathology Tests...", "5000+ पैथोलॉजी टेस्ट खोजें...", lang), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = {
                    viewModel.activeTestId.value = null
                    tCode = ""
                    tName = ""
                    tCategory = "Hematology"
                    tPrice = ""
                    tUnit = ""
                    tRangeGeneral = ""
                    tRangeMale = ""
                    tRangeFemale = ""
                    tRangeChild = ""
                    tMethod = ""
                    showAddTest = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            // Categories horizontal scroll list
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(categoryFilter).coerceAtLeast(0),
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = categoryFilter == cat,
                        onClick = { categoryFilter = cat },
                        text = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredTests) { test ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(test.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${test.code} | Category: ${test.category} | Method: ${test.method}", fontSize = 10.sp, color = SoftGrey)
                                val ranges = TestReferenceRanges.fromSerializedString(test.normalRange)
                                val rangeDisplay = buildString {
                                    if (ranges.general.isNotEmpty()) append("General: ${ranges.general}")
                                    if (ranges.male.isNotEmpty()) {
                                        if (isNotEmpty()) append(" | ")
                                        append("M: ${ranges.male}")
                                    }
                                    if (ranges.female.isNotEmpty()) {
                                        if (isNotEmpty()) append(" | ")
                                        append("F: ${ranges.female}")
                                    }
                                    if (ranges.child.isNotEmpty()) {
                                        if (isNotEmpty()) append(" | ")
                                        append("C: ${ranges.child}")
                                    }
                                    if (isEmpty()) append(test.normalRange)
                                }
                                Text("Normal Range: $rangeDisplay ${test.unit}", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = SoftGrey)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$currency${test.price}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ClinicalNavy)
                                Row {
                                    IconButton(onClick = { viewModel.activeTestId.value = test.id }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = ClinicalNavy, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteTest(test) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = AlertCrimson, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Form Add Test
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(t("Create Custom Lab Test", "कस्टम लैब परीक्षण जोड़ें", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = tCode, onValueChange = { tCode = it }, label = { Text(t("Test Code (e.g. CBC, TSH)", "टेस्ट कोड", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = tName, onValueChange = { tName = it }, label = { Text(t("Test Name", "परीक्षण का नाम", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                
                // Category dropdown representation
                Text("Category Selection", fontSize = 11.sp, color = SoftGrey)
                var catExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tCategory)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categories.filter { it != "All" }.forEach { ct ->
                            DropdownMenuItem(text = { Text(ct) }, onClick = { tCategory = ct; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = tPrice, onValueChange = { tPrice = it }, label = { Text(t("Price", "मूल्य", lang)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = tUnit, onValueChange = { tUnit = it }, label = { Text(t("Measurement Unit (e.g., g/dL, mg/dL)", "माप की इकाई", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(
                    value = tRangeGeneral,
                    onValueChange = { tRangeGeneral = it },
                    label = { Text(t("General Normal Range (e.g., 12.0 - 16.0)", "सामान्य सीमा", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = tRangeMale,
                    onValueChange = { tRangeMale = it },
                    label = { Text(t("Male-Specific Range (Optional, e.g., 13.5 - 17.5)", "पुरुष विशिष्ट सीमा", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = tRangeFemale,
                    onValueChange = { tRangeFemale = it },
                    label = { Text(t("Female-Specific Range (Optional, e.g., 12.0 - 15.5)", "महिला विशिष्ट सीमा", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = tRangeChild,
                    onValueChange = { tRangeChild = it },
                    label = { Text(t("Child-Specific Range (Optional, Age < 18)", "बच्चा विशिष्ट सीमा", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
                OutlinedTextField(value = tMethod, onValueChange = { tMethod = it }, label = { Text(t("Analysis Method (e.g., HPLC, CLIA)", "विश्लेषण विधि", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showAddTest = false }, modifier = Modifier.weight(1f)) {
                        Text(t("Cancel", "रद्द करें", lang))
                    }
                    Button(
                        onClick = {
                            if (tCode.trim().isEmpty() || tName.trim().isEmpty()) return@Button
                            val pr = tPrice.toDoubleOrNull() ?: 150.0
                            val finalNormalRange = TestReferenceRanges(
                                general = tRangeGeneral,
                                male = tRangeMale,
                                female = tRangeFemale,
                                child = tRangeChild
                            ).toSerializedString()
                            viewModel.saveTest(tCode, tName, tCategory, pr, tUnit, finalNormalRange, tMethod, isCustom = true) {
                                showAddTest = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(t("Save Test", "सुरक्षित करें", lang))
                    }
                }
            }
        }
    }
}


// --- 4. INVENTORY MANAGEMENT SCREEN ---
@Composable
fun InventoryScreen(
    viewModel: LabViewModel,
    inventoryList: List<InventoryItem>,
    lang: String
) {
    var showAddForm by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Reagents") }
    var qtyInput by remember { mutableStateOf("") }
    var minQtyInput by remember { mutableStateOf("") }
    var expiryInput by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierContact by remember { mutableStateOf("") }

    val activeId by viewModel.activeInventoryId
    LaunchedEffect(activeId) {
        if (activeId != null) {
            val item = inventoryList.find { it.id == activeId }
            if (item != null) {
                nameInput = item.name
                categoryInput = item.category
                qtyInput = item.quantity.toString()
                minQtyInput = item.minQuantity.toString()
                expiryInput = item.expiryDate
                supplierName = item.supplierName
                supplierContact = item.supplierContact
                showAddForm = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (!showAddForm) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(t("Lab Reagents & Chemicals", "लैब अभिकर्मक और रसायन", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy)
                Button(onClick = {
                    viewModel.activeInventoryId.value = null
                    nameInput = ""
                    categoryInput = "Reagents"
                    qtyInput = ""
                    minQtyInput = ""
                    expiryInput = "2027-12-31"
                    supplierName = ""
                    supplierContact = ""
                    showAddForm = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("Purchase Entry", "खरीद प्रविष्टि", lang))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(inventoryList) { item ->
                    val isLow = item.quantity <= item.minQuantity
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Category: ${item.category} | Supplier: ${item.supplierName}", fontSize = 11.sp, color = SoftGrey)
                                    Text("Expiry Date: ${item.expiryDate}", fontSize = 11.sp, color = AlertCrimson, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val countColor = if (isLow) AlertCrimson else SafeEmerald
                                    Text(text = "${item.quantity} units", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = countColor)
                                    if (isLow) {
                                        Box(modifier = Modifier.background(AlertCrimson.copy(alpha = 0.12f)).padding(4.dp)) {
                                            Text("Low Stock", fontSize = 9.sp, color = AlertCrimson, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { viewModel.activeInventoryId.value = item.id }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(t("Edit", "संपादित करें", lang), fontSize = 11.sp)
                                }
                                TextButton(onClick = { viewModel.deleteInventoryItem(item) }, colors = ButtonDefaults.textButtonColors(contentColor = AlertCrimson)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(t("Delete", "हटाएं", lang), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(t("Inventory Purchase Entry", "इन्वेंट्री प्रविष्टि दर्ज करें", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text(t("Item / Reagent Name", "रीएजेंट/सामग्री का नाम", lang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                
                var catExp by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    OutlinedButton(onClick = { catExp = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(categoryInput)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = catExp, onDismissRequest = { catExp = false }) {
                        listOf("Reagents", "Chemicals", "Test Kits", "Tubes", "Consumables").forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoryInput = cat; catExp = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = qtyInput, onValueChange = { qtyInput = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minQtyInput, onValueChange = { minQtyInput = it }, label = { Text("Low Limit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = expiryInput, onValueChange = { expiryInput = it }, label = { Text("Expiry Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Supplier Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                OutlinedTextField(value = supplierContact, onValueChange = { supplierContact = it }, label = { Text("Supplier Contact") }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showAddForm = false }, modifier = Modifier.weight(1f)) {
                        Text(t("Cancel", "रद्द करें", lang))
                    }
                    Button(
                        onClick = {
                            if (nameInput.trim().isEmpty()) return@Button
                            val q = qtyInput.toIntOrNull() ?: 50
                            val mq = minQtyInput.toIntOrNull() ?: 10
                            viewModel.saveInventoryItem(nameInput, categoryInput, q, mq, expiryInput, supplierName, supplierContact) {
                                showAddForm = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(t("Save Item", "सुरक्षित करें", lang))
                    }
                }
            }
        }
    }
}


// --- 5. STAFF / ROLE BASED ACCESS SCREEN ---
@Composable
fun StaffScreen(
    viewModel: LabViewModel,
    staffList: List<Staff>,
    lang: String
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(t("Diagnostic Lab Staff Clearance", "लैब कर्मचारी अनुमतियां", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy, modifier = Modifier.padding(bottom = 6.dp))
        Text(text = t("Role-based clearance regulates draft modifications, approvals, and financial logs.", "भूमिका-आधारित अनुमति रिपोर्ट अनुमोदन और वित्तीय लॉग को विनियमित करती है।", lang), fontSize = 11.sp, color = SoftGrey, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn {
            items(staffList) { st ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(ClinicalNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ClinicalNavy)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(st.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Username: ${st.username} | Role: ${st.role}", fontSize = 11.sp, color = SoftGrey)
                        }
                        Box(modifier = Modifier.background(SafeEmerald.copy(alpha = 0.15f), MaterialTheme.shapes.small).padding(6.dp)) {
                            Text(st.role.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = SafeEmerald)
                        }
                    }
                }
            }
        }
    }
}


// --- 6. SETTINGS SCREEN ---
@Composable
fun SettingsScreen(
    viewModel: LabViewModel,
    lang: String,
    currency: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(t("Laboratory General Configuration", "प्रयोगशाला सामान्य विन्यास", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClinicalNavy, modifier = Modifier.padding(bottom = 16.dp))

        // Lab Name Change
        Text(t("Change Lab Name", "प्रयोगशाला का नाम बदलें", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = viewModel.labName.value,
            onValueChange = { viewModel.labName.value = it },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
            shape = MaterialTheme.shapes.small,
            singleLine = true
        )

        // Dark Theme toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(t("Visual Theme Mode", "दृश्य थीम मोड", lang), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Toggle between light and dark slate themes", fontSize = 10.sp, color = SoftGrey)
            }
            Switch(checked = viewModel.isDarkTheme.value, onCheckedChange = { viewModel.isDarkTheme.value = it })
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // Currency Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(t("Default Billing Currency", "डिफ़ॉल्ट बिलिंग मुद्रा", lang), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Select currency symbol for invoice receipts", fontSize = 10.sp, color = SoftGrey)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("₹", "$", "£", "€").forEach { cur ->
                    FilterChip(
                        selected = currency == cur,
                        onClick = { viewModel.currencySymbol.value = cur },
                        label = { Text(cur, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // Language Select
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(t("Portal Language Support", "पोर्टल भाषा समर्थन", lang), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Toggle between Hindi and English localization", fontSize = 10.sp, color = SoftGrey)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = lang == "en", onClick = { viewModel.languageCode.value = "en" }, label = { Text("English") })
                FilterChip(selected = lang == "hi", onClick = { viewModel.languageCode.value = "hi" }, label = { Text("हिंदी") })
            }
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Change Password Section
        Text(t("Change Password", "पासवर्ड बदलें", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalNavy, modifier = Modifier.padding(bottom = 12.dp))
        
        var oldPasswordInput by remember { mutableStateOf("") }
        var newPasswordInput by remember { mutableStateOf("") }
        var confirmPasswordInput by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = oldPasswordInput,
                    onValueChange = { oldPasswordInput = it },
                    label = { Text(t("Current Password", "वर्तमान पासवर्ड", lang)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = { Text(t("New Password", "नया पासवर्ड", lang)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it },
                    label = { Text(t("Confirm New Password", "नया पासवर्ड पुष्टि करें", lang)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newPasswordInput != confirmPasswordInput) {
                            ToastHelper.showToast(context, t("New passwords do not match!", "नए पासवर्ड मेल नहीं खाते!", lang))
                            return@Button
                        }
                        if (newPasswordInput.trim().isEmpty()) {
                            ToastHelper.showToast(context, t("Password cannot be empty!", "पासवर्ड खाली नहीं हो सकता!", lang))
                            return@Button
                        }
                        viewModel.updatePassword(oldPasswordInput, newPasswordInput) { success, msg ->
                            if (success) {
                                oldPasswordInput = ""
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                ToastHelper.showToast(context, t("Password changed successfully!", "पासवर्ड सफलतापूर्वक बदला गया!", lang))
                            } else {
                                val localizedMsg = when (msg) {
                                    "Incorrect current password." -> t("Incorrect current password.", "गलत वर्तमान पासवर्ड।", lang)
                                    "Password cannot be empty." -> t("Password cannot be empty.", "पासवर्ड खाली नहीं हो सकता।", lang)
                                    else -> msg
                                }
                                ToastHelper.showToast(context, localizedMsg)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy)
                ) {
                    Text(t("Update Password", "पासवर्ड अपडेट करें", lang))
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Backup Actions
        Text(t("Database Maintenance", "डेटाबेस रखरखाव", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ClinicalNavy, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { viewModel.backupData(context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(t("Backup Data", "बैकअप लें", lang), fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { viewModel.restoreData(context) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(t("Restore Data", "पुनर्प्राप्त करें", lang), fontSize = 11.sp)
            }
        }
    }
}
