// File: com/example/voicenote/ui/auth/LoginActivity.java
package com.example.voicenote.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.MainActivity;
import com.example.voicenote.R;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.AuthViewModel; // [SỬA]

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText edtUsername;
    private EditText edtPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        TextView btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> login());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // [SỬA] Lắng nghe sự kiện điều hướng mới
        authViewModel.getLoginNavigationEvent().observe(this, event -> {
            if (event == null) return;

            // LƯU PHIÊN ĐĂNG NHẬP
            sessionManager.saveSession(event.userId);

            if (event.destination == AuthViewModel.Destination.MAIN_ACTIVITY) {
                // Đã có cửa hàng (hoặc là Nhân viên) -> Vào Main
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finishAffinity(); // Đóng tất cả activity (Login, Register...)

            } else if (event.destination == AuthViewModel.Destination.CREATE_STORE) {
                // Là Owner nhưng CHƯA có cửa hàng -> Vào Tạo Cửa Hàng
                Intent intent = new Intent(LoginActivity.this, CreateStoreActivity.class);
                // Gửi Owner ID sang
                intent.putExtra(CreateStoreActivity.EXTRA_OWNER_ID, event.userId);
                startActivity(intent);
                finish(); // Chỉ đóng Login, CreateStore sẽ tự đóng sau
            }
        });

        // [MỚI] Lắng nghe lỗi đăng nhập
        authViewModel.getLoginError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty()) {
            edtUsername.setError("Tên đăng nhập không được trống");
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Mật khẩu không được trống");
            edtPassword.requestFocus();
            return;
        }

        authViewModel.login(username, password);
    }
}