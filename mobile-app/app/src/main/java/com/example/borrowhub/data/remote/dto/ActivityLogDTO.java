package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ActivityLogDTO {
    @SerializedName("id")
    private long id;

    @SerializedName("performed_by")
    private String performedBy;

    @SerializedName("target_user_id")
    private String targetUserId;

    @SerializedName("target_user_name")
    private String targetUserName;

    @SerializedName("target_type")
    private String targetType;

    @SerializedName("action")
    private String action;

    @SerializedName("details")
    private String details;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() {
        return id;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
