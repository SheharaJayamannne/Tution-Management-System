package com.nibm.madcw.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Teacher;
import com.nibm.madcw.ui.admin.TeacherAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageTeacherActivity extends AppCompatActivity {

    ArrayList<Teacher> teacherList = new ArrayList<>();
    TeacherAdapter adapter;
    TuitionDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_teacher);

        dbHelper = new TuitionDbHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTeachers);
        adapter = new TeacherAdapter(this, teacherList, new TeacherAdapter.OnTeacherItemClickListener() {
            @Override
            public void onViewProfileClicked(Teacher teacher) {
                Intent intent = new Intent(ManageTeacherActivity.this, ViewTeacherProfileActivity.class);
                intent.putExtra("teacherId", teacher.getId());
                startActivity(intent);
            }

            @Override
            public void onAssignClicked(Teacher teacher) {
                showCourseManageDialog(teacher); // move the dialog code to this method in the activity
            }

            @Override
            public void onDeleteClicked(Teacher teacher) {
                new AlertDialog.Builder(ManageTeacherActivity.this)
                        .setTitle("Delete Teacher")
                        .setMessage("Are you sure you want to delete " + teacher.getName() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            // Delete from teacher_courses first (foreign key dependency)
                            db.delete("teacher_courses", "teacher_id=?", new String[]{String.valueOf(teacher.getId())});

                            // Then delete from users table
                            int deletedRows = db.delete("users", "id=?", new String[]{String.valueOf(teacher.getId())});

                            if (deletedRows > 0) {
                                Toast.makeText(ManageTeacherActivity.this, "Teacher deleted", Toast.LENGTH_SHORT).show();
                                loadTeachers(); // Refresh list
                            } else {
                                Toast.makeText(ManageTeacherActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                            }

                            db.close();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTeachers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeachers();
    }

    private void loadTeachers() {
        teacherList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, email FROM users WHERE role='teacher'", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            teacherList.add(new Teacher(id, name, email));
        }
        cursor.close();

        adapter.notifyDataSetChanged();
    }
    private AlertDialog currentDialog = null;  // add this as a field in your activity class

    public void showCourseManageDialog(Teacher teacher) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();  // close previous dialog before opening new one
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_manage_teacher_courses, null);

        LinearLayout assignedLayout = dialogView.findViewById(R.id.layoutAssignedCourses);
        LinearLayout availableLayout = dialogView.findViewById(R.id.layoutAvailableCourses);

        TuitionDbHelper dbHelper = new TuitionDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        assignedLayout.removeAllViews();
        availableLayout.removeAllViews();

        // Fetch assigned course IDs for teacher
        Set<Integer> assignedCourseIds = new HashSet<>();
        Cursor assignedCursor = db.rawQuery("SELECT course_id FROM teacher_courses WHERE teacher_id=?", new String[]{String.valueOf(teacher.getId())});
        while (assignedCursor.moveToNext()) {
            assignedCourseIds.add(assignedCursor.getInt(0));
        }
        assignedCursor.close();

        // Show assigned courses with a red ✖ to remove
        if (!assignedCourseIds.isEmpty()) {
            String query = "SELECT id, name FROM courses WHERE id IN (" + TextUtils.join(",", assignedCourseIds) + ")";
            Cursor assigned = db.rawQuery(query, null);
            while (assigned.moveToNext()) {
                int cid = assigned.getInt(0);
                String name = assigned.getString(1);
                TextView tv = new TextView(this);
                tv.setText(name + " ✖");
                tv.setTextColor(Color.RED);
                tv.setPadding(16, 16, 16, 16);
                tv.setTextSize(16);
                tv.setOnClickListener(v -> {
                    SQLiteDatabase wdb = dbHelper.getWritableDatabase();
                    wdb.delete("teacher_courses", "teacher_id=? AND course_id=?", new String[]{String.valueOf(teacher.getId()), String.valueOf(cid)});
                    Toast.makeText(this, "Removed " + name, Toast.LENGTH_SHORT).show();
                    showCourseManageDialog(teacher);  // refresh dialog
                });
                assignedLayout.addView(tv);
            }
            assigned.close();
        } else {
            TextView noAssigned = new TextView(this);
            noAssigned.setText("No courses assigned");
            noAssigned.setPadding(16, 16, 16, 16);
            assignedLayout.addView(noAssigned);
        }

        // Show available courses to assign (exclude already assigned)
        Cursor allCourses = db.rawQuery("SELECT id, name FROM courses", null);
        while (allCourses.moveToNext()) {
            int cid = allCourses.getInt(0);
            String name = allCourses.getString(1);
            if (assignedCourseIds.contains(cid)) continue;

            TextView tv = new TextView(this);
            tv.setText("+ " + name);
            tv.setTextColor(Color.parseColor("#00695C"));
            tv.setPadding(16, 16, 16, 16);
            tv.setTextSize(16);
            tv.setOnClickListener(v -> {
                SQLiteDatabase wdb = dbHelper.getWritableDatabase();
                wdb.execSQL("INSERT INTO teacher_courses (teacher_id, course_id) VALUES (?, ?)", new Object[]{teacher.getId(), cid});
                Toast.makeText(this, "Assigned " + name, Toast.LENGTH_SHORT).show();
                showCourseManageDialog(teacher);  // refresh dialog
            });
            availableLayout.addView(tv);
        }
        allCourses.close();

        // Create and show the dialog, save reference
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Manage Courses for " + teacher.getName())
                .setView(dialogView)
                .setNegativeButton("Close", (dialog, which) -> currentDialog = null);

        currentDialog = builder.show();
    }



}
