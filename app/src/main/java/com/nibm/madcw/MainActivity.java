package com.nibm.madcw;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nibm.madcw.auth.LoginActivity;
import com.nibm.madcw.ui.admin.AdminDashboardActivity;
import com.nibm.madcw.ui.student.StudentDashboardActivity;
import com.nibm.madcw.ui.teacher.TeacherDashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check session
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", null);

        if (role == null) {
            // Not logged in → go to login screen
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // Already logged in → redirect based on role
            switch (role) {
                case "admin":
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                    break;
                case "teacher":
                    startActivity(new Intent(this, TeacherDashboardActivity.class));
                    break;
                case "student":
                    startActivity(new Intent(this, StudentDashboardActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
            }
        }
    }
}