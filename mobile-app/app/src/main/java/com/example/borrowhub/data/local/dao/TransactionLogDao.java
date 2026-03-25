package com.example.borrowhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.borrowhub.data.local.entity.TransactionLogEntity;

import java.util.List;

@Dao
public interface TransactionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TransactionLogEntity> logs);

    @Query("SELECT * FROM transaction_logs ORDER BY created_at DESC")
    LiveData<List<TransactionLogEntity>> getAllLogs();

    @Query("SELECT * FROM transaction_logs WHERE action LIKE '%' || :action || '%' ORDER BY created_at DESC")
    LiveData<List<TransactionLogEntity>> getLogsByAction(String action);

    @Query("SELECT * FROM transaction_logs WHERE target_user_id LIKE '%' || :targetUserId || '%' ORDER BY created_at DESC")
    LiveData<List<TransactionLogEntity>> getLogsByTargetUserId(String targetUserId);

    @Query("SELECT * FROM transaction_logs WHERE performed_by LIKE '%' || :performedBy || '%' ORDER BY created_at DESC")
    LiveData<List<TransactionLogEntity>> getLogsByPerformedBy(String performedBy);

    @Query("DELETE FROM transaction_logs")
    void deleteAll();
}
