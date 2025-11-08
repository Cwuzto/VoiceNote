// File: com/example/voicenote/data/local/entity/OrderItemEntity.java
package com.example.voicenote.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;
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
public class OrderItemEntity implements Parcelable{
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

    // --- Code cho Parcelable ---
    protected OrderItemEntity(Parcel in) {
        id = in.readLong();
        orderId = in.readLong();
        productName = in.readString();
        unitPrice = in.readLong();
        quantity = in.readInt();
        note = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(orderId);
        dest.writeString(productName);
        dest.writeLong(unitPrice);
        dest.writeInt(quantity);
        dest.writeString(note);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderItemEntity> CREATOR = new Creator<OrderItemEntity>() {
        @Override
        public OrderItemEntity createFromParcel(Parcel in) {
            return new OrderItemEntity(in);
        }

        @Override
        public OrderItemEntity[] newArray(int size) {
            return new OrderItemEntity[size];
        }
    }; //end Parcelable
}