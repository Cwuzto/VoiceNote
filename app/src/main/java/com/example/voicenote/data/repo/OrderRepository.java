// File: com/example/voicenote/data/repo/OrderRepository.java
package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.OrderDao;
import com.example.voicenote.data.local.dao.OrderItemDao;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.BestSellerItem;
import com.example.voicenote.data.local.rel.ChartDataPoint;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.data.local.rel.RevenueSummary;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VI: Repository xử lý các thao tác hoá đơn (Order) và dòng hàng (OrderItem).
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
    public LiveData<RevenueSummary> getRevenueSummary(long startTime, long endTime) {
        return orderDao.getRevenueSummary(startTime, endTime);
    }
    public LiveData<List<BestSellerItem>> getBestSellers(long startTime, long endTime) {
        return orderItemDao.getBestSellers(startTime, endTime);
    }
    public LiveData<List<ChartDataPoint>> getChartData(long startTime, long endTime) {
        return orderDao.getChartData(startTime, endTime);
    }
    public LiveData<List<ChartDataPoint>> getChartDataHourly(long startTime, long endTime) {
        return orderDao.getChartDataHourly(startTime, endTime);
    }

    /**
     * Lưu (thêm mới/cập nhật) một đơn hàng và các món hàng.
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
     * Cập nhật trạng thái thanh toán của một đơn hàng.
     */
    public void updatePaymentStatus(OrderEntity order, boolean isPaid) {
        executor.execute(() -> {
            String status = isPaid ? "PAID" : "UNPAID";
            long updatedAt = System.currentTimeMillis();
            orderDao.updatePaymentStatus(order.id, status, updatedAt);
        });
    }

    /**
     * Xoá một đơn hàng (các item sẽ bị xoá theo nhờ CASCADE).
     */
    public void deleteOrder(OrderEntity order) {
        executor.execute(() -> orderDao.deleteOrder(order));
    }

    public LiveData<Integer> getPaidOrderCount() {
        return orderDao.getPaidOrderCount();
    }
}