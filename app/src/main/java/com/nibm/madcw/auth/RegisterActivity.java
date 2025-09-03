package com.nibm.madcw.auth;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, usernameInput, emailInput, passwordInput;
    Spinner roleSpinner;
    Button registerBtn;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.editTextName);
        usernameInput = findViewById(R.id.editTextUsername);
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        roleSpinner = findViewById(R.id.spinnerRole);
        registerBtn = findViewById(R.id.buttonRegister);
        dbHelper = new TuitionDbHelper(this);

        // Spinner values: admin, teacher, student
        String[] roles = {"admin", "teacher", "student"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Hide spinner if role passed from intent
        String forcedRole = getIntent().getStringExtra("forced_role");
        if (forcedRole != null) {
            roleSpinner.setVisibility(View.GONE);
        }

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(forcedRole);
            }
        });
    }

    private boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE username = ?", new String[]{username});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private void registerUser(String forcedRole) {
        String name = nameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = (forcedRole != null) ? forcedRole : roleSpinner.getSelectedItem().toString();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUsernameExists(username)) {
            Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailExists(email)) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);

        long result = db.insert("users", null, values);

        if (result != -1) {
            Toast.makeText(this, role + " registered successfully!", Toast.LENGTH_SHORT).show();

            if (forcedRole != null) {
                // Admin-initiated registration — stay on screen and clear form
                nameInput.setText("");
                usernameInput.setText("");
                emailInput.setText("");
                passwordInput.setText("");
            } else {
                // User self-registering — redirect to login
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
