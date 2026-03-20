package com.example.borrowhub.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room Entity for student Course local storage.
 * Represents an available course option in the local SQLite database.
 */
@Entity(tableName = "student_courses")
public class CourseEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    public CourseEntity() {}

    @Ignore
    public CourseEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Ignore
    public CourseEntity(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
