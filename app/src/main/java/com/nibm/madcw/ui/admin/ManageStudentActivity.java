package com.nibm.madcw.ui.admin;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Student;
import com.nibm.madcw.ui.admin.StudentAdapter;


import java.util.ArrayList;

public class ManageStudentActivity extends AppCompatActivity {

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();
    StudentAdapter adapter;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_student);

        dbHelper = new TuitionDbHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewStudents);
        adapter = new StudentAdapter(this, filteredList);  // Use filtered list for searching
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



        loadStudents();

        SearchView searchView = findViewById(R.id.searchViewStudents);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                for (Student student : studentList) {
                    if (student.name.toLowerCase().contains(newText.toLowerCase()) ||
                            student.email.toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(student);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    private void loadStudents() {
        studentList.clear();
        filteredList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, email FROM users WHERE role='student'", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            studentList.add(new Student(id, name, email));
        }
        cursor.close();

        filteredList.addAll(studentList);
        adapter.notifyDataSetChanged();
    }
}
