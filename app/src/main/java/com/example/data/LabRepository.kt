package com.example.data

import kotlinx.coroutines.flow.Flow

class LabRepository(private val labDao: LabDao) {
    // Patients
    val allPatients: Flow<List<Patient>> = labDao.getAllPatients()
    suspend fun getPatientById(id: Int): Patient? = labDao.getPatientById(id)
    suspend fun insertPatient(patient: Patient): Long = labDao.insertPatient(patient)
    suspend fun deletePatient(patient: Patient) = labDao.deletePatient(patient)

    // Doctors
    val allDoctors: Flow<List<Doctor>> = labDao.getAllDoctors()
    suspend fun getDoctorById(id: Int): Doctor? = labDao.getDoctorById(id)
    suspend fun insertDoctor(doctor: Doctor): Long = labDao.insertDoctor(doctor)
    suspend fun deleteDoctor(doctor: Doctor) = labDao.deleteDoctor(doctor)

    // Tests
    val allTests: Flow<List<LabTest>> = labDao.getAllTests()
    suspend fun getTestById(id: Int): LabTest? = labDao.getTestById(id)
    suspend fun insertTest(test: LabTest): Long = labDao.insertTest(test)
    suspend fun deleteTest(test: LabTest) = labDao.deleteTest(test)

    // Packages
    val allPackages: Flow<List<TestPackage>> = labDao.getAllPackages()
    suspend fun insertPackage(pkg: TestPackage): Long = labDao.insertPackage(pkg)
    suspend fun deletePackage(pkg: TestPackage) = labDao.deletePackage(pkg)

    // Reports
    val allReports: Flow<List<Report>> = labDao.getAllReports()
    suspend fun getReportById(id: Int): Report? = labDao.getReportById(id)
    suspend fun insertReport(report: Report): Long = labDao.insertReport(report)
    suspend fun deleteReport(report: Report) = labDao.deleteReport(report)

    // ReportResults
    fun getResultsForReport(reportId: Int): Flow<List<ReportResult>> = labDao.getResultsForReport(reportId)
    suspend fun insertReportResult(result: ReportResult): Long = labDao.insertReportResult(result)
    suspend fun insertReportResults(results: List<ReportResult>) = labDao.insertReportResults(results)
    suspend fun deleteResultsByReport(reportId: Int) = labDao.deleteResultsByReport(reportId)

    // Inventory
    val allInventoryItems: Flow<List<InventoryItem>> = labDao.getAllInventoryItems()
    suspend fun insertInventoryItem(item: InventoryItem): Long = labDao.insertInventoryItem(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = labDao.deleteInventoryItem(item)

    // Staff
    val allStaff: Flow<List<Staff>> = labDao.getAllStaff()
    suspend fun insertStaff(staff: Staff): Long = labDao.insertStaff(staff)
    suspend fun deleteStaff(staff: Staff) = labDao.deleteStaff(staff)

    // Commissions
    val allCommissions: Flow<List<DoctorCommission>> = labDao.getAllCommissions()
    suspend fun insertCommission(commission: DoctorCommission): Long = labDao.insertCommission(commission)
    suspend fun deleteCommission(commission: DoctorCommission) = labDao.deleteCommission(commission)
}
