package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentNis: String,
    val prayerName: String, // "Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya", "Dhuha", "Jumat"
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "HADIR_JAMAAH", "MUNFARID", "IZIN", "ALPA"
    val notes: String = ""
)
