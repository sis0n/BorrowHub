package com.example.borrowhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room Entity for Student local storage.
 * Represents a student record in the local SQLite database.
 */
@Entity(tableName = "students")
public class StudentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "student_number")
    public String studentNumber;

    @NonNull
    public String name;

    @NonNull
    public String course;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "updated_at")
    public String updatedAt;

    public StudentEntity() {
        this.studentNumber = "";
        this.name = "";
        this.course = "";
        this.createdAt = "";
        this.updatedAt = "";
    }

    @Ignore
    public StudentEntity(long id, @NonNull String studentNumber, @NonNull String name, @NonNull String course) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.name = name;
        this.course = course;
        this.createdAt = "";
        this.updatedAt = "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(@NonNull String studentNumber) { this.studentNumber = studentNumber; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull
    public String getCourse() { return course; }
    public void setCourse(@NonNull String course) { this.course = course; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
