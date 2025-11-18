// File: com/example/voicenote/vm/OrderEditViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.repo.OrderRepository;

import java.util.List;

public class OrderEditViewModel extends AndroidViewModel {
    private final OrderRepository repository;

    public OrderEditViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app);
    }

    /**
     * Lưu một hoá đơn mới cùng danh sách dòng hàng.
     */
    public void saveOrder(OrderEntity order, List<OrderItemEntity> items) {
        repository.saveOrder(order, items);
    }
}