package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AttendanceEntity
import com.example.data.local.StudentEntity
import com.example.ui.components.AttendanceDialog
import com.example.ui.theme.StatusAlpa
import com.example.ui.theme.StatusHadirJamaah
import com.example.ui.theme.StatusIzin
import com.example.ui.theme.StatusMunfarid
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val dailyAttendances by viewModel.dailyAttendances.collectAsState()
    val selectedPrayer by viewModel.selectedPrayer.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedHomeClassFilter by remember { mutableStateOf("Semua") }
    var manualAttendanceStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var showManualStudentPicker by remember { mutableStateOf(false) }

    // Calculate daily statistics for selected prayer
    val prayerAttendances = dailyAttendances.filter { it.prayerName == selectedPrayer }
    val countHadirJamaah = prayerAttendances.count { it.status == "HADIR_JAMAAH" }
    val countMunfarid = prayerAttendances.count { it.status == "MUNFARID" }
    val countIzin = prayerAttendances.count { it.status == "IZIN" }
    val countAlpa = prayerAttendances.count { it.status == "ALPA" }
    val totalRecorded = prayerAttendances.size

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToScan,
                icon = { Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Scan QR Absen") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_scan_qr")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Assalamu'alaikum",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Absensi Sholat Siswa",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = DateUtils.formatDateToIndonesian(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Prayer Tabs
            item {
                Text(
                    text = "Pilih Waktu Sholat",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DateUtils.PRAYERS) { prayer ->
                        val isSelected = prayer == selectedPrayer
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedPrayer(prayer) },
                            label = { Text("Sholat $prayer") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("prayer_chip_$prayer")
                        )
                    }
                }
            }

            // Statistics Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ringkasan Sholat $selectedPrayer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalRecorded / ${students.size} Terabsen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBadge(label = "Jamaah", count = countHadirJamaah, color = StatusHadirJamaah)
                            StatBadge(label = "Munfarid", count = countMunfarid, color = StatusMunfarid)
                            StatBadge(label = "Sakit/Izin", count = countIzin, color = StatusIzin)
                            StatBadge(label = "Alpa", count = countAlpa, color = StatusAlpa)
                        }
                    }
                }
            }

            // Manual Absen Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Absensi Hari Ini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showManualStudentPicker = true },
                        modifier = Modifier.testTag("manual_absen_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Absen Manual")
                    }
                }
            }

            // Class Chips Filter on Home Screen
            item {
                val classList = listOf("Semua") + allClasses
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(classList) { cls ->
                        FilterChip(
                            selected = cls == selectedHomeClassFilter,
                            onClick = { selectedHomeClassFilter = cls },
                            label = { Text(if (cls == "Semua") "Semua Kelas" else "Kelas $cls") },
                            modifier = Modifier.testTag("home_class_chip_$cls")
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama/NIS siswa...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_home_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Today's student attendance list
            val filteredStudents = students.filter { student ->
                val matchesClass = if (selectedHomeClassFilter == "Semua") true else student.className == selectedHomeClassFilter
                val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) || student.nis.contains(searchQuery, ignoreCase = true)
                matchesClass && matchesQuery
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data siswa ditemukan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredStudents, key = { it.nis }) { student ->
                    val attendance = prayerAttendances.find { it.studentNis == student.nis }
                    StudentAttendanceRow(
                        student = student,
                        attendance = attendance,
                        onRecordClick = {
                            manualAttendanceStudent = student
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Modal Attendance Dialog when tapping a student
    manualAttendanceStudent?.let { student ->
        AttendanceDialog(
            student = student,
            initialPrayer = selectedPrayer,
            onDismiss = { manualAttendanceStudent = null },
            onConfirm = { prayer, status, notes ->
                viewModel.recordAttendance(student.nis, prayer, status, notes)
                manualAttendanceStudent = null
            }
        )
    }

    // Manual Student Picker Dialog
    if (showManualStudentPicker) {
        ManualStudentPickerDialog(
            students = students,
            allClasses = allClasses,
            selectedPrayer = selectedPrayer,
            prayerAttendances = prayerAttendances,
            onDismiss = { showManualStudentPicker = false },
            onQuickRecord = { student ->
                viewModel.recordAttendance(student.nis, selectedPrayer, "HADIR_JAMAAH")
            },
            onDetailRecord = { student ->
                manualAttendanceStudent = student
            }
        )
    }
}

@Composable
fun StatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StudentAttendanceRow(
    student: StudentEntity,
    attendance: AttendanceEntity?,
    onRecordClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRecordClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "NIS: ${student.nis} • Kelas ${student.className}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (attendance != null) {
                val (label, color) = when (attendance.status) {
                    "HADIR_JAMAAH" -> "Hadir Jamaah" to StatusHadirJamaah
                    "MUNFARID" -> "Munfarid" to StatusMunfarid
                    "IZIN" -> "Sakit/Izin" to StatusIzin
                    "ALPA" -> "Alpa" to StatusAlpa
                    else -> attendance.status to MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onRecordClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Absen", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualStudentPickerDialog(
    students: List<StudentEntity>,
    allClasses: List<String>,
    selectedPrayer: String,
    prayerAttendances: List<AttendanceEntity>,
    onDismiss: () -> Unit,
    onQuickRecord: (StudentEntity) -> Unit,
    onDetailRecord: (StudentEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(allClasses.firstOrNull() ?: "7A") }

    val filtered = students.filter { student ->
        val matchesClass = if (selectedClass == "Semua") true else student.className == selectedClass
        val matchesQuery = student.name.contains(query, ignoreCase = true) || student.nis.contains(query, ignoreCase = true)
        matchesClass && matchesQuery
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Input Absen Manual - $selectedPrayer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pilih kelas lalu tekan siswa untuk absen (langsung hijau)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Menu Kelas Chips
                Text(
                    text = "Menu Pilih Kelas:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val classList = listOf("Semua") + allClasses
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(classList) { cls ->
                        val isSelected = cls == selectedClass
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedClass = cls },
                            label = { Text(if (cls == "Semua") "Semua Kelas" else "Kelas $cls") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("manual_dialog_class_$cls")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar inside dialog
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari nama/NIS...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Student List for selected class
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada siswa di ${if (selectedClass == "Semua") "semua kelas" else "Kelas $selectedClass"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.nis }) { student ->
                            val attendance = prayerAttendances.find { it.studentNis == student.nis }
                            val isAttended = attendance?.status == "HADIR_JAMAAH"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onQuickRecord(student) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAttended) StatusHadirJamaah.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isAttended) androidx.compose.foundation.BorderStroke(2.dp, StatusHadirJamaah) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isAttended) StatusHadirJamaah else MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isAttended) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Hadir",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = student.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAttended) StatusHadirJamaah else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "NIS: ${student.nis} • Kelas ${student.className}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isAttended) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = StatusHadirJamaah
                                        ) {
                                            Text(
                                                text = "HADIR",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else if (attendance != null) {
                                        Text(
                                            text = attendance.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = { onQuickRecord(student) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Absen", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    IconButton(onClick = { onDetailRecord(student) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Opsi",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Selesai")
                }
            }
        }
    }
}
