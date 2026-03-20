package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateStudentRequestDTO {

    @SerializedName("student_number")
    private String studentNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("course")
    private String course;

    public CreateStudentRequestDTO() {}

    public CreateStudentRequestDTO(String studentNumber, String name, String course) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.course = course;
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
}
