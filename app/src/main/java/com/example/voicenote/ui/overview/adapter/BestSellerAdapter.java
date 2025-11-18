// File: com/example/voicenote/ui/overview/adapter/BestSellerAdapter.java
package com.example.voicenote.ui.overview.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.rel.BestSellerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {

    private String sortCriteria = "QUANTITY";
    private final List<BestSellerItem> data = new ArrayList<>();

    // Màu cho Top 3
    private final int[] rankColors = { 0xFFF59E0B, 0xFF6B7280, 0xFF8D6E63 };
    private final int[] rankIconColors = { 0xFFFDE68A, 0xFFD1D5DB, 0xFFA1887F };
    // Màu cho Rank 4+
    private final int defaultRankColor = 0xFF6B7280;
    private final int defaultIconColor = 0xFFD1D5DB;

    public BestSellerAdapter() {
        // (Không cần super)
    }

    /**
     * Hàm để Activity cập nhật trạng thái Sort
     */
    public void setSortCriteria(String criteria) {
        this.sortCriteria = criteria;
    }

    /**
     * Hàm submitList mới
     */
    public void submitList(List<BestSellerItem> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged(); // Buộc vẽ lại toàn bộ
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_best_seller, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Lấy item tại 'position' mới và truyền 'position' đó vào
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BestSellerItem item = data.get(position); // Lấy từ list
        holder.bind(item, position, sortCriteria); // 'position' là vị trí mới (0, 1, 2...)
    }

    @Override
    public int getItemCount() {
        return data.size();
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
        /**
         * Hàm bind này nhận 'position' (vị trí 0, 1, 2...)
         */
        public void bind(BestSellerItem item, int position, String sortCriteria) {
            // Luôn lấy position (0, 1, 2...) + 1 để ra rank (1, 2, 3...)
            int rank = position + 1;

            tvRank.setText(String.valueOf(rank));
            tvProductName.setText(item.productName);

            // [SỬA LỖI] Hiển thị đúng dữ liệu
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
}