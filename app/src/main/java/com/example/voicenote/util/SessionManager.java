// File: com/example/voicenote/util/SessionManager.java
package com.example.voicenote.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý phiên đăng nhập của người dùng (lưu userId)
 */
public class SessionManager {
    private static final String PREF_NAME = "VoiceNotePrefs";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lưu ID người dùng khi đăng nhập thành công
     */
    public void saveSession(long userId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Lấy ID người dùng đang đăng nhập
     * @return userId, hoặc -1 nếu chưa đăng nhập
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Xoá phiên đăng nhập (dùng cho Logout)
     */
    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();
    }
}