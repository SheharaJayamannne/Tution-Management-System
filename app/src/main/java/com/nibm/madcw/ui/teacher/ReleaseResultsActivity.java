package com.nibm.madcw.ui.teacher;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Submission;

import java.util.ArrayList;

public class ReleaseResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private ArrayList<Submission> submissionsList = new ArrayList<>();
    private TuitionDbHelper dbHelper;
    private int teacherId;
    private Button buttonReleaseAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_results);

        recyclerView = findViewById(R.id.recyclerViewResults);
        buttonReleaseAll = findViewById(R.id.buttonReleaseAll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultsAdapter(this, submissionsList, this::confirmUpdateMarks);
        recyclerView.setAdapter(adapter);

        dbHelper = new TuitionDbHelper(this);

        if (getIntent().hasExtra("teacherId")) {
            teacherId = getIntent().getIntExtra("teacherId", -1);
            loadSubmissions();
        } else {
            Toast.makeText(this, "Teacher ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonReleaseAll.setOnClickListener(v -> confirmReleaseAllResults());
    }

    private void loadSubmissions() {
        submissionsList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT submissions.id, users.name, assignments.title, submissions.marks " +
                "FROM submissions " +
                "INNER JOIN assignments ON submissions.assignment_id = assignments.id " +
                "INNER JOIN courses ON assignments.course_id = courses.id " +
                "INNER JOIN users ON submissions.student_id = users.id " +
                "WHERE courses.teacher_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherId)});

        while (cursor.moveToNext()) {
            int submissionId = cursor.getInt(0);
            String studentName = cursor.getString(1);
            String assignmentTitle = cursor.getString(2);
            int marks = cursor.isNull(3) ? -1 : cursor.getInt(3);

            submissionsList.add(new Submission(submissionId, studentName, assignmentTitle, marks));
        }
        cursor.close();

        adapter.notifyDataSetChanged();
    }

    private void confirmUpdateMarks(int submissionId, int newMarks) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to save the new marks?")
                .setPositiveButton("Yes", (dialog, which) -> updateMarks(submissionId, newMarks))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateMarks(int submissionId, int newMarks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marks", newMarks);
        int rows = db.update("submissions", values, "id=?", new String[]{String.valueOf(submissionId)});
        if (rows > 0) {
            Toast.makeText(this, "Marks updated", Toast.LENGTH_SHORT).show();
            loadSubmissions(); // refresh
        } else {
            Toast.makeText(this, "Failed to update marks", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmReleaseAllResults() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Release All Results")
                .setMessage("Are you sure you want to release all results? This action cannot be undone.")
                .setPositiveButton("Release All", (dialog, which) -> releaseAllResults())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void releaseAllResults() {
        // For example, add a new column "released" in submissions table to mark released status
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("released", 1); // 1 = released

        int rows = db.update("submissions", values, null, null);
        if (rows > 0) {
            Toast.makeText(this, "All results released", Toast.LENGTH_SHORT).show();
            // Optionally refresh UI or disable editing after release
            loadSubmissions();
        } else {
            Toast.makeText(this, "Failed to release results", Toast.LENGTH_SHORT).show();
        }
    }
}
