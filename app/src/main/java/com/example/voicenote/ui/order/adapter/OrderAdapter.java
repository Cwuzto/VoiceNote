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

/** * Adapter cho danh sách Order (đã refactor từ InvoiceAdapter) 
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    // [SỬA] Interface
    public interface OnPaidChange { void onChange(OrderEntity order, boolean checked); }
    private final List<OrderWithItems> data = new ArrayList<>();
    private final OnPaidChange callback;

    public OrderAdapter(OnPaidChange cb) { this.callback = cb; }

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
        holder.bind(data.get(position), callback);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTime, tvTotal, tvLines;
        CheckBox cbPaid;
        VH(View v) { super(v);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvLines = v.findViewById(R.id.tvLines);
            cbPaid = v.findViewById(R.id.cbPaid);
        }

        // [SỬA] Cập nhật hàm bind
        void bind(OrderWithItems orderWithItems, OnPaidChange cb) {
            OrderEntity order = orderWithItems.order;

            // 1. Bind dữ liệu Order
            tvCustomer.setText(order.customerName);
            tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));
            boolean isPaid = "PAID".equals(order.status);
            cbPaid.setChecked(isPaid);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(order.createdAt));

            // 2. [MỚI] Build chuỗi tóm tắt món hàng
            if (orderWithItems.orderItems != null && !orderWithItems.orderItems.isEmpty()) {
                StringBuilder linesSummary = new StringBuilder();
                for (int i = 0; i < orderWithItems.orderItems.size(); i++) {
                    OrderItemEntity item = orderWithItems.orderItems.get(i);
                    // Dòng đầu tiên
                    if (i > 0) {
                        linesSummary.append("\n"); // Thêm xuống dòng nếu có > 1 món
                    }
                    linesSummary.append(item.quantity)
                            .append(" x ")
                            .append(item.productName);
                }
                tvLines.setText(linesSummary.toString());
            } else {
                tvLines.setText("Đơn hàng trống"); // Trường hợp không có món
            }

            // 3. Gán listener
            cbPaid.setOnCheckedChangeListener((b, checked) -> cb.onChange(order, checked));

            // (Bạn cũng có thể thêm OnClickListener cho itemView ở đây
            // để mở OrderDetailActivity)
        }
    }
}