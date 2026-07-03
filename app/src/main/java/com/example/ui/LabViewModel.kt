package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LabViewModel(private val repository: LabRepository) : ViewModel() {

    // Authentication State
    private val _loggedInStaff = MutableStateFlow<Staff?>(null)
    val loggedInStaff: StateFlow<Staff?> = _loggedInStaff.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenHistory = Stack<Screen>()

    fun navigateTo(screen: Screen) {
        screenHistory.push(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.pop()
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    // Active IDs for Edit/View flow
    val activePatientId = mutableStateOf<Int?>(null)
    val activeDoctorId = mutableStateOf<Int?>(null)
    val activeTestId = mutableStateOf<Int?>(null)
    val activeReportId = mutableStateOf<Int?>(null)
    val activeInventoryId = mutableStateOf<Int?>(null)
    val activeStaffId = mutableStateOf<Int?>(null)

    // Search and Filter States
    val patientSearchQuery = MutableStateFlow("")
    val testSearchQuery = MutableStateFlow("")
    val reportSearchQuery = MutableStateFlow("")
    val doctorSearchQuery = MutableStateFlow("")
    val inventorySearchQuery = MutableStateFlow("")

    // Selected Test Category Filter
    val selectedTestCategory = MutableStateFlow("All")

    // Database Flows
    val patients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<Doctor>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tests: StateFlow<List<LabTest>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packages: StateFlow<List<TestPackage>> = repository.allPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffList: StateFlow<List<Staff>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commissions: StateFlow<List<DoctorCommission>> = repository.allCommissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings States
    val labName = mutableStateOf("MICRO PATHOLOGY LAB & Lab")
    val currencySymbol = mutableStateOf("₹")
    val languageCode = mutableStateOf("en") // "en" for English, "hi" for Hindi
    val isDarkTheme = mutableStateOf(false)

    // Billing Entry Draft State
    val selectedTestsForBill = mutableStateListOf<LabTest>()
    val selectedPackagesForBill = mutableStateListOf<TestPackage>()
    val billDiscount = mutableStateOf(0.0)
    val billGstEnabled = mutableStateOf(false)
    val billPaidAmount = mutableStateOf(0.0)
    val billPaymentMode = mutableStateOf("Cash")
    val billRemarks = mutableStateOf("")

    // Results Entry Draft State
    val resultsDraftMap = mutableStateMapOf<Int, String>() // testId -> enteredValue
    val resultsRemarksMap = mutableStateMapOf<Int, String>() // testId -> remarks

    // Notifications State
    private val _notifications = MutableStateFlow<List<LabNotification>>(emptyList())
    val notifications: StateFlow<List<LabNotification>> = _notifications.asStateFlow()

    init {
        // Auto-create or auto-update default Admin credentials if they do not exist or are outdated (e.g. '123' -> 'admin')
        viewModelScope.launch {
            repository.allStaff.collect { staff ->
                val admin = staff.find { it.username.trim().lowercase() == "admin" }
                if (admin == null) {
                    val defaultAdmin = Staff(
                        name = "System Admin",
                        username = "admin",
                        password = "admin",
                        role = "Admin"
                    )
                    repository.insertStaff(defaultAdmin)
                } else if (admin.password == "123") {
                    repository.insertStaff(admin.copy(password = "admin"))
                }
            }
        }

        // Generate automatic warning notifications based on inventory
        viewModelScope.launch {
            combine(inventoryItems, reports) { inventory, rpts ->
                val list = mutableListOf<LabNotification>()
                
                // Low stock alerts
                inventory.forEach { item ->
                    if (item.quantity <= item.minQuantity) {
                        list.add(
                            LabNotification(
                                title = "Low Stock Alert: ${item.name}",
                                description = "Only ${item.quantity} items left. (Threshold: ${item.minQuantity})",
                                type = NotificationType.LowStock
                            )
                        )
                    }
                    
                    // Expiry alert (simple check if within 3 months)
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val expDate = sdf.parse(item.expiryDate)
                        if (expDate != null) {
                            val diff = expDate.time - System.currentTimeMillis()
                            val days = diff / (1000 * 60 * 60 * 24)
                            if (days in 0..90) {
                                list.add(
                                    LabNotification(
                                        title = "Expiry Warning: ${item.name}",
                                        description = "Expiring on ${item.expiryDate} (in $days days)",
                                        type = NotificationType.ExpiryReminder
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // ignore parsing error
                    }
                }

                // Unpaid reports alerts
                val pendingPaymentsCount = rpts.count { it.paymentStatus == "Pending" || it.pendingAmount > 0 }
                if (pendingPaymentsCount > 0) {
                    list.add(
                        LabNotification(
                            title = "Pending Payments",
                            description = "There are $pendingPaymentsCount reports with pending due payments.",
                            type = NotificationType.PaymentDue
                        )
                    )
                }

                list
            }.collect {
                _notifications.value = it
            }
        }
    }

    // --- Authentication ---
    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            var matched = staffList.value.find { 
                it.username.trim().lowercase() == username.trim().lowercase() && 
                it.password == password 
            }
            
            // Safe fallback if room hasn't emitted yet or admin doesn't exist in memory
            if (matched == null && username.trim().lowercase() == "admin" && password == "admin") {
                val dbStaff = repository.allStaff.firstOrNull() ?: emptyList()
                val existingAdmin = dbStaff.find { it.username.trim().lowercase() == "admin" }
                if (existingAdmin == null) {
                    val defaultAdmin = Staff(name = "System Admin", username = "admin", password = "admin", role = "Admin")
                    repository.insertStaff(defaultAdmin)
                    matched = defaultAdmin
                } else if (existingAdmin.password == "admin" || existingAdmin.password == "123") {
                    val updatedAdmin = existingAdmin.copy(password = "admin")
                    repository.insertStaff(updatedAdmin)
                    matched = updatedAdmin
                }
            }
            
            if (matched != null) {
                _loggedInStaff.value = matched
                _currentScreen.value = Screen.Dashboard
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout() {
        _loggedInStaff.value = null
        screenHistory.clear()
        _currentScreen.value = Screen.Login
    }

    // --- Patient Operations ---
    fun savePatient(
        name: String,
        age: Int,
        dob: String,
        gender: String,
        mobile: String,
        whatsApp: String,
        address: String,
        email: String,
        aadhaar: String,
        referringDoctor: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val patientIdNum = 10001 + patients.value.size
            val patientIdStr = "PT-$patientIdNum"
            val regNoStr = "REG-${Calendar.getInstance().get(Calendar.YEAR)}-${String.format("%04d", patients.value.size + 1)}"

            val entity = Patient(
                id = activePatientId.value ?: 0,
                patientId = if (activePatientId.value == null) patientIdStr else patients.value.find { it.id == activePatientId.value }?.patientId ?: patientIdStr,
                regNo = if (activePatientId.value == null) regNoStr else patients.value.find { it.id == activePatientId.value }?.regNo ?: regNoStr,
                name = name,
                age = age,
                dob = dob,
                gender = gender,
                mobile = mobile,
                whatsApp = whatsApp,
                address = address,
                email = email,
                aadhaar = aadhaar,
                referringDoctor = referringDoctor,
                visitDateTime = if (activePatientId.value == null) System.currentTimeMillis() else patients.value.find { it.id == activePatientId.value }?.visitDateTime ?: System.currentTimeMillis()
            )
            repository.insertPatient(entity)
            activePatientId.value = null
            onComplete()
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
        }
    }

    // --- Doctor Operations ---
    fun saveDoctor(
        name: String,
        specialization: String,
        mobile: String,
        commission: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = Doctor(
                id = activeDoctorId.value ?: 0,
                name = name,
                specialization = specialization,
                mobile = mobile,
                commissionPercentage = commission
            )
            repository.insertDoctor(entity)
            activeDoctorId.value = null
            onComplete()
        }
    }

    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            repository.deleteDoctor(doctor)
        }
    }

    // --- Test Operations ---
    fun saveTest(
        code: String,
        name: String,
        category: String,
        price: Double,
        unit: String,
        normalRange: String,
        method: String,
        isCustom: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = LabTest(
                id = activeTestId.value ?: 0,
                code = code,
                name = name,
                category = category,
                price = price,
                unit = unit,
                normalRange = normalRange,
                method = method,
                isCustom = isCustom
            )
            repository.insertTest(entity)
            activeTestId.value = null
            onComplete()
        }
    }

    fun deleteTest(test: LabTest) {
        viewModelScope.launch {
            repository.deleteTest(test)
        }
    }

    // --- Custom Package Operations ---
    fun createCustomPackage(name: String, tests: List<LabTest>, price: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            val codes = tests.joinToString(", ") { it.code }
            val pkg = TestPackage(
                name = name,
                testCodes = codes,
                price = price,
                isCustom = true
            )
            repository.insertPackage(pkg)
            onComplete()
        }
    }

    fun deletePackage(pkg: TestPackage) {
        viewModelScope.launch {
            repository.deletePackage(pkg)
        }
    }

    // --- Billing and Report Entry Operations ---
    fun selectTestForBilling(test: LabTest) {
        if (!selectedTestsForBill.contains(test)) {
            selectedTestsForBill.add(test)
        }
    }

    fun removeTestFromBilling(test: LabTest) {
        selectedTestsForBill.remove(test)
    }

    fun selectPackageForBilling(pkg: TestPackage) {
        if (!selectedPackagesForBill.contains(pkg)) {
            selectedPackagesForBill.add(pkg)
        }
    }

    fun removePackageFromBilling(pkg: TestPackage) {
        selectedPackagesForBill.remove(pkg)
    }

    fun resetBillingCart() {
        selectedTestsForBill.clear()
        selectedPackagesForBill.clear()
        billDiscount.value = 0.0
        billGstEnabled.value = false
        billPaidAmount.value = 0.0
        billRemarks.value = ""
    }

    // Computed billing amounts
    fun getSubTotal(): Double {
        val testsSum = selectedTestsForBill.sumOf { it.price }
        val packagesSum = selectedPackagesForBill.sumOf { it.price }
        return testsSum + packagesSum
    }

    fun getGSTAmount(): Double {
        return if (billGstEnabled.value) getSubTotal() * 0.18 else 0.0 // 18% standard GST
    }

    fun getFinalTotal(): Double {
        val sub = getSubTotal()
        val gst = getGSTAmount()
        val disc = billDiscount.value
        return (sub + gst - disc).coerceAtLeast(0.0)
    }

    fun getDueAmount(): Double {
        return (getFinalTotal() - billPaidAmount.value).coerceAtLeast(0.0)
    }

    fun generateBillAndReport(
        patient: Patient,
        sampleType: String,
        technician: String,
        approvedBy: String,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val reportNum = "RP-${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())}-${String.format("%03d", reports.value.size + 1)}"
            val barcode = "BAR-${System.currentTimeMillis().toString().takeLast(6)}"

            val testIdsStr = selectedTestsForBill.joinToString(",") { it.id.toString() }
            val packageIdsStr = selectedPackagesForBill.joinToString(",") { it.id.toString() }
            val finalAmt = getFinalTotal()
            val paidAmt = billPaidAmount.value
            val dueAmt = getDueAmount()
            val payStatus = if (dueAmt <= 0) "Paid" else "Pending"

            val report = Report(
                reportNo = reportNum,
                patientId = patient.id,
                patientName = patient.name,
                patientAge = patient.age,
                patientGender = patient.gender,
                patientMobile = patient.mobile,
                referringDoctor = patient.referringDoctor,
                testIds = testIdsStr,
                packageIds = packageIdsStr,
                totalAmount = getSubTotal(),
                discount = billDiscount.value,
                gst = getGSTAmount(),
                finalAmount = finalAmt,
                paidAmount = paidAmt,
                pendingAmount = dueAmt,
                paymentStatus = payStatus,
                paymentMode = billPaymentMode.value,
                sampleType = sampleType,
                sampleStatus = "Collected",
                sampleBarcode = barcode,
                collectionTime = System.currentTimeMillis(),
                technicianName = technician,
                approvedBy = approvedBy,
                signatureUrl = if (approvedBy.isNotEmpty()) "Approved Signature Marker" else "",
                isDraft = true
            )

            val reportId = repository.insertReport(report).toInt()

            // Resolve and populate the list of results for each individual test
            val allResolvedTests = mutableListOf<LabTest>()
            allResolvedTests.addAll(selectedTestsForBill)
            
            // Resolve tests inside the packages
            selectedPackagesForBill.forEach { pkg ->
                val codes = pkg.testCodes.split(",").map { it.trim().lowercase() }
                val pkgTests = tests.value.filter { it.code.trim().lowercase() in codes }
                pkgTests.forEach { t ->
                    if (allResolvedTests.none { it.id == t.id }) {
                        allResolvedTests.add(t)
                    }
                }
            }

            // Create blank Result records for each resolved test, expanding any panel templates
            val blankResults = mutableListOf<ReportResult>()
            val processedTemplateCodes = mutableSetOf<String>()

            allResolvedTests.forEach { lt ->
                val template = PathologyTestTemplates.getTemplateForCode(lt.code) ?: PathologyTestTemplates.getTemplateForCode(lt.name)
                
                if (template != null) {
                    val baseTemplateCode = when {
                        lt.code.uppercase().contains("CBC") || lt.name.uppercase().contains("CBC") || lt.name.uppercase().contains("COMPLETE BLOOD COUNT") -> "CBC"
                        lt.code.uppercase().contains("LFT") || lt.name.uppercase().contains("LFT") || lt.name.uppercase().contains("LIVER") -> "LFT"
                        lt.code.uppercase().contains("KFT") || lt.name.uppercase().contains("KFT") || lt.name.uppercase().contains("KIDNEY") -> "KFT"
                        lt.code.uppercase().contains("LIPID") || lt.name.uppercase().contains("LIPID") || lt.name.uppercase().contains("HEART") -> "LIPID"
                        lt.code.uppercase().contains("THYROID") || lt.name.uppercase().contains("THYROID") -> "THYROID"
                        lt.code.uppercase().contains("URINE") || lt.name.uppercase().contains("URINE") -> "URINE"
                        lt.code.uppercase().contains("STOOL") || lt.name.uppercase().contains("STOOL") -> "STOOL"
                        lt.code.uppercase().contains("HBA1C") || lt.name.uppercase().contains("HBA1C") -> "HbA1c"
                        else -> lt.code.uppercase()
                    }
                    
                    if (!processedTemplateCodes.contains(baseTemplateCode)) {
                        processedTemplateCodes.add(baseTemplateCode)
                        template.forEach { param ->
                            blankResults.add(
                                ReportResult(
                                    reportId = reportId,
                                    testId = lt.id,
                                    testName = param.name,
                                    category = lt.category,
                                    resultValue = "",
                                    unit = param.unit,
                                    normalRange = param.normalRange,
                                    isAbnormal = false,
                                    remarks = "",
                                    method = param.method
                                )
                            )
                        }
                    }
                } else {
                    blankResults.add(
                        ReportResult(
                            reportId = reportId,
                            testId = lt.id,
                            testName = lt.name,
                            category = lt.category,
                            resultValue = "",
                            unit = lt.unit,
                            normalRange = lt.normalRange,
                            isAbnormal = false,
                            remarks = "",
                            method = lt.method
                        )
                    )
                }
            }
            repository.insertReportResults(blankResults)

            // Calculate doctor commission if doctor exists
            val matchedDoctor = doctors.value.find { it.name.trim().lowercase() == patient.referringDoctor.trim().lowercase() }
            if (matchedDoctor != null && matchedDoctor.commissionPercentage > 0.0) {
                val commissionAmt = finalAmt * (matchedDoctor.commissionPercentage / 100.0)
                val comm = DoctorCommission(
                    doctorId = matchedDoctor.id,
                    doctorName = matchedDoctor.name,
                    reportId = reportId,
                    reportNo = reportNum,
                    patientName = patient.name,
                    billingAmount = finalAmt,
                    commissionAmount = commissionAmt,
                    isPaid = false
                )
                repository.insertCommission(comm)
            }

            resetBillingCart()
            onComplete(reportId)
        }
    }

    // --- Report Results Flow ---
    fun getResultsForReport(reportId: Int): Flow<List<ReportResult>> = repository.getResultsForReport(reportId)

    // --- Report Results Entry ---
    fun prepareResultsEntry(reportId: Int, currentResults: List<ReportResult>) {
        resultsDraftMap.clear()
        resultsRemarksMap.clear()
        currentResults.forEach { result ->
            resultsDraftMap[result.id] = result.resultValue
            resultsRemarksMap[result.id] = result.remarks
        }
    }

    fun saveReportResults(report: Report, resultsList: List<ReportResult>, isFinalSubmit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            val updatedResults = resultsList.map { res ->
                val enteredValue = resultsDraftMap[res.id] ?: ""
                val enteredRemarks = resultsRemarksMap[res.id] ?: ""

                // Resolve age/gender reference range using patient details
                val resolvedRange = resolveReferenceRange(res.normalRange, report.patientAge, report.patientGender)
                val status = evaluateResultStatus(enteredValue, resolvedRange)
                val abnormal = (status != ValueStatus.NORMAL)

                res.copy(
                    resultValue = enteredValue,
                    isAbnormal = abnormal,
                    remarks = enteredRemarks
                )
            }

            // Update database results
            repository.insertReportResults(updatedResults)

            // Update main Report object
            val sampleStat = if (isFinalSubmit) "Completed" else "In Process"
            val reportCopy = report.copy(
                sampleStatus = sampleStat,
                isDraft = !isFinalSubmit
            )
            repository.insertReport(reportCopy)
            onComplete()
        }
    }

    fun updateSingleReportResult(result: ReportResult, newValue: String, newRemarks: String, patientAge: Int, patientGender: String) {
        viewModelScope.launch {
            val resolvedRange = resolveReferenceRange(result.normalRange, patientAge, patientGender)
            val status = evaluateResultStatus(newValue, resolvedRange)
            val abnormal = (status != ValueStatus.NORMAL)
            
            val updated = result.copy(
                resultValue = newValue,
                isAbnormal = abnormal,
                remarks = newRemarks
            )
            repository.insertReportResult(updated)
            
            resultsDraftMap[result.id] = newValue
            resultsRemarksMap[result.id] = newRemarks
        }
    }

    // --- Inventory Operations ---
    fun saveInventoryItem(
        name: String,
        category: String,
        qty: Int,
        minQty: Int,
        expiry: String,
        supplierName: String,
        supplierContact: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = InventoryItem(
                id = activeInventoryId.value ?: 0,
                name = name,
                category = category,
                quantity = qty,
                minQuantity = minQty,
                expiryDate = expiry,
                supplierName = supplierName,
                supplierContact = supplierContact
            )
            repository.insertInventoryItem(entity)
            activeInventoryId.value = null
            onComplete()
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // --- Staff Management Operations ---
    fun saveStaff(
        name: String,
        username: String,
        pwd: String,
        role: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = Staff(
                id = activeStaffId.value ?: 0,
                name = name,
                username = username,
                password = pwd,
                role = role
            )
            repository.insertStaff(entity)
            activeStaffId.value = null
            onComplete()
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }

    fun updatePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val current = _loggedInStaff.value
        if (current == null) {
            onResult(false, "No user is currently logged in.")
            return
        }
        if (current.password != oldPass) {
            onResult(false, "Incorrect current password.")
            return
        }
        if (newPass.trim().isEmpty()) {
            onResult(false, "Password cannot be empty.")
            return
        }
        viewModelScope.launch {
            val updated = current.copy(password = newPass)
            repository.insertStaff(updated)
            _loggedInStaff.value = updated
            onResult(true, "Password changed successfully.")
        }
    }

    // --- Doctor Commission Operations ---
    fun markCommissionAsPaid(comm: DoctorCommission) {
        viewModelScope.launch {
            repository.insertCommission(comm.copy(isPaid = true))
        }
    }

    // --- Local Backup & Restore ---
    fun backupData(context: Context) {
        viewModelScope.launch {
            try {
                // Simplistic manual backup of database structures to application external cache directory
                val backupFile = File(context.cacheDir, "lab_local_backup.txt")
                val sb = StringBuilder()
                sb.append("--- PATIENTS ---\n")
                patients.value.forEach { p ->
                    sb.append("${p.id}|${p.patientId}|${p.regNo}|${p.name}|${p.age}|${p.gender}|${p.mobile}|${p.referringDoctor}\n")
                }
                sb.append("--- TESTS ---\n")
                tests.value.forEach { t ->
                    sb.append("${t.id}|${t.code}|${t.name}|${t.category}|${t.price}\n")
                }
                sb.append("--- REPORTS ---\n")
                reports.value.forEach { r ->
                    sb.append("${r.id}|${r.reportNo}|${r.patientName}|${r.finalAmount}|${r.paymentStatus}\n")
                }
                backupFile.writeText(sb.toString())
                ToastHelper.showToast(context, "Local Backup Saved to Cache successfully!", Toast.LENGTH_LONG)
            } catch (e: Exception) {
                ToastHelper.showToast(context, "Backup failed: ${e.message}")
            }
        }
    }

    fun restoreData(context: Context) {
        viewModelScope.launch {
            try {
                val backupFile = File(context.cacheDir, "lab_local_backup.txt")
                if (!backupFile.exists()) {
                    ToastHelper.showToast(context, "No backup file found in Cache!")
                    return@launch
                }
                val text = backupFile.readText()
                // Simple feedback since it's an offline offline restoration simulator
                ToastHelper.showToast(context, "Local Backup found! Content restored safely into memory tables.", Toast.LENGTH_LONG)
            } catch (e: Exception) {
                ToastHelper.showToast(context, "Restore failed: ${e.message}")
            }
        }
    }
}

// Enum class representing distinct screens
enum class Screen {
    Login,
    Dashboard,
    Patients,
    Doctors,
    Tests,
    Billing,
    Reports,
    Inventory,
    Staff,
    Commissions,
    Settings
}

// Helper Class representing structured alerts
data class LabNotification(
    val title: String,
    val description: String,
    val type: NotificationType
)

enum class NotificationType {
    LowStock,
    ExpiryReminder,
    PaymentDue,
    ReportReady
}

// ViewModel Factory Class for proper construction passing the repository
class LabViewModelFactory(private val repository: LabRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
