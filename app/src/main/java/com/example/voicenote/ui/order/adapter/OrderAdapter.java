// File: com/example/voicenote/ui/invoice/adapter/OrderAdapter.java
package com.example.voicenote.ui.order.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import entity và relation mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách Order (đã refactor từ InvoiceAdapter)
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    // [SỬA] Thêm interface mới
    public interface OnPaidChange {
        void onChange(OrderEntity order, boolean checked);
    }

    public interface OnItemClickListener {
        void onItemClick(OrderWithItems orderWithItems);
    }

    private final List<OrderWithItems> data = new ArrayList<>();
    private final OnPaidChange onPaidChangeCallback;
    private final OnItemClickListener onItemClickCallback; // [MỚI]

    public OrderAdapter(OnPaidChange onPaidChange, OnItemClickListener onItemClick) {
        this.onPaidChangeCallback = onPaidChange;
        this.onItemClickCallback = onItemClick; // [MỚI]
    }

    public void submit(List<OrderWithItems> orders) { // [SỬA]
        data.clear();
        data.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // [SỬA] Truyền cả 2 callback vào
        holder.bind(data.get(position), onPaidChangeCallback, onItemClickCallback);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTime, tvTotal, tvLines;
        CheckBox cbPaid;

        VH(View v) {
            super(v);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvLines = v.findViewById(R.id.tvLines);
            cbPaid = v.findViewById(R.id.cbPaid);
        }

        // [SỬA] Cập nhật hàm bind
        // [SỬA] Cập nhật hàm bind
        void bind(OrderWithItems orderWithItems, OnPaidChange paidChangeCb, OnItemClickListener itemClickCb) {
            OrderEntity order = orderWithItems.order;

            // 1. Bind dữ liệu Order (Code cũ)
            tvCustomer.setText(order.customerName);
            tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));
            boolean isPaid = "PAID".equals(order.status);
            cbPaid.setChecked(isPaid);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(order.createdAt));

            // 2. Build chuỗi tóm tắt món hàng (Code cũ)
            if (orderWithItems.orderItems != null && !orderWithItems.orderItems.isEmpty()) {
                StringBuilder linesSummary = new StringBuilder();
                for (int i = 0; i < orderWithItems.orderItems.size(); i++) {
                    OrderItemEntity item = orderWithItems.orderItems.get(i);
                    if (i > 0) {
                        linesSummary.append("\n");
                    }
                    linesSummary.append(item.quantity)
                            .append(" x ")
                            .append(item.productName);
                }
                tvLines.setText(linesSummary.toString());
            } else {
                tvLines.setText("Đơn hàng trống");
            }

            // 3. Gán listener
            cbPaid.setOnCheckedChangeListener((b, checked) -> paidChangeCb.onChange(order, checked));

            // [MỚI] Gán listener cho toàn bộ card
            itemView.setOnClickListener(v -> {
                if (itemClickCb != null) {
                    itemClickCb.onItemClick(orderWithItems);
                }
            });
        }
    }
}