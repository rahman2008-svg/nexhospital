package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_info")
data class HospitalEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val logoPath: String = "",
    val type: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val licenseNumber: String = ""
)

@Entity(tableName = "admin_account")
data class AdminEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val password: String = "",
    val pin: String = "",
    val fingerprintEnabled: Boolean = false
)

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientIdString: String = "",
    val admissionNo: String = "",
    val status: String = "Admitted", // Admitted, ICU, Observation, Emergency, Discharged
    val photoUri: String? = null,
    val admissionDate: String = "",
    val admissionTime: String = "",
    val name: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val dob: String = "",
    val age: Int = 0,
    val gender: String = "Male",
    val mobile: String = "",
    val address: String = "",
    val district: String = "",
    val subDistrict: String = "",
    val nid: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val diseaseName: String = "",
    val primaryComplaint: String = "",
    val doctorName: String = "",
    val department: String = "",
    val ward: String = "",
    val cabin: String = "",
    val bedNumber: String = "",
    val bloodGroup: String = "",
    val allergy: String = "",
    val height: String = "",
    val weight: String = "",
    val dischargeDate: String? = null,
    val dischargeTime: String? = null,
    val finalDiagnosis: String? = null,
    val dischargeRemarks: String? = null,
    val followUpDate: String? = null,
    val dischargeSummary: String? = null
)

@Entity(tableName = "patient_notes")
data class PatientNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientIdString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val timeString: String = "",
    val type: String = "Note", // Admission, Visit, Report, Medicine, Note, Discharge, Vital
    val title: String = "",
    val content: String = "",
    val bp: String? = null,
    val temp: String? = null,
    val pulse: String? = null,
    val oxygen: String? = null,
    val sugar: String? = null
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientIdString: String,
    val doctorName: String = "",
    val dateString: String = "",
    val medicineName: String = "",
    val dosage: String = "", // e.g. 1+0+1
    val timing: String = "", // e.g. After Meal
    val durationDays: Int = 0
)

@Entity(tableName = "billing")
data class BillingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientIdString: String,
    val admissionFee: Double = 0.0,
    val bedCharge: Double = 0.0,
    val doctorFee: Double = 0.0,
    val medicineFee: Double = 0.0,
    val labTestFee: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // Doctor, Nurse, Receptionist, Accountant
    val mobile: String = "",
    val email: String = "",
    val specialty: String? = null
)
