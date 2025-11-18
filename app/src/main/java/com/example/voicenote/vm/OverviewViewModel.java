// File: com/example/voicenote/vm/OverviewViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import androidx.core.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.voicenote.data.local.rel.BestSellerItem;
import com.example.voicenote.data.local.rel.ChartDataPoint;
import com.example.voicenote.data.local.rel.RevenueSummary;
import com.example.voicenote.data.repo.OrderRepository;
import java.util.Calendar;
import java.util.List;

public class OverviewViewModel extends AndroidViewModel {

    private final OrderRepository repository;
    // Biến theo dõi thời gian
    // Pair<startTime, endTime>
    private final MutableLiveData<Pair<Long, Long>> revenueTimeRange = new MutableLiveData<>();
    private final MutableLiveData<Pair<Long, Long>> chartTimeRange = new MutableLiveData<>();
    private final MutableLiveData<Pair<Long, Long>> bestSellerTimeRange = new MutableLiveData<>();
    // 3 LiveData dữ liệu, mỗi cái lắng nghe 1 timeRange riêng
    private final LiveData<RevenueSummary> revenueSummary;
    private final LiveData<List<ChartDataPoint>> chartData;
    private final LiveData<List<BestSellerItem>> bestSellers;
    private final LiveData<Integer> paidOrderCount;
    // Key để biết biểu đồ đang lọc theo Ngày hay Giờ
    private String chartRangeKey = "THIS_MONTH";

    public OverviewViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        // Lấy tổng số đơn
        paidOrderCount = repository.getPaidOrderCount();

        // Mặc định cả 3 là "Tháng này"
        Pair<Long, Long> thisMonthRange = calculateTimeRange("THIS_MONTH");
        revenueTimeRange.setValue(thisMonthRange);
        chartTimeRange.setValue(thisMonthRange);
        bestSellerTimeRange.setValue(thisMonthRange);

        // --- Kết nối các luồng (switchMap) ---
        revenueSummary = Transformations.switchMap(revenueTimeRange, range ->
                repository.getRevenueSummary(range.first, range.second) // Truyền 2 tham số
        );

        // BestSellers (mặc định) sẽ lấy theo SỐ LƯỢNG
        bestSellers = Transformations.switchMap(bestSellerTimeRange, range ->
                repository.getBestSellersByQuantity(range.first, range.second)
        );

        chartData = Transformations.switchMap(chartTimeRange, range -> {
            // "TODAY" dùng query theo giờ, còn lại dùng query theo ngày
            if ("TODAY".equals(chartRangeKey) || "YESTERDAY".equals(chartRangeKey)) {
                return repository.getChartDataHourly(range.first, range.second); // Truyền 2 tham số
            } else {
                return repository.getChartData(range.first, range.second); // Truyền 2 tham số
            }
        });
    }

    // --- Getters cho Fragment ---
    public LiveData<RevenueSummary> getRevenueSummary() {
        return revenueSummary;
    }
    public LiveData<List<BestSellerItem>> getBestSellers() {
        // Lọc 3 item đầu tiên cho Overview
        return Transformations.map(bestSellers, list -> {
            if (list == null) return null;
            if (list.size() > 3) {
                return list.subList(0, 3);
            }
            return list;
        });
    }
    public LiveData<List<ChartDataPoint>> getChartData() { return chartData; }

    public LiveData<Integer> getPaidOrderCount() {
        return paidOrderCount;
    }

    /**
     * getter Cho Fragment biết khoảng thời gian của biểu đồ
     */
    public LiveData<Pair<Long, Long>> getChartTimeRange() {
        return chartTimeRange;
    }

    /** Setters cho Fragment */
    /**
     * Dùng cho ô "Doanh thu tháng này"
     */
    public void setRevenueTimeRange(String rangeKey) {
        revenueTimeRange.setValue(calculateTimeRange(rangeKey));
    }
    public void setRevenueTimeRangeCustom(long startTime, long endTime) {
        revenueTimeRange.setValue(new Pair<>(startTime, endTime));
    }

    /**
     * Dùng cho "Biểu đồ"
     */
    public void setChartTimeRange(String rangeKey) {
        this.chartRangeKey = rangeKey; // Lưu lại key
        chartTimeRange.setValue(calculateTimeRange(rangeKey));
    }
    public void setChartTimeRangeCustom(long startTime, long endTime) {
        this.chartRangeKey = "CUSTOM"; // Lưu lại key
        chartTimeRange.setValue(new Pair<>(startTime, endTime));
    }

    /**
     * Dùng cho "Hàng bán chạy"
     */
    public void setBestSellerTimeRange(String rangeKey) {
        bestSellerTimeRange.setValue(calculateTimeRange(rangeKey));
    }
    public void setBestSellerTimeRangeCustom(long startTime, long endTime) {
        bestSellerTimeRange.setValue(new Pair<>(startTime, endTime));
    }

    /**
     * Tính toán startTime dựa trên Key, hàm này sẽ trả về 1 Pair (Start/End)
     */
    private Pair<Long, Long> calculateTimeRange(String rangeKey) {
        Calendar cal = Calendar.getInstance();
        long startTime, endTime;

        // Set endTime về 23:59:59 của ngày HÔM NAY
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endTime = cal.getTimeInMillis();

        // Set startTime về 00:00:00 của ngày HÔM NAY
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTimeInMillis();

        switch(rangeKey) {
            case "TODAY":
                // Đã set (Hôm nay 00:00 -> Hôm nay 23:59)
                break;
            case "YESTERDAY":
                cal.add(Calendar.DAY_OF_YEAR, -1); // Về 00:00 hôm qua
                startTime = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 23); // Về 23:59 hôm qua
                cal.set(Calendar.MINUTE, 59);
                endTime = cal.getTimeInMillis();
                break;
            case "7DAYS":
                cal.add(Calendar.DAY_OF_YEAR, -6); // Về 00:00 của 6 ngày trước
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            case "THIS_MONTH":
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            case "LAST_MONTH":
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1 tháng này
                cal.add(Calendar.MILLISECOND, -1); // Về 23:59 ngày cuối tháng TRƯỚC
                endTime = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1 tháng TRƯỚC
                startTime = cal.getTimeInMillis();
                break;
            case "THIS_YEAR":
                cal.set(Calendar.DAY_OF_YEAR, 1); // Về 00:00 ngày 1/1
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            default: // "ALL"
                startTime = 0;
                endTime = System.currentTimeMillis(); // (Mặc dù query ko dùng)
                break;
        }
        return new Pair<>(startTime, endTime);
    }
}