package com.example.data

import kotlinx.coroutines.flow.Flow

class HospitalRepository(private val db: AppDatabase) {
    val hospitalDao = db.hospitalDao()
    val adminDao = db.adminDao()
    val patientDao = db.patientDao()
    val patientNoteDao = db.patientNoteDao()
    val prescriptionDao = db.prescriptionDao()
    val billingDao = db.billingDao()
    val staffDao = db.staffDao()

    val hospitalInfo: Flow<HospitalEntity?> = hospitalDao.getHospitalInfo()
    val adminAccount: Flow<AdminEntity?> = adminDao.getAdminAccount()
    val allPatients: Flow<List<PatientEntity>> = patientDao.getAllPatients()
    val allStaff: Flow<List<StaffEntity>> = staffDao.getAllStaff()

    suspend fun getHospitalInfoOnce(): HospitalEntity? = hospitalDao.getHospitalInfoOnce()
    suspend fun getAdminAccountOnce(): AdminEntity? = adminDao.getAdminAccountOnce()

    fun getPatientById(id: Int): Flow<PatientEntity?> = patientDao.getPatientById(id)
    fun getPatientByStringId(id: String): Flow<PatientEntity?> = patientDao.getPatientByStringId(id)
    suspend fun getPatientByStringIdOnce(id: String): PatientEntity? = patientDao.getPatientByStringIdOnce(id)

    fun getNotesForPatient(patientId: String): Flow<List<PatientNoteEntity>> = patientNoteDao.getNotesForPatient(patientId)
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>> = prescriptionDao.getPrescriptionsForPatient(patientId)
    fun getBillingForPatient(patientId: String): Flow<BillingEntity?> = billingDao.getBillingForPatient(patientId)
    suspend fun getBillingForPatientOnce(patientId: String): BillingEntity? = billingDao.getBillingForPatientOnce(patientId)
    fun getPatientsForDate(dateString: String): Flow<List<PatientEntity>> = patientDao.getPatientsForDate(dateString)

    suspend fun insertOrUpdateHospital(hospital: HospitalEntity) = hospitalDao.insertOrUpdate(hospital)
    suspend fun insertOrUpdateAdmin(admin: AdminEntity) = adminDao.insertOrUpdate(admin)

    suspend fun insertPatient(patient: PatientEntity): Long = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: PatientEntity) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: PatientEntity) = patientDao.deletePatient(patient)

    suspend fun insertNote(note: PatientNoteEntity): Long = patientNoteDao.insertNote(note)
    suspend fun deleteNote(note: PatientNoteEntity) = patientNoteDao.deleteNote(note)

    suspend fun insertPrescription(prescription: PrescriptionEntity): Long = prescriptionDao.insertPrescription(prescription)
    suspend fun deletePrescription(prescription: PrescriptionEntity) = prescriptionDao.deletePrescription(prescription)

    suspend fun insertOrUpdateBilling(billing: BillingEntity): Long = billingDao.insertOrUpdateBilling(billing)

    suspend fun insertStaff(staff: StaffEntity): Long = staffDao.insertStaff(staff)
    suspend fun deleteStaff(staff: StaffEntity) = staffDao.deleteStaff(staff)
}
