package com.nibm.madcw.ui.admin;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

public class ViewTeacherProfileActivity extends AppCompatActivity {

    TextView textName, textEmail;
    TextView textPassword, textAssignedCourses, textAssignmentsGiven, textMaterialsUploaded;
    Button buttonResetPassword, buttonEditDetails;

    int teacherId;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_teacher_profile);

        textName = findViewById(R.id.editTeacherName);  // TextView
        textEmail = findViewById(R.id.editTeacherEmail); // TextView
        textPassword = findViewById(R.id.textTeacherPassword);
        textAssignedCourses = findViewById(R.id.textAssignedCourses);
        textAssignmentsGiven = findViewById(R.id.textAssignmentsGiven);
        textMaterialsUploaded = findViewById(R.id.textMaterialsUploaded);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonEditDetails = findViewById(R.id.buttonEditDetails);

        dbHelper = new TuitionDbHelper(this);

        if (getIntent() != null && getIntent().hasExtra("teacherId")) {
            teacherId = getIntent().getIntExtra("teacherId", -1);
            loadTeacherProfile();
        } else {
            Toast.makeText(this, "Teacher ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonResetPassword.setOnClickListener(v -> resetTeacherPassword());

        // Show dialog for editing
        buttonEditDetails.setOnClickListener(v -> showEditDialog());
    }

    private void loadTeacherProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name, email, password FROM users WHERE id=? AND role='teacher'",
                new String[]{String.valueOf(teacherId)});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            String password = cursor.getString(2);

            textName.setText(name);
            textEmail.setText(email);

            textPassword.setText("Password: " + password);
        }
        cursor.close();

        // Load assigned courses
        Cursor courseCursor = db.rawQuery(
                "SELECT c.name FROM courses c " +
                        "INNER JOIN teacher_courses tc ON c.id = tc.course_id " +
                        "WHERE tc.teacher_id = ?",
                new String[]{String.valueOf(teacherId)});
        StringBuilder coursesBuilder = new StringBuilder();
        while (courseCursor.moveToNext()) {
            coursesBuilder.append("• ").append(courseCursor.getString(0)).append("\n");
        }
        courseCursor.close();
        textAssignedCourses.setText("Courses Assigned:\n" + (coursesBuilder.length() > 0 ? coursesBuilder.toString() : "None"));

        // Load number of assignments given
        Cursor assignmentCursor = db.rawQuery(
                "SELECT COUNT(*) FROM assignments WHERE course_id IN (SELECT id FROM courses WHERE teacher_id = ?)",
                new String[]{String.valueOf(teacherId)});
        if (assignmentCursor.moveToFirst()) {
            int count = assignmentCursor.getInt(0);
            textAssignmentsGiven.setText("Assignments Given: " + count);
        }
        assignmentCursor.close();

        // Load number of uploaded materials
        Cursor materialCursor = db.rawQuery(
                "SELECT COUNT(*) FROM materials WHERE course_id IN (SELECT id FROM courses WHERE teacher_id = ?)",
                new String[]{String.valueOf(teacherId)});
        if (materialCursor.moveToFirst()) {
            int materialCount = materialCursor.getInt(0);
            textMaterialsUploaded.setText("Materials Uploaded: " + materialCount);
        }
        materialCursor.close();
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Teacher Details");

        // Inflate custom layout for dialog
        LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_edit_teacher, null);
        builder.setView(dialogView);

        EditText editName = dialogView.findViewById(R.id.editNameDialog);
        EditText editEmail = dialogView.findViewById(R.id.editEmailDialog);

        // Pre-fill current values
        editName.setText(textName.getText());
        editEmail.setText(textEmail.getText());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = editName.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newEmail.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update DB
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", newName);
            values.put("email", newEmail);

            int rows = db.update("users", values, "id=? AND role='teacher'", new String[]{String.valueOf(teacherId)});
            if (rows > 0) {
                Toast.makeText(this, "Details updated", Toast.LENGTH_SHORT).show();
                // Refresh profile display
                loadTeacherProfile();
            } else {
                Toast.makeText(this, "Failed to update details", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetTeacherPassword() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Are you sure you want to reset this teacher's password to the default?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String newPassword = "teacher123";
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("password", newPassword);

                    int rows = db.update("users", values, "id=? AND role='teacher'", new String[]{String.valueOf(teacherId)});

                    if (rows > 0) {
                        Toast.makeText(this, "Password reset to: " + newPassword, Toast.LENGTH_LONG).show();
                        textPassword.setText("Password: " + newPassword);
                    } else {
                        Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
