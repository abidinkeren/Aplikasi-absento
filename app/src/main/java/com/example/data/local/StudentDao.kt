package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY className ASC, name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE nis = :nis LIMIT 1")
    suspend fun getStudentByNis(nis: String): StudentEntity?

    @Query("SELECT DISTINCT className FROM students ORDER BY className ASC")
    fun getAllClasses(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE nis = :nis")
    suspend fun deleteByNis(nis: String)

    @Query("UPDATE students SET className = :newClassName WHERE className = :oldClassName")
    suspend fun updateStudentClassName(oldClassName: String, newClassName: String)
}
