package com.nibm.madcw.ui.teacher;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity {

    Button btnScan;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        dbHelper = new TuitionDbHelper(this);
        btnScan = findViewById(R.id.btnScanQR);

        btnScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(AttendanceActivity.this);
            integrator.setPrompt("Scan student QR");
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String studentId = result.getContents(); // Assuming student ID is encoded in QR

            // Insert into attendance table
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            db.execSQL("INSERT INTO attendance (student_id, course_id, date, status) VALUES (?, ?, ?, ?)",
                    new Object[]{studentId, 1, date, "present"}); // Hardcoded course_id = 1 for demo

            Toast.makeText(this, "Attendance marked for student ID: " + studentId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

