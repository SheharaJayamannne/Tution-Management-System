package com.nibm.madcw.ui.student;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AssignmentDetailActivity extends AppCompatActivity {

    TextView textTitle, textDescription, textDueDate, textStatus;
    Button buttonDownload, buttonRemove;
    String filePath, dueDate;
    int assignmentId, studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        textTitle = findViewById(R.id.textTitle);
        textDescription = findViewById(R.id.textDescription);
        textDueDate = findViewById(R.id.textDueDate);
        textStatus = findViewById(R.id.textStatus);
        buttonDownload = findViewById(R.id.buttonDownload);
        buttonRemove = findViewById(R.id.buttonRemove);

        Intent intent = getIntent();
        textTitle.setText(intent.getStringExtra("title"));
        textDescription.setText(intent.getStringExtra("description"));
        dueDate = intent.getStringExtra("due_date");
        textDueDate.setText("Due: " + dueDate);
        textStatus.setText(intent.getStringExtra("submission_status"));
        filePath = intent.getStringExtra("file_path");

        assignmentId = intent.getIntExtra("assignment_id", -1);
        studentId = intent.getIntExtra("student_id", -1);

        // Debug print
        Log.d("AssignmentDetail", "assignment_id=" + assignmentId + ", student_id=" + studentId);
        Log.d("AssignmentDetail", "file_path=" + filePath);

        // Handle download button
        if (filePath != null && !filePath.trim().isEmpty() && !filePath.equalsIgnoreCase("null")) {
            buttonDownload.setEnabled(true);
            buttonDownload.setAlpha(1f);
            buttonDownload.setText("Download Submission");

            buttonDownload.setOnClickListener(v -> {
                try {
                    Uri fileUri = Uri.parse(filePath);
                    Intent openFile = new Intent(Intent.ACTION_VIEW);
                    openFile.setDataAndType(fileUri, "*/*");
                    openFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(Intent.createChooser(openFile, "Open file using"));
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open file.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            buttonDownload.setEnabled(false);
            buttonDownload.setAlpha(0.5f);
            buttonDownload.setText("No Submission Available");
        }

        // Handle remove button
        if (isBeforeDueDate(dueDate) && filePath != null && !filePath.trim().isEmpty() && !filePath.equalsIgnoreCase("null")) {
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setOnClickListener(v -> {
                try {
                    TuitionDbHelper dbHelper = new TuitionDbHelper(this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    int deleted = db.delete("submissions", "assignment_id = ? AND student_id = ?",
                            new String[]{String.valueOf(assignmentId), String.valueOf(studentId)});

                    db.close();

                    if (deleted > 0) {
                        Toast.makeText(this, "Submission removed.", Toast.LENGTH_SHORT).show();
                        buttonDownload.setEnabled(false);
                        buttonDownload.setAlpha(0.5f);
                        buttonDownload.setText("No Submission Available");
                        buttonRemove.setVisibility(View.GONE);
                        textStatus.setText("Not Submitted");
                    } else {
                        Toast.makeText(this, "No submission found to remove.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Delete error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        } else {
            buttonRemove.setVisibility(View.GONE);
        }
    }

    private boolean isBeforeDueDate(String dueDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date due = sdf.parse(dueDateStr);
            return new Date().before(due);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
