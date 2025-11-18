// File: com/example/voicenote/data/local/dao/OrderDao.java
package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.rel.ChartDataPoint;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.data.local.rel.RevenueSummary;

import java.util.List;

/**
 * DAO cho Order (thay thế InvoiceDao)
 */
@Dao
public interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrder(OrderEntity order);

    @Transaction
    @Query("SELECT * FROM `order` ORDER BY created_at DESC")
    LiveData<List<OrderWithItems>> getOrdersWithItems();

    @Transaction
    @Query("SELECT * FROM `order` WHERE id = :orderId LIMIT 1")
    LiveData<OrderWithItems> getOrderById(long orderId);

    @Delete
    void deleteOrder(OrderEntity order);

    // Query để cập nhật status (ví dụ)
    @Query("UPDATE `order` SET status = :status, updated_at = :updatedAt WHERE id = :orderId")
    void updatePaymentStatus(long orderId, String status, long updatedAt);

    /**
     * Lấy tổng doanh thu và số đơn (chỉ tính đơn ĐÃ TRẢ TIỀN) Kể từ startTime
     */
    @Query("SELECT SUM(total_amount) as total_revenue, COUNT(id) as order_count " +
            "FROM `order` WHERE status = 'PAID' AND created_at >= :startTime")
    LiveData<RevenueSummary> getRevenueSummary(long startTime);

    /**
     * Lấy dữ liệu doanh thu (đã trả) nhóm theo NGÀY
     */
    @Query("SELECT (created_at / 86400000) * 86400000 as day_millis, SUM(total_amount) as day_revenue " +
            "FROM `order` " +
            "WHERE status = 'PAID' AND created_at >= :startTime " +
            "GROUP BY day_millis " +
            "ORDER BY day_millis ASC")
    LiveData<List<ChartDataPoint>> getChartData(long startTime);

    /**
     * Lấy dữ liệu doanh thu (đã trả) nhóm theo GIỜ
     * (Chỉ dùng cho "Hôm nay" và "Hôm qua")
     */
    @Query("SELECT (created_at / 3600000) * 3600000 as day_millis, SUM(total_amount) as day_revenue " +
            "FROM `order` " +
            "WHERE status = 'PAID' AND created_at >= :startTime " +
            "GROUP BY day_millis " +
            "ORDER BY day_millis ASC")
    LiveData<List<ChartDataPoint>> getChartDataHourly(long startTime);

    /**
     * Đếm tổng số đơn đã thanh toán (để kiểm tra empty state)
     */
    @Query("SELECT COUNT(id) FROM `order` WHERE status = 'PAID'")
    LiveData<Integer> getPaidOrderCount();

    /**
     * Thêm :endTime vào TẤT CẢ các query
     */
    @Query("SELECT SUM(total_amount) as total_revenue, COUNT(id) as order_count " +
            "FROM `order` WHERE status = 'PAID' AND created_at >= :startTime AND created_at <= :endTime")
    LiveData<RevenueSummary> getRevenueSummary(long startTime, long endTime);

    @Query("SELECT (created_at / 86400000) * 86400000 as day_millis, SUM(total_amount) as day_revenue " +
            "FROM `order` " +
            "WHERE status = 'PAID' AND created_at >= :startTime AND created_at <= :endTime " +
            "GROUP BY day_millis " +
            "ORDER BY day_millis ASC")
    LiveData<List<ChartDataPoint>> getChartData(long startTime, long endTime);

    @Query("SELECT (created_at / 3600000) * 3600000 as day_millis, SUM(total_amount) as day_revenue " +
            "FROM `order` " +
            "WHERE status = 'PAID' AND created_at >= :startTime AND created_at <= :endTime " +
            "GROUP BY day_millis " +
            "ORDER BY day_millis ASC")
    LiveData<List<ChartDataPoint>> getChartDataHourly(long startTime, long endTime);
}