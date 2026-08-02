package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    val PRAYERS = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya", "Dhuha")

    val MONTHS_ID = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateToIndonesian(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString
            val outSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            outSdf.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getMonthName(monthIndex: Int): String { // 0-indexed
        return if (monthIndex in 0..11) MONTHS_ID[monthIndex] else ""
    }

    fun getDaysInMonth(year: Int, month: Int): Int { // month is 1-indexed (1..12)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
