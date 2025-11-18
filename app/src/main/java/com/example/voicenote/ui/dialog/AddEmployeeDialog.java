// File: com/example/voicenote/ui/dialog/AddEmployeeDialog.java
package com.example.voicenote.ui.dialog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

public class AddEmployeeDialog extends DialogFragment {

    public interface OnSaveListener {
        void onSave(UserEntity user, String password);
    }

    private UserEntity existingUser; // null nếu là thêm mới
    private OnSaveListener onSaveListener;
    private EditText edtFullName, edtUsername, edtPassword, edtEmail, edtPhone;
    private TextInputLayout tilPassword;
    private TextView tvDialogTitle;
    private SwitchMaterial switchActive;
    private RadioGroup rgGender;
    private ImageView imgAvatar;
    private TextView tvInitial;
    private String newImageUriString = null;

    // Trình khởi chạy (Launcher) để chọn ảnh
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImageSelection(uri);
                }
            });

    // Trình khởi chạy (Launcher) để xin quyền
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imagePickerLauncher.launch("image/*");
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    public static AddEmployeeDialog newInstance(UserEntity user) {
        AddEmployeeDialog dialog = new AddEmployeeDialog();
        Bundle args = new Bundle();
        if (user != null) {
            args.putString("fullName", user.fullName);
            args.putString("username", user.username);
            args.putLong("id", user.id);
            args.putBoolean("isActive", user.isActive);
            args.putString("email", user.email);
            args.putString("phone", user.phone);
            args.putString("gender", user.gender);
            args.putString("role", user.role);
            args.putString("passwordHash", user.passwordHash);
            args.putString("passwordSalt", user.passwordSalt);
            args.putLong("createdAt", user.createdAt);
            args.putString("imageUrl", user.imageUrl);
            // Không truyền mật khẩu
        }
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("id")) {
            existingUser = new UserEntity();
            existingUser.id = getArguments().getLong("id");
            existingUser.fullName = getArguments().getString("fullName");
            existingUser.username = getArguments().getString("username");
            existingUser.isActive = getArguments().getBoolean("isActive");
            existingUser.email = getArguments().getString("email");
            existingUser.phone = getArguments().getString("phone");
            existingUser.gender = getArguments().getString("gender");
            existingUser.role = getArguments().getString("role");
            existingUser.passwordHash = getArguments().getString("passwordHash");
            existingUser.passwordSalt = getArguments().getString("passwordSalt");
            existingUser.createdAt = getArguments().getLong("createdAt");
            existingUser.imageUrl = getArguments().getString("imageUrl");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();
            w.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_employee, container, false);

        tvDialogTitle = v.findViewById(R.id.tvDialogTitle);
        tilPassword = v.findViewById(R.id.tilPassword);
        edtFullName = v.findViewById(R.id.edtFullName);
        edtUsername = v.findViewById(R.id.edtUsername);
        edtPassword = v.findViewById(R.id.edtPassword);
        edtEmail = v.findViewById(R.id.edtEmail);
        edtPhone = v.findViewById(R.id.edtPhone);
        rgGender = v.findViewById(R.id.rgGender);
        switchActive = v.findViewById(R.id.switchActive);
        imgAvatar = v.findViewById(R.id.imgAvatar);
        tvInitial = v.findViewById(R.id.tvInitial);
        TextView btnCancel = v.findViewById(R.id.btnCancel);
        TextView btnSave = v.findViewById(R.id.btnSave);

        if (existingUser != null) {
            // Chế độ Sửa
            tvDialogTitle.setText("Sửa thông tin nhân viên");
            edtFullName.setText(existingUser.fullName);
            edtUsername.setText(existingUser.username);
            edtUsername.setEnabled(false); // Không cho sửa tên đăng nhập
            tilPassword.setHint("Mật khẩu mới");
            switchActive.setChecked(existingUser.isActive);
            edtEmail.setText(existingUser.email);
            edtPhone.setText(existingUser.phone);

           // Tải ảnh (hoặc chữ cái)
            loadAvatar(existingUser.fullName, existingUser.imageUrl, existingUser.gender);

            // Set Gender
            if ("Nữ".equals(existingUser.gender)) {
                rgGender.check(R.id.radioFemale);
            } else {
                rgGender.check(R.id.radioMale); // Mặc định là Nam
            }
        } else {
            // Chế độ Thêm mới
            tvDialogTitle.setText("Thêm nhân viên mới");
            switchActive.setChecked(true); // Mặc định là kích hoạt
            rgGender.check(R.id.radioMale);
            loadAvatar(null, null, "Nam"); // Hiển thị chữ cái đầu mặc định
        }

        btnCancel.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> save());
        imgAvatar.setOnClickListener(view -> checkPermissionAndPickImage());
        tvInitial.setOnClickListener(view -> checkPermissionAndPickImage());

        return v;
    }

    /**
     * [MỚI] Tải ảnh/chữ cái vào Avatar
     */
    private void loadAvatar(String name, String imageUrl, String gender) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Có ảnh -> Dùng Glide
            tvInitial.setVisibility(View.GONE);
            imgAvatar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(Uri.parse(imageUrl))
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            // Không có ảnh -> Dùng Chữ cái đầu
            imgAvatar.setVisibility(View.GONE);
            tvInitial.setVisibility(View.VISIBLE);
            tvInitial.setText(makeInitial(name));

            int bgColorRes = "Nữ".equals(gender) ? R.color.pastel_pink : R.color.pastel_blue;
            tvInitial.getBackground().setTint(ContextCompat.getColor(getContext(), bgColorRes));
        }
    }

    /**
     * [MỚI] Hàm tạo chữ cái đầu
     */
    private String makeInitial(String name){
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) return "?";
        if (parts.length == 1) return parts[0].substring(0,1).toUpperCase();
        return (parts[parts.length-2].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
    }

    /**
     * [MỚI] Kiểm tra quyền
     */
    private void checkPermissionAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            imagePickerLauncher.launch("image/*");
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * [MỚI] Xử lý ảnh
     */
    private void handleImageSelection(Uri uri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

            this.newImageUriString = uri.toString();

            // Hiển thị ảnh đã chọn
            tvInitial.setVisibility(View.GONE);
            imgAvatar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(imgAvatar);

        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Không thể truy cập ảnh này", Toast.LENGTH_SHORT).show();
        }
    }

    private void save() {
        String fullName = edtFullName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        // Lấy dữ liệu mới
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String gender = "Nam"; // Mặc định
        int checkedGenderId = rgGender.getCheckedRadioButtonId();
        if (checkedGenderId == R.id.radioFemale) {
            gender = "Nữ";
        }

        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được trống"); return;
        }
        if (username.isEmpty()) {
            edtUsername.setError("Tên đăng nhập không được trống"); return;
        }

        if (existingUser == null) {
            // Thêm mới
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu cần ít nhất 6 ký tự"); return;
            }
            existingUser = new UserEntity(); // Tạo mới
            existingUser.username = username;
        }
        // Gán dữ liệu
        existingUser.fullName = fullName;
        existingUser.isActive = isActive;
        existingUser.email = email;
        existingUser.phone = phone;
        existingUser.gender = gender;

        // Gán ảnh nếu có
        if (newImageUriString != null) {
            existingUser.imageUrl = newImageUriString;
        }

        if (onSaveListener != null) {
            onSaveListener.onSave(existingUser, password);
        }
        dismiss();
    }
}