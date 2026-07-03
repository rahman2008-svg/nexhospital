package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospital_info WHERE id = 1 LIMIT 1")
    fun getHospitalInfo(): Flow<HospitalEntity?>

    @Query("SELECT * FROM hospital_info WHERE id = 1 LIMIT 1")
    suspend fun getHospitalInfoOnce(): HospitalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(hospital: HospitalEntity)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_account WHERE id = 1 LIMIT 1")
    fun getAdminAccount(): Flow<AdminEntity?>

    @Query("SELECT * FROM admin_account WHERE id = 1 LIMIT 1")
    suspend fun getAdminAccountOnce(): AdminEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(admin: AdminEntity)
}

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY id DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun getPatientById(id: Int): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE patientIdString = :patientId LIMIT 1")
    fun getPatientByStringId(patientId: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE patientIdString = :patientId LIMIT 1")
    suspend fun getPatientByStringIdOnce(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE admissionDate = :dateString")
    fun getPatientsForDate(dateString: String): Flow<List<PatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    @Query("SELECT COUNT(*) FROM patients")
    fun getPatientsCountFlow(): Flow<Int>
}

@Dao
interface PatientNoteDao {
    @Query("SELECT * FROM patient_notes WHERE patientIdString = :patientId ORDER BY timestamp DESC")
    fun getNotesForPatient(patientId: String): Flow<List<PatientNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PatientNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: PatientNoteEntity)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE patientIdString = :patientId ORDER BY id DESC")
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long

    @Delete
    suspend fun deletePrescription(prescription: PrescriptionEntity)
}

@Dao
interface BillingDao {
    @Query("SELECT * FROM billing WHERE patientIdString = :patientId LIMIT 1")
    fun getBillingForPatient(patientId: String): Flow<BillingEntity?>

    @Query("SELECT * FROM billing WHERE patientIdString = :patientId LIMIT 1")
    suspend fun getBillingForPatientOnce(patientId: String): BillingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBilling(billing: BillingEntity): Long
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)
}
