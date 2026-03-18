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

@Database(entities = {ExampleEntity.class, User.class, DashboardStatsEntity.class, RecentTransactionEntity.class, ItemEntity.class, CategoryEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ExampleDao exampleDao();
    public abstract UserDao userDao();
    public abstract DashboardStatsDao dashboardStatsDao();
    public abstract RecentTransactionDao recentTransactionDao();
    public abstract ItemDao itemDao();
    public abstract CategoryDao categoryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "borrowhub_database"
                    ).build();
                }
            }
        }
        return instance;
    }
}
