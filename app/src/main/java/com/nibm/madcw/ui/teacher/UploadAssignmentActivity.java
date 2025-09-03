package com.nibm.madcw.ui.teacher;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.*;

public class UploadAssignmentActivity extends AppCompatActivity {

    EditText editTextTitle, editTextDescription, editTextDueDate;
    Spinner spinnerCourses;
    Button buttonUpload, buttonSelectFile;

    TuitionDbHelper dbHelper;
    int teacherId;
    Uri selectedFileUri = null;
    Map<String, Integer> courseMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_assignment);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDueDate = findViewById(R.id.editTextDueDate);
        spinnerCourses = findViewById(R.id.spinnerCourses);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectFile = findViewById(R.id.buttonSelectFile);

        dbHelper = new TuitionDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        teacherId = prefs.getInt("user_id", -1);

        loadCourses();

        editTextDueDate.setOnClickListener(view -> showDatePicker());

        buttonSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 101);
        });

        buttonUpload.setOnClickListener(v -> uploadAssignment());
    }

    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM courses WHERE teacher_id = ?",
                new String[]{String.valueOf(teacherId)});

        List<String> courseNames = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            courseMap.put(name, id);
            courseNames.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, courseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = year + "-" + (month + 1) + "-" + dayOfMonth;
                    editTextDueDate.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Prevent selecting past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }


    private void uploadAssignment() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String dueDate = editTextDueDate.getText().toString().trim();
        String courseName = spinnerCourses.getSelectedItem().toString();

        if (title.isEmpty() || courseName.isEmpty()) {
            Toast.makeText(this, "Title and Course are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int courseId = courseMap.get(courseName);
        String filePath = selectedFileUri != null ? selectedFileUri.toString() : null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO assignments (course_id, title, description, due_date, file_path) VALUES (?, ?, ?, ?, ?)",
                new Object[]{courseId, title, description, dueDate, filePath});

        Toast.makeText(this, "Assignment uploaded successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
        }
    }
}
