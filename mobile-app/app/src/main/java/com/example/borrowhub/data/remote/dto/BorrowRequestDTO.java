package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO for a borrow transaction request.
 */
public class BorrowRequestDTO {

    @SerializedName("student_id")
    private Long studentId;

    @SerializedName("student_number")
    private String studentNumber;

    @SerializedName("collateral")
    private String collateral;

    @SerializedName("items")
    private List<BorrowItemRequestDTO> items;

    public BorrowRequestDTO(Long studentId, String studentNumber, String collateral, List<BorrowItemRequestDTO> items) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.collateral = collateral;
        this.items = items;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getCollateral() {
        return collateral;
    }

    public void setCollateral(String collateral) {
        this.collateral = collateral;
    }

    public List<BorrowItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<BorrowItemRequestDTO> items) {
        this.items = items;
    }
}
