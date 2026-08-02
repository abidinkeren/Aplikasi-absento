package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.local.AttendanceWithStudent
import com.example.data.local.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExporter {

    fun exportMonthlyReportToExcel(
        context: Context,
        yearMonth: String,
        className: String,
        prayerFilter: String = "Dzuhur",
        students: List<StudentEntity>,
        monthlyAttendances: List<AttendanceWithStudent>
    ): Pair<Boolean, String> {
        return try {
            val parts = yearMonth.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val monthIndex = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val monthName = DateUtils.getMonthName(monthIndex)

            val fileName = "Laporan_Absensi_Sholat_${prayerFilter}_${monthName}_${year}_${if (className == "Semua") "Semua_Kelas" else className}.csv"

            val sb = StringBuilder()
            // UTF-8 BOM for Excel compatibility
            sb.append("\uFEFF")

            // Header info
            sb.append("LAPORAN ABSENSI SHOLAT SISWA - SHOLAT ${prayerFilter.uppercase()}\n")
            sb.append("Bulan / Tahun;\"$monthName $year\"\n")
            sb.append("Waktu Sholat;\"Sholat $prayerFilter\"\n")
            sb.append("Kelas;\"${if (className == "Semua") "Semua Kelas" else className}\"\n")
            sb.append("Tanggal Cetak;\"${DateUtils.getTodayDateString()}\"\n\n")

            // Table Columns Header
            sb.append("No;NIS;Nama Siswa;Kelas;Jenis Kelamin")
            sb.append(";Jumlah Kehadiran;Jumlah Tidak Hadir;Hadir Jamaah;Munfarid;Sakit/Izin;Alpa;Rata-rata Kehadiran (%)\n")

            // Filter students by class if specified
            val filteredStudents = if (className == "Semua") students else students.filter { it.className == className }

            // Filter attendances by prayer filter if not "Semua"
            val targetAttendances = if (prayerFilter == "Semua") monthlyAttendances else monthlyAttendances.filter { it.prayerName == prayerFilter }

            var totalHadirAll = 0
            var totalTidakHadirAll = 0

            filteredStudents.forEachIndexed { index, student ->
                val studentRecords = targetAttendances.filter { it.studentNis == student.nis }
                
                val hadirJamaah = studentRecords.count { it.status == "HADIR_JAMAAH" }
                val munfarid = studentRecords.count { it.status == "MUNFARID" }
                val izin = studentRecords.count { it.status == "IZIN" }
                val alpa = studentRecords.count { it.status == "ALPA" }

                val jmlKehadiran = hadirJamaah + munfarid
                val jmlTidakHadir = izin + alpa

                totalHadirAll += jmlKehadiran
                totalTidakHadirAll += jmlTidakHadir

                val totalRecorded = jmlKehadiran + jmlTidakHadir
                val percent = if (totalRecorded > 0) {
                    (jmlKehadiran.toDouble() / totalRecorded * 100).let { String.format("%.1f", it) }
                } else "0.0"

                sb.append("${index + 1};\"${student.nis}\";\"${student.name}\";\"${student.className}\";\"${if (student.gender == "L") "Laki-laki" else "Perempuan"}\"")
                sb.append(";$jmlKehadiran;$jmlTidakHadir;$hadirJamaah;$munfarid;$izin;$alpa;\"$percent%\"\n")
            }

            // Total Summary Row
            val grandTotal = totalHadirAll + totalTidakHadirAll
            val avgPercentAll = if (grandTotal > 0) String.format("%.1f", (totalHadirAll.toDouble() / grandTotal * 100)) else "0.0"

            sb.append("\nTOTAL REKAPITULASI;;;;\";\"")
            sb.append(";$totalHadirAll;$totalTidakHadirAll;;;;;\"Rata-rata: $avgPercentAll%\"\n")

            // Detailed Attendance Log section
            sb.append("\n\nDETAIL RIWAYAT LOG ABSENSI SHOLAT\n")
            sb.append("Tanggal;Sholat;NIS;Nama Siswa;Kelas;Status;Catatan\n")

            val sortedLogs = targetAttendances.sortedWith(compareBy({ it.date }, { it.studentName }))
            sortedLogs.forEach { log ->
                val statusText = when (log.status) {
                    "HADIR_JAMAAH" -> "Hadir Jamaah"
                    "MUNFARID" -> "Munfarid (Sendiri)"
                    "IZIN" -> "Sakit / Izin"
                    "ALPA" -> "Alpa / Tidak Hadir"
                    else -> log.status
                }
                sb.append("\"${log.date}\";\"${log.prayerName}\";\"${log.studentNis}\";\"${log.studentName}\";\"${log.className}\";\"$statusText\";\"${log.notes}\"\n")
            }

            val fileContent = sb.toString()

            // Save file based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                            writer.write(fileContent)
                        }
                    }
                    shareExcelFile(context, uri, fileName)
                    Pair(true, "File $fileName berhasil disimpan di folder Downloads")
                } else {
                    Pair(false, "Gagal membuat file di Downloads")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { os ->
                    OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                        writer.write(fileContent)
                    }
                }
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                shareExcelFile(context, fileUri, fileName)
                Pair(true, "File $fileName berhasil disimpan di ${file.absolutePath}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Gagal mengekspor laporan: ${e.localizedMessage}")
        }
    }

    private fun shareExcelFile(context: Context, uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Absensi Sholat - $fileName")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan / Buka Laporan Excel"))
    }
}
