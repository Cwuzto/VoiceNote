package com.example.voicenote.ui.quick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import entity mới
import com.example.voicenote.data.local.entity.ProductEntity;

import java.util.List;

/**
 * 4-column grid with a static "Add item" tile at position 0.
 */
public class QuickGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // --- Interfaces for callbacks ---
    public interface OnAddClick { void onAddClick(); }
    // [SỬA] Cập nhật interface
    public interface OnPick { void onPick(ProductEntity item, int position); }
    public interface OnRemove { void onRemove(ProductEntity item, int position); }

    // --- View Types ---
    private static final int VT_ADD = 0;
    private static final int VT_ITEM = 1;

    // --- Fields ---
    private final List<ProductEntity> data; // [SỬA]
    private final OnAddClick onAddClick;
    private final OnPick onPick;
    private final OnRemove onRemove;

    // [SỬA] Cập nhật constructor
    public QuickGridAdapter(List<ProductEntity> data, OnAddClick onAddClick, OnPick onPick, OnRemove onRemove) {
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
        if (getItemViewType(position) == VT_ADD) {
            ((VHAdd) holder).bind(onAddClick);
            return;
        }

        // [SỬA] Lấy ProductEntity
        final ProductEntity item = data.get(position - 1);
        final VHItem itemViewHolder = (VHItem) holder;

        // Gán dữ liệu cơ bản
        itemViewHolder.tvName.setText(item.name);

        // [MỚI] Tính toán "initial" vì ProductEntity không có trường này
        itemViewHolder.tvInitial.setText(makeInitial(item.name));

        // EN: pastel background per position; VI: đổi màu pastel theo vị trí
        int[] pastel = {0xFFEAF1FF, 0xFFF0E7FF, 0xFFEFFCF3, 0xFFFFF3E0, 0xFFFFE4EC};
        View chip = itemViewHolder.itemView.findViewById(R.id.chipBox);
        if (chip.getBackground() != null) {
            chip.getBackground().setTint(pastel[(position - 1) % pastel.length]);
        }

        // Hiển thị badge số lượng (dùng trường @Ignore 'selected')
        if (item.selected > 0) {
            itemViewHolder.tvBadge.setVisibility(View.VISIBLE);
            itemViewHolder.tvBadge.setText(String.valueOf(item.selected));
        } else {
            itemViewHolder.tvBadge.setVisibility(View.GONE);
        }

        // Hiển thị nút xoá (dùng trường @Ignore 'showRemove')
        itemViewHolder.ivRemove.setVisibility(item.showRemove ? View.VISIBLE : View.GONE);

        // --- Gán Listeners ---

        itemViewHolder.itemView.setOnClickListener(view -> {
            if (item.showRemove) return;
            if (onPick != null) {
                onPick.onPick(item, position - 1);
            }
        });

        itemViewHolder.itemView.setOnLongClickListener(view -> {
            item.showRemove = !item.showRemove;
            RecyclerView.Adapter<?> adapter = itemViewHolder.getBindingAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(itemViewHolder.getBindingAdapterPosition());
            }
            return true;
        });

        itemViewHolder.ivRemove.setOnClickListener(view -> {
            if (onRemove != null) {
                onRemove.onRemove(item, position - 1);
            }
        });
    }

    /**
     * [MỚI] Hàm helper để tính toán initial từ tên
     */
    private String makeInitial(String name){
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) return "?";
        if (parts.length == 1) return parts[0].substring(0,1).toUpperCase();
        return (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
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