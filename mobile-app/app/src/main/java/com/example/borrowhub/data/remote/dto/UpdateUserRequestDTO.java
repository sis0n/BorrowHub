package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequestDTO {
    @SerializedName("name")
    private String name;
    @SerializedName("username")
    private String username;
    @SerializedName("role")
    private String role;

    public UpdateUserRequestDTO() {
    }

    public UpdateUserRequestDTO(String name, String username, String role) {
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
