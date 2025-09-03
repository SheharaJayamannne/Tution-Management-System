package com.nibm.madcw.ui.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.auth.LoginActivity;
import com.nibm.madcw.data.TuitionDbHelper;

public class StudentDashboardActivity extends AppCompatActivity {

    TextView textViewWelcome, textUnreadMessages;
    Button buttonScanAttendance, buttonViewAssignments,
            buttonSubmitAssignments, buttonViewResults, buttonViewMaterials, buttonViewMessages,btnLogout;
    TextView locationText;

    TuitionDbHelper dbHelper;
    int studentId = -1;
    int courseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // UI initialization
        buttonScanAttendance = findViewById(R.id.buttonScanAttendance);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonViewAssignments = findViewById(R.id.buttonViewAssignments);
        buttonSubmitAssignments = findViewById(R.id.buttonSubmitAssignments);
        buttonViewResults = findViewById(R.id.buttonViewResults);
        buttonViewMaterials = findViewById(R.id.buttonViewMaterials);
        buttonViewMessages = findViewById(R.id.buttonViewMessages);
        textUnreadMessages = findViewById(R.id.textUnreadCount);
        locationText = findViewById(R.id.textViewInstituteLocation);
        btnLogout = findViewById(R.id.btnLogout);

        dbHelper = new TuitionDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        loadStudentName(studentId);
        loadStudentCourseId();
        updateUnreadMessageCount();

        // Navigation listeners
        buttonViewAssignments.setOnClickListener(v -> startActivity(new Intent(this, ViewAssignmentsActivity.class)));

        buttonSubmitAssignments.setOnClickListener(v -> startActivity(new Intent(this, SubmitAssignmentActivity.class)));

        buttonViewResults.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewResultsActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        buttonViewMaterials.setOnClickListener(v -> {
            if (courseId == -1) {
                Toast.makeText(this, "Course not assigned to student.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ViewMaterialsActivity.class);
            intent.putExtra("courseId", courseId);
            startActivity(intent);
        });

        buttonViewMessages.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewMessageActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        buttonScanAttendance.setOnClickListener(v -> {
            startActivity(new Intent(this, ScanAttendanceActivity.class));
        });

        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentDashboardActivity.this, MapsActivity.class);
                startActivity(intent);
            }
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


    private void loadStudentName(int studentId) {
        if (studentId == -1) {
            textViewWelcome.setText("Welcome");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE id = ?", new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            textViewWelcome.setText("Welcome, " + name);
        }
        cursor.close();
    }

    private void loadStudentCourseId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT course_id FROM student_courses WHERE student_id = ? LIMIT 1",
                new String[]{String.valueOf(studentId)});

        if (cursor.moveToFirst()) {
            courseId = cursor.getInt(0);
        } else {
            Toast.makeText(this, "No course assigned to this student", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void updateUnreadMessageCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM notifications WHERE receiver_id = ? AND read_status = 0",
                new String[]{String.valueOf(studentId)}
        );

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                textUnreadMessages.setText("Unread Messages: " + count);
                textUnreadMessages.setVisibility(TextView.VISIBLE);
            } else {
                textUnreadMessages.setVisibility(TextView.GONE);
            }
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadMessageCount(); // Refresh on return
    }
}
