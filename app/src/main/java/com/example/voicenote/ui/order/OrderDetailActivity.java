// File: com/example/voicenote/ui/order/OrderDetailActivity.java
package com.example.voicenote.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderWithItems;
import com.example.voicenote.ui.sale.SaleActivity;
import com.example.voicenote.vm.OrderDetailViewModel;

import org.jspecify.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private OrderDetailViewModel viewModel;

    private TextView tvCustomer, tvDate, tvSubtotal, tvTotal;
    private CheckBox cbPaid;
    private LinearLayout containerItems;
    private LinearLayout btnPaid;
    private OrderWithItems currentOrder;
    private ImageButton btnEdit;
    private LinearLayout btnNewOrder, btnShare, btnPrint;
    private WebView webViewForPrinting; // Dùng để In
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        long orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            finish(); // Không có ID thì đóng lại
            return;
        }

        // --- Khởi tạo ViewModel ---
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        // --- Ánh xạ View ---
        tvCustomer = findViewById(R.id.tvCustomer);
        tvTotal = findViewById(R.id.tvTotal);
        cbPaid = findViewById(R.id.cbPaid);
        btnPaid = findViewById(R.id.btnPaid);
        tvDate = findViewById(R.id.tvDate);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        containerItems = findViewById(R.id.containerItems);
        btnEdit = findViewById(R.id.btnEdit);

        // Ánh xạ 3 nút dưới
        btnNewOrder = findViewById(R.id.btnNewOrder);
        btnShare = findViewById(R.id.btnShare);
        btnPrint = findViewById(R.id.btnPrint);

        // --- Lấy ViewModel và Quan sát Dữ liệu ---
        viewModel.getOrderById(orderId).observe(this, orderWithItems -> {
            if (orderWithItems != null && orderWithItems.order != null) {
                this.currentOrder = orderWithItems; // Lưu lại đơn hàng hiện tại
                populateData(orderWithItems); // Gọi hàm để điền dữ liệu
            }
        });

        // Thêm OnClickListener vào layout cha
        btnPaid.setOnClickListener(v -> {
            if (this.currentOrder == null || this.currentOrder.order == null) {
                // Dữ liệu chưa tải xong, không làm gì cả
                return;
            }

            // Lấy trạng thái hiện tại
            OrderEntity current = this.currentOrder.order; // Lấy order từ biến đã lưu

            // Nếu đã paid (bị khóa) thì không làm gì
            if ("PAID".equals(current.status)) return;

            // Hỏi xác nhận
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

        findViewById(R.id.tvClose).setOnClickListener(v -> finish()); // Nút đóng
        btnEdit.setOnClickListener(v -> openEditMode()); // Nút sửa

        // Gán listener cho 3 nút
        btnNewOrder.setOnClickListener(v -> {
            startActivity(new Intent(OrderDetailActivity.this, SaleActivity.class));
            finish();
        });

        btnShare.setOnClickListener(v -> {
            shareOrder(); // Gọi hàm chia sẻ
        });

        btnPrint.setOnClickListener(v -> {
            printOrder(); // Gọi hàm in
        });
    }

    /**
     * Gửi dữ liệu đơn hàng sang SaleActivity
     */
    private void openEditMode() {
        if (currentOrder == null || currentOrder.order == null) return;

        Intent intent = new Intent(this, SaleActivity.class);

        // Gửi ID của đơn hàng đang sửa
        intent.putExtra("EDIT_ORDER_ID", currentOrder.order.id);

        // Gửi Tên khách
        intent.putExtra("CUSTOMER_NAME", currentOrder.order.customerName);

        // Gửi thời gian tạo đơn
        intent.putExtra("CREATED_AT", currentOrder.order.createdAt);

        // Gửi danh sách món (đã làm Parcelable)
        intent.putParcelableArrayListExtra(
                "ORDER_ITEMS",
                new ArrayList<>(currentOrder.orderItems)
        );

        startActivity(intent);
        finish(); // Đóng màn hình Detail
    }

    /**
     * Tạo tóm tắt đơn hàng và gọi Intent.ACTION_SEND
     */
    private void shareOrder() {
        if (currentOrder == null) {
            Toast.makeText(this, "Chưa tải xong đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo nội dung Text (dùng hàm helper)
        String summary = buildOrderSummaryText();

        // 2. Tạo Intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, summary);
        sendIntent.setType("text/plain");

        // 3. Hiển thị cửa sổ chọn App
        Intent shareIntent = Intent.createChooser(sendIntent, "Chia sẻ hóa đơn qua");
        startActivity(shareIntent);
    }

    /**
     * In hóa đơn (chuyển thành HTML và dùng PrintManager)
     */
    private void printOrder() {
        if (currentOrder == null) {
            Toast.makeText(this, "Chưa tải xong đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo một WebView (ẩn)
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // 3. Khi HTML đã tải xong, gọi PrintManager
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                String jobName = "HoaDon_" + currentOrder.order.id;
                PrintDocumentAdapter printAdapter = view.createPrintDocumentAdapter(jobName);

                // 4. Mở cửa sổ In của Android (cho phép in Wifi hoặc Save as PDF)
                printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                webViewForPrinting = null; // Xóa tham chiếu
            }
        });

        // 2. Chuyển tóm tắt đơn hàng thành HTML
        String htmlDocument = buildOrderSummaryHtml();
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Giữ tham chiếu đến WebView (để nó không bị GC)
        webViewForPrinting = webView;
    }

    /**
     * Hàm helper tạo nội dung Text (cho Share)
     */
    private String buildOrderSummaryText() {
        StringBuilder summary = new StringBuilder();
        summary.append("--- HÓA ĐƠN ---\n");
        summary.append("Khách hàng: ").append(currentOrder.order.customerName).append("\n");
        summary.append("Thời gian: ").append(tvDate.getText().toString()).append("\n");
        summary.append("----------------\n");

        for (OrderItemEntity item : currentOrder.orderItems) {
            summary.append(String.format(Locale.US, "%d x %s = %,d\n",
                    item.quantity,
                    item.productName,
                    (item.quantity * item.unitPrice)
            ));
            if (item.note != null && !item.note.isEmpty()) {
                summary.append("  (Ghi chú: ").append(item.note).append(")\n");
            }
        }

        summary.append("----------------\n");
        summary.append("TỔNG CỘNG: ").append(String.format(Locale.US, "%,dđ", currentOrder.order.totalAmount));
        return summary.toString();
    }

    /**
     * Hàm helper tạo nội dung HTML (cho Print)
     */
    private String buildOrderSummaryHtml() {
        // (Đây là code HTML cơ bản, bạn có thể CSS tùy ý)
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>body{font-family:sans-serif; padding:10px;}");
        html.append("table{width:100%; border-collapse:collapse;}");
        html.append("th, td{border-bottom:1px solid #ddd; padding:8px; text-align:left;}");
        html.append("th{background-color:#f2f2f2;}");
        html.append(".total{font-weight:bold; font-size:1.2em;}");
        html.append("</style></head><body>");

        html.append("<h2>Hóa Đơn Bán Hàng</h2>");
        html.append("<p>Khách hàng: <b>").append(currentOrder.order.customerName).append("</b></p>");
        html.append("<p>Thời gian: ").append(tvDate.getText().toString()).append("</p>");

        html.append("<table>");
        html.append("<tr><th>Món</th><th>SL</th><th>Ghi chú</th><th>Thành tiền</th></tr>");

        for (OrderItemEntity item : currentOrder.orderItems) {
            html.append("<tr>");
            html.append("<td>").append(item.productName).append("</td>");
            html.append("<td>").append(item.quantity).append("</td>");
            html.append("<td>").append(item.note != null ? item.note : "").append("</td>");
            html.append("<td>").append(String.format(Locale.US, "%,d", item.quantity * item.unitPrice)).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("<p class='total'>TỔNG CỘNG: ").append(String.format(Locale.US, "%,dđ", currentOrder.order.totalAmount)).append("</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Hàm điền toàn bộ dữ liệu thật vào View
     */
    private void populateData(@NonNull OrderWithItems orderWithItems) {
        OrderEntity order = orderWithItems.order;

        // Điền thông tin cơ bản
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

        // Ẩn nút "Sửa" nếu đã thanh toán
        if (isPaid) {
            btnEdit.setVisibility(View.GONE);
        } else {
            btnEdit.setVisibility(View.VISIBLE);
        }

        // Điền danh sách món hàng
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
                tvItemNote.setText(item.note); // tvItemNote.setText("Ghi chú: " + item.note);
                tvItemNote.setVisibility(View.VISIBLE);
            } else {
                tvItemNote.setVisibility(View.GONE);
            }

            containerItems.addView(itemView);
        }
    }
}