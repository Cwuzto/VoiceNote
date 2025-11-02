package com.example.voicenote.ui.order;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.vm.OrderEditViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity sửa/tạo Order (đã refactor từ InvoiceEditActivity)
 */
public class OrderEditActivity extends AppCompatActivity {
    private OrderEditViewModel viewModel; // [SỬA]

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // [SỬA] Đổi layout (bạn cần đổi tên file XML)
        setContentView(R.layout.activity_order_edit);

        // [SỬA] ViewModel
        viewModel = new ViewModelProvider(this).get(OrderEditViewModel.class);
        findViewById(R.id.btnDone).setOnClickListener(v -> saveOrder()); // [SỬA]
    }

    // [SỬA] Cập nhật logic
    private void saveOrder() {
        OrderEntity order = new OrderEntity();
        order.customerName = "Khách mới";
        order.totalAmount = 50000; // Tính tổng
        order.status = "UNPAID";
        order.paymentMethod = "CASH";
        // createdAt và updatedAt sẽ được set trong Repository

        List<OrderItemEntity> items = new ArrayList<>();
        OrderItemEntity item = new OrderItemEntity();
        item.productName = "Phở bò";
        item.quantity = 1;
        item.unitPrice = 50000;
        item.note = "";
        items.add(item);

        viewModel.saveOrder(order, items); // [SỬA]
        finish();
    }
}