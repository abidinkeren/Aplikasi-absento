package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StudentEntity
import com.example.ui.components.StudentQrDialog
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("Semua") }

    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var isAddingNewStudent by remember { mutableStateOf(false) }
    var qrDialogStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var isManagingClasses by remember { mutableStateOf(false) }

    val filteredStudents = students.filter { student ->
        val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) || student.nis.contains(searchQuery, ignoreCase = true)
        val matchesClass = if (selectedClassFilter == "Semua") true else student.className == selectedClassFilter
        matchesQuery && matchesClass
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddingNewStudent = true },
                icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Tambah Siswa") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_student")
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data Siswa & Kelas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kelola data siswa, pilihan kelas, dan kartu QR absensi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { isManagingClasses = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("manage_classes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kelola Kelas")
                    }
                }
            }

            // Search input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama/NIS...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_student_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Class Filter Chips
            item {
                val classList = listOf("Semua") + allClasses
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(classList) { cls ->
                        FilterChip(
                            selected = cls == selectedClassFilter,
                            onClick = { selectedClassFilter = cls },
                            label = { Text(if (cls == "Semua") "Semua Kelas" else "Kelas $cls") },
                            modifier = Modifier.testTag("filter_class_$cls")
                        )
                    }
                }
            }

            // Student count header
            item {
                Text(
                    text = "Total Siswa: ${filteredStudents.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Student Items
            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data siswa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredStudents, key = { it.nis }) { student ->
                    StudentCardItem(
                        student = student,
                        onQrClick = { qrDialogStudent = student },
                        onEditClick = { editingStudent = student },
                        onDeleteClick = { studentToDelete = student }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add / Edit Student Dialog
    if (isAddingNewStudent || editingStudent != null) {
        StudentFormDialog(
            student = editingStudent,
            allClasses = allClasses,
            onDismiss = {
                isAddingNewStudent = false
                editingStudent = null
            },
            onSave = { newStudent ->
                viewModel.saveStudent(newStudent)
                isAddingNewStudent = false
                editingStudent = null
            }
        )
    }

    // Manage Classes Dialog
    if (isManagingClasses) {
        ManageClassesDialog(
            classes = allClasses,
            onDismiss = { isManagingClasses = false },
            onAddClass = { newClass -> viewModel.addClass(newClass) },
            onEditClass = { oldName, newName -> viewModel.updateClass(oldName, newName) },
            onDeleteClass = { className -> viewModel.deleteClass(className) }
        )
    }

    // Delete Student Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Hapus Siswa") },
            text = { Text("Apakah Anda yakin ingin menghapus data siswa ${student.name} (NIS: ${student.nis})?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) { Text("Batal") }
            }
        )
    }

    // Printable QR Dialog
    qrDialogStudent?.let { student ->
        StudentQrDialog(
            student = student,
            onDismiss = { qrDialogStudent = null }
        )
    }
}

@Composable
fun StudentCardItem(
    student: StudentEntity,
    onQrClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "NIS: ${student.nis} • Kelas ${student.className} • ${if (student.gender == "L") "Laki-laki" else "Perempuan"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onQrClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_qr_${student.nis}")
            ) {
                Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lihat & Cetak Kartu QR")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    student: StudentEntity?,
    allClasses: List<String>,
    onDismiss: () -> Unit,
    onSave: (StudentEntity) -> Unit
) {
    var nis by remember { mutableStateOf(student?.nis ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var className by remember { mutableStateOf(student?.className ?: (allClasses.firstOrNull() ?: "7A")) }
    var gender by remember { mutableStateOf(student?.gender ?: "L") }

    var classDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "Tambah Siswa Baru" else "Edit Data Siswa") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("NIS Siswa") },
                    enabled = (student == null), // Cannot edit NIS primary key
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nis"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nama"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown to Select Class
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Pilih Kelas Siswa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("input_kelas"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        allClasses.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text("Kelas $cls") },
                                onClick = {
                                    className = cls
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Jenis Kelamin:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "L", onClick = { gender = "L" })
                    Text("Laki-laki")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = gender == "P", onClick = { gender = "P" })
                    Text("Perempuan")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nis.isNotBlank() && name.isNotBlank() && className.isNotBlank()) {
                        onSave(StudentEntity(nis.trim(), name.trim(), className.trim(), gender))
                    }
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ManageClassesDialog(
    classes: List<String>,
    onDismiss: () -> Unit,
    onAddClass: (String) -> Unit,
    onEditClass: (String, String) -> Unit,
    onDeleteClass: (String) -> Unit
) {
    var newClassName by remember { mutableStateOf("") }
    var editingClassOldName by remember { mutableStateOf<String?>(null) }
    var editingClassNewName by remember { mutableStateOf("") }
    var deletingClassName by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Kelola Data Kelas")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah kelas baru atau ubah data kelas yang sudah ada:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Add Class Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newClassName,
                        onValueChange = { newClassName = it },
                        placeholder = { Text("Nama Kelas Baru (mis: 9C)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_new_class_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newClassName.isNotBlank()) {
                                onAddClass(newClassName.trim())
                                newClassName = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_class_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                    }
                }

                // List of existing classes
                Text(
                    text = "Daftar Kelas Saat Ini (${classes.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (classes.isEmpty()) {
                    Text(
                        text = "Belum ada kelas terdaftar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        classes.forEach { cls ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Kelas $cls",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingClassOldName = cls
                                                editingClassNewName = cls
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Kelas",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { deletingClassName = cls }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Kelas",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )

    // Edit Class Dialog
    editingClassOldName?.let { oldName ->
        AlertDialog(
            onDismissRequest = { editingClassOldName = null },
            title = { Text("Edit Nama Kelas $oldName") },
            text = {
                OutlinedTextField(
                    value = editingClassNewName,
                    onValueChange = { editingClassNewName = it },
                    label = { Text("Nama Kelas Baru") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingClassNewName.isNotBlank() && editingClassNewName != oldName) {
                            onEditClass(oldName, editingClassNewName)
                            editingClassOldName = null
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingClassOldName = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Class Confirmation
    deletingClassName?.let { cls ->
        AlertDialog(
            onDismissRequest = { deletingClassName = null },
            title = { Text("Hapus Kelas $cls") },
            text = { Text("Apakah Anda yakin ingin menghapus Kelas $cls? Kelas ini akan dihapus dari daftar kelas.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(cls)
                        deletingClassName = null
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingClassName = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
