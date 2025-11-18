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
    private final OrderRepository repository;

    public OrderDetailViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app);
    }

    public LiveData<OrderWithItems> getOrderById(long id) {
        return repository.getOrderById(id);
    }

    public void updatePaymentStatus(OrderEntity order, boolean isPaid) {
        repository.updatePaymentStatus(order, isPaid);
    }
    /**
     * Hàm xoá
     */
    public void deleteOrder(OrderEntity order) {
        repository.deleteOrder(order);
    }
}