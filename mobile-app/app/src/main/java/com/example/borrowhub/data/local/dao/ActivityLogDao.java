package com.example.borrowhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.borrowhub.data.local.entity.ActivityLogEntity;

import java.util.List;

@Dao
public interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ActivityLogEntity> logs);

    @Query("SELECT * FROM activity_logs ORDER BY created_at DESC")
    LiveData<List<ActivityLogEntity>> getAllLogs();

    @Query("SELECT * FROM activity_logs WHERE action LIKE '%' || :action || '%' ORDER BY created_at DESC")
    LiveData<List<ActivityLogEntity>> getLogsByAction(String action);

    @Query("SELECT * FROM activity_logs WHERE target_user_id LIKE '%' || :targetUserId || '%' ORDER BY created_at DESC")
    LiveData<List<ActivityLogEntity>> getLogsByTargetUserId(String targetUserId);

    @Query("SELECT * FROM activity_logs WHERE performed_by LIKE '%' || :performedBy || '%' ORDER BY created_at DESC")
    LiveData<List<ActivityLogEntity>> getLogsByPerformedBy(String performedBy);

    @Query("DELETE FROM activity_logs")
    void deleteAll();
}
