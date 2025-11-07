// File: com/example/voicenote/ui/splash/SplashActivity.java
package com.example.voicenote.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voicenote.MainActivity;
import com.example.voicenote.R;
import com.example.voicenote.ui.auth.LoginActivity;
import com.example.voicenote.util.SessionManager;

/**
 * Màn hình LAUNCHER mới.
 * Quyết định đi đến Login hay Main.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();

        // Thêm một chút delay (ví dụ: 1 giây) để màn hình chờ
        // có ý nghĩa, thay vì "nháy" một cái rồi tắt
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (userId != -1) {
                // ĐÃ ĐĂNG NHẬP: Vào thẳng MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // CHƯA ĐĂNG NHẬP: Vào LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            // Đóng SplashActivity
            finish();

        }, 500); // 500 mili-giây
    }
}