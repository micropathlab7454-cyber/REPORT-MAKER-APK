package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,       // Auto-generated like PT-10001
    val regNo: String,           // Auto-generated registration number
    val name: String,
    val age: Int,
    val dob: String,
    val gender: String,
    val mobile: String,
    val whatsApp: String,
    val address: String,
    val email: String,
    val aadhaar: String,         // Optional
    val referringDoctor: String,
    val visitDateTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialization: String,
    val mobile: String,
    val commissionPercentage: Double
)

@Entity(tableName = "lab_tests")
data class LabTest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,            // E.g., "CBC", "TSH", "GLU"
    val name: String,
    val category: String,        // E.g., Hematology, Biochemistry
    val price: Double,
    val unit: String,            // E.g., "g/dL", "mg/dL", "μIU/mL"
    val normalRange: String,     // E.g., "12.0 - 16.0", "0.4 - 4.5"
    val method: String,          // E.g., "Automated Cell Counter", "CLIA"
    val isCustom: Boolean = false
)

@Entity(tableName = "test_packages")
data class TestPackage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val testCodes: String,       // Comma-separated list of test codes or names
    val price: Double,
    val isCustom: Boolean = false
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reportNo: String,        // Auto-generated like RP-260702-001
    val patientId: Int,          // FK from Patient
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val patientMobile: String,
    val referringDoctor: String,
    val testIds: String,         // Comma-separated test IDs
    val packageIds: String,      // Comma-separated package IDs
    val totalAmount: Double,
    val discount: Double,
    val gst: Double,             // Optional GST
    val finalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentStatus: String,   // "Paid", "Pending"
    val paymentMode: String,     // "Cash", "Card", "UPI"
    val sampleType: String,      // "Blood", "Urine", "Stool", etc.
    val sampleStatus: String,    // "Collected", "In Process", "Completed"
    val sampleBarcode: String,   // E.g., BAR-48937
    val collectionTime: Long,
    val technicianName: String,
    val approvedBy: String,      // Doctor or Pathologist name
    val signatureUrl: String,    // Digital signature marker / path
    val isDraft: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "report_results")
data class ReportResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reportId: Int,           // FK from Report
    val testId: Int,             // FK from LabTest
    val testName: String,
    val category: String,
    val resultValue: String,
    val unit: String,
    val normalRange: String,
    val isAbnormal: Boolean = false,
    val remarks: String,
    val method: String
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,        // Reagents, Chemicals, Test Kits, Tubes, Consumables
    val quantity: Int,
    val minQuantity: Int,        // Low Stock Alert threshold
    val expiryDate: String,      // YYYY-MM-DD
    val supplierName: String,
    val supplierContact: String
)

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val password: String,
    val role: String             // Admin, Receptionist, Lab Technician, Pathologist
)

@Entity(tableName = "doctor_commissions")
data class DoctorCommission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val doctorName: String,
    val reportId: Int,
    val reportNo: String,
    val patientName: String,
    val billingAmount: Double,
    val commissionAmount: Double,
    val date: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false
)
