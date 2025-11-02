package com.example.voicenote.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.OrderItemEntity;

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
}