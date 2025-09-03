package com.nibm.madcw.model;

public class Message {
    public int id;
    public int courseId;
    public int senderId;
    public int receiverId;
    public String message;
    public String timestamp;

    public Message(int id, int courseId, int senderId, int receiverId, String message, String timestamp) {
        this.id = id;
        this.courseId = courseId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }
}

