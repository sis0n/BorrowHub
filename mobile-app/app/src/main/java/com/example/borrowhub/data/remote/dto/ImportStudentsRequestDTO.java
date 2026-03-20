package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImportStudentsRequestDTO {

    @SerializedName("students")
    private List<CreateStudentRequestDTO> students;

    public ImportStudentsRequestDTO() {}

    public ImportStudentsRequestDTO(List<CreateStudentRequestDTO> students) {
        this.students = students;
    }

    public List<CreateStudentRequestDTO> getStudents() { return students; }
    public void setStudents(List<CreateStudentRequestDTO> students) { this.students = students; }
}
