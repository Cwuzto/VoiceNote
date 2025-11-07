// File: com/example/voicenote/ui/more/ProfileActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {
    private ProfileViewModel viewModel; // [SỬA]
    private SessionManager sessionManager; // [MỚI]
    private UserEntity currentUser; // [MỚI]

    private EditText edtFullName, edtUsername, edtPhone, edtEmail;
    private EditText edtOldPassword, edtNewPassword;
    private TextView btnSave;
    private long userId; // [MỚI]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        findViews();

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        observeViewModel(); // [MỚI]
        loadCurrentUserData(); // [SỬA]
    }

    private void findViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadCurrentUserData() {
        if (userId == -1) {
            Toast.makeText(this, "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // [SỬA] Lấy dữ liệu từ ViewModel
        viewModel.getUser(userId).observe(this, user -> {
            if (user != null) {
                currentUser = user; // Lưu lại user hiện tại
                edtFullName.setText(user.fullName);
                edtUsername.setText(user.username);
                edtPhone.setText(user.phone);
                edtEmail.setText(user.email);
            }
        });
    }

    // Lắng nghe kết quả từ ViewModel
    private void observeViewModel() {
        viewModel.getUpdateResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getPasswordChangeResult().observe(this, message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (message.equals("Đổi mật khẩu thành công")) {
                edtOldPassword.setText("");
                edtNewPassword.setText("");
            } else {
                edtOldPassword.requestFocus();
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được trống");
            return;
        }

        // 1. Cập nhật thông tin
        currentUser.fullName = fullName;
        currentUser.phone = phone;
        currentUser.email = email;
        viewModel.updateProfile(currentUser);

        // 2. Đổi mật khẩu (nếu có)
        if (!oldPass.isEmpty() || !newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                edtOldPassword.setError("Cần nhập mật khẩu cũ"); return;
            }
            if (newPass.length() < 6) {
                edtNewPassword.setError("Mật khẩu mới ít nhất 6 ký tự"); return;
            }
            viewModel.changePassword(userId, oldPass, newPass);
        }

        // Không finish() ngay, đợi kết quả từ observeViewModel
    }
}