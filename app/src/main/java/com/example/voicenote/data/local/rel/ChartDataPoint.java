// File: com/example/voicenote/data/local/rel/ChartDataPoint.java
package com.example.voicenote.data.local.rel;

import androidx.room.ColumnInfo;

// POJO để chứa kết quả query cho biểu đồ
public class ChartDataPoint {

    // Mốc thời gian (đã được làm tròn về 00:00 của ngày)
    @ColumnInfo(name = "day_millis")
    public long dayMillis;

    // Tổng doanh thu của ngày đó
    @ColumnInfo(name = "day_revenue")
    public long dayRevenue;
}