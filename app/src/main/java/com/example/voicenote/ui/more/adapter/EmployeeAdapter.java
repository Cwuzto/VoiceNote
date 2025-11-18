// File: com/example/voicenote/ui/more/EmployeeAdapter.java
package com.example.voicenote.ui.more.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;

public class EmployeeAdapter extends ListAdapter<UserEntity, EmployeeAdapter.EmployeeViewHolder> {

    public interface OnEmployeeClickListener {
        void onEditClick(UserEntity user);
        void onDeleteClick(UserEntity user);
        void onDeactivateClick(UserEntity user);
    }

    private final OnEmployeeClickListener listener;

    public EmployeeAdapter(@NonNull OnEmployeeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        UserEntity user = getItem(position);
        holder.bind(user, listener);
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmployeeName;
        private final TextView tvEmployeeUsername;
        private final ImageButton btnMore;
        private final ImageView imgAvatar;
        private final TextView tvInitial;
        private final Context context;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeUsername = itemView.findViewById(R.id.tvEmployeeUsername);
            btnMore = itemView.findViewById(R.id.btnMore);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvInitial = itemView.findViewById(R.id.tvInitial);
        }

        public void bind(UserEntity user, OnEmployeeClickListener listener) {
            tvEmployeeName.setText(user.fullName);
            tvEmployeeUsername.setText(user.username);

            // 1. Xử lý Avatar
            if (user.imageUrl != null && !user.imageUrl.isEmpty()) {
                // Có ảnh -> Dùng Glide
                tvInitial.setVisibility(View.GONE);
                imgAvatar.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(Uri.parse(user.imageUrl))
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                // Không có ảnh -> Dùng Chữ cái đầu
                imgAvatar.setVisibility(View.GONE);
                tvInitial.setVisibility(View.VISIBLE);
                tvInitial.setText(makeInitial(user.fullName));

                // Đổi màu nền theo giới tính (ví dụ)
                int bgColorRes = "Nữ".equals(user.gender) ? R.color.pastel_pink : R.color.pastel_blue;
                tvInitial.getBackground().setTint(ContextCompat.getColor(context, bgColorRes));
            }

            // 2. Xử lý Trạng thái (Active/Inactive)
            if (user.isActive) {
                itemView.setAlpha(1.0f);
                btnMore.setEnabled(true);
            } else {
                itemView.setAlpha(0.5f); // Làm mờ
            }
            btnMore.setEnabled(true); // Vẫn cho phép bật lại

            // 3. Xử lý PopupMenu
            btnMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, btnMore);
                popup.getMenuInflater().inflate(R.menu.menu_employee_item, popup.getMenu());

                // Cập nhật text của menu
                MenuItem deactivateItem = popup.getMenu().findItem(R.id.action_deactivate);
                if (user.isActive) {
                    deactivateItem.setTitle("Vô hiệu hoá");
                } else {
                    deactivateItem.setTitle("Kích hoạt lại");
                }

                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        listener.onEditClick(user);
                        return true;
                    }
                    if (id == R.id.action_deactivate) {
                        // [SỬA] Gọi listener
                        listener.onDeactivateClick(user);
                        return true;
                    }
                    if (id == R.id.action_delete) {
                        listener.onDeleteClick(user);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
        // Hàm helper tạo chữ cái đầu
        private String makeInitial(String name){
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 0) return "?";
            if (parts.length == 1) return parts[0].substring(0,1).toUpperCase();
            // Lấy chữ cái đầu của 2 từ cuối (ví dụ: "Hoàng Ngọc" -> "HN")
            return (parts[parts.length-2].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
        }
    }

    // DiffUtil để RecyclerView tự động cập nhật
    private static final DiffUtil.ItemCallback<UserEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UserEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserEntity oldItem, @NonNull UserEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull UserEntity oldItem, @NonNull UserEntity newItem) {
                    return oldItem.fullName.equals(newItem.fullName) &&
                            oldItem.username.equals(newItem.username) &&
                            oldItem.isActive == newItem.isActive;
                }
            };
}