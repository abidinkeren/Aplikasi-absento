package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class AttendanceWithStudent(
    val id: Long,
    val studentNis: String,
    val studentName: String,
    val className: String,
    val prayerName: String,
    val date: String,
    val timestamp: Long,
    val status: String,
    val notes: String
)

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances WHERE date = :date ORDER BY timestamp DESC")
    fun getAttendancesByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE date = :date AND prayerName = :prayerName")
    fun getAttendancesByDateAndPrayer(date: String, prayerName: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE studentNis = :nis AND date = :date AND prayerName = :prayerName LIMIT 1")
    suspend fun getStudentAttendanceForPrayer(nis: String, date: String, prayerName: String): AttendanceEntity?

    @Query("""
        SELECT a.id, a.studentNis, s.name AS studentName, s.className, a.prayerName, a.date, a.timestamp, a.status, a.notes 
        FROM attendances a 
        INNER JOIN students s ON a.studentNis = s.nis 
        WHERE a.date LIKE :yearMonth || '%' 
        ORDER BY a.date DESC, s.className ASC, s.name ASC
    """)
    fun getMonthlyAttendances(yearMonth: String): Flow<List<AttendanceWithStudent>>

    @Query("SELECT * FROM attendances WHERE date LIKE :yearMonth || '%'")
    fun getMonthlyAttendancesRaw(yearMonth: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<AttendanceEntity>)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendances WHERE id = :id")
    suspend fun deleteById(id: Long)
}
