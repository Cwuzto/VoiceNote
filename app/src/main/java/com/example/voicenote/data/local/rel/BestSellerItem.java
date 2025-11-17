// File: com/example/voicenote/data/local/rel/BestSellerItem.java
package com.example.voicenote.data.local.rel;

import androidx.room.ColumnInfo;

// để chứa kết quả từ query
public class BestSellerItem {
    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "total_quantity")
    public int totalQuantity;

    @ColumnInfo(name = "total_revenue") // tổng doanh thu
    public long totalRevenue;
}