// File: com/example/voicenote/ui/invoice/adapter/OrderAdapter.java
package com.example.voicenote.ui.order.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        LinearLayout btnPaidArea;
        Context context;

        VH(View v) {
            super(v);
            context = v.getContext(); // Lấy context để show Dialog
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvLines = v.findViewById(R.id.tvLines);
            cbPaid = v.findViewById(R.id.cbPaid);
            btnPaidArea = v.findViewById(R.id.btnPaidArea);
        }

        void bind(OrderWithItems orderWithItems, OnPaidChange paidChangeCb, OnItemClickListener itemClickCb) {
            OrderEntity order = orderWithItems.order;

            // Bind dữ liệu Order
            tvCustomer.setText(order.customerName);
            tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));

            // Logic hiển thị và Khóa Checkbox
            boolean isPaid = "PAID".equals(order.status);
            cbPaid.setChecked(isPaid);
            cbPaid.setEnabled(!isPaid); // Yêu cầu 2: Khóa nếu đã thanh toán
            btnPaidArea.setEnabled(!isPaid); // Khóa luôn cả layout cha

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(order.createdAt));

            // Build chuỗi tóm tắt món hàng (Code cũ)
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

            // OnClickListener
            btnPaidArea.setOnClickListener(v -> {
                // Nếu đã paid (bị khóa) thì không làm gì
                if (isPaid) return;
                //  Hỏi xác nhận
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận thanh toán")
                        .setMessage("Bạn có chắc chắn muốn đánh dấu đơn hàng này là ĐÃ NHẬN TIỀN?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            // Chỉ gọi callback khi người dùng bấm "Xác nhận"
                            paidChangeCb.onChange(order, true);
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            });

            // Gán listener cho toàn bộ card
            itemView.setOnClickListener(v -> {
                if (itemClickCb != null) {
                    itemClickCb.onItemClick(orderWithItems);
                }
            });
        }
    }
}