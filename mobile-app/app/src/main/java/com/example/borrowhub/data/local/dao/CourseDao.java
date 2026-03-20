package com.example.borrowhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.borrowhub.data.local.entity.CourseEntity;

import java.util.List;

/**
 * Data Access Object for student CourseEntity.
 * Provides methods to interact with the student_courses table in the local database.
 */
@Dao
public interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CourseEntity course);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CourseEntity> courses);

    @Update
    void update(CourseEntity course);

    @Delete
    void delete(CourseEntity course);

    @Query("SELECT * FROM student_courses ORDER BY name ASC")
    LiveData<List<CourseEntity>> getAllCourses();

    @Query("SELECT * FROM student_courses ORDER BY name ASC")
    List<CourseEntity> getAllCoursesSync();

    @Query("SELECT * FROM student_courses WHERE id = :courseId")
    LiveData<CourseEntity> getCourseById(int courseId);

    @Query("DELETE FROM student_courses")
    void deleteAll();

    @Query("DELETE FROM student_courses WHERE id IN (:courseIds)")
    void deleteByIds(List<Integer> courseIds);
}
