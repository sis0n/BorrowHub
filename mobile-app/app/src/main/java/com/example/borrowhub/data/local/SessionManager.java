package com.example.borrowhub.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SessionManager {
    private static final String PREF_NAME = "BorrowHubPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_USER_NAME = "user_name";
    private static final int DEFAULT_THEME_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void clearSession() {
        prefs.edit().remove(KEY_AUTH_TOKEN).remove(KEY_USER_NAME).apply();
    }

    public void saveUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, DEFAULT_THEME_MODE);
    }
}
