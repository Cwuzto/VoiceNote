// File: com/example/voicenote/data/local/entity/StoreEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng lưu thông tin cửa hàng
 */
@Entity(
        tableName = "store",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "owner_id",
                onDelete = ForeignKey.CASCADE // Nếu xoá Owner, xoá luôn Store
        ),
        indices = {@Index(value = "owner_id", unique = true)} // 1 Owner chỉ có 1 Store (tạm thời)
)
public class StoreEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "name")
    public String name; // Tên cửa hàng

    @ColumnInfo(name = "address")
    public String address; // (nullable)

    @ColumnInfo(name = "owner_id")
    public long ownerId; // (FK -> users.id)

    public StoreEntity() {}
}