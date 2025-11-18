package com.example.voicenote.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.ui.auth.LoginActivity;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.BankAccountViewModel;
import com.example.voicenote.vm.ProfileViewModel;

/**
 * Fragment hiển thị thông tin người dùng, hotline và các tuỳ chọn thêm.
 */
public class MoreFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private BankAccountViewModel bankAccountViewModel;
    private SessionManager sessionManager;

    // Views cho header
    private TextView tvOwnerName, tvOwnerPhone, tvRole;
    private TextView btnLogout;
    private ImageView imgAvatar;

    // [PHÂN QUYỀN] Các View cần Ẩn/Hiện
    private View rowStoreInfo, dividerStoreInfo;     // Card 1
    private View rowSpeakerSettings, dividerSpeaker; // Card 2 (Loa)
    private LinearLayout rowAddQR;                   // Card 2 (Bank/QR)
    private View dividerEmployee;                    // Card 2 (Vạch kẻ dưới QR)
    private View rowEmployeeManagement;              // Card 2 (Nhân viên)

    // Views bên trong rowAddQR (để đổi icon/text)
    private ImageView imgQrIcon;
    private TextView tvQrText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo các đối tượng này 1 lần
        sessionManager = new SessionManager(requireContext());
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        bankAccountViewModel = new ViewModelProvider(this).get(BankAccountViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);

        TextView tvHotline = v.findViewById(R.id.tvHotline);

        // --- Ánh xạ View header ---
        tvOwnerName = v.findViewById(R.id.tvOwnerName);
        tvOwnerPhone = v.findViewById(R.id.tvOwnerPhone);
        tvRole = v.findViewById(R.id.tvRole);
        btnLogout = v.findViewById(R.id.btnLogout);
        imgAvatar = v.findViewById(R.id.imgAvatar);

        // --- 2. Ánh xạ Views cần Phân quyền ---

        // Card 1: Thông tin cửa hàng
        rowStoreInfo = v.findViewById(R.id.rowStoreInfo);
        dividerStoreInfo = v.findViewById(R.id.dividerStoreInfo);

        // Card 2: Loa đọc tiền
        rowSpeakerSettings = v.findViewById(R.id.rowSpeakerSettings);
        dividerSpeaker = v.findViewById(R.id.dividerSpeaker);

        // Card 2: Tài khoản ngân hàng (QR)
        rowAddQR = v.findViewById(R.id.rowAddQR);
        imgQrIcon = rowAddQR.findViewById(R.id.imgQrIcon);
        tvQrText = rowAddQR.findViewById(R.id.tvQrText);

        // Card 2: Quản lý nhân viên
        dividerEmployee = v.findViewById(R.id.dividerEmployee);
        rowEmployeeManagement = v.findViewById(R.id.rowEmployeeManagement);

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

        // 5. Quản lý tài khoản ngân hàng
        v.findViewById(R.id.rowAddQR).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), BankAccountListActivity.class));
        });

        // 6. Loa Đọc Tiền
        v.findViewById(R.id.tvSpeakerTitle).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), SpeakerSettingsActivity.class));
        });

        // Nút đăng xuất ---
        btnLogout.setOnClickListener(view -> {
            showLogoutConfirmDialog();
        });

        // Tải dữ liệu người dùng ---
        loadUserData();
        checkBankAccountStatus();

        return v;
    }

    /**
     * Kiểm tra số lượng tài khoản ngân hàng
     */
    private void checkBankAccountStatus() {
        bankAccountViewModel.getAllAccounts().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                // Chưa có tài khoản: "Thêm QR..." + icon QR
                tvQrText.setText("Thêm QR để bật loa báo ting ting");
                imgQrIcon.setImageResource(R.drawable.ic_qr_24);
            } else {
                // Đã có tài khoản: "Tài khoản ngân hàng" + icon Wallet
                tvQrText.setText("Tài khoản ngân hàng");
                imgQrIcon.setImageResource(R.drawable.ic_wallet_24);
            }
        });
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
                // OWNER: Thấy tất cả
                rowStoreInfo.setVisibility(View.VISIBLE);
                dividerStoreInfo.setVisibility(View.VISIBLE);

                rowSpeakerSettings.setVisibility(View.VISIBLE);
                dividerSpeaker.setVisibility(View.VISIBLE);

                rowAddQR.setVisibility(View.VISIBLE);
                dividerEmployee.setVisibility(View.VISIBLE);

                rowEmployeeManagement.setVisibility(View.VISIBLE);
            } else {
                roleDisplay = "Nhân viên";

                // EMPLOYEE: Ẩn các mục quản lý
                rowStoreInfo.setVisibility(View.GONE);
                dividerStoreInfo.setVisibility(View.GONE);

                rowSpeakerSettings.setVisibility(View.GONE); // Ẩn Loa
                dividerSpeaker.setVisibility(View.GONE);     // Ẩn gạch

                rowAddQR.setVisibility(View.GONE);           // Ẩn Ngân hàng
                dividerEmployee.setVisibility(View.GONE);    // Ẩn gạch

                rowEmployeeManagement.setVisibility(View.GONE); // Ẩn QL Nhân viên
            }
            tvRole.setText(roleDisplay + " " + user.fullName);

            // Cập nhật Avatar
            if (user.imageUrl != null && !user.imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(user.imageUrl))
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                // (Nếu không có ảnh, set ảnh mặc định)
                imgAvatar.setImageResource(R.drawable.ic_user_circle_24);
            }
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
     * Hiển thị hộp thoại xác nhận đăng xuất
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