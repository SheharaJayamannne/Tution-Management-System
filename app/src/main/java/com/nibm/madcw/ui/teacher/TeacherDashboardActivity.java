package com.nibm.madcw.ui.teacher;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.auth.LoginActivity;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Course;
import com.nibm.madcw.model.Student;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    Button btnAttendance, btnAssignments, btnReleaseResults, btnUploadMaterials, btnMaterials, btnNotify, btnLogout;

    TextView textViewWelcome;
    int teacherId;
    TuitionDbHelper dbHelper;

    // Lists to hold course info
    List<Integer> courseIdList = new ArrayList<>();
    List<String> courseNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize buttons
        btnAttendance = findViewById(R.id.btnAttendance);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnAssignments = findViewById(R.id.btnAssignments);
        btnUploadMaterials = findViewById(R.id.btnUploadMaterials);
        btnMaterials = findViewById(R.id.btnMaterials);
        btnNotify = findViewById(R.id.btnNotify);
        btnLogout = findViewById(R.id.btnLogout);
        btnReleaseResults = findViewById(R.id.buttonReleaseResults);

        dbHelper = new TuitionDbHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        teacherId = prefs.getInt("user_id", -1);

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadTeacherName(teacherId);
        // Load teacher's courses
        loadTeacherCourses();

        btnAttendance.setOnClickListener(view -> {
            if (courseIdList.isEmpty()) {
                Toast.makeText(this, "No courses assigned.", Toast.LENGTH_SHORT).show();
                return;
            }
            showCourseSelectionDialog();
        });

        btnAssignments.setOnClickListener(view ->
                startActivity(new Intent(this, UploadAssignmentActivity.class)));

        btnUploadMaterials.setOnClickListener(view ->
                startActivity(new Intent(this, UploadMaterialActivity.class)));

        btnMaterials.setOnClickListener(view ->
                startActivity(new Intent(this, ManageMaterialsActivity.class)));

        btnNotify.setOnClickListener(view ->
        {
            showMessageSelectionDialog(); // Launch course+student selector
        });

        btnReleaseResults.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ReleaseResultsActivity.class);
            intent.putExtra("teacherId", teacherId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> {
            SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
            preferences.edit().clear().apply(); // Clear session
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void loadTeacherName(int teacherId) {
        if (teacherId == -1) {
            textViewWelcome.setText("Welcome");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE id = ?", new String[]{String.valueOf(teacherId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            textViewWelcome.setText("Welcome, " + name);
        }
        cursor.close();
    }
    private void loadTeacherCourses() {
        courseIdList.clear();
        courseNameList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Assuming your courses table has 'id', 'name', 'teacher_id' columns
            cursor = db.rawQuery("SELECT id, name FROM courses WHERE teacher_id = ?",
                    new String[]{String.valueOf(teacherId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    courseIdList.add(cursor.getInt(0));
                    courseNameList.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void showMessageSelectionDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_select_student_course, null);
        Spinner spinnerCourses = view.findViewById(R.id.spinnerCourses);
        Spinner spinnerStudents = view.findViewById(R.id.spinnerStudents);

        // Get teacherId
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int teacherId = prefs.getInt("user_id", -1);

        // Fetch courses assigned to this teacher
        List<Course> courses = dbHelper.getCoursesByTeacher(teacherId);
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);

        // Fetch students when a course is selected
        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Course selectedCourse = (Course) parent.getItemAtPosition(pos);
                List<Student> students = dbHelper.getStudentsByCourse(selectedCourse.getId());

                ArrayAdapter<Student> studentAdapter = new ArrayAdapter<>(TeacherDashboardActivity.this, android.R.layout.simple_spinner_item, students);
                studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerStudents.setAdapter(studentAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Send Message")
                .setView(view)
                .setPositiveButton("Start Chat", (dialog, which) -> {
                    Course selectedCourse = (Course) spinnerCourses.getSelectedItem();
                    Student selectedStudent = (Student) spinnerStudents.getSelectedItem();

                    if (selectedCourse != null && selectedStudent != null) {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("senderId", teacherId);
                        intent.putExtra("receiverId", selectedStudent.getId());
                        intent.putExtra("courseId", selectedCourse.getId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Please select both course and student", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showCourseSelectionDialog() {
        String[] courseArray = courseNameList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select Course")
                .setItems(courseArray, (dialog, which) -> {
                    int selectedCourseId = courseIdList.get(which);
                    String selectedCourseName = courseNameList.get(which);

                    Intent intent = new Intent(TeacherDashboardActivity.this, GenerateQrActivity.class);
                    intent.putExtra("courseId", selectedCourseId);
                    intent.putExtra("courseName", selectedCourseName);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void releaseResults() {
        if (teacherId == -1) {
            Toast.makeText(this, "Invalid teacher session.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_released", 1); // Set released

        int rows = db.update("submissions", values,
                "assignment_id IN (SELECT id FROM assignments WHERE course_id IN " +
                        "(SELECT id FROM courses WHERE teacher_id = ?))",
                new String[]{String.valueOf(teacherId)});

        if (rows > 0) {
            Toast.makeText(this, "Results released successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No results found to release.", Toast.LENGTH_SHORT).show();
        }
    }
}
