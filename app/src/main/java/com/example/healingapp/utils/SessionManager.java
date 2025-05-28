package com.example.healingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class SessionManager {

    private static final String TAG = "SessionManager";
    // Đổi tên PREF_NAME để phân biệt với phiên bản Encrypted nếu bạn đã từng chạy
    private static final String PREF_NAME = "UserSession_Plain";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_CONFIG_PROFILE = "isConfigProfile";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        Context appContext = context.getApplicationContext();
        // Sử dụng SharedPreferences thông thường
        this.sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = this.sharedPreferences.edit();
        Log.i(TAG, "Đang sử dụng SharedPreferences thông thường (không mã hóa).");
    }

    public void createLoginSession(String token, String userId, String username, boolean isConfigProfile) {
        if (editor == null) {
            Log.e(TAG, "Editor chưa được khởi tạo, không thể lưu session.");
            return;
        }
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_CONFIG_PROFILE, isConfigProfile);
        editor.apply();
        Log.d(TAG, "Phiên đăng nhập đã được tạo (không mã hóa). Token: " + token);
    }

    public String getAuthToken() {
        if (sharedPreferences == null) return null;
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public String getUserId() {
        if (sharedPreferences == null) return null;
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        if (sharedPreferences == null) return null;
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    public boolean isConfigProfile() {
        if (sharedPreferences == null) return false;
        return sharedPreferences.getBoolean(KEY_IS_CONFIG_PROFILE, false);
    }
    public boolean isLoggedIn() {
        if (sharedPreferences == null) return false;
        // Kiểm tra cả cờ isLoggedIn và sự tồn tại của token
        boolean loggedInFlag = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = getAuthToken(); // getAuthToken() trả về null nếu không có token
        return loggedInFlag && (token != null && !token.isEmpty());
    }
    public void setConfigProfileStatus(boolean isConfigured) {
        if (editor == null) {
            Log.e(TAG, "Editor chưa được khởi tạo, không thể lưu trạng thái config profile.");
            return;
        }
        editor.putBoolean(KEY_IS_CONFIG_PROFILE, isConfigured);
        editor.apply();
        Log.d(TAG, "Trạng thái config profile đã được cập nhật: " + isConfigured);
    }
    public void clearSession() {
        if (editor == null) {
            Log.e(TAG, "Editor chưa được khởi tạo, không thể xóa session.");
            return;
        }
        editor.clear();
        editor.apply();
        Log.d(TAG, "Phiên đăng nhập đã được xóa (không mã hóa).");
    }
}
