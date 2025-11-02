// File: com/example/voicenote/ui/order/OrderDetailActivity.java
package com.example.voicenote.ui.order; // [SỬA] Package

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
// [SỬA] Import entity và viewmodel mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.vm.OrderDetailViewModel;

import java.util.Locale;

/** * Activity chi tiết Order (đã refactor từ InvoiceDetailActivity) 
 */
public class OrderDetailActivity extends AppCompatActivity {
    private OrderDetailViewModel viewModel; // [SỬA]

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [SỬA] Đổi layout (bạn cần đổi tên file XML)
        setContentView(R.layout.activity_order_detail);

        // [SỬA] Key intent
        long orderId = getIntent().getLongExtra("order_id", -1);
        TextView tvCustomer = findViewById(R.id.tvCustomer);
        TextView tvTotal = findViewById(R.id.tvTotal);
        CheckBox cbPaid = findViewById(R.id.cbPaid);

        // [SỬA] ViewModel
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);
        viewModel.getOrderById(orderId).observe(this, orderWithItems -> { // [SỬA]
            if (orderWithItems != null && orderWithItems.order != null) {
                OrderEntity order = orderWithItems.order; // [SỬA]
                tvCustomer.setText(order.customerName); // [SỬA]
                // [SỬA] Cập nhật format tiền
                tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));
                cbPaid.setChecked("PAID".equals(order.status)); // [SỬA]
            }
        });

        cbPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // [SỬA] Lấy đúng order và gọi hàm mới
            if (viewModel.getOrderById(orderId).getValue() != null) {
                OrderEntity current = viewModel.getOrderById(orderId).getValue().order;
                if (current != null) {
                    viewModel.updatePaymentStatus(current, isChecked); // [SỬA]
                }
            }
        });
    }
}