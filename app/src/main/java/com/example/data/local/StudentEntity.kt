package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val nis: String,
    val name: String,
    val className: String,
    val gender: String // "L" or "P"
)
