// File: com/example/voicenote/data/local/entity/OrderItemEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Quản lý chi tiết các món trong đơn hàng (thay thế cho LineItemEntity)
 */
@Entity(
        tableName = "order_item",
        foreignKeys = @ForeignKey(
                entity = OrderEntity.class,
                parentColumns = "id",
                childColumns = "order_id",
                onDelete = ForeignKey.CASCADE // Xoá Order thì xoá luôn Item
        ),
        indices = {@Index("order_id")}
)
public class OrderItemEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id; // Dùng long cho PK auto-generate

    @ColumnInfo(name = "order_id")
    public long orderId; // (FK -> Order.id)

    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "unit_price")
    public long unitPrice;

    @ColumnInfo(name = "quantity")
    public int quantity;

    @ColumnInfo(name = "note")
    public String note; // (nullable)

    public OrderItemEntity() {}
}