package vn.edu.hust.studentman

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import vn.edu.hust.studentman.StudentModel


class MainActivity : AppCompatActivity() {
  private val students = mutableListOf(
    StudentModel("Nguyễn Văn An", "SV001"),
    StudentModel("Trần Thị Bảo", "SV002"),
    StudentModel("Lê Hoàng Cường", "SV003")
    // Thêm các sinh viên khác như ví dụ
  )
  private lateinit var studentAdapter: StudentAdapter
  private lateinit var rootView: View
  private var recentlyDeletedStudent: StudentModel? = null
  private var recentlyDeletedPosition: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    rootView = findViewById(R.id.main)
    studentAdapter = StudentAdapter(students, { student, position -> editStudent(student, position) },
      { student, position -> deleteStudent(student, position) })

    findViewById<RecyclerView>(R.id.recycler_view_students).apply {
      adapter = studentAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    findViewById<Button>(R.id.btn_add_new).setOnClickListener {
      showStudentDialog(null) { newStudent ->
        students.add(newStudent)
        studentAdapter.notifyItemInserted(students.size - 1)
      }
    }
  }

  private fun showStudentDialog(student: StudentModel?, onSave: (StudentModel) -> Unit) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student, null)
    val editName = dialogView.findViewById<EditText>(R.id.edit_student_name)
    val editId = dialogView.findViewById<EditText>(R.id.edit_student_id)

    student?.let {
      editName.setText(it.studentName)
      editId.setText(it.studentId)
    }

    AlertDialog.Builder(this)
      .setTitle(if (student == null) "Add Student" else "Edit Student")
      .setView(dialogView)
      .setPositiveButton("Save") { _, _ ->
        val name = editName.text.toString()
        val id = editId.text.toString()
        if (name.isNotEmpty() && id.isNotEmpty()) {
          onSave(StudentModel(name, id))
        }
      }
      .setNegativeButton("Cancel", null)
      .show()
  }

  private fun editStudent(student: StudentModel, position: Int) {
    showStudentDialog(student) { updatedStudent ->
      students[position] = updatedStudent
      studentAdapter.notifyItemChanged(position)
    }
  }

  private fun deleteStudent(student: StudentModel, position: Int) {
    AlertDialog.Builder(this)
      .setTitle("Delete Student")
      .setMessage("Are you sure you want to delete ${student.studentName}?")
      .setPositiveButton("Delete") { _, _ ->
        recentlyDeletedStudent = student
        recentlyDeletedPosition = position
        students.removeAt(position)
        studentAdapter.notifyItemRemoved(position)
        showUndoSnackbar()
      }
      .setNegativeButton("Cancel", null)
      .show()
  }

  private fun showUndoSnackbar() {
    Snackbar.make(rootView, "Student deleted", Snackbar.LENGTH_LONG)
      .setAction("Undo") {
        recentlyDeletedStudent?.let {
          students.add(recentlyDeletedPosition, it)
          studentAdapter.notifyItemInserted(recentlyDeletedPosition)
        }
      }
      .show()
  }
}