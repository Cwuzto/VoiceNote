// File: com/example/voicenote/ui/more/EmployeeAdapter.java
package com.example.voicenote.ui.more.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;

public class EmployeeAdapter extends ListAdapter<UserEntity, EmployeeAdapter.EmployeeViewHolder> {

    public interface OnEmployeeClickListener {
        void onEditClick(UserEntity user);
        void onDeleteClick(UserEntity user);
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

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeUsername = itemView.findViewById(R.id.tvEmployeeUsername);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(UserEntity user, OnEmployeeClickListener listener) {
            tvEmployeeName.setText(user.fullName);
            tvEmployeeUsername.setText(user.username);

            // Xử lý menu Sửa/Xoá (bạn có thể dùng PopupMenu)
            btnMore.setOnClickListener(v -> {
                // Tạm thời: Bấm vào là Edit
                listener.onEditClick(user);
            });

            // Tạm thời: Bấm giữ là Delete
            itemView.setOnLongClickListener(v -> {
                listener.onDeleteClick(user);
                return true;
            });
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
                            oldItem.username.equals(newItem.username);
                }
            };
}