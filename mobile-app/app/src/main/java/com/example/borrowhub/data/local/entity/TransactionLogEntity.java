package com.example.borrowhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_logs")
public class TransactionLogEntity {
    @PrimaryKey(autoGenerate = false)
    public long id;

    @NonNull
    @ColumnInfo(name = "performed_by")
    public String performedBy;

    @NonNull
    @ColumnInfo(name = "target_user_id")
    public String targetUserId;

    @NonNull
    @ColumnInfo(name = "target_user_name")
    public String targetUserName;

    @NonNull
    @ColumnInfo(name = "target_type")
    public String targetType;

    @NonNull
    public String action;

    @NonNull
    public String details;

    @ColumnInfo(name = "created_at")
    @NonNull
    public String createdAt;

    public TransactionLogEntity() {
        performedBy = "";
        targetUserId = "";
        targetUserName = "";
        targetType = "";
        action = "";
        details = "";
        createdAt = "";
    }

    @Ignore
    public TransactionLogEntity(long id, @NonNull String performedBy, @NonNull String targetUserId,
                                @NonNull String targetUserName, @NonNull String targetType,
                                @NonNull String action, @NonNull String details, @NonNull String createdAt) {
        this.id = id;
        this.performedBy = performedBy;
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.targetType = targetType;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

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
