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
        TextView tvCustomer, tvTime, tvTotal; CheckBox cbPaid;
        VH(View v) { super(v);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            cbPaid = v.findViewById(R.id.cbPaid);
        }

        // [SỬA] Bind logic
        void bind(OrderWithItems orderWithItems, OnPaidChange cb) {
            OrderEntity order = orderWithItems.order;
            tvCustomer.setText(order.customerName);
            tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));

            // [SỬA] So sánh status
            boolean isPaid = "PAID".equals(order.status);
            cbPaid.setChecked(isPaid);

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(order.createdAt));

            // [SỬA] Cập nhật listener
            cbPaid.setOnCheckedChangeListener((b, checked) -> cb.onChange(order, checked));
        }
    }
}