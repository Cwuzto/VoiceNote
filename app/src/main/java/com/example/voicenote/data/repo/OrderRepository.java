// File: com/example/voicenote/data/repo/OrderRepository.java
package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.OrderDao;
import com.example.voicenote.data.local.dao.OrderItemDao;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EN: Repository handles order + order item operations.
 * VI: Repository xử lý các thao tác hoá đơn (Order) và dòng hàng (OrderItem).
 * (Đã refactor từ InvoiceRepository)
 */
public class OrderRepository {
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public OrderRepository(Application app) {
        // [SỬA] Sử dụng AppDatabase singleton
        AppDatabase db = AppDatabase.getInstance(app);
        this.orderDao = db.orderDao();
        this.orderItemDao = db.orderItemDao();
    }

    /**
     * EN: Get all orders with their items.
     * VI: Lấy tất cả đơn hàng và các món hàng bên trong.
     */
    public LiveData<List<OrderWithItems>> getOrdersWithItems() {
        return orderDao.getOrdersWithItems();
    }

    /**
     * EN: Get a specific order by its ID.
     * VI: Lấy một đơn hàng cụ thể bằng ID.
     */
    public LiveData<OrderWithItems> getOrderById(long id) {
        return orderDao.getOrderById(id);
    }

    /**
     * EN: Save (insert/update) an order with its items.
     * VI: Lưu (thêm mới/cập nhật) một đơn hàng và các món hàng.
     */
    public void saveOrder(OrderEntity order, List<OrderItemEntity> items) {
        executor.execute(() -> {
            // Cập nhật thời gian
            long now = System.currentTimeMillis();
            if (order.id == 0) { // Đơn mới
                order.createdAt = now;
            }
            order.updatedAt = now;

            // 1. Insert order để lấy ID
            long orderId = orderDao.insertOrder(order); // REPLACE acts as update

            // 2. Xoá item cũ (nếu là update) và thêm item mới
            orderItemDao.deleteByOrderId(orderId);
            for (OrderItemEntity item : items) {
                item.orderId = orderId;
                orderItemDao.insertOrderItem(item);
            }
        });
    }

    /**
     * EN: Update the payment status of an order.
     * VI: Cập nhật trạng thái thanh toán của một đơn hàng.
     */
    public void updatePaymentStatus(OrderEntity order, boolean isPaid) {
        executor.execute(() -> {
            String status = isPaid ? "PAID" : "UNPAID";
            long updatedAt = System.currentTimeMillis();
            orderDao.updatePaymentStatus(order.id, status, updatedAt);
        });
    }

    /**
     * EN: Delete an order (items are deleted by CASCADE).
     * VI: Xoá một đơn hàng (các item sẽ bị xoá theo nhờ CASCADE).
     */
    public void deleteOrder(OrderEntity order) {
        executor.execute(() -> orderDao.deleteOrder(order));
    }
}