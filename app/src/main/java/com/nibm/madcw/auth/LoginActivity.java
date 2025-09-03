package com.nibm.madcw.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.ui.admin.AdminDashboardActivity;
import com.nibm.madcw.ui.teacher.TeacherDashboardActivity;
import com.nibm.madcw.ui.student.StudentDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginBtn;
    TextView signupLink;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.editTextUsername);
        passwordInput = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        signupLink = findViewById(R.id.textViewSignup);

        dbHelper = new TuitionDbHelper(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username = ? AND password = ?",
                new String[]{username, password}
        );

        if (cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

            SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", userId);
            editor.putString("user_role", role);
            editor.apply();

            // Redirect based on role
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
                    Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
                    return;
            }

            finish(); // Close login activity
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
