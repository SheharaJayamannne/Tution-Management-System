package com.nibm.madcw.ui.admin;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

public class StudentReportActivity extends AppCompatActivity {

    TextView textStudentInfo, textCourses, textAssignments, textAttendance;
    TuitionDbHelper dbHelper;
    int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);

        textStudentInfo = findViewById(R.id.textStudentInfo);
        textCourses = findViewById(R.id.textCourses);
        textAssignments = findViewById(R.id.textAssignments);
        textAttendance = findViewById(R.id.textAttendance);

        dbHelper = new TuitionDbHelper(this);

        // Receive studentId from intent
        studentId = getIntent().getIntExtra("studentId", -1);
        if (studentId == -1) {
            finish(); // No student id, close activity
            return;
        }

        loadStudentInfo();
        loadCourses();
        loadAssignments();
        loadAttendance();
    }

    private void loadStudentInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email FROM users WHERE id = ?", new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            textStudentInfo.setText("Name: " + name + "\nEmail: " + email);
        } else {
            textStudentInfo.setText("Student info not found.");
        }
        cursor.close();
    }

    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT c.name FROM courses c " +
                "INNER JOIN student_courses sc ON c.id = sc.course_id WHERE sc.student_id = ?", new String[]{String.valueOf(studentId)});

        StringBuilder coursesBuilder = new StringBuilder("Courses Enrolled:\n");
        if (cursor.moveToFirst()) {
            do {
                coursesBuilder.append("• ").append(cursor.getString(0)).append("\n");
            } while (cursor.moveToNext());
        } else {
            coursesBuilder.append("No courses enrolled.");
        }
        cursor.close();

        textCourses.setText(coursesBuilder.toString());
    }

    private void loadAssignments() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder assignmentsBuilder = new StringBuilder("Assignments:\n");

        Cursor cursor = db.rawQuery(
                "SELECT a.title, s.submitted_at, s.file_path " +
                        "FROM assignments a " +
                        "LEFT JOIN submissions s ON a.id = s.assignment_id AND s.student_id = ? " +
                        "WHERE a.course_id IN (SELECT course_id FROM student_courses WHERE student_id = ?)",
                new String[]{String.valueOf(studentId), String.valueOf(studentId)});

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(0);
                String submittedAt = cursor.getString(1);
                String filePath = cursor.getString(2);

                String status = (filePath != null && !filePath.trim().isEmpty()) ? "Submitted" : "Not Submitted";
                String submissionInfo = (submittedAt != null) ? " at " + submittedAt : "";

                assignmentsBuilder.append("• ").append(title)
                        .append(" — ").append(status)
                        .append(submissionInfo)
                        .append("\n");
            } while (cursor.moveToNext());
        } else {
            assignmentsBuilder.append("No assignments found.");
        }
        cursor.close();

        textAssignments.setText(assignmentsBuilder.toString());
    }

    private void loadAttendance() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Count total classes and attended classes (status = 'present')
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) AS total_classes, " +
                        "SUM(CASE WHEN status = 'present' THEN 1 ELSE 0 END) AS attended " +
                        "FROM attendance WHERE student_id = ?", new String[]{String.valueOf(studentId)});

        if (cursor.moveToFirst()) {
            int totalClasses = cursor.getInt(cursor.getColumnIndexOrThrow("total_classes"));
            int attended = cursor.getInt(cursor.getColumnIndexOrThrow("attended"));

            double attendancePercent = totalClasses == 0 ? 0 : (attended * 100.0) / totalClasses;
            textAttendance.setText(String.format("Attendance: %d/%d (%.2f%%)", attended, totalClasses, attendancePercent));
        } else {
            textAttendance.setText("No attendance records found.");
        }
        cursor.close();
    }
}
