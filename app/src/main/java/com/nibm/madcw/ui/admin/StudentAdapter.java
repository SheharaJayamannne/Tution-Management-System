package com.nibm.madcw.ui.admin;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Student;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    Context context;
    ArrayList<Student> studentList;
    TuitionDbHelper dbHelper;

    public StudentAdapter(Context context, ArrayList<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
        dbHelper = new TuitionDbHelper(context);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.textName.setText(student.name);
        holder.textEmail.setText(student.email);

        String password = getPasswordForStudent(student.id);
        holder.textPassword.setText("Password: " + (password != null ? password : "N/A"));

        String assignedCourses = getAssignedCoursesString(student.id);
        holder.textCourses.setText("Courses: " + (assignedCourses.isEmpty() ? "None" : assignedCourses));

        holder.buttonAssign.setOnClickListener(v -> showCourseAssignDialog(student.id, holder));

        holder.buttonRemoveCourse.setOnClickListener(v -> showRemoveCourseDialog(student.id, holder));

        holder.buttonEdit.setOnClickListener(v -> showEditStudentDialog(student));

        holder.buttonReport.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentReportActivity.class);
            intent.putExtra("studentId", student.id);
            context.startActivity(intent);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete this student?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("users", "id=?", new String[]{String.valueOf(student.id)});
                        studentList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, studentList.size());
                        Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.buttonResetPassword.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Reset Password")
                    .setMessage("Are you sure you want to reset this student's password to the default?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        resetPassword(student.id);
                        holder.textPassword.setText("Password: student123");
                        Toast.makeText(context, "Password reset to: student123", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private String getPasswordForStudent(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE id=? AND role='student'",
                new String[]{String.valueOf(studentId)});
        String password = null;
        if (cursor.moveToFirst()) {
            password = cursor.getString(0);
        }
        cursor.close();
        return password;
    }

    private String getAssignedCoursesString(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM courses WHERE id IN (SELECT course_id FROM student_courses WHERE student_id=?)",
                new String[]{String.valueOf(studentId)});
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append(cursor.getString(0)).append(", ");
        }
        cursor.close();
        if (sb.length() > 0) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private void resetPassword(int studentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", "student123");
        db.update("users", values, "id=? AND role='student'", new String[]{String.valueOf(studentId)});
    }

    private void showEditStudentDialog(Student student) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_student, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);

        editName.setText(student.name);
        editEmail.setText(student.email);

        new AlertDialog.Builder(context)
                .setTitle("Edit Student")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("email", email);

                    int rows = db.update("users", values, "id=? AND role='student'",
                            new String[]{String.valueOf(student.id)});

                    if (rows > 0) {
                        Toast.makeText(context, "Student updated", Toast.LENGTH_SHORT).show();
                        student.name = name;
                        student.email = email;
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCourseAssignDialog(int studentId, StudentViewHolder holder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> courseNames = new ArrayList<>();
        ArrayList<Integer> courseIds = new ArrayList<>();

        Set<Integer> alreadyAssigned = new HashSet<>();
        Cursor assignedCursor = db.rawQuery("SELECT course_id FROM student_courses WHERE student_id=?",
                new String[]{String.valueOf(studentId)});
        while (assignedCursor.moveToNext()) {
            alreadyAssigned.add(assignedCursor.getInt(0));
        }
        assignedCursor.close();

        Cursor cursor = db.rawQuery("SELECT id, name FROM courses", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            if (!alreadyAssigned.contains(id)) {
                courseIds.add(id);
                courseNames.add(name);
            }
        }
        cursor.close();

        if (courseNames.isEmpty()) {
            Toast.makeText(context, "All courses already assigned", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] coursesArray = courseNames.toArray(new String[0]);

        new AlertDialog.Builder(context)
                .setTitle("Assign Course")
                .setItems(coursesArray, (dialog, which) -> {
                    int selectedCourseId = courseIds.get(which);
                    SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
                    writableDb.execSQL("INSERT INTO student_courses (student_id, course_id) VALUES (?, ?)",
                            new Object[]{studentId, selectedCourseId});
                    Toast.makeText(context, "Assigned to " + coursesArray[which], Toast.LENGTH_SHORT).show();
                    holder.textCourses.setText("Courses: " + getAssignedCoursesString(studentId));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveCourseDialog(int studentId, StudentViewHolder holder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> courseNames = new ArrayList<>();
        ArrayList<Integer> courseIds = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT c.id, c.name FROM courses c JOIN student_courses sc ON c.id = sc.course_id WHERE sc.student_id=?",
                new String[]{String.valueOf(studentId)});

        while (cursor.moveToNext()) {
            courseIds.add(cursor.getInt(0));
            courseNames.add(cursor.getString(1));
        }
        cursor.close();

        if (courseNames.isEmpty()) {
            Toast.makeText(context, "No courses to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] coursesArray = courseNames.toArray(new String[0]);

        new AlertDialog.Builder(context)
                .setTitle("Remove Course")
                .setItems(coursesArray, (dialog, which) -> {
                    int selectedCourseId = courseIds.get(which);
                    SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
                    writableDb.delete("student_courses", "student_id=? AND course_id=?",
                            new String[]{String.valueOf(studentId), String.valueOf(selectedCourseId)});
                    Toast.makeText(context, "Removed from " + coursesArray[which], Toast.LENGTH_SHORT).show();
                    holder.textCourses.setText("Courses: " + getAssignedCoursesString(studentId));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textEmail, textPassword, textCourses;
        Button buttonAssign, buttonReport, buttonDelete, buttonResetPassword, buttonEdit, buttonRemoveCourse;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textPassword = itemView.findViewById(R.id.textPassword);
            textCourses = itemView.findViewById(R.id.textCourses);
            buttonAssign = itemView.findViewById(R.id.buttonAssign);
            buttonReport = itemView.findViewById(R.id.buttonReport);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonResetPassword = itemView.findViewById(R.id.buttonResetPassword);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonRemoveCourse = itemView.findViewById(R.id.buttonRemoveCourse);
        }
    }
}
