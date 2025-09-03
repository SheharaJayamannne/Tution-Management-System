package com.nibm.madcw.ui.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewAssignmentsActivity extends AppCompatActivity {

    ListView listViewAssignments;
    TuitionDbHelper dbHelper;
    ArrayList<HashMap<String, String>> assignmentList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assignments);

        listViewAssignments = findViewById(R.id.listViewAssignments);
        dbHelper = new TuitionDbHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        loadAssignments();

        listViewAssignments.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> selected = assignmentList.get(position);

            Intent intent = new Intent(ViewAssignmentsActivity.this, AssignmentDetailActivity.class);
            intent.putExtra("assignment_id", selected.get("id"));
            intent.putExtra("title", selected.get("title"));
            intent.putExtra("description", selected.get("description"));
            intent.putExtra("due_date", selected.get("due_date"));
            intent.putExtra("file_path", selected.get("file_path"));
            intent.putExtra("submission_status", selected.get("submission_status"));
            startActivity(intent);
        });

    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.UNSPECIFIED
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void loadAssignments() {
        assignmentList.clear();
        ArrayList<String> displayList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor courseCursor = db.rawQuery(
                "SELECT course_id FROM student_courses WHERE student_id = ?",
                new String[]{String.valueOf(studentId)});

        ArrayList<String> courseIds = new ArrayList<>();
        while (courseCursor.moveToNext()) {
            courseIds.add(courseCursor.getString(0));
        }
        courseCursor.close();

        if (courseIds.isEmpty()) {
            displayList.add("No assignments (not enrolled)");
        } else {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < courseIds.size(); i++) {
                placeholders.append("?");
                if (i < courseIds.size() - 1) placeholders.append(",");
            }

            Cursor assignmentCursor = db.rawQuery(
                    "SELECT a.id, a.title, a.description, a.due_date, a.file_path FROM assignments a " +
                            "WHERE a.course_id IN (" + placeholders + ")",
                    courseIds.toArray(new String[0])
            );

            while (assignmentCursor.moveToNext()) {
                String id = assignmentCursor.getString(0);
                String title = assignmentCursor.getString(1);
                String description = assignmentCursor.getString(2);
                String dueDate = assignmentCursor.getString(3);
                String filePath = assignmentCursor.getString(4);

                // Check submission status
                Cursor subCursor = db.rawQuery(
                        "SELECT submitted_at FROM submissions WHERE assignment_id = ? AND student_id = ?",
                        new String[]{id, String.valueOf(studentId)});
                String status = subCursor.moveToFirst() ?
                        "Submitted on " + subCursor.getString(0) : "Not submitted yet";
                subCursor.close();

                HashMap<String, String> item = new HashMap<>();
                item.put("id", id);
                item.put("title", title);
                item.put("description", description);
                item.put("due_date", dueDate);
                item.put("file_path", filePath);
                item.put("submission_status", status);

                assignmentList.add(item);
                displayList.add(title + " (Due: " + dueDate + ") - " + status);
            }
            assignmentCursor.close();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewAssignments.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listViewAssignments);

    }
}
