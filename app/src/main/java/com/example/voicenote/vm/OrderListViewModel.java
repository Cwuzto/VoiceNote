// File: com/example/voicenote/vm/OrderListViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.data.repo.OrderRepository; // [SỬA]

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel cho màn danh sách hoá đơn (Order), có lọc theo từ khoá.
 * (Đã refactor từ InvoiceListViewModel)
 */
public class OrderListViewModel extends AndroidViewModel {

    private final OrderRepository repository; // [SỬA]

    // Nguồn dữ liệu gốc từ Room
    private final LiveData<List<OrderWithItems>> allOrders; // [SỬA]

    // Dữ liệu đã lọc mà UI lắng nghe
    private final MediatorLiveData<List<OrderWithItems>> filteredOrders = new MediatorLiveData<>(); // [SỬA]

    // Từ khoá hiện tại
    private String currentKeyword = "";

    public OrderListViewModel(@NonNull Application app) {
        super(app);
        repository = new OrderRepository(app); // [SỬA]

        // Lấy dữ liệu gốc từ Repository (Room)
        allOrders = repository.getOrdersWithItems(); // [SỬA]

        // Gắn nguồn vào Mediator
        filteredOrders.addSource(allOrders, list -> {
            filteredOrders.setValue(applyFilter(list, currentKeyword));
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
    public void filterOrders(String keyword) { // [SỬA]
        currentKeyword = keyword != null ? keyword.trim() : "";
        List<OrderWithItems> source = allOrders.getValue();
        filteredOrders.setValue(applyFilter(source, currentKeyword));
    }

    /**
     * Cập nhật trạng thái "đã thanh toán"
     */
    public void updatePaymentStatus(OrderEntity order, boolean isPaid) { // [SỬA]
        repository.updatePaymentStatus(order, isPaid); // [SỬA]
    }

    // [XOÁ] Hàm setPaid cũ (thay bằng updatePaymentStatus)

    // ----------------- Helpers -----------------

    private List<OrderWithItems> applyFilter(List<OrderWithItems> sourceList, String keyword) {
        if (sourceList == null) return new ArrayList<>();
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());
        if (query.isEmpty()) {
            return new ArrayList<>(sourceList);
        }

        List<OrderWithItems> outputList = new ArrayList<>();
        for (OrderWithItems orderWithItems : sourceList) {
            boolean match = false;

            // 1) So khớp theo tên khách
            if (orderWithItems.order != null && orderWithItems.order.customerName != null) {
                if (orderWithItems.order.customerName.toLowerCase(Locale.getDefault()).contains(query)) { // [SỬA]
                    match = true;
                }
            }

            // 2) Nếu chưa khớp, so tiếp theo tên từng dòng hàng
            if (!match && orderWithItems.orderItems != null) { // [SỬA]
                for (int i = 0; i < orderWithItems.orderItems.size(); i++) {
                    if (orderWithItems.orderItems.get(i).productName != null && // [SỬA]
                            orderWithItems.orderItems.get(i).productName.toLowerCase(Locale.getDefault()).contains(query)) { // [SỬA]
                        match = true;
                        break;
                    }
                }
            }

            if (match) outputList.add(orderWithItems);
        }
        return outputList;
    }
}