package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StatusAlpa
import com.example.ui.theme.StatusHadirJamaah
import com.example.ui.theme.StatusIzin
import com.example.ui.theme.StatusMunfarid
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val yearMonth by viewModel.monthlyYearMonth.collectAsState()
    val selectedClassFilter by viewModel.monthlyClassFilter.collectAsState()
    val monthlyAttendances by viewModel.monthlyAttendances.collectAsState()

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPrayerFilter by remember { mutableStateOf("Dzuhur") } // Default Focus: Sholat Dzuhur

    val availableMonths = listOf("2026-08", "2026-07", "2026-06", "2026-05", "2026-04")

    val filteredStudents = if (selectedClassFilter == "Semua") students else students.filter { it.className == selectedClassFilter }

    // Target logs filtered by class and selected prayer
    val targetLogs = monthlyAttendances.filter { log ->
        val matchesClass = selectedClassFilter == "Semua" || log.className == selectedClassFilter
        val matchesPrayer = if (selectedPrayerFilter == "Semua") true else log.prayerName == selectedPrayerFilter
        matchesClass && matchesPrayer
    }

    val countJamaah = targetLogs.count { it.status == "HADIR_JAMAAH" }
    val countMunfarid = targetLogs.count { it.status == "MUNFARID" }
    val countIzin = targetLogs.count { it.status == "IZIN" }
    val countAlpa = targetLogs.count { it.status == "ALPA" }

    val totalHadir = countJamaah + countMunfarid
    val totalTidakHadir = countIzin + countAlpa
    val totalLogsCount = totalHadir + totalTidakHadir
    val avgAttendancePercent = if (totalLogsCount > 0) (totalHadir.toDouble() / totalLogsCount * 100) else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Bento Title Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan Absensi Bulanan",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Rekapitulasi Sholat Dzuhur & cetak laporan Excel (.csv)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Month Selector & Prayer Filter & Class Filter Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Periode, Waktu Sholat & Filter Kelas",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Month Picker
                    ExposedDropdownMenuBox(
                        expanded = monthDropdownExpanded,
                        onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = formatYearMonth(yearMonth),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Bulan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            availableMonths.forEach { ym ->
                                DropdownMenuItem(
                                    text = { Text(formatYearMonth(ym)) },
                                    onClick = {
                                        viewModel.setMonthlyYearMonth(ym)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Prayer Chips Filter
                    Text(
                        text = "Waktu Sholat (Fokus: Dzuhur):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val prayersList = listOf("Dzuhur", "Semua", "Subuh", "Ashar", "Maghrib", "Isya")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(prayersList) { prayer ->
                            val isSelected = prayer == selectedPrayerFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPrayerFilter = prayer },
                                label = { Text(if (prayer == "Semua") "Semua Sholat" else "Sholat $prayer") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Class Filter Chips
                    Text(
                        text = "Filter Kelas:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val classList = listOf("Semua") + allClasses
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(classList) { cls ->
                            FilterChip(
                                selected = cls == selectedClassFilter,
                                onClick = { viewModel.setMonthlyClassFilter(cls) },
                                label = { Text(if (cls == "Semua") "Semua Kelas" else "Kelas $cls") }
                            )
                        }
                    }
                }
            }
        }

        // Comprehensive Attendance Summary Cards
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Rata-Rata Absensi Sholat $selectedPrayerFilter (${formatYearMonth(yearMonth)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalHadir",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StatusHadirJamaah
                            )
                            Text(
                                text = "Total Kehadiran",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalTidakHadir",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StatusAlpa
                            )
                            Text(
                                text = "Total Tidak Hadir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", avgAttendancePercent)}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Rata-rata Hadir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Breakdown Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge(label = "Jamaah", count = countJamaah, color = StatusHadirJamaah)
                        StatBadge(label = "Munfarid", count = countMunfarid, color = StatusMunfarid)
                        StatBadge(label = "Sakit/Izin", count = countIzin, color = StatusIzin)
                        StatBadge(label = "Alpa", count = countAlpa, color = StatusAlpa)
                    }
                }
            }
        }

        // Table Header Label & Table View Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tabel Rekapitulasi Per Siswa",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mencakup jumlah kehadiran, tidak hadir & rata-rata per sholat",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            TableCell("No", width = 36.dp, isHeader = true)
                            TableCell("NIS", width = 70.dp, isHeader = true)
                            TableCell("Nama Siswa", width = 160.dp, isHeader = true)
                            TableCell("Kelas", width = 50.dp, isHeader = true)
                            TableCell("Jml Hadir", width = 70.dp, isHeader = true)
                            TableCell("Jml Absen", width = 70.dp, isHeader = true)
                            TableCell("Jamaah", width = 55.dp, isHeader = true)
                            TableCell("Munfarid", width = 60.dp, isHeader = true)
                            TableCell("Izin", width = 45.dp, isHeader = true)
                            TableCell("Alpa", width = 45.dp, isHeader = true)
                            TableCell("Rata-rata %", width = 75.dp, isHeader = true)
                        }

                        if (filteredStudents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada data siswa")
                            }
                        } else {
                            filteredStudents.forEachIndexed { index, student ->
                                val logs = targetLogs.filter { it.studentNis == student.nis }
                                val j = logs.count { it.status == "HADIR_JAMAAH" }
                                val m = logs.count { it.status == "MUNFARID" }
                                val i = logs.count { it.status == "IZIN" }
                                val a = logs.count { it.status == "ALPA" }

                                val jmlHadir = j + m
                                val jmlAbsen = i + a
                                val total = jmlHadir + jmlAbsen
                                val percent = if (total > 0) (jmlHadir.toDouble() / total * 100).let { String.format("%.0f", it) } + "%" else "0%"

                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell("${index + 1}", width = 36.dp)
                                    TableCell(student.nis, width = 70.dp)
                                    TableCell(student.name, width = 160.dp, isBold = true)
                                    TableCell(student.className, width = 50.dp)
                                    TableCell("$jmlHadir", width = 70.dp, isBold = true, color = StatusHadirJamaah)
                                    TableCell("$jmlAbsen", width = 70.dp, isBold = true, color = StatusAlpa)
                                    TableCell("$j", width = 55.dp, color = StatusHadirJamaah)
                                    TableCell("$m", width = 60.dp, color = StatusMunfarid)
                                    TableCell("$i", width = 45.dp, color = StatusIzin)
                                    TableCell("$a", width = 45.dp, color = StatusAlpa)
                                    TableCell(percent, width = 75.dp, isBold = true)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prominent Cetak Excel Button (Bento Bottom Bar)
        item {
            Button(
                onClick = { viewModel.exportExcel(context, selectedPrayerFilter) },
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_excel_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cetak & Ekspor Laporan Excel (.csv)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else color,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Start
    )
}

private fun formatYearMonth(yearMonth: String): String {
    return try {
        val parts = yearMonth.split("-")
        val year = parts[0]
        val monthIdx = parts[1].toInt() - 1
        "${DateUtils.getMonthName(monthIdx)} $year"
    } catch (e: Exception) {
        yearMonth
    }
}
