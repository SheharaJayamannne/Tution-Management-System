package com.nibm.madcw.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Material;

import java.util.ArrayList;
import java.util.List;

public class ManageMaterialsActivity extends AppCompatActivity {

    private Spinner spinnerFilter;
    private RecyclerView recyclerView;
    private Button buttonUploadMaterial;

    private TuitionDbHelper dbHelper;
    private MaterialManageAdapter adapter;
    private List<Material> materialList = new ArrayList<>();

    private List<String> filterOptions = new ArrayList<>();
    private List<Integer> filterCourseIds = new ArrayList<>();  // to track matching course IDs
    private int teacherId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_materials);

        spinnerFilter = findViewById(R.id.spinnerCourseFilter);
        recyclerView = findViewById(R.id.recyclerViewMaterials);
        buttonUploadMaterial = findViewById(R.id.buttonUploadNew);

        dbHelper = new TuitionDbHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialManageAdapter(this, materialList);
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        teacherId = prefs.getInt("user_id", -1);

        setupSpinner();
        //loadMaterials("All");  // Load all materials initially

        buttonUploadMaterial.setOnClickListener(v -> {
            // Redirect to your UploadMaterialActivity
            Intent intent = new Intent(ManageMaterialsActivity.this, UploadMaterialActivity.class);
            startActivity(intent);
        });
    }

    private void setupSpinner() {
        filterOptions.clear();
        filterCourseIds.clear();

        filterOptions.add("All"); // Default
        filterCourseIds.add(-1);  // Dummy ID for "All"

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT c.id, c.name FROM courses c " +
                        "INNER JOIN teacher_courses tc ON c.id = tc.course_id " +
                        "WHERE tc.teacher_id = ?", new String[]{String.valueOf(teacherId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int courseId = cursor.getInt(0);
                String courseName = cursor.getString(1);

                filterOptions.add(courseName);
                filterCourseIds.add(courseId);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedCourseId = filterCourseIds.get(position);
                loadMaterials(selectedCourseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }


    private void loadMaterials(int courseId) {

        materialList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        try {
            if (courseId == -1) { // Load all
                cursor = db.rawQuery("SELECT id, title, file_path FROM materials", null);
            } else {
                cursor = db.rawQuery("SELECT id, title, file_path FROM materials WHERE course_id = ?", new String[]{String.valueOf(courseId)});
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String filePath = cursor.getString(2);
                    materialList.add(new Material(id, title, filePath));
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();
        int selectedIndex = spinnerFilter.getSelectedItemPosition();
        int selectedCourseId = filterCourseIds.get(selectedIndex);
        loadMaterials(selectedCourseId);
    }

}
