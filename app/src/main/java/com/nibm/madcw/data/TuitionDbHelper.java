package com.nibm.madcw.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nibm.madcw.model.Course;
import com.nibm.madcw.model.Student;

import java.util.ArrayList;
import java.util.List;

public class TuitionDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tuition.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    public TuitionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Users table
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL CHECK(role IN ('admin', 'teacher', 'student')))");

        // Courses table
        db.execSQL("CREATE TABLE courses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "teacher_id INTEGER, " +
                "FOREIGN KEY(teacher_id) REFERENCES users(id) ON DELETE SET NULL)");

        // Student-Course assignment table
        db.execSQL("CREATE TABLE student_courses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " +
                "course_id INTEGER NOT NULL, " +
                "FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        // Attendance table
        db.execSQL("CREATE TABLE attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " +
                "course_id INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "status TEXT DEFAULT 'present', " +
                "FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        // Assignments table
        db.execSQL("CREATE TABLE assignments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "due_date TEXT, " +
                "file_path TEXT, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        // Submissions table
        db.execSQL("CREATE TABLE submissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "assignment_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "file_path TEXT, " +
                "submitted_at TEXT, " +
                "marks INTEGER, " +
                "is_released INTEGER DEFAULT 0, " +  // <-- Add this line
                "FOREIGN KEY(assignment_id) REFERENCES assignments(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE)");

        // Materials table
        db.execSQL("CREATE TABLE materials (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER NOT NULL, " +
                "title TEXT, " +
                "file_path TEXT, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        // Notifications table
        db.execSQL("CREATE TABLE notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id INTEGER NOT NULL, " +
                "receiver_id INTEGER, " +
                "course_id INTEGER NOT NULL, " +
                "message TEXT NOT NULL, " +
                "timestamp TEXT, " +
                "read_status INTEGER DEFAULT 0, " +
                "FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE course_teacher (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "teacher_id INTEGER NOT NULL," +
                "course_id INTEGER NOT NULL," +
                "FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE," +
                "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                ")");


        db.execSQL("CREATE TABLE IF NOT EXISTS teacher_courses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "teacher_id INTEGER," +
                "course_id INTEGER)");

        // Results table
        db.execSQL("CREATE TABLE results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " +
                "course_id INTEGER NOT NULL, " +
                "marks INTEGER, " +
                "is_released INTEGER DEFAULT 0, " +  // 0 = Not released, 1 = Released
                "FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");

        db.execSQL("INSERT INTO users (username, name, email, password, role) VALUES (" +
                "'admin', 'Admin', 'admin@gmail.com', 'admin123', 'admin')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS results");
        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS materials");
        db.execSQL("DROP TABLE IF EXISTS submissions");
        db.execSQL("DROP TABLE IF EXISTS assignments");
        db.execSQL("DROP TABLE IF EXISTS attendance");
        db.execSQL("DROP TABLE IF EXISTS student_courses");
        db.execSQL("DROP TABLE IF EXISTS courses");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
    // Get courses by teacher using linking table course_teacher
    public List<Course> getCoursesByTeacher(int teacherId) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, name FROM courses WHERE id IN (SELECT course_id FROM teacher_courses WHERE teacher_id = ?)",
                new String[]{String.valueOf(teacherId)}
        );

        if (cursor.moveToFirst()) {
            do {
                courses.add(new Course(cursor.getInt(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courses;
    }

    // Get students by course
    public List<Student> getStudentsByCourse(int courseId) {
        List<Student> students = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT u.id, u.name, u.email FROM users u " +
                        "INNER JOIN student_courses sc ON u.id = sc.student_id " +
                        "WHERE sc.course_id = ? AND u.role = 'student'",
                new String[]{String.valueOf(courseId)}
        );

        if (cursor.moveToFirst()) {
            do {
                students.add(new Student(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return students;
    }
    public List<String> getNotificationsForStudent(int studentId) {
        List<String> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT message, timestamp, read_status FROM notifications WHERE receiver_id = ? ORDER BY timestamp DESC",
                new String[]{String.valueOf(studentId)}
        );

        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(0);
                String timestamp = cursor.getString(1);
                int readStatus = cursor.getInt(2);
                String status = readStatus == 0 ? "[UNREAD] " : "";
                messages.add(status + message + "\n🕒 " + timestamp);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return messages;
    }

    public void markMessagesAsRead(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE notifications SET read_status = 1 WHERE receiver_id = ? AND read_status = 0",
                new Object[]{studentId});
    }



}
