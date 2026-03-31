package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateStudentRequestDTO {

    @SerializedName("student_number")
    private String studentNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("course_id")
    private int courseId;

    public CreateStudentRequestDTO() {}

    public CreateStudentRequestDTO(String studentNumber, String name, int courseId) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.courseId = courseId;
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
}
