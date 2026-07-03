package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LabDao {
    // Patients
    @Query("SELECT * FROM patients ORDER BY visitDateTime DESC")
    fun getAllPatients(): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): Patient?

    @Delete
    suspend fun deletePatient(patient: Patient)

    // Doctors
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Int): Doctor?

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)

    // LabTests
    @Query("SELECT * FROM lab_tests ORDER BY name ASC")
    fun getAllTests(): Flow<List<LabTest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: LabTest): Long

    @Query("SELECT * FROM lab_tests WHERE id = :id")
    suspend fun getTestById(id: Int): LabTest?

    @Delete
    suspend fun deleteTest(test: LabTest)

    // TestPackages
    @Query("SELECT * FROM test_packages ORDER BY name ASC")
    fun getAllPackages(): Flow<List<TestPackage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: TestPackage): Long

    @Delete
    suspend fun deletePackage(pkg: TestPackage)

    // Reports
    @Query("SELECT * FROM reports ORDER BY dateCreated DESC")
    fun getAllReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Int): Report?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report): Long

    @Delete
    suspend fun deleteReport(report: Report)

    // ReportResults
    @Query("SELECT * FROM report_results WHERE reportId = :reportId")
    fun getResultsForReport(reportId: Int): Flow<List<ReportResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportResult(result: ReportResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportResults(results: List<ReportResult>)

    @Query("DELETE FROM report_results WHERE reportId = :reportId")
    suspend fun deleteResultsByReport(reportId: Int)

    // InventoryItems
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // Staff
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Delete
    suspend fun deleteStaff(staff: Staff)

    // DoctorCommissions
    @Query("SELECT * FROM doctor_commissions ORDER BY date DESC")
    fun getAllCommissions(): Flow<List<DoctorCommission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: DoctorCommission): Long

    @Delete
    suspend fun deleteCommission(commission: DoctorCommission)
}
