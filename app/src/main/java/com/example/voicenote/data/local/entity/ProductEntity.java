// File: com/example/voicenote/data/local/entity/ProductEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Quản lý danh mục sản phẩm (thay thế cho QuickItemEntity)
 */
@Entity(tableName = "product")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id; // Dùng long cho PK auto-generate

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "price")
    public long price;

    // --- Các trường này dùng cho UI (SaleActivity), không lưu vào DB ---
    @Ignore public int selected = 0;
    @Ignore public boolean showRemove = false;
    // ---

    public ProductEntity() {}

    // Constructor để code dễ đọc hơn
    @Ignore
    public ProductEntity(String name, long price) {
        this.name = name;
        this.price = price;
    }
}