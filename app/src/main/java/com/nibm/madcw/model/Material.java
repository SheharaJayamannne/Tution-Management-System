package com.nibm.madcw.model;

public class Material {
    private int id;
    private String title;
    private String filePath;

    public Material(int id, String title, String filePath) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    // Add this setter to fix the error
    public void setTitle(String title) {
        this.title = title;
    }
}


