package com.nibm.madcw.ui.admin;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.*;

public class AssignStudentActivity extends AppCompatActivity {

    SearchView searchViewStudents;
    ListView listViewStudents;
    Spinner spinnerCourses;
    Button buttonAssign;

    TuitionDbHelper dbHelper;

    Map<String, Integer> studentMap = new HashMap<>();
    Map<String, Integer> courseMap = new HashMap<>();
    List<String> allStudents = new ArrayList<>();
    List<String> visibleStudents = new ArrayList<>();
    Set<String> selectedStudents = new HashSet<>();

    ArrayAdapter<String> studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_student);

        searchViewStudents = findViewById(R.id.searchViewStudents);
        listViewStudents = findViewById(R.id.listViewStudents);
        spinnerCourses = findViewById(R.id.spinnerCourses);
        buttonAssign = findViewById(R.id.buttonAssign);

        dbHelper = new TuitionDbHelper(this);

        loadCourses(); // load course list first

        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudentsForSelectedCourse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonAssign.setOnClickListener(v -> assignStudentsToCourse());
    }

    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM courses", null);

        List<String> courseNames = new ArrayList<>();
        courseMap.clear();

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

    private void loadStudentsForSelectedCourse() {
        String selectedCourse = (String) spinnerCourses.getSelectedItem();
        if (selectedCourse == null) return;

        int courseId = courseMap.get(selectedCourse);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name FROM users WHERE role = 'student' AND id NOT IN " +
                        "(SELECT student_id FROM student_courses WHERE course_id = ?)",
                new String[]{String.valueOf(courseId)}
        );

        studentMap.clear();
        allStudents.clear();
        visibleStudents.clear();
        selectedStudents.clear();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            studentMap.put(name, id);
            allStudents.add(name);
        }
        cursor.close();

        // Initially show up to 5 students
        int countToShow = Math.min(5, allStudents.size());
        visibleStudents.clear();
        for (int i = 0; i < countToShow; i++) {
            visibleStudents.add(allStudents.get(i));
        }

        studentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, visibleStudents);
        listViewStudents.setAdapter(studentAdapter);
        listViewStudents.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewStudents.setOnItemClickListener((parent, view, position, id) -> {
            String student = studentAdapter.getItem(position);
            if (listViewStudents.isItemChecked(position)) {
                selectedStudents.add(student);
            } else {
                selectedStudents.remove(student);
            }
        });

        searchViewStudents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateFilteredStudentList(newText);
                return true;
            }
        });
    }

    private void updateFilteredStudentList(String query) {
        List<String> filtered = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(visibleStudents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (String student : allStudents) {
                if (student.toLowerCase().contains(lowerQuery)) {
                    filtered.add(student);
                }
            }
        }

        studentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, filtered);
        listViewStudents.setAdapter(studentAdapter);

        for (int i = 0; i < filtered.size(); i++) {
            if (selectedStudents.contains(filtered.get(i))) {
                listViewStudents.setItemChecked(i, true);
            }
        }
    }

    private void assignStudentsToCourse() {
        String selectedCourse = (String) spinnerCourses.getSelectedItem();
        if (selectedCourse == null) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        int courseId = courseMap.get(selectedCourse);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean assignedAtLeastOne = false;

        for (String studentName : selectedStudents) {
            int studentId = studentMap.get(studentName);

            Cursor checkCursor = db.rawQuery(
                    "SELECT * FROM student_courses WHERE student_id = ? AND course_id = ?",
                    new String[]{String.valueOf(studentId), String.valueOf(courseId)}
            );

            if (!checkCursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("student_id", studentId);
                values.put("course_id", courseId);
                db.insert("student_courses", null, values);
                assignedAtLeastOne = true;
            }

            checkCursor.close();
        }

        if (assignedAtLeastOne) {
            Toast.makeText(this, "Students assigned successfully", Toast.LENGTH_SHORT).show();
            loadStudentsForSelectedCourse(); // Refresh list to remove newly assigned students
        } else {
            Toast.makeText(this, "No new students were assigned", Toast.LENGTH_SHORT).show();
        }
    }
}
