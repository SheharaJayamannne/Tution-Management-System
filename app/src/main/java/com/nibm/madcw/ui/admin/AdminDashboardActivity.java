package com.nibm.madcw.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.auth.LoginActivity;
import com.nibm.madcw.auth.RegisterActivity;
import com.nibm.madcw.data.TuitionDbHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView textViewWelcome;
    Button btnRegisterStudent, btnRegisterTeacher, btnAddCourse;
    Button btnManageStudents, btnManageTeachers, buttonAssignStudent,btnLogout;
    TuitionDbHelper dbHelper;
    int teacherId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnRegisterStudent = findViewById(R.id.buttonRegisterStudent);
        btnRegisterTeacher = findViewById(R.id.buttonRegisterTeacher);
        btnAddCourse = findViewById(R.id.buttonAddCourse);
        btnManageStudents = findViewById(R.id.buttonManageStudents);
        btnManageTeachers = findViewById(R.id.buttonManageTeachers);
        buttonAssignStudent = findViewById(R.id.buttonAssignStudent);
        btnLogout = findViewById(R.id.btnLogout);

        dbHelper = new TuitionDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        teacherId = prefs.getInt("user_id", -1);

        loadTeacherName(teacherId);

        btnRegisterStudent.setOnClickListener(v -> openRegisterActivityWithRole("student"));
        btnRegisterTeacher.setOnClickListener(v -> openRegisterActivityWithRole("teacher"));

        btnAddCourse.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AddCourseActivity.class)));

        buttonAssignStudent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AssignStudentActivity.class);
            startActivity(intent);
        });

        btnManageStudents.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ManageStudentActivity.class)));

        btnManageTeachers.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ManageTeacherActivity.class)));

        btnLogout.setOnClickListener(view -> {
            SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
            preferences.edit().clear().apply(); // Clear session
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        // Uncomment if you implement ViewReportsActivity
        // btnViewReports.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ViewReportsActivity.class)));
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

    private void openRegisterActivityWithRole(String role) {
        Intent intent = new Intent(AdminDashboardActivity.this, RegisterActivity.class);
        intent.putExtra("forced_role", role);
        startActivity(intent);
    }
}
