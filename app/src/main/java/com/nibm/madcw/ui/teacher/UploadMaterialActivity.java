package com.nibm.madcw.ui.teacher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.ArrayList;

public class UploadMaterialActivity extends AppCompatActivity {

    Spinner spinnerCourses;
    EditText editMaterialTitle;
    TextView textSelectedFile;
    Button buttonChooseFile, buttonUpload;

    private static final int PICK_FILE_REQUEST = 1;
    Uri selectedFileUri;
    int teacherId;

    ArrayList<Integer> courseIds = new ArrayList<>();
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_material);

        spinnerCourses = findViewById(R.id.spinnerCourses);
        editMaterialTitle = findViewById(R.id.editMaterialTitle);
        textSelectedFile = findViewById(R.id.textSelectedFile);
        buttonChooseFile = findViewById(R.id.buttonChooseFile);
        buttonUpload = findViewById(R.id.buttonUpload);

        dbHelper = new TuitionDbHelper(this);

        //  Get teacherId from SharedPreferences instead of Intent
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        teacherId = prefs.getInt("user_id", -1);

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        loadCourses();

        buttonChooseFile.setOnClickListener(v -> chooseFile());

        buttonUpload.setOnClickListener(v -> uploadMaterial());
    }

    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name FROM courses WHERE id IN (SELECT course_id FROM teacher_courses WHERE teacher_id = ?)",
                new String[]{String.valueOf(teacherId)}
        );

        ArrayList<String> courseNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            courseIds.add(id);
            courseNames.add(name);
        }
        cursor.close();

        if (courseNames.isEmpty()) {
            Toast.makeText(this, "No courses assigned to you", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseNames);
        spinnerCourses.setAdapter(adapter);
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            String fileName = getFileName(selectedFileUri);
            textSelectedFile.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String name = "unknown";
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return name;
    }

    private void uploadMaterial() {
        String title = editMaterialTitle.getText().toString().trim();

        if (title.isEmpty() || selectedFileUri == null) {
            Toast.makeText(this, "Please enter title and select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerCourses.getSelectedItemPosition();
        int selectedCourseId = courseIds.get(selectedPosition);
        String filePath = selectedFileUri.toString(); // Save as URI string

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO materials (course_id, title, file_path) VALUES (?, ?, ?)",
                new Object[]{selectedCourseId, title, filePath});

        Toast.makeText(this, "Material uploaded successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
