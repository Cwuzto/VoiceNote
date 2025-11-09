// File: com/example/voicenote/vm/OrderListViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.data.repo.OrderRepository;

import java.util.Calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel cho màn danh sách hoá đơn (Order), có lọc theo từ khoá.
 * (Đã refactor từ InvoiceListViewModel)
 */
public class OrderListViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    // Nguồn dữ liệu gốc từ Room
    private final LiveData<List<OrderWithItems>> allOrders;

    // Dữ liệu đã lọc mà UI lắng nghe
    private final MediatorLiveData<List<OrderWithItems>> filteredOrders = new MediatorLiveData<>();

    // Từ khoá hiện tại
    private String currentKeyword = "";
    private String currentStatusFilter = "ALL"; // Thêm bộ lọc trạng thái
    // Thêm biến lọc thời gian
    private long filterStartDate = 0;
    private long filterEndDate = 0;

    public OrderListViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app);

        // Lấy dữ liệu gốc từ Repository (Room)
        allOrders = repository.getOrdersWithItems();

        // Gắn nguồn vào Mediator
        filteredOrders.addSource(allOrders, list -> {
            // [SỬA] Áp dụng cả 3 bộ lọc
            filteredOrders.setValue(applyFilter(list, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate));
        });
    }

    /**
     * UI lắng nghe danh sách đã lọc.
     */
    public LiveData<List<OrderWithItems>> getAllOrders() { // [SỬA]
        return filteredOrders;
    }

    /**
     * Lọc theo từ khoá, kiểm tra tên khách và tên từng món trong hoá đơn.
     */
    public void filterOrders(String keyword) {
        currentKeyword = keyword != null ? keyword.trim() : "";
        List<OrderWithItems> source = allOrders.getValue();
        //  Áp dụng cả 3 bộ lọc
        filteredOrders.setValue(applyFilter(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate));
    }

    /**
     * [MỚI] Lọc theo trạng thái (chip)
     */
    public void setStatusFilter(String status) {
        currentStatusFilter = status;
        List<OrderWithItems> source = allOrders.getValue();
        // Áp dụng cả 2 bộ lọc
        filteredOrders.setValue(applyFilter(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate));
    }

    /**
     * [MỚI] Set khoảng thời gian lọc (từ mili-giây)
     */
    public void setDateRange(long startTime, long endTime) {
        filterStartDate = startTime;
        filterEndDate = endTime;
        List<OrderWithItems> source = allOrders.getValue();
        filteredOrders.setValue(applyFilter(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate));
    }

    /**
     * Cập nhật trạng thái "đã thanh toán"
     */
    public void updatePaymentStatus(OrderEntity order, boolean isPaid) {
        repository.updatePaymentStatus(order, isPaid);
    }

    // ----------------- Helpers -----------------

    // Cập nhật hàm applyFilter
    private List<OrderWithItems> applyFilter(List<OrderWithItems> src, String keyword, String status, long startTime, long endTime) {
        if (src == null) return new ArrayList<>();

        // 1. Lọc theo Trạng thái trước
        List<OrderWithItems> statusFilteredList = new ArrayList<>();
        if ("ALL".equals(status)) {
            statusFilteredList.addAll(src); // Lấy tất cả
        } else {
            for (OrderWithItems ivw : src) {
                if (ivw.order != null && status.equals(ivw.order.status)) {
                    statusFilteredList.add(ivw); // Chỉ lấy trạng thái UNPAID/PAID
                }
            }
        }

        // 2. Lọc theo Thời gian
        List<OrderWithItems> timeFilteredList = new ArrayList<>();
        if (startTime == 0) {
            timeFilteredList.addAll(statusFilteredList); // Không lọc thời gian
        } else {
            for (OrderWithItems ivw : statusFilteredList) {
                if (ivw.order != null && ivw.order.createdAt >= startTime && ivw.order.createdAt <= endTime) {
                    timeFilteredList.add(ivw);
                }
            }
        }

        // 3. Lọc theo Từ khoá
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());
        if (q.isEmpty()) {
            return timeFilteredList; // Trả về ds đã lọc status & time
        }

        List<OrderWithItems> outPutList = new ArrayList<>();
        for (OrderWithItems ivw : statusFilteredList) { // Lọc trên ds đã lọc status
            boolean match = false;
            if (ivw.order != null && ivw.order.customerName != null) {
                if (ivw.order.customerName.toLowerCase(Locale.getDefault()).contains(q)) {
                    match = true;
                }
            }
            if (!match && ivw.orderItems != null) {
                for (int i = 0; i < ivw.orderItems.size(); i++) {
                    if (ivw.orderItems.get(i).productName != null &&
                            ivw.orderItems.get(i).productName.toLowerCase(Locale.getDefault()).contains(q)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) outPutList.add(ivw);
        }
        return outPutList;
    }
}