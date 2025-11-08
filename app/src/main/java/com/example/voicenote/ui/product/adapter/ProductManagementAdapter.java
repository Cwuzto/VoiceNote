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
import java.util.Locale;

public class ProductManagementAdapter extends ListAdapter<ProductEntity, ProductManagementAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onEditClick(ProductEntity product);
        void onDeleteClick(ProductEntity product);
    }

    private final OnProductClickListener listener;

    public ProductManagementAdapter(@NonNull OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductEntity product = getItem(position);
        holder.bind(product, listener);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName, tvProductPrice;
        private final ImageButton btnMore;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(ProductEntity product, OnProductClickListener listener) {
            tvProductName.setText(product.name);
            tvProductPrice.setText(String.format(Locale.US, "%,d", product.price));

            // Xử lý menu Sửa/Xoá (bạn có thể dùng PopupMenu)
            btnMore.setOnClickListener(v -> {
                // Tạm thời: Bấm vào là Edit
                listener.onEditClick(product);
            });

            // Tạm thời: Bấm giữ là Delete
            itemView.setOnLongClickListener(v -> {
                listener.onDeleteClick(product);
                return true;
            });
        }
    }

    private static final DiffUtil.ItemCallback<ProductEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ProductEntity>() {
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