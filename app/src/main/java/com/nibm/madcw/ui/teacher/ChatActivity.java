package com.nibm.madcw.ui.teacher;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Message;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private TuitionDbHelper dbHelper;
    private int senderId;    // Teacher user ID (sender)
    private int receiverId;  // Student user ID (receiver)
    private int courseId;
    TextView textViewChatHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        textViewChatHeader = findViewById(R.id.textViewChatHeader);

        // Get IDs passed via Intent
        senderId = getIntent().getIntExtra("senderId", -1);
        receiverId = getIntent().getIntExtra("receiverId", -1);
        courseId = getIntent().getIntExtra("courseId", -1);

        if (senderId == -1 || receiverId == -1 || courseId == -1) {
            Toast.makeText(this, "Missing chat parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new TuitionDbHelper(this);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, senderId);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(chatAdapter);

        loadMessages();
        loadStudentName(receiverId);

        buttonSend.setOnClickListener(v -> {
            String messageText = editTextMessage.getText().toString().trim();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(messageText);
        });
    }

    private void loadStudentName(int receiverId) {
        if (receiverId == -1) {
            textViewChatHeader.setText("Send Messages to ");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE id = ?", new String[]{String.valueOf(receiverId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            textViewChatHeader.setText("Send Messages to " + name);
        }
        cursor.close();
    }

    private void sendMessage(String messageText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        db.execSQL("INSERT INTO notifications (sender_id, receiver_id, course_id, message, timestamp) VALUES (?, ?, ?, ?, ?)",
                new Object[]{senderId, receiverId, courseId, messageText, timestamp});

        editTextMessage.setText("");
        loadMessages();
    }

    private void loadMessages() {
        messageList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT id, sender_id, receiver_id, course_id, message, timestamp FROM notifications " +
                "WHERE course_id = ? AND " +
                "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                "ORDER BY timestamp ASC";

        Cursor cursor = db.rawQuery(query,
                new String[]{
                        String.valueOf(courseId),
                        String.valueOf(senderId),
                        String.valueOf(receiverId),
                        String.valueOf(receiverId),
                        String.valueOf(senderId)
                });

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            int sender = cursor.getInt(1);
            int receiver = cursor.getInt(2);
            int course = cursor.getInt(3);
            String message = cursor.getString(4);
            String timestamp = cursor.getString(5);

            Message msg = new Message(id, course, sender, receiver, message, timestamp);
            messageList.add(msg);
        }
        cursor.close();

        chatAdapter.notifyDataSetChanged();

        if (!messageList.isEmpty()) {
            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        }
    }
}
