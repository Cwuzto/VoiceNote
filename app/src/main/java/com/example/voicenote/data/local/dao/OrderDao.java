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
import com.example.voicenote.data.local.rel.OrderWithItems;

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

    // [MỚI] Query để cập nhật status (ví dụ)
    @Query("UPDATE `order` SET status = :status, updated_at = :updatedAt WHERE id = :orderId")
    void updatePaymentStatus(long orderId, String status, long updatedAt);
}