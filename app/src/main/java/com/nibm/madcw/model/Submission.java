package com.nibm.madcw.model;

public class Submission {
    public int id;
    public String studentName;
    public String assignmentTitle;
    public int marks;

    public Submission(int id, String studentName, String assignmentTitle, int marks) {
        this.id = id;
        this.studentName = studentName;
        this.assignmentTitle = assignmentTitle;
        this.marks = marks;
    }

    public int getId() { return id; }
    public String getStudentName() { return studentName; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public int getMarks() { return marks; }
}
