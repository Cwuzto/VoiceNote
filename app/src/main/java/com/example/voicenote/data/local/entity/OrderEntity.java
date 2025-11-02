// File: com/example/voicenote/data/local/entity/OrderEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Quản lý đơn hàng (thay thế cho InvoiceEntity)
 */
@Entity(
        tableName = "order",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "seller_id",
                onDelete = ForeignKey.SET_NULL // Nếu xoá User, giữ lại Order
        ),
        indices = {@Index("seller_id")}
)
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id; // Dùng long cho PK auto-generate

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "total_amount")
    public long totalAmount;

    @ColumnInfo(name = "payment_method")
    public String paymentMethod; // "CASH" / "QR"

    @ColumnInfo(name = "status")
    public String status; // "PAID" / "UNPAID"

    @ColumnInfo(name = "seller_id")
    public Long sellerId; // (FK -> User.id), dùng Long (nullable)

    @ColumnInfo(name = "customer_name")
    public String customerName;

    @ColumnInfo(name = "sync_status")
    public String syncStatus; // "PENDING", "SYNCED", "FAILED"

    public OrderEntity() {}
}