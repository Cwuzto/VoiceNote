package com.example.voicenote.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.ui.auth.LoginActivity;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.ProfileViewModel;

/**
 * EN: Fragment displaying user info, hotline, and extra options.
 * VI: Fragment hiển thị thông tin người dùng, hotline và các tuỳ chọn thêm.
 */
public class MoreFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private SessionManager sessionManager;

    // Views cho header
    private TextView tvOwnerName, tvOwnerPhone, tvRole;
    private TextView btnLogout;

    // [MỚI] Views cho phân quyền
    private View rowStoreInfo, dividerStoreInfo;
    private View rowEmployeeManagement, dividerEmployee;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // [MỚI]
        super.onCreate(savedInstanceState);

        // Khởi tạo các đối tượng này 1 lần
        sessionManager = new SessionManager(requireContext());
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);

        // --- Ánh xạ View header ---
        tvOwnerName = v.findViewById(R.id.tvOwnerName);
        tvOwnerPhone = v.findViewById(R.id.tvOwnerPhone);
        tvRole = v.findViewById(R.id.tvRole);
        btnLogout = v.findViewById(R.id.btnLogout);

        // [MỚI] Ánh xạ view phân quyền
        rowStoreInfo = v.findViewById(R.id.rowStoreInfo);
        dividerStoreInfo = v.findViewById(R.id.dividerStoreInfo);
        rowEmployeeManagement = v.findViewById(R.id.rowEmployeeManagement);
        dividerEmployee = v.findViewById(R.id.dividerEmployee);

        TextView tvHotline = v.findViewById(R.id.tvHotline);

        // 1. Thông tin cá nhân
        v.findViewById(R.id.rowProfile).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), ProfileActivity.class));
        });

        // 2. Thông tin cửa hàng
        v.findViewById(R.id.rowStoreInfo).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), StoreInfoActivity.class));
        });

        // 3. Quản lý nhân viên
        v.findViewById(R.id.rowEmployeeManagement).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), EmployeeListActivity.class));
        });

        // 4. Hiển thị số điện thoại theo user
        v.findViewById(R.id.rowHotline).setOnClickListener(x -> {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvHotline.getText().toString().replace(" ", "")));
            startActivity(i);
        });

        // Nút đăng xuất ---
        btnLogout.setOnClickListener(view -> {
            showLogoutConfirmDialog();
        });

        // Tải dữ liệu người dùng ---
        loadUserData();

        return v;
    }

    /**
     * Tải dữ liệu người dùng VÀ XỬ LÝ PHÂN QUYỀN
     */
    private void loadUserData() {
        long userId = sessionManager.getUserId();
        if (userId == -1) {
            // Trường hợp lỗi (chưa đăng nhập)
            tvOwnerName.setText("Khách");
            tvOwnerPhone.setText("");
            return;
        }

        // Lắng nghe dữ liệu User từ DB
        profileViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            // 1. Cập nhật tên ở Header
            tvOwnerName.setText(user.fullName);

            // 2. Cập nhật SĐT (nếu có)
            if (user.phone != null && !user.phone.isEmpty()) {
                tvOwnerPhone.setText(user.phone);
            } else {
                tvOwnerPhone.setText(""); // Để trống
            }

            // 3. Cập nhật luôn dòng Role trong Card
            String roleDisplay;
            if ("OWNER".equals(user.role)) {
                roleDisplay = "Chủ quán";
                // Hiển thị các mục của Owner
                rowStoreInfo.setVisibility(View.VISIBLE);
                dividerStoreInfo.setVisibility(View.VISIBLE);
                rowEmployeeManagement.setVisibility(View.VISIBLE);
                dividerEmployee.setVisibility(View.VISIBLE);
            } else {
                roleDisplay = "Nhân viên";
                // Ẩn các mục của Owner
                rowStoreInfo.setVisibility(View.GONE);
                dividerStoreInfo.setVisibility(View.GONE);
                rowEmployeeManagement.setVisibility(View.GONE);
                dividerEmployee.setVisibility(View.GONE);
            }
            tvRole.setText(roleDisplay + " " + user.fullName);
        });
    }

    /**
     *  Xử lý logic Đăng xuất
     */
    private void logout() {
        // 1. Xoá session đã lưu
        sessionManager.clearSession();

        // 2. Chuyển về màn hình Login
        Intent intent = new Intent(getContext(), LoginActivity.class);

        // 3. Xoá tất cả Activity cũ khỏi stack
        // (Đảm bảo người dùng không thể bấm Back quay lại MainActivity)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }

    /**
     * [MỚI] Hiển thị hộp thoại xác nhận đăng xuất
     */
    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Nếu người dùng bấm "Đăng xuất"
                    logout();
                })
                .setNegativeButton("Huỷ", (dialog, which) -> {
                    // Nếu người dùng bấm "Huỷ", không làm gì cả
                    dialog.dismiss();
                })
                .show();
    }
}