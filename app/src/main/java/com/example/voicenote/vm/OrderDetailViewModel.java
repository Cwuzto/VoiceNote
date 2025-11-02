// File: com/example/voicenote/vm/OrderDetailViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.entity.OrderEntity; // [SỬA]
import com.example.voicenote.data.local.rel.OrderWithItems; // [SỬA]
import com.example.voicenote.data.repo.OrderRepository; // [SỬA]

/**
 * EN: ViewModel for order detail screen.
 * VI: ViewModel cho màn chi tiết hoá đơn (Order).
 * (Đã refactor từ InvoiceDetailViewModel)
 */
public class OrderDetailViewModel extends AndroidViewModel {
    private final OrderRepository repository; // [SỬA]

    public OrderDetailViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app); // [SỬA]
    }

    public LiveData<OrderWithItems> getOrderById(long id) { // [SỬA]
        return repository.getOrderById(id); // [SỬA]
    }

    public void updatePaymentStatus(OrderEntity order, boolean isPaid) { // [SỬA]
        repository.updatePaymentStatus(order, isPaid); // [SỬA]
    }

    // [XOÁ] Hàm setPaid và deleteInvoice cũ (nếu cần delete thì thêm lại)
}