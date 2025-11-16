package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.BestSellerItem;

import java.util.List;

/**
 * DAO cho OrderItem (thay thế LineItemDao)
 */
@Dao
public interface OrderItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrderItem(OrderItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItems(List<OrderItemEntity> items);

    @Query("DELETE FROM order_item WHERE order_id = :orderId")
    void deleteByOrderId(long orderId);

    @Query("SELECT * FROM order_item WHERE order_id = :orderId ORDER BY id ASC")
    List<OrderItemEntity> getByOrderId(long orderId);

    /**
     * Lấy top 3 sản phẩm bán chạy (theo số lượng) Kể từ startTime
     */
    @Query("SELECT product_name, SUM(quantity) as total_quantity " +
            "FROM order_item " +
            "INNER JOIN `order` ON order_item.order_id = `order`.id " +
            "WHERE `order`.status = 'PAID' AND `order`.created_at >= :startTime AND `order`.created_at <= :endTime " +
            "GROUP BY product_name " +
            "ORDER BY total_quantity DESC " +
            "LIMIT 3")
    LiveData<List<BestSellerItem>> getBestSellers(long startTime, long endTime);
}