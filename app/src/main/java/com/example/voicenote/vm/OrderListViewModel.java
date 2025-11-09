// File: com/example/voicenote/vm/OrderListViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.rel.OrderHeaderItem;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.data.repo.OrderRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ViewModel cho màn danh sách hoá đơn (Order), có lọc theo từ khoá.
 * (Đã refactor từ InvoiceListViewModel)
 */
public class OrderListViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    // Nguồn dữ liệu gốc từ Room
    private final LiveData<List<OrderWithItems>> allOrders;

    // LiveData này sẽ chứa List<Object> (gồm Header và Item)
    private final MediatorLiveData<List<Object>> groupedAndFilteredOrders = new MediatorLiveData<>();
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

        // Khi 'allOrders' thay đổi, chạy lại logic lọc VÀ NHÓM
        groupedAndFilteredOrders.addSource(allOrders, list -> {
            groupedAndFilteredOrders.setValue(
                    applyFilterAndGrouping(list, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate)
            );
        });
    }

    /**
     * [SỬA] Fragment sẽ observe LiveData này
     */
    public LiveData<List<Object>> getGroupedOrders() {
        return groupedAndFilteredOrders;
    }

    /**
     * Lọc theo từ khoá, kiểm tra tên khách và tên từng món trong hoá đơn.
     */
    public void filterOrders(String keyword) {
        currentKeyword = keyword != null ? keyword.trim() : "";
        List<OrderWithItems> source = allOrders.getValue();
        groupedAndFilteredOrders.setValue(
                applyFilterAndGrouping(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate)
        );
    }

    /**
     *  Lọc theo trạng thái (chip)
     */
    public void setStatusFilter(String status) {
        currentStatusFilter = status;
        List<OrderWithItems> source = allOrders.getValue();
        groupedAndFilteredOrders.setValue(
                applyFilterAndGrouping(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate)
        );
    }

    /**
     *  Set khoảng thời gian lọc (từ mili-giây)
     */
    public void setDateRange(long startTime, long endTime) {
        filterStartDate = startTime;
        filterEndDate = endTime;
        List<OrderWithItems> source = allOrders.getValue();
        groupedAndFilteredOrders.setValue(
                applyFilterAndGrouping(source, currentKeyword, currentStatusFilter, filterStartDate, filterEndDate)
        );
    }

    /**
     * Cập nhật trạng thái "đã thanh toán"
     */
    public void updatePaymentStatus(OrderEntity order, boolean isPaid) {
        repository.updatePaymentStatus(order, isPaid);
    }

    // ----------------- Helpers -----------------

    // Cập nhật hàm applyFilter
    private List<Object> applyFilterAndGrouping(List<OrderWithItems> src, String keyword, String status, long startTime, long endTime) {
        if (src == null) return new ArrayList<>();

        // 1. Lọc theo Trạng thái trước
        List<OrderWithItems> filteredList = new ArrayList<>();
        if ("ALL".equals(status)) {
            filteredList.addAll(src); // Lấy tất cả
        } else {
            for (OrderWithItems ivw : src) {
                if (ivw.order != null && status.equals(ivw.order.status)) {
                    filteredList.add(ivw); // Chỉ lấy trạng thái UNPAID/PAID
                }
            }
        }

        // 2. Lọc theo Thời gian
        List<OrderWithItems> timeFilteredList = new ArrayList<>();
        if (startTime == 0) {
            timeFilteredList.addAll(filteredList); // Không lọc thời gian
        } else {
            for (OrderWithItems ivw : filteredList) {
                if (ivw.order != null && ivw.order.createdAt >= startTime && ivw.order.createdAt <= endTime) {
                    timeFilteredList.add(ivw);
                }
            }
        }

        // 3. Lọc theo Từ khoá (lọc trên danh sách 2)
        List<OrderWithItems> keywordFilteredList = new ArrayList<>();
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());

        if (q.isEmpty()) {
            keywordFilteredList.addAll(timeFilteredList); // Không có từ khoá
        } else {
            for (OrderWithItems ivw : timeFilteredList) {
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
                if (match) keywordFilteredList.add(ivw);
            }
        }
        // Nhóm danh sách (danh sách cuối cùng đã lọc)
        return groupFilteredList(keywordFilteredList);
    }
    /**
     * [MỚI] Hàm này nhận danh sách đã lọc và chèn các Header vào
     */
    private List<Object> groupFilteredList(List<OrderWithItems> filteredList) {
        List<Object> displayList = new ArrayList<>();
        if (filteredList == null || filteredList.isEmpty()) {
            return displayList;
        }

        // Dùng LinkedHashMap để giữ đúng thứ tự các ngày
        Map<Long, List<OrderWithItems>> groupedMap = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();

        // 1. Nhóm đơn hàng theo ngày (chuẩn hóa về 00:00)
        for (OrderWithItems orderItem : filteredList) {
            cal.setTimeInMillis(orderItem.order.createdAt);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayKey = cal.getTimeInMillis();

            groupedMap.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(orderItem);
        }

        // 2. Tạo danh sách List<Object> cuối cùng
        for (Map.Entry<Long, List<OrderWithItems>> entry : groupedMap.entrySet()) {
            long dayKey = entry.getKey();
            List<OrderWithItems> dayOrders = entry.getValue();

            // Tính tổng tiền của ngày
            long dayTotal = 0;
            for (OrderWithItems order : dayOrders) {
                dayTotal += order.order.totalAmount;
            }

            // Thêm Header
            displayList.add(new OrderHeaderItem(dayKey, dayTotal));

            // Thêm tất cả đơn hàng của ngày đó
            displayList.addAll(dayOrders);
        }

        return displayList;
    }
}