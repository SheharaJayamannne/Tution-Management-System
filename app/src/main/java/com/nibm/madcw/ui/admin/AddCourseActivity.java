package com.nibm.madcw.ui.admin;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
        import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    EditText editTextCourseName;
    Spinner spinnerTeachers;
    Button buttonAddCourse;

    TuitionDbHelper dbHelper;
    Map<String, Integer> teacherMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        editTextCourseName = findViewById(R.id.editTextCourseName);
        spinnerTeachers = findViewById(R.id.spinnerTeachers);
        buttonAddCourse = findViewById(R.id.buttonAddCourse);

        dbHelper = new TuitionDbHelper(this);

        loadTeachers();

        buttonAddCourse.setOnClickListener(v -> addCourse());
    }

    private void loadTeachers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM users WHERE role = 'teacher'", null);

        List<String> teacherNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            teacherMap.put(name, id);
            teacherNames.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teacherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(adapter);
    }

    private void addCourse() {
        String courseName = editTextCourseName.getText().toString().trim();
        String teacherName = (String) spinnerTeachers.getSelectedItem();

        if (courseName.isEmpty()) {
            Toast.makeText(this, "Please enter course name", Toast.LENGTH_SHORT).show();
            return;
        }

        int teacherId = teacherMap.getOrDefault(teacherName, -1);
        if (teacherId == -1) {
            Toast.makeText(this, "Invalid teacher selected", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", courseName);
        values.put("teacher_id", teacherId);

        long result = db.insert("courses", null, values);

        if (result != -1) {
            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding course", Toast.LENGTH_SHORT).show();
        }
    }
}

