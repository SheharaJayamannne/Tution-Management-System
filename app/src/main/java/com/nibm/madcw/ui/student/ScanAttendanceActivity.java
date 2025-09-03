package com.nibm.madcw.ui.student;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanAttendanceActivity extends AppCompatActivity {

    Button buttonScanAttendance;
    TextView textScanStatus;
    TuitionDbHelper dbHelper;
    int studentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_attendance);

        buttonScanAttendance = findViewById(R.id.buttonScanAttendance);
        textScanStatus = findViewById(R.id.textScanStatus);

        dbHelper = new TuitionDbHelper(this);

        // Get student ID from session
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        buttonScanAttendance.setOnClickListener(v -> {
            if (studentId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start QR code scanner
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Scan course QR code");
            integrator.setOrientationLocked(true);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });
    }

    // Handle scanner result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String rawContent = result.getContents().trim();
                Log.d("QR_SCAN_DEBUG", "RAW: [" + rawContent + "]");
                Log.d("QR_SCAN_DEBUG", "LENGTH: " + rawContent.length());

                try {
                    int courseId = -1;

                    // Handle array or object
                    if (rawContent.startsWith("[")) {
                        // It's a JSON array
                        JSONArray jsonArray = new JSONArray(rawContent);
                        if (jsonArray.length() > 0) {
                            JSONObject obj = jsonArray.getJSONObject(0);
                            courseId = obj.has("courseId") ? obj.getInt("courseId") : obj.getInt("courseid");
                        }
                    } else if (rawContent.startsWith("{")) {
                        // It's a single JSON object
                        JSONObject obj = new JSONObject(rawContent);
                        courseId = obj.has("courseId") ? obj.getInt("courseId") : obj.getInt("courseid");
                    }

                    if (courseId != -1) {
                        markAttendance(studentId, courseId);
                    } else {
                        Toast.makeText(this, "Course ID not found", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.e("QR_PARSE_ERROR", "Error parsing QR content", e);
                    Toast.makeText(this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void markAttendance(int studentId, int courseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Prevent duplicate entries
        db.execSQL("INSERT OR IGNORE INTO attendance (student_id, course_id, date, status) VALUES (?, ?, ?, 'present')",
                new Object[]{studentId, courseId, currentDate});

        textScanStatus.setText("Attendance marked for Course ID: " + courseId);
        Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show();
    }
}
