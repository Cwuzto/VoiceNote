// File: com/example/voicenote/data/local/entity/QuickItemEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * EN: Quick item entity for the 4-column grid. Added @Ignore fields for UI state.
 * VI: Entity món nhanh cho lưới 4 cột. Thêm field @Ignore phục vụ trạng thái UI.
 */
@Entity(tableName = "quick_items")
public class QuickItemEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "initial")
    public String initial; // e.g. "PB"

    @ColumnInfo(name = "price")
    public long price;

    @Ignore public int selected = 0;     // số lần đã chọn (badge)
    @Ignore public boolean showRemove = false; // hiển thị dấu trừ để xoá

    public QuickItemEntity() {}

    public QuickItemEntity(String name, String initial, long price) {
        this.name = name;
        this.initial = initial;
        this.price = price;
    }
}
