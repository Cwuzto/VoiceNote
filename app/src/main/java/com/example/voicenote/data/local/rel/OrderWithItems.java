// File: com/example/voicenote/data/local/rel/OrderWithItems.java
package com.example.voicenote.data.local.rel;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.entity.UserEntity;

import java.util.List;

/**
 * Relation model joining Order (1) and its OrderItems (n).
 * (Thay thế cho InvoiceWithLines)
 */
public class OrderWithItems {

    @Embedded
    public OrderEntity order;

    // Thêm liên kết với User (Seller)
    @Relation(
            parentColumn = "seller_id",
            entityColumn = "id"
    )
    public UserEntity seller; // Room sẽ tự động lấy User có id = seller_id

    @Relation(
            parentColumn = "id",
            entityColumn = "order_id"
    )
    public List<OrderItemEntity> orderItems;
}