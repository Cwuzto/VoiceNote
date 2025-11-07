// File: com/example/voicenote/ui/order/OrderDetailActivity.java
package com.example.voicenote.ui.order; // [SỬA] Package

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
// [SỬA] Import entity và viewmodel mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.vm.OrderDetailViewModel;

import org.jspecify.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** * Activity chi tiết Order (đã refactor từ InvoiceDetailActivity) 
 */
public class OrderDetailActivity extends AppCompatActivity {
    private OrderDetailViewModel viewModel;

    // [MỚI] Khai báo tất cả View
    private TextView tvCustomer, tvDate, tvSubtotal, tvTotal;
    private CheckBox cbPaid;
    private LinearLayout containerItems;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        long orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            finish(); // Không có ID thì đóng lại
            return;
        }

        // --- Ánh xạ View ---
        tvCustomer = findViewById(R.id.tvCustomer);
        tvTotal = findViewById(R.id.tvTotal);
        cbPaid = findViewById(R.id.cbPaid);
        tvDate = findViewById(R.id.tvDate);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        containerItems = findViewById(R.id.containerItems);

        // --- Lấy ViewModel và Quan sát Dữ liệu ---
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);
        viewModel.getOrderById(orderId).observe(this, orderWithItems -> {
            if (orderWithItems != null && orderWithItems.order != null) {
                // [MỚI] Gọi hàm để điền dữ liệu
                populateData(orderWithItems);
            }
        });

        // --- Listener cho Checkbox (Giữ nguyên) ---
        cbPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (viewModel.getOrderById(orderId).getValue() != null) {
                OrderEntity current = viewModel.getOrderById(orderId).getValue().order;
                if (current != null) {
                    viewModel.updatePaymentStatus(current, isChecked);
                }
            }
        });

        // Nút đóng
        findViewById(R.id.tvClose).setOnClickListener(v -> finish());
    }

    /**
     * Hàm điền toàn bộ dữ liệu thật vào View
     */
    private void populateData(@NonNull OrderWithItems orderWithItems) {
        OrderEntity order = orderWithItems.order;

        // 1. Điền thông tin cơ bản
        tvCustomer.setText(order.customerName);
        tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));
        tvSubtotal.setText(String.format(Locale.US, "%,d", order.totalAmount)); // (Tạm thời subtotal = total)
        cbPaid.setChecked("PAID".equals(order.status));

        // Định dạng ngày giờ
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(order.createdAt));

        // 2. Điền danh sách món hàng
        containerItems.removeAllViews(); // Xoá hết view giả (nếu có)
        LayoutInflater inflater = LayoutInflater.from(this);

        if (orderWithItems.orderItems == null || orderWithItems.orderItems.isEmpty()) {
            // Không có món nào
            TextView emptyView = new TextView(this);
            emptyView.setText("Đơn hàng không có món nào.");
            containerItems.addView(emptyView);
            return;
        }

        // Lặp qua danh sách món và thêm vào layout
        for (OrderItemEntity item : orderWithItems.orderItems) {
            View itemView = inflater.inflate(R.layout.item_order_detail_line, containerItems, false);

            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemTotalPrice = itemView.findViewById(R.id.tvItemTotalPrice);
            TextView tvItemQty = itemView.findViewById(R.id.tvItemQty);
            TextView tvItemNote = itemView.findViewById(R.id.tvItemNote);

            long lineTotal = item.unitPrice * item.quantity;

            tvItemName.setText(item.productName);
            tvItemTotalPrice.setText(String.format(Locale.US, "%,d", lineTotal));
            tvItemQty.setText(String.format(Locale.US, "%d x %,d", item.quantity, item.unitPrice));

            if (item.note != null && !item.note.isEmpty()) {
                tvItemNote.setText("Ghi chú: " + item.note);
                tvItemNote.setVisibility(View.VISIBLE);
            } else {
                tvItemNote.setVisibility(View.GONE);
            }

            containerItems.addView(itemView);
        }
    }
}