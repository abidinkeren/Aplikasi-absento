package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceEntity
import com.example.data.local.AttendanceWithStudent
import com.example.data.local.StudentEntity
import com.example.data.repository.AttendanceRepository
import com.example.util.DateUtils
import com.example.util.ExcelExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    val students: StateFlow<List<StudentEntity>>
    val allClasses: StateFlow<List<String>>

    // Filters for Daily View
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedPrayer = MutableStateFlow("Dzuhur")
    val selectedPrayer: StateFlow<String> = _selectedPrayer.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyAttendances: StateFlow<List<AttendanceEntity>>

    // Filters for Monthly Report View
    private val _monthlyYearMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val monthlyYearMonth: StateFlow<String> = _monthlyYearMonth.asStateFlow()

    private val _monthlyClassFilter = MutableStateFlow("Semua")
    val monthlyClassFilter: StateFlow<String> = _monthlyClassFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyAttendances: StateFlow<List<AttendanceWithStudent>>

    // UI Toast/Snackbar Messages
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(db.studentDao(), db.attendanceDao(), db.classDao())

        students = repository.allStudents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allClasses = repository.allClasses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        dailyAttendances = _selectedDate.flatMapLatest { date ->
            repository.getAttendancesByDate(date)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        monthlyAttendances = _monthlyYearMonth.flatMapLatest { ym ->
            repository.getMonthlyAttendances(ym)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        seedSampleDataIfEmpty()
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedPrayer(prayer: String) {
        _selectedPrayer.value = prayer
    }

    fun setMonthlyYearMonth(yearMonth: String) {
        _monthlyYearMonth.value = yearMonth
    }

    fun setMonthlyClassFilter(className: String) {
        _monthlyClassFilter.value = className
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun saveStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.insertStudent(student)
            _snackbarMessage.value = "Data siswa ${student.name} berhasil disimpan"
        }
    }

    fun addClass(className: String) {
        viewModelScope.launch {
            if (className.isNotBlank()) {
                repository.insertClass(className.trim())
                _snackbarMessage.value = "Kelas ${className.trim()} berhasil ditambahkan"
            }
        }
    }

    fun updateClass(oldName: String, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank() && oldName != newName) {
                repository.updateClassName(oldName, newName.trim())
                _snackbarMessage.value = "Nama kelas $oldName berhasil diubah menjadi $newName"
            }
        }
    }

    fun deleteClass(className: String) {
        viewModelScope.launch {
            repository.deleteClass(className)
            _snackbarMessage.value = "Kelas $className berhasil dihapus"
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _snackbarMessage.value = "Siswa ${student.name} berhasil dihapus"
        }
    }

    fun recordAttendance(
        nis: String,
        prayerName: String,
        status: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val student = repository.getStudentByNis(nis)
            if (student == null) {
                _snackbarMessage.value = "Siswa dengan NIS $nis tidak ditemukan!"
                return@launch
            }

            val today = DateUtils.getTodayDateString()
            val existing = repository.getStudentAttendanceForPrayer(nis, today, prayerName)

            val attendance = AttendanceEntity(
                id = existing?.id ?: 0,
                studentNis = nis,
                prayerName = prayerName,
                date = today,
                timestamp = System.currentTimeMillis(),
                status = status,
                notes = notes
            )

            repository.recordAttendance(attendance)
            val statusLabel = when (status) {
                "HADIR_JAMAAH" -> "Hadir Jamaah"
                "MUNFARID" -> "Munfarid"
                "IZIN" -> "Sakit/Izin"
                "ALPA" -> "Alpa"
                else -> status
            }
            _snackbarMessage.value = "Absensi Sholat $prayerName: ${student.name} ($statusLabel)"
        }
    }

    fun exportExcel(context: Context, prayerFilter: String = "Dzuhur") {
        viewModelScope.launch {
            val ym = _monthlyYearMonth.value
            val cls = _monthlyClassFilter.value
            val stds = students.value
            val logs = monthlyAttendances.value

            val (success, message) = ExcelExporter.exportMonthlyReportToExcel(
                context = context,
                yearMonth = ym,
                className = cls,
                prayerFilter = prayerFilter,
                students = stds,
                monthlyAttendances = logs
            )
            _snackbarMessage.value = message
        }
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            val currentStudents = repository.getStudentByNis("1001")
            if (currentStudents == null) {
                val sampleStudents = listOf(
                    StudentEntity("1001", "Ahmad Fauzi", "7A", "L"),
                    StudentEntity("1002", "Aisyah Nur Rahma", "7A", "P"),
                    StudentEntity("1003", "Budi Santoso", "7A", "L"),
                    StudentEntity("1004", "Dewi Lestari", "7B", "P"),
                    StudentEntity("1005", "Fajar Hidayat", "7B", "L"),
                    StudentEntity("1006", "Fatimah Az-Zahra", "7B", "P"),
                    StudentEntity("1007", "Muhammad Rizky", "8A", "L"),
                    StudentEntity("1008", "Nabila Putri", "8A", "P"),
                    StudentEntity("1009", "Zaky Mubarak", "8A", "L")
                )
                repository.insertAllStudents(sampleStudents)

                // Seed some initial attendance for today and previous days of current month
                val today = DateUtils.getTodayDateString()
                val currentYM = DateUtils.getCurrentYearMonth()

                val sampleAttendances = mutableListOf<AttendanceEntity>()
                val prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")

                // Seed today's Dzuhur attendance for sample
                sampleStudents.take(6).forEachIndexed { idx, s ->
                    val status = when (idx % 4) {
                        0 -> "HADIR_JAMAAH"
                        1 -> "HADIR_JAMAAH"
                        2 -> "MUNFARID"
                        else -> "IZIN"
                    }
                    sampleAttendances.add(
                        AttendanceEntity(
                            studentNis = s.nis,
                            prayerName = "Dzuhur",
                            date = today,
                            status = status,
                            notes = if (status == "IZIN") "Kurang sehat" else ""
                        )
                    )
                }

                // Seed a few past dates in current month for report testing
                val datePrev1 = "$currentYM-01"
                val datePrev2 = "$currentYM-02"

                sampleStudents.forEach { s ->
                    sampleAttendances.add(
                        AttendanceEntity(
                            studentNis = s.nis,
                            prayerName = "Dzuhur",
                            date = datePrev1,
                            status = if (s.nis == "1003") "ALPA" else "HADIR_JAMAAH"
                        )
                    )
                    sampleAttendances.add(
                        AttendanceEntity(
                            studentNis = s.nis,
                            prayerName = "Ashar",
                            date = datePrev2,
                            status = if (s.nis == "1004") "MUNFARID" else "HADIR_JAMAAH"
                        )
                    )
                }

                repository.insertAllAttendances(sampleAttendances)
            }
        }
    }
}
