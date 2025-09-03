package com.nibm.madcw.ui.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SubmitAssignmentActivity extends AppCompatActivity {

    Spinner spinnerAssignments;
    Button buttonSelectFile, buttonSubmit;
    TextView textSelectedFile;
    TuitionDbHelper dbHelper;
    Uri selectedFileUri = null;
    int studentId;

    ArrayList<HashMap<String, String>> assignments = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;

    ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        spinnerAssignments = findViewById(R.id.spinnerAssignments);
        buttonSelectFile = findViewById(R.id.buttonSelectFile);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textSelectedFile = findViewById(R.id.textSelectedFile);

        dbHelper = new TuitionDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        textSelectedFile.setText(selectedFileUri.getLastPathSegment());
                    }
                });

        buttonSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });

        buttonSubmit.setOnClickListener(v -> submitAssignment());

        loadAssignments();
    }

    private void loadAssignments() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor courseCursor = db.rawQuery("SELECT course_id FROM student_courses WHERE student_id = ?",
                new String[]{String.valueOf(studentId)});

        ArrayList<String> courseIds = new ArrayList<>();
        while (courseCursor.moveToNext()) {
            courseIds.add(courseCursor.getString(0));
        }
        courseCursor.close();

        if (courseIds.isEmpty()) return;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < courseIds.size(); i++) {
            placeholders.append("?");
            if (i < courseIds.size() - 1) placeholders.append(",");
        }

        Cursor assignmentCursor = db.rawQuery(
                "SELECT id, title FROM assignments WHERE course_id IN (" + placeholders + ")",
                courseIds.toArray(new String[0]));

        ArrayList<String> titles = new ArrayList<>();
        while (assignmentCursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("id", assignmentCursor.getString(0));
            map.put("title", assignmentCursor.getString(1));
            assignments.add(map);
            titles.add(assignmentCursor.getString(1));
        }
        assignmentCursor.close();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, titles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignments.setAdapter(spinnerAdapter);
    }

    private void submitAssignment() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerAssignments.getSelectedItemPosition();
        if (selectedPosition == -1) {
            Toast.makeText(this, "Please select an assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        String assignmentId = assignments.get(selectedPosition).get("id");
        String filePath = selectedFileUri.toString();
        String submittedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO submissions (assignment_id, student_id, file_path, submitted_at) VALUES (?, ?, ?, ?)",
                new Object[]{assignmentId, studentId, filePath, submittedAt});

        Toast.makeText(this, "Assignment submitted successfully", Toast.LENGTH_SHORT).show();
        selectedFileUri = null;
        textSelectedFile.setText("No file selected");
    }
}

