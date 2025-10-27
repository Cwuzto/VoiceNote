package com.example.voicenote.data.local.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * EN: Quick item now includes `initial` to match your chip UI.
 * VI: Quick item có thêm `initial` đúng với UI chip của bạn.
 */
@Entity(tableName = "quick_items")
public class QuickItemEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;


    @ColumnInfo(name = "name")
    public String name;


    @ColumnInfo(name = "initial")
    public String initial; // e.g. PB, BB


    @ColumnInfo(name = "price")
    public long price;


    public QuickItemEntity() {}


    public QuickItemEntity(String name, String initial, long price) {
        this.name = name;
        this.initial = initial;
        this.price = price;
    }
}