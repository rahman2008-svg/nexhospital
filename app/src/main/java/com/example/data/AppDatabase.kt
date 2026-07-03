package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HospitalEntity::class,
        AdminEntity::class,
        PatientEntity::class,
        PatientNoteEntity::class,
        PrescriptionEntity::class,
        BillingEntity::class,
        StaffEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hospitalDao(): HospitalDao
    abstract fun adminDao(): AdminDao
    abstract fun patientDao(): PatientDao
    abstract fun patientNoteDao(): PatientNoteDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun billingDao(): BillingDao
    abstract fun staffDao(): StaffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexhospital_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
