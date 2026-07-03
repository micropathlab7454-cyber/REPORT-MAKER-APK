package com.example.ui

import androidx.compose.animation.*
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
import com.example.data.Patient
import com.example.ui.theme.SoftGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(
    viewModel: LabViewModel,
    patientsList: List<Patient>,
    doctorsList: List<com.example.data.Doctor>,
    lang: String
) {
    val searchVal by viewModel.patientSearchQuery.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    // Form inputs
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("Male") }
    var mobileInput by remember { mutableStateOf("") }
    var whatsappInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var aadhaarInput by remember { mutableStateOf("") }
    var docInput by remember { mutableStateOf("Direct Walk-in") }

    val activeId by viewModel.activePatientId

    // When editing active patient, fill form values
    LaunchedEffect(activeId) {
        if (activeId != null) {
            val patient = patientsList.find { it.id == activeId }
            if (patient != null) {
                nameInput = patient.name
                ageInput = patient.age.toString()
                dobInput = patient.dob
                genderInput = patient.gender
                mobileInput = patient.mobile
                whatsappInput = patient.whatsApp
                addressInput = patient.address
                emailInput = patient.email
                aadhaarInput = patient.aadhaar
                docInput = patient.referringDoctor
                showAddForm = true
            }
        }
    }

    val filteredPatients = patientsList.filter { p ->
        p.name.lowercase().contains(searchVal.lowercase()) ||
        p.mobile.contains(searchVal) ||
        p.patientId.lowercase().contains(searchVal.lowercase()) ||
        p.regNo.lowercase().contains(searchVal.lowercase())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!showAddForm) {
            // Search and List Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.patientSearchQuery.value = it },
                    placeholder = { Text(t("Search Patient (Name, Mobile, ID, Reg...)", "मरीज खोजें (नाम, मोबाइल, आईडी...)", lang), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchVal.isNotEmpty()) {
                            IconButton(onClick = { viewModel.patientSearchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = {
                        viewModel.activePatientId.value = null
                        nameInput = ""
                        ageInput = ""
                        dobInput = "1995-01-01"
                        genderInput = "Male"
                        mobileInput = ""
                        whatsappInput = ""
                        addressInput = ""
                        emailInput = ""
                        aadhaarInput = ""
                        docInput = "Direct Walk-in"
                        showAddForm = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Patient")
                }
            }

            if (filteredPatients.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.PersonSearch, contentDescription = "Search Empty", tint = SoftGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = t("No Patients Registered", "कोई मरीज पंजीकृत नहीं है", lang), fontWeight = FontWeight.Bold, color = SoftGrey)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    items(filteredPatients) { patient ->
                        PatientListItem(
                            patient = patient,
                            lang = lang,
                            onEdit = {
                                viewModel.activePatientId.value = patient.id
                            },
                            onDelete = {
                                viewModel.deletePatient(patient)
                            }
                        )
                    }
                }
            }
        } else {
            // Add/Edit Patient Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (activeId == null) t("Register New Patient", "नया मरीज पंजीकृत करें", lang) else t("Edit Patient details", "मरीज का विवरण संपादित करें", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(t("Patient Name (Required)", "मरीज का नाम", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text(t("Age (Yrs)", "उम्र (वर्ष)", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    )

                    OutlinedTextField(
                        value = dobInput,
                        onValueChange = { dobInput = it },
                        label = { Text(t("DOB (YYYY-MM-DD)", "जन्म तिथि", lang)) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    )
                }

                // Gender Selection Row
                Text(text = t("Gender Selection", "लिंग चयन", lang), fontSize = 12.sp, color = SoftGrey, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { gen ->
                        FilterChip(
                            selected = genderInput == gen,
                            onClick = { genderInput = gen },
                            label = { Text(t(gen, if (gen=="Male") "पुरुष" else if (gen=="Female") "महिला" else "अन्य", lang)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = mobileInput,
                    onValueChange = { mobileInput = it },
                    label = { Text(t("Mobile Number", "मोबाइल नंबर", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                OutlinedTextField(
                    value = whatsappInput,
                    onValueChange = { whatsappInput = it },
                    label = { Text(t("WhatsApp Number (Optional)", "व्हाट्सएप नंबर", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(t("Email Address", "ईमेल पता", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text(t("Residential Address", "पता", lang)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                OutlinedTextField(
                    value = aadhaarInput,
                    onValueChange = { aadhaarInput = it },
                    label = { Text(t("Aadhaar Number (Optional)", "आधार नंबर (वैकल्पिक)", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = MaterialTheme.shapes.small
                )

                // Referring Doctor Selector
                Text(text = t("Referring Doctor", "संदर्भित डॉक्टर", lang), fontSize = 12.sp, color = SoftGrey, modifier = Modifier.padding(bottom = 4.dp))
                var docExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    OutlinedButton(
                        onClick = { docExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(docInput)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = docExpanded, onDismissRequest = { docExpanded = false }) {
                        doctorsList.forEach { doctor ->
                            DropdownMenuItem(
                                text = { Text(doctor.name) },
                                onClick = {
                                    docInput = doctor.name
                                    docExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showAddForm = false
                            viewModel.activePatientId.value = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(t("Cancel", "रद्द करें", lang))
                    }

                    Button(
                        onClick = {
                            if (nameInput.trim().isEmpty() || mobileInput.trim().isEmpty()) {
                                return@Button
                            }
                            val ageInt = ageInput.toIntOrNull() ?: 30
                            viewModel.savePatient(
                                name = nameInput,
                                age = ageInt,
                                dob = dobInput,
                                gender = genderInput,
                                mobile = mobileInput,
                                whatsApp = whatsappInput,
                                address = addressInput,
                                email = emailInput,
                                aadhaar = aadhaarInput,
                                referringDoctor = docInput,
                                onComplete = {
                                    showAddForm = false
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(t("Save Record", "सुरक्षित करें", lang))
                    }
                }
            }
        }
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    lang: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
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
                    Text(
                        text = patient.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${patient.patientId} | ${patient.gender} | ${patient.age} " + t("Yrs", "वर्ष", lang),
                        fontSize = 11.sp,
                        color = SoftGrey
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRow(t("Registration No", "पंजीकरण संख्या", lang), patient.regNo)
                    DetailRow(t("DOB", "जन्म तिथि", lang), patient.dob)
                    DetailRow(t("Mobile", "मोबाइल नंबर", lang), patient.mobile)
                    if (patient.whatsApp.isNotEmpty()) DetailRow(t("WhatsApp", "व्हाट्सएप", lang), patient.whatsApp)
                    if (patient.email.isNotEmpty()) DetailRow(t("Email", "ईमेल", lang), patient.email)
                    if (patient.address.isNotEmpty()) DetailRow(t("Address", "पता", lang), patient.address)
                    if (patient.aadhaar.isNotEmpty()) DetailRow(t("Aadhaar", "आधार संख्या", lang), patient.aadhaar)
                    DetailRow(t("Referring Doctor", "संदर्भित डॉक्टर", lang), patient.referringDoctor)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onEdit,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(t("Edit", "संपादित करें", lang))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(t("Delete", "हटाएं", lang))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = SoftGrey)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
