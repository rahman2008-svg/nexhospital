package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Welcome : Screen()
    object SetupHospital : Screen()
    object SetupAdmin : Screen()
    object Login : Screen()
    object Dashboard : Screen()
    object PatientList : Screen()
    object AddPatient : Screen()
    data class EditPatient(val patient: PatientEntity) : Screen()
    data class PatientProfile(val patient: PatientEntity) : Screen()
    object Reports : Screen()
    object StaffManagement : Screen()
    object Settings : Screen()
}

class HospitalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HospitalRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = HospitalRepository(db)
    }

    // Settings & Configuration
    val hospitalInfo = repository.hospitalInfo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val adminAccount = repository.adminAccount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allPatients = repository.allPatients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStaff = repository.allStaff.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State
    val currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val selectedDate = MutableStateFlow("15 July 2026")
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("All") // All, Admitted, Discharged, ICU, Cabin, General Ward, Emergency, Male, Female, Child
    val selectedSort = MutableStateFlow("Admission Time") // Admission Time, Name, Patient ID, Bed Number, Doctor, Disease
    val themeMode = MutableStateFlow("Light") // Light, Dark
    val languageMode = MutableStateFlow("Bangla") // Bangla, English

    // Detail UI state
    val selectedPatientId = MutableStateFlow<String?>(null)
    
    val selectedPatientNotes = selectedPatientId.flatMapLatest { id ->
        if (id != null) repository.getNotesForPatient(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPatientPrescriptions = selectedPatientId.flatMapLatest { id ->
        if (id != null) repository.getPrescriptionsForPatient(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPatientBilling = selectedPatientId.flatMapLatest { id ->
        if (id != null) repository.getBillingForPatient(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Check configuration and move to appropriate initial screen
    fun checkAppConfig() {
        viewModelScope.launch {
            val hospital = repository.getHospitalInfoOnce()
            val admin = repository.getAdminAccountOnce()
            if (hospital == null) {
                currentScreen.value = Screen.Welcome
            } else if (admin == null) {
                currentScreen.value = Screen.SetupAdmin
            } else {
                currentScreen.value = Screen.Login
            }
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }

    fun selectPatient(patient: PatientEntity) {
        selectedPatientId.value = patient.patientIdString
        navigateTo(Screen.PatientProfile(patient))
    }

    // Setup Actions
    fun saveHospitalSetup(hospital: HospitalEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateHospital(hospital)
            currentScreen.value = Screen.SetupAdmin
        }
    }

    fun saveAdminSetup(admin: AdminEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateAdmin(admin)
            currentScreen.value = Screen.Login
        }
    }

    // Direct Quick Skip with Mock Demo Data
    fun skipAndLoadDemoData() {
        viewModelScope.launch {
            // Setup Hospital
            val demoHospital = HospitalEntity(
                name = "NexHospital General",
                type = "General Hospital",
                address = "Dhanmondi, Dhaka",
                phone = "01712345678",
                email = "info@nexhospital.com",
                website = "www.nexhospital.com",
                licenseNumber = "LIC-2026-98765"
            )
            repository.insertOrUpdateHospital(demoHospital)

            // Setup Admin
            val demoAdmin = AdminEntity(
                name = "Super Admin",
                mobile = "01700000000",
                email = "admin@hospital.com",
                password = "admin",
                pin = "1234",
                fingerprintEnabled = true
            )
            repository.insertOrUpdateAdmin(demoAdmin)

            // Load Demo Patients for July 15, 2026
            val sdfDate = "15 July 2026"
            
            // Insert exact user specified patients
            val p1 = PatientEntity(
                patientIdString = "PT-20260715-0001",
                admissionNo = "ADM-260715001",
                status = "Admitted",
                name = "Abdur Rahman",
                fatherName = "Md. Karim",
                motherName = "Rahima Begum",
                dob = "02-05-1992",
                age = 34,
                gender = "Male",
                mobile = "01711111111",
                address = "Dhaka",
                district = "Dhaka",
                subDistrict = "Dhanmondi",
                nid = "199245127814",
                guardianName = "Md. Rafiq",
                guardianPhone = "01911111111",
                diseaseName = "Typhoid Fever",
                primaryComplaint = "High fever for 5 days, severe headache and weakness",
                doctorName = "Dr. Hasan",
                department = "Medicine",
                ward = "Male Medicine",
                cabin = "None",
                bedNumber = "B-12",
                bloodGroup = "B+",
                allergy = "Penicillin",
                height = "170 cm",
                weight = "68 kg"
            )
            val p2 = PatientEntity(
                patientIdString = "PT-20260715-0002",
                admissionNo = "ADM-260715002",
                status = "ICU",
                name = "Rafiq",
                fatherName = "Siddique Ali",
                motherName = "Rabeya Begum",
                dob = "10-08-1980",
                age = 45,
                gender = "Male",
                mobile = "01811111111",
                address = "Dhaka",
                district = "Dhaka",
                subDistrict = "Mirpur",
                nid = "198076541234",
                guardianName = "Ayesha Akhter",
                guardianPhone = "01511111111",
                diseaseName = "Stroke",
                primaryComplaint = "Sudden paralysis on left side, slurred speech",
                doctorName = "Dr. Imran",
                department = "Neurology",
                ward = "ICU",
                cabin = "None",
                bedNumber = "ICU-04",
                bloodGroup = "A+",
                allergy = "None",
                height = "165 cm",
                weight = "72 kg"
            )
            val p3 = PatientEntity(
                patientIdString = "PT-20260715-0003",
                admissionNo = "ADM-260715003",
                status = "Discharged",
                name = "Shafiq",
                fatherName = "Abdul Latif",
                motherName = "Amena Khatun",
                dob = "15-12-1967",
                age = 58,
                gender = "Male",
                mobile = "01999999999",
                address = "Sylhet",
                district = "Sylhet",
                subDistrict = "Beanibazar",
                nid = "196711223344",
                guardianName = "Kamal Hossain",
                guardianPhone = "01611111111",
                diseaseName = "Diabetes",
                primaryComplaint = "Uncontrolled blood sugar, extreme fatigue",
                doctorName = "Dr. Sayem",
                department = "Endocrinology",
                ward = "Cabin",
                cabin = "C-105",
                bedNumber = "C-105",
                bloodGroup = "O+",
                allergy = "Sulfa drugs",
                height = "172 cm",
                weight = "75 kg",
                dischargeDate = "15 July 2026",
                dischargeTime = "4:20 PM",
                finalDiagnosis = "Type 2 Diabetes with hyperglycemia",
                dischargeRemarks = "Blood sugar stabilized, continue oral medication and insulin.",
                followUpDate = "22 July 2026",
                dischargeSummary = "Patient discharged in stable condition. Diet plan given."
            )

            repository.insertPatient(p1)
            repository.insertPatient(p2)
            repository.insertPatient(p3)

            // Let's seed 15 more admissions and 11 more discharges to make it admissions=18, discharges=12!
            // Admitted patients (total 18: p1, p2, plus 16 more)
            val extraAdmitted = listOf(
                Pair("Siam", "Pneumonia"), Pair("Karim", "Dengue"), Pair("Rubel", "Kidney Stone"),
                Pair("Nipa", "Asthma Attack"), Pair("Asif", "Appendicitis"), Pair("Salma", "Maternity"),
                Pair("Jamil", "Hernia"), Pair("Keya", "Migraine"), Pair("Emon", "Dengue"),
                Pair("Anika", "Typhoid"), Pair("Hasan", "Fractured Leg"), Pair("Mitu", "Observation"),
                Pair("Riaz", "Gastric Ulcer"), Pair("Shumi", "Pregnancy"), Pair("Parvez", "Heart Attack"),
                Pair("Rita", "Fever")
            )
            extraAdmitted.forEachIndexed { i, item ->
                val gender = if (i % 3 == 0) "Female" else "Male"
                val idStr = String.format("PT-20260715-%04d", i + 4)
                val admStr = String.format("ADM-260715%03d", i + 4)
                val status = if (i == 14) "Emergency" else "Admitted"
                val ward = if (status == "Emergency") "Emergency Ward" else if (i % 2 == 0) "Male Medicine" else "Female Ward"
                repository.insertPatient(PatientEntity(
                    patientIdString = idStr,
                    admissionNo = admStr,
                    status = status,
                    name = item.first,
                    dob = "01-01-1995",
                    age = 22 + i,
                    gender = gender,
                    mobile = "017" + String.format("%08d", 100000 + i),
                    address = "Dhaka",
                    district = "Dhaka",
                    diseaseName = item.second,
                    primaryComplaint = "Admitted for " + item.second,
                    doctorName = "Dr. Hasan",
                    department = "General Medicine",
                    ward = ward,
                    bedNumber = "B-${i + 20}",
                    admissionDate = sdfDate,
                    admissionTime = "${8 + (i % 4)}:${(10 * i) % 60} AM"
                ))
            }

            // Discharged patients (total 12: p3, plus 11 more)
            val extraDischarged = listOf(
                Pair("Hamid", "Dengue"), Pair("Jesmin", "Jaundice"), Pair("Fahim", "Diarrhea"),
                Pair("Babul", "Chest Pain"), Pair("Farhana", "Fever"), Pair("Momin", "Asthma"),
                Pair("Lima", "Pregnancy"), Pair("Sumon", "Cold"), Pair("Koly", "Headache"),
                Pair("Sajal", "Allergy"), Pair("Rokeya", "High BP")
            )
            extraDischarged.forEachIndexed { i, item ->
                val gender = if (i % 2 == 0) "Female" else "Male"
                val idStr = String.format("PT-20260715-%04d", i + 20)
                val admStr = String.format("ADM-260715%03d", i + 20)
                repository.insertPatient(PatientEntity(
                    patientIdString = idStr,
                    admissionNo = admStr,
                    status = "Discharged",
                    name = item.first,
                    dob = "01-01-1985",
                    age = 30 + i,
                    gender = gender,
                    mobile = "017" + String.format("%08d", 200000 + i),
                    address = "Dhaka",
                    district = "Dhaka",
                    diseaseName = item.second,
                    primaryComplaint = "Resolved " + item.second,
                    doctorName = "Dr. Karim",
                    department = "General Medicine",
                    ward = "Cabin",
                    cabin = "C-11$i",
                    bedNumber = "C-11$i",
                    admissionDate = sdfDate,
                    admissionTime = "7:15 AM",
                    dischargeDate = sdfDate,
                    dischargeTime = "${2 + (i % 3)}:${(15 * i) % 60} PM",
                    finalDiagnosis = "Cured from " + item.second,
                    dischargeRemarks = "Stable condition. Normal reports."
                ))
            }

            // Add Notes, Billing and Prescriptions to the primary patient Abdur Rahman (PT-20260715-0001)
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "8:30 AM",
                type = "Admission",
                title = "Patient Admitted",
                content = "Admitted to Male Medicine Ward, Bed B-12 under Dr. Hasan."
            ))
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "10:00 AM",
                type = "Visit",
                title = "Doctor Checked",
                content = "Dr. Hasan checked the patient. Diagnosed with suspected Typhoid. Ordered blood test (CBC & Widal)."
            ))
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "12:00 PM",
                type = "Report",
                title = "Blood Test Sample Taken",
                content = "Blood sample collected for CBC, Widal and Blood Culture."
            ))
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "3:00 PM",
                type = "Medicine",
                title = "Medicine Started",
                content = "Paracetamol 650mg given. IV saline started."
            ))
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "6:00 PM",
                type = "Report",
                title = "Lab Report Received",
                content = "Widal Test positive for S. Typhi. WBC counts normal."
            ))
            repository.insertNote(PatientNoteEntity(
                patientIdString = "PT-20260715-0001",
                dateString = "15 July 2026",
                timeString = "9:00 PM",
                type = "Visit",
                title = "Nurse Visit",
                content = "Temperature checked: 101F. Administered next dose of Paracetamol. Resting comfortably."
            ))

            // Billing for Abdur Rahman
            repository.insertOrUpdateBilling(BillingEntity(
                patientIdString = "PT-20260715-0001",
                admissionFee = 500.0,
                bedCharge = 1200.0,
                doctorFee = 800.0,
                medicineFee = 1500.0,
                labTestFee = 2200.0,
                discount = 200.0,
                totalAmount = 6000.0,
                paidAmount = 4500.0,
                dueAmount = 1500.0
            ))

            // Prescriptions for Abdur Rahman
            repository.insertPrescription(PrescriptionEntity(
                patientIdString = "PT-20260715-0001",
                doctorName = "Dr. Hasan",
                dateString = "15 July 2026",
                medicineName = "Tab Paracetamol 650mg",
                dosage = "1+1+1",
                timing = "After Meal",
                durationDays = 5
            ))
            repository.insertPrescription(PrescriptionEntity(
                patientIdString = "PT-20260715-0001",
                doctorName = "Dr. Hasan",
                dateString = "15 July 2026",
                medicineName = "Cap Cefixime 400mg",
                dosage = "1+0+1",
                timing = "After Meal",
                durationDays = 7
            ))

            // Seed Staff members
            repository.insertStaff(StaffEntity(name = "Dr. Hasan", role = "Doctor", specialty = "General Medicine"))
            repository.insertStaff(StaffEntity(name = "Dr. Imran", role = "Doctor", specialty = "Neurology"))
            repository.insertStaff(StaffEntity(name = "Dr. Sayem", role = "Doctor", specialty = "Endocrinology"))
            repository.insertStaff(StaffEntity(name = "Nurse Maria", role = "Nurse", mobile = "01722222222"))
            repository.insertStaff(StaffEntity(name = "Nurse Lipi", role = "Nurse", mobile = "01733333333"))
            repository.insertStaff(StaffEntity(name = "Rahim Mia", role = "Receptionist", mobile = "01744444444"))
            repository.insertStaff(StaffEntity(name = "Sujon Dey", role = "Accountant", mobile = "01755555555"))

            // Redirect to Login
            currentScreen.value = Screen.Login
        }
    }

    // Patient Actions
    fun addNewPatient(patient: PatientEntity, bill: BillingEntity? = null) {
        viewModelScope.launch {
            val autoCount = allPatients.value.count { it.admissionDate == patient.admissionDate }
            val pid = if (patient.patientIdString.isEmpty()) generatePatientId(patient.admissionDate, autoCount) else patient.patientIdString
            val admNo = if (patient.admissionNo.isEmpty()) generateAdmissionNo(patient.admissionDate, autoCount) else patient.admissionNo
            
            val updatedPatient = patient.copy(
                patientIdString = pid,
                admissionNo = admNo
            )
            val pidGenerated = repository.insertPatient(updatedPatient)
            
            // Insert default notes for admission
            repository.insertNote(PatientNoteEntity(
                patientIdString = pid,
                dateString = patient.admissionDate,
                timeString = patient.admissionTime,
                type = "Admission",
                title = "Patient Admitted",
                content = "Admitted to ${patient.ward} ${if (patient.cabin != "None") "Cabin: ${patient.cabin}" else ""} Bed: ${patient.bedNumber} under ${patient.doctorName}."
            ))

            // If BP or pulse provided in form, add a vital log
            if (patient.bloodGroup.isNotEmpty() || patient.height.isNotEmpty() || patient.weight.isNotEmpty()) {
                repository.insertNote(PatientNoteEntity(
                    patientIdString = pid,
                    dateString = patient.admissionDate,
                    timeString = patient.admissionTime,
                    type = "Vital",
                    title = "Initial Vitals",
                    content = "Blood Group: ${patient.bloodGroup}, Height: ${patient.height}, Weight: ${patient.weight}"
                ))
            }

            // Insert default billing
            val billing = bill ?: BillingEntity(
                patientIdString = pid,
                admissionFee = 500.0,
                bedCharge = if (patient.ward == "ICU") 5000.0 else if (patient.cabin != "None") 3000.0 else 1000.0,
                doctorFee = 1000.0,
                medicineFee = 0.0,
                labTestFee = 0.0,
                discount = 0.0,
                totalAmount = if (patient.ward == "ICU") 6000.0 else if (patient.cabin != "None") 4500.0 else 2500.0,
                paidAmount = 1000.0,
                dueAmount = if (patient.ward == "ICU") 5000.0 else if (patient.cabin != "None") 3500.0 else 1500.0
            )
            repository.insertOrUpdateBilling(billing)

            currentScreen.value = Screen.Dashboard
        }
    }

    fun updatePatient(patient: PatientEntity) {
        viewModelScope.launch {
            repository.updatePatient(patient)
            selectedPatientId.value = patient.patientIdString
            currentScreen.value = Screen.PatientProfile(patient)
        }
    }

    fun deletePatient(patient: PatientEntity) {
        viewModelScope.launch {
            repository.deletePatient(patient)
            currentScreen.value = Screen.Dashboard
        }
    }

    // Patient Note / Vital History
    fun addPatientNote(note: PatientNoteEntity) {
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }

    // Patient Prescription
    fun addPatientPrescription(prescription: PrescriptionEntity) {
        viewModelScope.launch {
            repository.insertPrescription(prescription)
        }
    }

    // Patient Bed Transfer
    fun transferPatientBed(patient: PatientEntity, oldBed: String, newBed: String, newWard: String, newCabin: String) {
        viewModelScope.launch {
            val updated = patient.copy(
                ward = newWard,
                cabin = newCabin,
                bedNumber = newBed
            )
            repository.updatePatient(updated)
            
            // Add timeline note
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
            val stf = SimpleDateFormat("h:mm a", Locale.US)
            val curDate = sdf.format(Date())
            val curTime = stf.format(Date())
            
            repository.insertNote(PatientNoteEntity(
                patientIdString = patient.patientIdString,
                dateString = curDate,
                timeString = curTime,
                type = "Visit",
                title = "Bed Transferred",
                content = "Bed changed from $oldBed to $newBed ($newWard ${if (newCabin != "None") "Cabin: $newCabin" else ""})"
            ))
            
            selectedPatientId.value = patient.patientIdString
            currentScreen.value = Screen.PatientProfile(updated)
        }
    }

    // Patient Discharge
    fun dischargePatient(
        patient: PatientEntity, 
        dischargeDate: String, 
        dischargeTime: String, 
        finalDiagnosis: String, 
        doctorRemarks: String, 
        followUpDate: String, 
        summary: String
    ) {
        viewModelScope.launch {
            val updated = patient.copy(
                status = "Discharged",
                dischargeDate = dischargeDate,
                dischargeTime = dischargeTime,
                finalDiagnosis = finalDiagnosis,
                dischargeRemarks = doctorRemarks,
                followUpDate = followUpDate,
                dischargeSummary = summary
            )
            repository.updatePatient(updated)

            // Add notes for discharge
            repository.insertNote(PatientNoteEntity(
                patientIdString = patient.patientIdString,
                dateString = dischargeDate,
                timeString = dischargeTime,
                type = "Discharge",
                title = "Patient Discharged",
                content = "Patient discharged. Final Diagnosis: $finalDiagnosis. Doctor Remarks: $doctorRemarks. Follow up on: $followUpDate."
            ))

            // Update Billing (make paid amount full or mock final payment)
            val currentBill = repository.getBillingForPatientOnce(patient.patientIdString)
            if (currentBill != null) {
                val finalBill = currentBill.copy(
                    paidAmount = currentBill.totalAmount,
                    dueAmount = 0.0
                )
                repository.insertOrUpdateBilling(finalBill)
            }

            selectedPatientId.value = patient.patientIdString
            currentScreen.value = Screen.PatientProfile(updated)
        }
    }

    // Billing updates
    fun updateBilling(billing: BillingEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateBilling(billing)
        }
    }

    // Manage Staff
    fun addStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.insertStaff(staff)
        }
    }

    fun removeStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }

    // Generator helpers for Patient ID and Admission Number
    private fun generatePatientId(dateString: String, count: Int): String {
        val cleanDate = "20260715"
        val parts = dateString.split(" ")
        val yyyymmdd = if (parts.size >= 3) {
            val day = parts[0].padStart(2, '0')
            val monthStr = parts[1].lowercase()
            val year = parts[2]
            val month = when {
                monthStr.contains("jan") || monthStr.contains("জানু") -> "01"
                monthStr.contains("feb") || monthStr.contains("ফেব্রু") -> "02"
                monthStr.contains("mar") || monthStr.contains("মার্চ") -> "03"
                monthStr.contains("apr") || monthStr.contains("এপ্রিল") -> "04"
                monthStr.contains("may") || monthStr.contains("মে") -> "05"
                monthStr.contains("jun") || monthStr.contains("জুন") -> "06"
                monthStr.contains("jul") || monthStr.contains("জুলাই") -> "07"
                monthStr.contains("aug") || monthStr.contains("আগস্ট") -> "08"
                monthStr.contains("sep") || monthStr.contains("সেপ্টে") -> "09"
                monthStr.contains("oct") || monthStr.contains("অক্টো") -> "10"
                monthStr.contains("nov") || monthStr.contains("নভে") -> "11"
                monthStr.contains("dec") || monthStr.contains("ডিসে") -> "12"
                else -> "07"
            }
            "$year$month$day"
        } else {
            cleanDate
        }
        val seq = String.format("%04d", count + 1)
        return "PT-$yyyymmdd-$seq"
    }

    private fun generateAdmissionNo(dateString: String, count: Int): String {
        val parts = dateString.split(" ")
        val yymmdd = if (parts.size >= 3) {
            val day = parts[0].padStart(2, '0')
            val monthStr = parts[1].lowercase()
            val year = parts[2]
            val yy = if (year.length >= 4) year.substring(2) else "26"
            val month = when {
                monthStr.contains("jan") || monthStr.contains("জানু") -> "01"
                monthStr.contains("feb") || monthStr.contains("ফেব্রু") -> "02"
                monthStr.contains("mar") || monthStr.contains("মার্চ") -> "03"
                monthStr.contains("apr") || monthStr.contains("এপ্রিল") -> "04"
                monthStr.contains("may") || monthStr.contains("মে") -> "05"
                monthStr.contains("jun") || monthStr.contains("জুন") -> "06"
                monthStr.contains("jul") || monthStr.contains("জুলাই") -> "07"
                monthStr.contains("aug") || monthStr.contains("আগস্ট") -> "08"
                monthStr.contains("sep") || monthStr.contains("সেপ্টে") -> "09"
                monthStr.contains("oct") || monthStr.contains("অক্টো") -> "10"
                monthStr.contains("nov") || monthStr.contains("নভে") -> "11"
                monthStr.contains("dec") || monthStr.contains("ডিসে") -> "12"
                else -> "07"
            }
            "$yy$month$day"
        } else {
            "260715"
        }
        val seq = String.format("%03d", count + 1)
        return "ADM-$yymmdd$seq"
    }

    // Localization Map
    fun trans(bangla: String, english: String): String {
        return if (languageMode.value == "Bangla") bangla else english
    }
}
