// File: com/example/voicenote/ui/order/OrderDetailActivity.java
package com.example.voicenote.ui.order; // [SỬA] Package

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
// [SỬA] Import entity và viewmodel mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.ui.sale.SaleActivity;
import com.example.voicenote.vm.OrderDetailViewModel;

import org.jspecify.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/** * Activity chi tiết Order (đã refactor từ InvoiceDetailActivity) 
 */
public class OrderDetailActivity extends AppCompatActivity {
    private OrderDetailViewModel viewModel;

    // [MỚI] Khai báo tất cả View
    private TextView tvCustomer, tvDate, tvSubtotal, tvTotal;
    private CheckBox cbPaid;
    private LinearLayout containerItems;
    private LinearLayout btnPaid; //layout cha của checkbox
    private OrderWithItems currentOrder; // Biến này được Observer gán
    private ImageButton btnEdit;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        long orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            finish(); // Không có ID thì đóng lại
            return;
        }

        // --- Ánh xạ View ---
        tvCustomer = findViewById(R.id.tvCustomer);
        tvTotal = findViewById(R.id.tvTotal);
        cbPaid = findViewById(R.id.cbPaid);
        btnPaid = findViewById(R.id.btnPaid);
        tvDate = findViewById(R.id.tvDate);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        containerItems = findViewById(R.id.containerItems);
        btnEdit = findViewById(R.id.btnEdit);

        // --- Lấy ViewModel và Quan sát Dữ liệu ---
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);
        viewModel.getOrderById(orderId).observe(this, orderWithItems -> {
            if (orderWithItems != null && orderWithItems.order != null) {
                this.currentOrder = orderWithItems; // Lưu lại đơn hàng hiện tại
                populateData(orderWithItems); // Gọi hàm để điền dữ liệu
            }
        });

        // [MỚI] Thêm OnClickListener vào layout cha
        btnPaid.setOnClickListener(v -> {
            if (this.currentOrder == null || this.currentOrder.order == null) {
                // Dữ liệu chưa tải xong, không làm gì cả
                return;
            }

            // Lấy trạng thái hiện tại
            OrderEntity current = this.currentOrder.order; // Lấy order từ biến đã lưu

            // Nếu đã paid (bị khóa) thì không làm gì
            if ("PAID".equals(current.status)) return;

            // Yêu cầu 1: Hỏi xác nhận
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thanh toán")
                    .setMessage("Bạn có chắc chắn muốn đánh dấu đơn hàng này là ĐÃ NHẬN TIỀN?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        // Chỉ gọi ViewModel khi người dùng bấm "Xác nhận"
                        viewModel.updatePaymentStatus(current, true);
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        // Nút đóng
        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        // Nút sửa
        btnEdit.setOnClickListener(v -> openEditMode());
    }

    /**
     * [MỚI] Gửi dữ liệu đơn hàng sang SaleActivity
     */
    private void openEditMode() {
        if (currentOrder == null || currentOrder.order == null) return;

        Intent intent = new Intent(this, SaleActivity.class);

        // Gửi ID của đơn hàng đang sửa
        intent.putExtra("EDIT_ORDER_ID", currentOrder.order.id);

        // Gửi Tên khách
        intent.putExtra("CUSTOMER_NAME", currentOrder.order.customerName);

        // Gửi danh sách món (đã làm Parcelable)
        intent.putParcelableArrayListExtra(
                "ORDER_ITEMS",
                new ArrayList<>(currentOrder.orderItems)
        );

        startActivity(intent);
        finish(); // Đóng màn hình Detail
    }

    /**
     * Hàm điền toàn bộ dữ liệu thật vào View
     */
    private void populateData(@NonNull OrderWithItems orderWithItems) {
        OrderEntity order = orderWithItems.order;

        // 1. Điền thông tin cơ bản
        tvCustomer.setText(order.customerName);
        tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));
        tvSubtotal.setText(String.format(Locale.US, "%,d", order.totalAmount)); // (Tạm thời subtotal = total)
        cbPaid.setChecked("PAID".equals(order.status));

        // Định dạng ngày giờ
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(order.createdAt));

        // Logic hiển thị và Khóa Checkbox
        boolean isPaid = "PAID".equals(order.status);
        cbPaid.setChecked(isPaid);
        cbPaid.setEnabled(!isPaid); // Khóa nếu đã thanh toán
        btnPaid.setEnabled(!isPaid); // Khóa luôn cả layout cha

        // 2. Điền danh sách món hàng
        containerItems.removeAllViews(); // Xoá hết view giả (nếu có)
        LayoutInflater inflater = LayoutInflater.from(this);

        if (orderWithItems.orderItems == null || orderWithItems.orderItems.isEmpty()) {
            // Không có món nào
            TextView emptyView = new TextView(this);
            emptyView.setText("Đơn hàng không có món nào.");
            containerItems.addView(emptyView);
            return;
        }

        // Lặp qua danh sách món và thêm vào layout
        for (OrderItemEntity item : orderWithItems.orderItems) {
            View itemView = inflater.inflate(R.layout.item_order_detail_line, containerItems, false);

            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemTotalPrice = itemView.findViewById(R.id.tvItemTotalPrice);
            TextView tvItemQty = itemView.findViewById(R.id.tvItemQty);
            TextView tvItemNote = itemView.findViewById(R.id.tvItemNote);

            long lineTotal = item.unitPrice * item.quantity;

            tvItemName.setText(item.productName);
            tvItemTotalPrice.setText(String.format(Locale.US, "%,d", lineTotal));
            tvItemQty.setText(String.format(Locale.US, "%d x %,d", item.quantity, item.unitPrice));

            if (item.note != null && !item.note.isEmpty()) {
                tvItemNote.setText("Ghi chú: " + item.note);
                tvItemNote.setVisibility(View.VISIBLE);
            } else {
                tvItemNote.setVisibility(View.GONE);
            }

            containerItems.addView(itemView);
        }
    }
}