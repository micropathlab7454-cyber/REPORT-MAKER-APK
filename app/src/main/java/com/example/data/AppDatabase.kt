package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Patient::class,
        Doctor::class,
        LabTest::class,
        TestPackage::class,
        Report::class,
        ReportResult::class,
        InventoryItem::class,
        Staff::class,
        DoctorCommission::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labDao(): LabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "micro_path_lab_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.labDao()
                                seedTests(dao)
                                seedPackages(dao)
                                seedStaffAndDoctors(dao)
                                seedInventory(dao)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedTests(dao: LabDao) {
            val tests = listOf(
                // Hematology
                LabTest(code = "CBC", name = "Complete Blood Count (CBC)", category = "Hematology", price = 300.0, unit = "g/dL", normalRange = "General: 12.0 - 16.0 | Male: 13.5 - 17.5 | Female: 12.0 - 15.5 | Child: 11.0 - 14.5", method = "Automated Cell Counter"),
                LabTest(code = "ESR", name = "Erythrocyte Sedimentation Rate (ESR)", category = "Hematology", price = 150.0, unit = "mm/hr", normalRange = "0 - 15", method = "Westergren"),
                LabTest(code = "BG", name = "Blood Group (ABO & Rh Typ)", category = "Hematology", price = 100.0, unit = "Group", normalRange = "N/A", method = "Slide Agglutination"),
                LabTest(code = "PS", name = "Peripheral Blood Smear Examination", category = "Hematology", price = 250.0, unit = "N/A", normalRange = "Normocytic Normochromic", method = "Microscopy"),
                
                // Diabetes Profile
                LabTest(code = "HbA1c", name = "Glycated Hemoglobin (HbA1c)", category = "Diabetes Profile", price = 450.0, unit = "%", normalRange = "4.5 - 5.6 (Non-Diabetic)", method = "HPLC"),
                LabTest(code = "FBS", name = "Fasting Blood Sugar (FBS)", category = "Diabetes Profile", price = 80.0, unit = "mg/dL", normalRange = "General: 70 - 100 | Child: 60 - 100", method = "GOD-POD"),
                LabTest(code = "PPBS", name = "Post-Prandial Blood Sugar (PPBS)", category = "Diabetes Profile", price = 80.0, unit = "mg/dL", normalRange = "100 - 140", method = "GOD-POD"),
                
                // Thyroid Profile
                LabTest(code = "TSH", name = "Thyroid Stimulating Hormone (TSH)", category = "Thyroid Profile", price = 350.0, unit = "μIU/mL", normalRange = "0.4 - 4.5", method = "CLIA"),
                LabTest(code = "T3", name = "Triiodothyronine (Total T3)", category = "Thyroid Profile", price = 250.0, unit = "ng/dL", normalRange = "80 - 200", method = "CLIA"),
                LabTest(code = "T4", name = "Thyroxine (Total T4)", category = "Thyroid Profile", price = 250.0, unit = "μg/dL", normalRange = "5.1 - 14.1", method = "CLIA"),

                // Kidney Function Tests
                LabTest(code = "UREA", name = "Blood Urea", category = "Kidney Function Tests", price = 150.0, unit = "mg/dL", normalRange = "15 - 45", method = "Urease GLDH"),
                LabTest(code = "CRE", name = "Serum Creatinine", category = "Kidney Function Tests", price = 150.0, unit = "mg/dL", normalRange = "General: 0.6 - 1.2 | Male: 0.6 - 1.2 | Female: 0.5 - 1.1 | Child: 0.3 - 0.7", method = "Modified Jaffe's"),
                LabTest(code = "UA", name = "Serum Uric Acid", category = "Kidney Function Tests", price = 180.0, unit = "mg/dL", normalRange = "General: 3.5 - 7.2 | Male: 3.5 - 7.2 | Female: 2.6 - 6.0 | Child: 2.0 - 5.5", method = "Uricase"),

                // Liver Function Tests
                LabTest(code = "BIL", name = "Serum Bilirubin (Total & Direct)", category = "Liver Function Tests", price = 250.0, unit = "mg/dL", normalRange = "0.2 - 1.2", method = "Diazo"),
                LabTest(code = "SGOT", name = "Aspartate Aminotransferase (SGOT)", category = "Liver Function Tests", price = 180.0, unit = "U/L", normalRange = "5 - 40", method = "IFCC"),
                LabTest(code = "SGPT", name = "Alanine Aminotransferase (SGPT)", category = "Liver Function Tests", price = 180.0, unit = "U/L", normalRange = "5 - 45", method = "IFCC"),
                LabTest(code = "ALP", name = "Alkaline Phosphatase (ALP)", category = "Liver Function Tests", price = 200.0, unit = "U/L", normalRange = "40 - 129", method = "pNPP"),
                LabTest(code = "PROT", name = "Total Protein & Albumin", category = "Liver Function Tests", price = 250.0, unit = "g/dL", normalRange = "6.0 - 8.3", method = "Biuret / BCG"),

                // Lipid Profile
                LabTest(code = "CHOL", name = "Total Cholesterol", category = "Lipid Profile", price = 150.0, unit = "mg/dL", normalRange = "150 - 200", method = "CHOD-PAP"),
                LabTest(code = "TRIG", name = "Serum Triglycerides", category = "Lipid Profile", price = 180.0, unit = "mg/dL", normalRange = "60 - 150", method = "GPO-PAP"),
                LabTest(code = "HDL", name = "HDL Cholesterol (Good)", category = "Lipid Profile", price = 200.0, unit = "mg/dL", normalRange = "40 - 60", method = "Direct Homogeneous"),
                LabTest(code = "LDL", name = "LDL Cholesterol (Bad)", category = "Lipid Profile", price = 200.0, unit = "mg/dL", normalRange = "80 - 130", method = "Calculated"),

                // Electrolytes
                LabTest(code = "LYTE", name = "Serum Electrolytes (Na+, K+, Cl-)", category = "Electrolytes", price = 350.0, unit = "mmol/L", normalRange = "Na+: 135-145, K+: 3.5-5.0", method = "ISE"),

                // Vitamin Tests
                LabTest(code = "VITD", name = "Vitamin D3 (25-Hydroxy)", category = "Vitamin Tests", price = 1200.0, unit = "ng/mL", normalRange = "30 - 100 (Sufficient)", method = "CLIA"),
                LabTest(code = "VITB12", name = "Vitamin B12", category = "Vitamin Tests", price = 800.0, unit = "pg/mL", normalRange = "211 - 911", method = "CLIA"),

                // Cardiac Markers
                LabTest(code = "TROP", name = "Troponin-I (High Sensitivity)", category = "Cardiac Markers", price = 900.0, unit = "ng/L", normalRange = "< 14", method = "ECLIA"),
                LabTest(code = "CKMB", name = "CK-MB", category = "Cardiac Markers", price = 500.0, unit = "U/L", normalRange = "< 25", method = "Immunoinhibition"),

                // Infectious Diseases
                LabTest(code = "DENG", name = "Dengue NS1 Antigen & IgM", category = "Infectious Disease Tests", price = 650.0, unit = "Index", normalRange = "< 1.0", method = "ELISA"),
                LabTest(code = "TYPH", name = "Typhipoint / Widal", category = "Infectious Disease Tests", price = 250.0, unit = "Titre", normalRange = "< 1:80", method = "Slide Agglutination"),
                LabTest(code = "MAL", name = "Malaria Antigen (Pv / Pf)", category = "Infectious Disease Tests", price = 200.0, unit = "N/A", normalRange = "Negative", method = "Rapid Card"),
                LabTest(code = "MP_WIDAL", name = "MP + Widal Test", category = "Infectious Disease Tests", price = 350.0, unit = "N/A", normalRange = "Negative", method = "Rapid Card & Slide Agglutination"),
                LabTest(code = "HIV", name = "HIV 1 & 2 Antibody Screening", category = "Infectious Disease Tests", price = 350.0, unit = "N/A", normalRange = "Non-Reactive", method = "Rapid / ELISA"),
                LabTest(code = "HBS", name = "HBsAg (Hepatitis B)", category = "Infectious Disease Tests", price = 300.0, unit = "N/A", normalRange = "Non-Reactive", method = "ELISA"),
                LabTest(code = "HCV", name = "HCV (Hepatitis C)", category = "Infectious Disease Tests", price = 400.0, unit = "N/A", normalRange = "Non-Reactive", method = "ELISA"),
                LabTest(code = "CRP", name = "C-Reactive Protein (CRP)", category = "Infectious Disease Tests", price = 350.0, unit = "mg/L", normalRange = "< 6.0", method = "Nephelometry"),
                LabTest(code = "RA", name = "Rheumatoid Arthritis (RA) Factor", category = "Infectious Disease Tests", price = 300.0, unit = "IU/mL", normalRange = "< 15.0", method = "Nephelometry"),

                // Urine & Stool Tests
                LabTest(code = "URINE", name = "Urine Routine & Microscopy", category = "Urine Tests", price = 120.0, unit = "N/A", normalRange = "Normal", method = "Microscopy"),
                LabTest(code = "STOOL", name = "Stool Routine & Occult Blood", category = "Stool Tests", price = 150.0, unit = "N/A", normalRange = "Normal", method = "Microscopy"),

                // Tumor Markers
                LabTest(code = "PSA", name = "Prostate Specific Antigen (PSA)", category = "Tumor Markers", price = 700.0, unit = "ng/mL", normalRange = "0.0 - 4.0", method = "CLIA")
            )
            tests.forEach { dao.insertTest(it) }
        }

        private suspend fun seedPackages(dao: LabDao) {
            val packages = listOf(
                TestPackage(name = "Full Body Checkup", testCodes = "CBC, FBS, TSH, CHOL, TRIG, UREA, CRE, SGPT, SGOT, BIL", price = 2000.0),
                TestPackage(name = "Fever Profile", testCodes = "CBC, ESR, DENG, TYPH, MAL, URINE", price = 1100.0),
                TestPackage(name = "Diabetes Package", testCodes = "FBS, PPBS, HbA1c", price = 500.0),
                TestPackage(name = "Thyroid Package", testCodes = "T3, T4, TSH", price = 600.0),
                TestPackage(name = "Kidney Package", testCodes = "UREA, CRE, UA, LYTE, URINE", price = 800.0),
                TestPackage(name = "Liver Package", testCodes = "BIL, SGOT, SGPT, ALP, PROT", price = 850.0),
                TestPackage(name = "Heart Package", testCodes = "CHOL, TRIG, HDL, LDL, TROP, CKMB", price = 1800.0)
            )
            packages.forEach { dao.insertPackage(it) }
        }

        private suspend fun seedStaffAndDoctors(dao: LabDao) {
            // Default logins:
            // Admin: admin / admin
            // Receptionist: recep / 123
            // Technician: tech / 123
            // Pathologist: patho / 123
            val staffList = listOf(
                Staff(name = "System Admin", username = "admin", password = "admin", role = "Admin"),
                Staff(name = "Aanya Sharma", username = "recep", password = "123", role = "Receptionist"),
                Staff(name = "Rahul Varma", username = "tech", password = "123", role = "Lab Technician"),
                Staff(name = "Dr. Sameer Kapoor, MD", username = "patho", password = "123", role = "Pathologist")
            )
            staffList.forEach { dao.insertStaff(it) }

            // Default Doctors
            val doctors = listOf(
                Doctor(name = "Dr. Amit Roy", specialization = "Cardiologist", mobile = "9876543210", commissionPercentage = 15.0),
                Doctor(name = "Dr. Priya Sen", specialization = "General Physician", mobile = "8765432109", commissionPercentage = 10.0),
                Doctor(name = "Dr. Rajesh Gupta", specialization = "Pediatrician", mobile = "7654321098", commissionPercentage = 12.5),
                Doctor(name = "Self / Direct Walk-in", specialization = "None", mobile = "0000000000", commissionPercentage = 0.0)
            )
            doctors.forEach { dao.insertDoctor(it) }
        }

        private suspend fun seedInventory(dao: LabDao) {
            val items = listOf(
                InventoryItem(name = "CBC Reagent Pack", category = "Reagents", quantity = 15, minQuantity = 5, expiryDate = "2027-01-15", supplierName = "Sysmex Diagnostics", supplierContact = "+91 9999999991"),
                InventoryItem(name = "EDTA Blood Collection Tubes (2ml)", category = "Tubes", quantity = 250, minQuantity = 50, expiryDate = "2028-06-30", supplierName = "BD Healthcare", supplierContact = "+91 9999999992"),
                InventoryItem(name = "Sodium Fluoride Tubes (Sugar)", category = "Tubes", quantity = 120, minQuantity = 30, expiryDate = "2028-05-15", supplierName = "BD Healthcare", supplierContact = "+91 9999999992"),
                InventoryItem(name = "Dengue NS1 Antigen Rapid Kits", category = "Test Kits", quantity = 8, minQuantity = 10, expiryDate = "2026-09-10", supplierName = "SD Biosensor", supplierContact = "+91 9999999993"),
                InventoryItem(name = "HIV 1/2 Triline Rapid Kits", category = "Test Kits", quantity = 35, minQuantity = 15, expiryDate = "2027-03-22", supplierName = "J. Mitra & Co.", supplierContact = "+91 9999999994"),
                InventoryItem(name = "Ethanol / Spirit Disinfectant", category = "Chemicals", quantity = 4, minQuantity = 2, expiryDate = "2029-12-01", supplierName = "Merck Chemicals", supplierContact = "+91 9999999995"),
                InventoryItem(name = "Urine Sterile Containers", category = "Consumables", quantity = 40, minQuantity = 50, expiryDate = "2030-01-01", supplierName = "BD Healthcare", supplierContact = "+91 9999999992")
            )
            items.forEach { dao.insertInventoryItem(it) }
        }
    }
}
