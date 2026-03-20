package com.example.borrowhub.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.borrowhub.data.local.dao.ExampleDao;
import com.example.borrowhub.data.local.entity.ExampleEntity;
import com.example.borrowhub.data.local.dao.UserDao;
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.data.local.dao.DashboardStatsDao;
import com.example.borrowhub.data.local.entity.DashboardStatsEntity;
import com.example.borrowhub.data.local.dao.RecentTransactionDao;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;
import com.example.borrowhub.data.local.dao.ItemDao;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.data.local.dao.CategoryDao;
import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.local.dao.StudentDao;
import com.example.borrowhub.data.local.entity.StudentEntity;
import com.example.borrowhub.data.local.dao.CourseDao;
import com.example.borrowhub.data.local.entity.CourseEntity;
import com.example.borrowhub.data.local.dao.ActivityLogDao;
import com.example.borrowhub.data.local.dao.TransactionLogDao;
import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;

@Database(entities = {ExampleEntity.class, User.class, DashboardStatsEntity.class, RecentTransactionEntity.class, ItemEntity.class, CategoryEntity.class, StudentEntity.class, CourseEntity.class, ActivityLogEntity.class, TransactionLogEntity.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ExampleDao exampleDao();
    public abstract UserDao userDao();
    public abstract DashboardStatsDao dashboardStatsDao();
    public abstract RecentTransactionDao recentTransactionDao();
    public abstract ItemDao itemDao();
    public abstract CategoryDao categoryDao();
    public abstract StudentDao studentDao();
    public abstract CourseDao courseDao();
    public abstract ActivityLogDao activityLogDao();
    public abstract TransactionLogDao transactionLogDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "borrowhub_database"
                    )
                    // Intentionally using destructive migrations for now to keep local cache schema
                    // changes simple during active development.
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}
