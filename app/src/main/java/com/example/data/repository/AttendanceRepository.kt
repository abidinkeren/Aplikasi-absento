package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.AttendanceEntity
import com.example.data.local.AttendanceWithStudent
import com.example.data.local.ClassDao
import com.example.data.local.ClassEntity
import com.example.data.local.StudentDao
import com.example.data.local.StudentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val classDao: ClassDao
) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()

    // Combine distinct classes from student entities and explicit ClassEntity records
    val allClasses: Flow<List<String>> = combine(
        studentDao.getAllClasses(),
        classDao.getAllClasses()
    ) { studentClasses, dbClasses ->
        (studentClasses + dbClasses.map { it.name }).distinct().sorted()
    }

    suspend fun insertClass(className: String) {
        classDao.insertClass(ClassEntity(className))
    }

    suspend fun updateClassName(oldName: String, newName: String) {
        classDao.updateClassName(oldName, newName)
        studentDao.updateStudentClassName(oldName, newName)
    }

    suspend fun deleteClass(className: String) {
        classDao.deleteClass(className)
    }

    suspend fun getStudentByNis(nis: String): StudentEntity? = studentDao.getStudentByNis(nis)

    suspend fun insertStudent(student: StudentEntity) = studentDao.insertStudent(student)

    suspend fun insertAllStudents(students: List<StudentEntity>) = studentDao.insertAll(students)

    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    suspend fun deleteStudentByNis(nis: String) = studentDao.deleteByNis(nis)

    fun getAttendancesByDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendancesByDate(date)

    fun getAttendancesByDateAndPrayer(date: String, prayerName: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendancesByDateAndPrayer(date, prayerName)

    suspend fun getStudentAttendanceForPrayer(nis: String, date: String, prayerName: String): AttendanceEntity? =
        attendanceDao.getStudentAttendanceForPrayer(nis, date, prayerName)

    fun getMonthlyAttendances(yearMonth: String): Flow<List<AttendanceWithStudent>> =
        attendanceDao.getMonthlyAttendances(yearMonth)

    fun getMonthlyAttendancesRaw(yearMonth: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getMonthlyAttendancesRaw(yearMonth)

    suspend fun recordAttendance(attendance: AttendanceEntity) =
        attendanceDao.insertAttendance(attendance)

    suspend fun insertAllAttendances(attendances: List<AttendanceEntity>) =
        attendanceDao.insertAll(attendances)

    suspend fun deleteAttendance(attendance: AttendanceEntity) =
        attendanceDao.deleteAttendance(attendance)
}
