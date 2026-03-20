package com.example.borrowhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.borrowhub.data.local.entity.StudentEntity;

import java.util.List;

/**
 * Data Access Object for Student entity.
 * Provides methods to interact with the students table in the local database.
 */
@Dao
public interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StudentEntity student);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StudentEntity> students);

    @Update
    void update(StudentEntity student);

    @Delete
    void delete(StudentEntity student);

    @Query("SELECT * FROM students ORDER BY name ASC")
    LiveData<List<StudentEntity>> getAllStudents();

    @Query("SELECT * FROM students ORDER BY name ASC")
    List<StudentEntity> getAllStudentsSync();

    @Query("SELECT * FROM students WHERE id = :studentId")
    LiveData<StudentEntity> getStudentById(long studentId);

    @Query("SELECT * FROM students WHERE student_number LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<StudentEntity>> searchStudents(String query);

    @Query("DELETE FROM students")
    void deleteAll();

    @Query("DELETE FROM students WHERE id = :studentId")
    void deleteById(long studentId);
}
