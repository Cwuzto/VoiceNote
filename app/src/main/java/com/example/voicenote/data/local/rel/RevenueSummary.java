// File: com/example/voicenote/data/local/rel/RevenueSummary.java
package com.example.voicenote.data.local.rel;

import androidx.room.ColumnInfo;

// POJO để chứa kết quả từ query
public class RevenueSummary {
    @ColumnInfo(name = "total_revenue")
    public long totalRevenue;

    @ColumnInfo(name = "order_count")
    public int orderCount;
}