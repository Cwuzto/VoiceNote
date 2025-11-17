// File: com/example/voicenote/ui/overview/adapter/BestSellerAdapter.java
package com.example.voicenote.ui.overview.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.rel.BestSellerItem;

import java.util.List;
import java.util.Locale;

public class BestSellerAdapter extends ListAdapter<BestSellerItem, BestSellerAdapter.ViewHolder> {
    // Biến để biết đang sort theo cái gì
    private String sortCriteria = "QUANTITY";

    // Màu cho Top 3
    private final int[] rankColors = { 0xFFF59E0B, 0xFF6B7280, 0xFF8D6E63 }; // Vàng, Bạc, Đồng
    private final int[] rankIconColors = { 0xFFFDE68A, 0xFFD1D5DB, 0xFFA1887F };
    // Màu cho Rank 4+
    private final int defaultRankColor = 0xFF6B7280; // Xám
    private final int defaultIconColor = 0xFFD1D5DB; // Xám nhạt

    public BestSellerAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Hàm để Activity cập nhật trạng thái Sort
     */
    public void setSortCriteria(String criteria) {
        this.sortCriteria = criteria;
        // (Không cần notify, submitList sẽ làm việc đó)
    }

    // Ghi đè submitList để cập nhật sortCriteria
    @Override
    public void submitList(@Nullable List<BestSellerItem> list) {
        // (Chúng ta có thể làm 1 hàm submitList(list, criteria) riêng,
        // nhưng làm vậy sẽ phải sửa cả BestSellerViewModel)
        // Tạm thời, chúng ta sẽ gọi setSortCriteria từ Activity
        super.submitList(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_best_seller, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BestSellerItem item = getItem(position);
        holder.bind(item, position, sortCriteria);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvProductName, tvQuantity;
        ImageView ivRankIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivRankIcon = itemView.findViewById(R.id.ivRankIcon);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }

        public void bind(BestSellerItem item, int position, String sortCriteria) {
            int rank = position + 1;
            tvRank.setText(String.valueOf(rank));
            tvProductName.setText(item.productName);

            // Hiển thị đúng dữ liệu
            if ("REVENUE".equals(sortCriteria)) {
                // Hiển thị Doanh thu
                tvQuantity.setText(String.format(Locale.US, "%,dđ", item.totalRevenue));
                tvQuantity.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.brand_blue));
            } else {
                // Hiển thị Số lượng
                tvQuantity.setText("x" + item.totalQuantity);
                tvQuantity.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
            }

            // Đổi màu Rank
            Drawable rankBg = tvRank.getBackground().mutate();
            if (rank <= 3 && rank - 1 < rankColors.length) {
                // Top 3
                rankBg.setTint(rankColors[rank - 1]);
                ivRankIcon.setColorFilter(rankIconColors[rank - 1]);
                ivRankIcon.setVisibility(View.VISIBLE);
            } else {
                // Rank 4 trở đi
                rankBg.setTint(defaultRankColor);
                ivRankIcon.setVisibility(View.GONE); // Ẩn ngôi sao
            }
            tvRank.setBackground(rankBg);
        }
    }

    private static final DiffUtil.ItemCallback<BestSellerItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BestSellerItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull BestSellerItem oldItem, @NonNull BestSellerItem newItem) {
                    // ProductName là key duy nhất
                    return oldItem.productName.equals(newItem.productName);
                }
                @Override
                public boolean areContentsTheSame(@NonNull BestSellerItem oldItem, @NonNull BestSellerItem newItem) {
                    // Phải kiểm tra cả hai
                    return oldItem.totalQuantity == newItem.totalQuantity &&
                            oldItem.totalRevenue == newItem.totalRevenue;
                }
            };
}