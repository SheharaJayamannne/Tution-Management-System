package com.nibm.madcw.ui.student;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Submission;

import java.util.ArrayList;

public class ViewResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ResultsViewAdapter adapter;
    private ArrayList<Submission> resultsList = new ArrayList<>();
    private TuitionDbHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        recyclerView = findViewById(R.id.recyclerViewStudentResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new TuitionDbHelper(this);

        // Assume studentId is passed through intent
        if (getIntent().hasExtra("studentId")) {
            studentId = getIntent().getIntExtra("studentId", -1);
            loadResults();
        } else {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadResults() {
        resultsList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT assignments.title, courses.name, submissions.marks " +
                "FROM submissions " +
                "INNER JOIN assignments ON submissions.assignment_id = assignments.id " +
                "INNER JOIN courses ON assignments.course_id = courses.id " +
                "WHERE submissions.student_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
        while (cursor.moveToNext()) {
            String assignmentTitle = cursor.getString(0);
            String courseName = cursor.getString(1);
            int marks = cursor.isNull(2) ? -1 : cursor.getInt(2);

            resultsList.add(new Submission(0, "", assignmentTitle + " (" + courseName + ")", marks));
        }
        cursor.close();

        adapter = new ResultsViewAdapter(resultsList);
        recyclerView.setAdapter(adapter);
    }
}

