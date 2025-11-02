// File: com/example/voicenote/vm/OrderEditViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.voicenote.data.local.entity.OrderEntity; // [SỬA]
import com.example.voicenote.data.local.entity.OrderItemEntity; // [SỬA]
import com.example.voicenote.data.repo.OrderRepository; // [SỬA]

import java.util.List;

/**
 * EN: ViewModel for order editing / creation screen.
 * VI: ViewModel cho màn hình chỉnh sửa / tạo hoá đơn (Order).
 * (Đã refactor từ InvoiceEditViewModel)
 */
public class OrderEditViewModel extends AndroidViewModel {
    private final OrderRepository repository; // [SỬA]

    public OrderEditViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app); // [SỬA]
    }

    /**
     * EN: Save a new order with lines.
     * VI: Lưu một hoá đơn mới cùng danh sách dòng hàng.
     */
    public void saveOrder(OrderEntity order, List<OrderItemEntity> items) { // [SỬA]
        repository.saveOrder(order, items); // [SỬA]
    }

    // [XOÁ] Hàm insertInvoiceWithLines cũ
}