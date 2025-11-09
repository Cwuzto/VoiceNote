// File: com/example/voicenote/data/local/rel/OrderHeaderItem.java
package com.example.voicenote.data.local.rel;

/**
 * Đây là một POJO (Model) đơn giản
 * chỉ để chứa dữ liệu cho dòng Header (Ngày + Tổng tiền) trong danh sách Order
 */
public class OrderHeaderItem {

    // Mốc thời gian của ngày (đã được chuẩn hoá về 00:00)
    public long dateMillis;

    // Tổng tiền của tất cả đơn hàng trong ngày đó
    public long dayTotal;

    public OrderHeaderItem(long dateMillis, long dayTotal) {
        this.dateMillis = dateMillis;
        this.dayTotal = dayTotal;
    }
}