package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateUserRequestDTO {
    @SerializedName("name")
    private String name;
    @SerializedName("username")
    private String username;
    @SerializedName("role")
    private String role;
    @SerializedName("password")
    private String password;

    public CreateUserRequestDTO() {
    }

    public CreateUserRequestDTO(String name, String username, String role, String password) {
        this.name = name;
        this.username = username;
        this.role = role;
        this.password = password;
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

    public String getPassword() {
        return password;
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

    public void setPassword(String password) {
        this.password = password;
    }
}
