// File: com/example/voicenote/ui/product/ProductManagementAdapter.java (MỚI)
package com.example.voicenote.ui.product.adapter;

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
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.data.local.rel.AlphabetHeaderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductManagementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    public interface OnProductClickListener {
        void onEditClick(ProductEntity product);
        void onDeleteClick(ProductEntity product);
    }

    private final OnProductClickListener listener;
    private final List<Object> data = new ArrayList<>();

    public ProductManagementAdapter(@NonNull OnProductClickListener listener) {
        this.listener = listener;
    }

    // Hàm submitList thay thế cho ListAdapter
    public void submitList(List<Object> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged(); // Cập nhật toàn bộ
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof AlphabetHeaderItem) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_alphabet_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            AlphabetHeaderItem header = (AlphabetHeaderItem) data.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else {
            ProductEntity product = (ProductEntity) data.get(position);
            ((ProductViewHolder) holder).bind(product, listener);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // --- ViewHolders ---

    /** ViewHolder cho Chữ cái (A, B, C) */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAlphabetHeader;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlphabetHeader = itemView.findViewById(R.id.tvAlphabetHeader);
        }
        void bind(AlphabetHeaderItem header) {
            tvAlphabetHeader.setText(header.letter);
        }
    }
    /** ViewHolder cho Sản phẩm */
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName, tvProductPrice;
        private final ImageButton btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(ProductEntity product, OnProductClickListener listener) {
            tvProductName.setText(product.name);
            tvProductPrice.setText(String.format(Locale.US, "%,d", product.price));

            // Click vào item -> Sửa
            itemView.setOnClickListener(v -> {
                listener.onEditClick(product);
            });

            // Click vào nút Xóa -> Xóa
            btnDelete.setOnClickListener(v -> {
                listener.onDeleteClick(product);
            });
        }
    }

    private static final DiffUtil.ItemCallback<ProductEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProductEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.id == newItem.id;
        }
        @Override
        public boolean areContentsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.name.equals(newItem.name) && oldItem.price == newItem.price;
        }
    };
}