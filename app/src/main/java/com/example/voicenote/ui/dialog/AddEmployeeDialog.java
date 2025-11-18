// File: com/example/voicenote/ui/dialog/AddEmployeeDialog.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AddEmployeeDialog extends DialogFragment {

    public interface OnSaveListener {
        void onSave(UserEntity user, String password);
    }

    private UserEntity existingUser; // null nếu là thêm mới
    private OnSaveListener onSaveListener;

    private EditText edtFullName, edtUsername, edtPassword, edtEmail, edtPhone;
    private TextView tvDialogTitle, tvPasswordLabel;
    private SwitchMaterial switchActive;
    private RadioGroup rgGender;

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
        tvPasswordLabel = v.findViewById(R.id.tvPasswordLabel);
        edtFullName = v.findViewById(R.id.edtFullName);
        edtUsername = v.findViewById(R.id.edtUsername);
        edtPassword = v.findViewById(R.id.edtPassword);
        edtEmail = v.findViewById(R.id.edtEmail);
        edtPhone = v.findViewById(R.id.edtPhone);
        rgGender = v.findViewById(R.id.rgGender);
        switchActive = v.findViewById(R.id.switchActive);
        TextView btnCancel = v.findViewById(R.id.btnCancel);
        TextView btnSave = v.findViewById(R.id.btnSave);

        if (existingUser != null) {
            // Chế độ Sửa
            tvDialogTitle.setText("Sửa thông tin nhân viên");
            edtFullName.setText(existingUser.fullName);
            edtUsername.setText(existingUser.username);
            edtUsername.setEnabled(false); // Không cho sửa tên đăng nhập
            tvPasswordLabel.setText("Mật khẩu mới (Bỏ trống nếu không đổi)");
            edtPassword.setHint("Nhập mật khẩu mới");
            switchActive.setChecked(existingUser.isActive);
            edtEmail.setText(existingUser.email);
            edtPhone.setText(existingUser.phone);

            // Set Gender
            if ("Nữ".equals(existingUser.gender)) {
                rgGender.check(R.id.radioFemale);
            } else if ("Khác".equals(existingUser.gender)) {
                rgGender.check(R.id.radioOther);
            } else {
                rgGender.check(R.id.radioMale); // Mặc định là Nam
            }
        } else {
            // Chế độ Thêm mới
            tvDialogTitle.setText("Thêm nhân viên mới");
            switchActive.setChecked(true); // Mặc định là kích hoạt
        }

        btnCancel.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> save());

        return v;
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
        } else if (checkedGenderId == R.id.radioOther) {
            gender = "Khác";
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

        if (onSaveListener != null) {
            onSaveListener.onSave(existingUser, password);
        }
        dismiss();
    }
}