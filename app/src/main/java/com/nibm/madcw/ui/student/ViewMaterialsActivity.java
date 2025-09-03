package com.nibm.madcw.ui.student;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Material;
import com.nibm.madcw.ui.student.MaterialAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewMaterialsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MaterialAdapter adapter;
    List<Material> materialList = new ArrayList<>();
    TuitionDbHelper dbHelper;

    int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_materials);

        recyclerView = findViewById(R.id.recyclerViewMaterials);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new TuitionDbHelper(this);

        // Get the courseId from intent
        courseId = getIntent().getIntExtra("courseId", -1);

        if (courseId == -1) {
            Toast.makeText(this, "Course ID missing", Toast.LENGTH_SHORT).show();
            Log.e("ViewMaterials", "Missing courseId in intent");
            finish();
            return;
        }

        Log.d("ViewMaterials", "Received courseId: " + courseId);

        loadMaterials(courseId);
    }

    private void loadMaterials(int courseId) {
        materialList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id, title, file_path FROM materials WHERE course_id = ?",
                    new String[]{String.valueOf(courseId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String path = cursor.getString(2);
                    materialList.add(new Material(id, title, path));
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "No materials found for this course.", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        if (adapter == null) {
            adapter = new MaterialAdapter(this, materialList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}