// File: com/example/voicenote/ui/more/ProfileActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
    private ProfileViewModel viewModel;
    private SessionManager sessionManager;
    private UserEntity currentUser;

    private EditText edtFullName, edtUsername, edtPhone, edtEmail;
    private EditText edtOldPassword, edtNewPassword;
    private TextView btnSave;
    private ImageView imgAvatar;
    private RadioGroup rgGender;
    private long userId;

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

        observeViewModel();
        loadCurrentUserData();
    }

    private void findViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);
        rgGender = findViewById(R.id.rgGender);
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
                //Điền dữ liệu
                currentUser = user; // Lưu lại user hiện tại
                edtFullName.setText(user.fullName);
                edtUsername.setText(user.username);
                edtPhone.setText(user.phone);
                edtEmail.setText(user.email);

                // (Tải ảnh - Dùng Glide)
                // if (user.imageUrl != null) {
                //    Glide.with(this).load(user.imageUrl).into(imgAvatar);
                // }

                // Set Gender
                if ("Nữ".equals(user.gender)) {
                    rgGender.check(R.id.radioFemale);
                } else if ("Khác".equals(user.gender)) {
                    rgGender.check(R.id.radioOther);
                } else {
                    rgGender.check(R.id.radioMale);
                }

                // PHÂN QUYỀN
                if ("OWNER".equals(user.role)) {
                    // Chủ quán được sửa
                    edtFullName.setEnabled(true);
                    setRadioGroupEnabled(rgGender, true);
                    // (Thêm code cho phép upload ảnh)
                } else {
                    // Nhân viên không được sửa
                    edtFullName.setEnabled(false);
                    setRadioGroupEnabled(rgGender, false);
                    // (Thêm code ẩn nút upload ảnh)
                }
            }
        });
    }

    // Hàm helper để disable RadioGroup
    private void setRadioGroupEnabled(RadioGroup radioGroup, boolean enabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(enabled);
        }
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

        // [SỬA] Chỉ cập nhật các trường được phép

        // 1. Cập nhật thông tin (Chỉ OWNER mới cập nhật Tên/Giới tính)
        if ("OWNER".equals(currentUser.role)) {
            currentUser.fullName = edtFullName.getText().toString().trim();
            int checkedId = rgGender.getCheckedRadioButtonId();
            if (checkedId == R.id.radioFemale) currentUser.gender = "Nữ";
            else if (checkedId == R.id.radioOther) currentUser.gender = "Khác";
            else currentUser.gender = "Nam";
        }

        // Cả hai đều được cập nhật SĐT/Email
        currentUser.phone = edtPhone.getText().toString().trim();
        currentUser.email = edtEmail.getText().toString().trim();

        viewModel.updateProfile(currentUser);

        // 2. Đổi mật khẩu (Cả hai đều được)
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        if (!oldPass.isEmpty() || !newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                edtOldPassword.setError("Cần nhập mật khẩu cũ"); return;
            }
            if (newPass.length() < 6) {
                edtNewPassword.setError("Mật khẩu mới ít nhất 6 ký tự"); return;
            }
            viewModel.changePassword(userId, oldPass, newPass);
        }
        Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
        // Không finish() ngay, đợi kết quả từ observeViewModel
    }
}