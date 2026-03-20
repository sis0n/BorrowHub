package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class StudentDTO {

    @SerializedName("id")
    private long id;

    @SerializedName("student_number")
    private String studentNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("course")
    private String course;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public StudentDTO() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
