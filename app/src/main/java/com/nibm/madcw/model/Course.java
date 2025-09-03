package com.nibm.madcw.model;

import androidx.annotation.NonNull;


public class Course {
    private int id;
    private String name;

    public Course(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}

