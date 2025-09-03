package com.nibm.madcw.ui.student;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;

import java.util.ArrayList;
import java.util.List;

public class ViewMessageActivity extends AppCompatActivity {

    ListView listViewMessages;
    TuitionDbHelper dbHelper;
    int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);

        listViewMessages = findViewById(R.id.listViewMessages);
        dbHelper = new TuitionDbHelper(this);

        // Get student ID from session
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        if (studentId == -1) {
            Toast.makeText(this, "Student session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessages(studentId);
        markMessagesAsRead(studentId); // Mark unread messages as read after loading
    }

    private void loadMessages(int studentId) {
        List<String> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT message, timestamp FROM notifications " +
                        "WHERE receiver_id = ? ORDER BY timestamp DESC",
                new String[]{String.valueOf(studentId)}
        );

        while (cursor.moveToNext()) {
            String msg = cursor.getString(0);
            String time = cursor.getString(1);
            messages.add("📩 " + msg + "\n🕒 " + time);
        }

        cursor.close();

        if (messages.isEmpty()) {
            messages.add("No messages found.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, messages);

        listViewMessages.setAdapter(adapter);
    }

    private void markMessagesAsRead(int studentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE notifications SET read_status = 1 WHERE receiver_id = ? AND read_status = 0",
                new Object[]{studentId});
    }
}

