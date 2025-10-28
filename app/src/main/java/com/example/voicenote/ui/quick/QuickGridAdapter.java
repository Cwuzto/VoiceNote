package com.example.voicenote.ui.quick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.QuickItemEntity;

import java.util.List;

public class QuickGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // --- Interfaces for callbacks ---
    public interface OnAddClick { void onAddClick(); }
    public interface OnPick { void onPick(QuickItemEntity item, int position); }
    public interface OnRemove { void onRemove(QuickItemEntity item, int position); }

    // --- View Types ---
    private static final int VT_ADD = 0;
    private static final int VT_ITEM = 1;

    // --- Fields ---
    private final List<QuickItemEntity> data; // chỉ chứa item thật, KHÔNG chứa ô Add
    private final OnAddClick onAddClick;
    private final OnPick onPick;
    private final OnRemove onRemove;

    public QuickGridAdapter(List<QuickItemEntity> data, OnAddClick onAddClick, OnPick onPick, OnRemove onRemove) {
        this.data = data;
        this.onAddClick = onAddClick;
        this.onPick = onPick;
        this.onRemove = onRemove;
        setHasStableIds(false);
    }

    // --- Adapter Overrides ---

    @Override
    public int getItemCount() {
        return data.size() + 1; // +1 cho ô Add ở đầu
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VT_ADD : VT_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VT_ADD) {
            View itemView = inflater.inflate(R.layout.item_quick_add, parent, false);
            return new VHAdd(itemView);
        }
        View itemView = inflater.inflate(R.layout.item_quick_chip, parent, false);
        return new VHItem(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Case 1: Bind ô "Thêm hàng" (vị trí 0)
        if (getItemViewType(position) == VT_ADD) {
            ((VHAdd) holder).bind(onAddClick);
            return;
        }

        // Case 2: Bind ô item hàng hoá (vị trí 1 trở đi)
        // Lấy item thật (phải trừ 1 vì data list không chứa ô "Add")
        final QuickItemEntity item = data.get(position - 1);
        final VHItem itemViewHolder = (VHItem) holder;

        // Gán dữ liệu cơ bản
        itemViewHolder.tvInitial.setText(item.initial);
        itemViewHolder.tvName.setText(item.name);

        // EN: pastel background per position; VI: đổi màu pastel theo vị trí
        int[] pastel = {0xFFEAF1FF, 0xFFF0E7FF, 0xFFEFFCF3, 0xFFFFF3E0, 0xFFFFE4EC};
        View chip = itemViewHolder.itemView.findViewById(R.id.chipBox);
        if (chip.getBackground() != null) {
            // Dùng (position - 1) để index của màu khớp với index của data
            chip.getBackground().setTint(pastel[(position - 1) % pastel.length]);
        }

        // Hiển thị badge số lượng
        if (item.selected > 0) {
            itemViewHolder.tvBadge.setVisibility(View.VISIBLE);
            itemViewHolder.tvBadge.setText(String.valueOf(item.selected));
        } else {
            itemViewHolder.tvBadge.setVisibility(View.GONE);
        }

        // Hiển thị nút xoá (dấu trừ)
        itemViewHolder.ivRemove.setVisibility(item.showRemove ? View.VISIBLE : View.GONE);

        // --- Gán Listeners ---

        // 1. Click thường:
        itemViewHolder.itemView.setOnClickListener(view -> {
            if (item.showRemove) return; // Nếu đang ở chế độ xoá thì không làm gì
            if (onPick != null) {
                // Chỉ gọi callback, để Activity xử lý logic (tăng selected, notify...)
                onPick.onPick(item, position - 1); // position - 1 là index trong 'data'
            }
        });

        // 2. Long click:
        itemViewHolder.itemView.setOnLongClickListener(view -> {
            item.showRemove = !item.showRemove; // Đảo trạng thái hiển thị nút xoá

            // Thông báo cho adapter cập nhật lại chính item này
            RecyclerView.Adapter<?> adapter = itemViewHolder.getBindingAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(itemViewHolder.getBindingAdapterPosition());
            }
            return true; // Đã xử lý long click
        });

        // 3. Click nút xoá (dấu trừ):
        itemViewHolder.ivRemove.setOnClickListener(view -> {
            if (onRemove != null) {
                onRemove.onRemove(item, position - 1); // position - 1 là index trong 'data'
            }
        });
    }

    // --- ViewHolders ---

    /** ViewHolder cho ô "Thêm hàng" */
    static class VHAdd extends RecyclerView.ViewHolder {
        VHAdd(@NonNull View itemView) {
            super(itemView);
        }

        void bind(OnAddClick listener) {
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onAddClick();
                }
            });
        }
    }

    /** ViewHolder cho ô item hàng hoá */
    static class VHItem extends RecyclerView.ViewHolder {
        // Khai báo các view
        TextView tvInitial, tvName, tvBadge, ivRemove;

        VHItem(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvName = itemView.findViewById(R.id.tvName);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }
    }
}