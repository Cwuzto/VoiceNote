// File: com/example/voicenote/ui/invoice/adapter/OrderAdapter.java
package com.example.voicenote.ui.order.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import entity và relation mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.rel.OrderHeaderItem;
import com.example.voicenote.data.local.rel.OrderWithItems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách Order (đã refactor từ InvoiceAdapter)
 */
public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // Thêm interface mới
    public interface OnPaidChange {
        void onChange(OrderEntity order, boolean checked);
    }

    public interface OnItemClickListener {
        void onItemClick(OrderWithItems orderWithItems);
    }

    private final List<Object> data = new ArrayList<>(); // Adapter này giờ sẽ chứa List<Object>
    private final OnPaidChange onPaidChangeCallback;
    private final OnItemClickListener onItemClickCallback; // [MỚI]

    public OrderAdapter(OnPaidChange onPaidChange, OnItemClickListener onItemClick) {
        this.onPaidChangeCallback = onPaidChange;
        this.onItemClickCallback = onItemClick; // [MỚI]
    }

    // Nhận List<Object> từ ViewModel
    public void submit(List<Object> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    // Quyết định kiểu View
    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof OrderHeaderItem) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // [SỬA] Inflate layout dựa trên viewType
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_header, parent, false);
            return new VHHeader(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_card, parent, false);
            return new VHItem(v); // Đổi tên VH cũ thành VHItem
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // [SỬA] Bind dựa trên kiểu ViewHolder
        if (holder.getItemViewType() == TYPE_HEADER) {
            OrderHeaderItem header = (OrderHeaderItem) data.get(position);
            ((VHHeader) holder).bind(header);
        } else {
            OrderWithItems order = (OrderWithItems) data.get(position);
            ((VHItem) holder).bind(order, onPaidChangeCallback, onItemClickCallback);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * [MỚI] ViewHolder cho Header
     */
    static class VHHeader extends RecyclerView.ViewHolder {
        TextView tvDateHeader, tvDateTotal;
        SimpleDateFormat sdf;

        VHHeader(View v) {
            super(v);
            tvDateHeader = v.findViewById(R.id.tvDateHeader);
            tvDateTotal = v.findViewById(R.id.tvDateTotal);
            // Định dạng: "Chủ Nhật, 09/11/2025"
            sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        }
        void bind(OrderHeaderItem header) {
            // Hiển thị tổng tiền
            tvDateTotal.setText(String.format(Locale.US, "%,d", header.dayTotal));

            // Hiển thị ngày (Xử lý logic "Hôm nay")
            if (DateUtils.isToday(header.dateMillis)) {
                SimpleDateFormat sdfToday = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDateHeader.setText("Hôm nay, " + sdfToday.format(header.dateMillis));
            } else {
                tvDateHeader.setText(sdf.format(header.dateMillis));
            }
        }
    }

    static class VHItem extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTime, tvTotal, tvLines;
        CheckBox cbPaid;
        LinearLayout btnPaidArea;
        TextView btnDelete;
        Context context;

        VHItem(View v) { super(v);
            context = v.getContext();
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvLines = v.findViewById(R.id.tvLines);
            cbPaid = v.findViewById(R.id.cbPaid);
            btnPaidArea = v.findViewById(R.id.btnPaidArea);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(OrderWithItems orderWithItems, OnPaidChange paidChangeCb, OnItemClickListener itemClickCb) {
            OrderEntity order = orderWithItems.order;

            // Bind dữ liệu Order
            tvCustomer.setText(order.customerName);
            tvTotal.setText(String.format(Locale.US, "%,d", order.totalAmount));

            // Logic hiển thị và Khóa Checkbox
            boolean isPaid = "PAID".equals(order.status);
            cbPaid.setChecked(isPaid);
            cbPaid.setEnabled(!isPaid); // Yêu cầu 2: Khóa nếu đã thanh toán
            btnPaidArea.setEnabled(!isPaid); // Khóa luôn cả layout cha

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(order.createdAt));

            // Build chuỗi tóm tắt món hàng giới hạn là 5
            if (orderWithItems.orderItems != null && !orderWithItems.orderItems.isEmpty()) {
                StringBuilder linesSummary = new StringBuilder();

                // Giới hạn 5 dòng
                int maxLinesToShow = 5;
                int totalItems = orderWithItems.orderItems.size();

                for (int i = 0; i < totalItems; i++) {

                    if (i < maxLinesToShow) {
                        // Hiển thị các món 1, 2, 3, 4
                        OrderItemEntity item = orderWithItems.orderItems.get(i);
                        if (i > 0) {
                            linesSummary.append("\n"); // Thêm xuống dòng
                        }
                        linesSummary.append(item.quantity)
                                .append(" x ")
                                .append(item.productName);
                    } else if (i == maxLinesToShow) {
                        // Tại món thứ 5 (nếu có nhiều hơn 5)
                        int remaining = totalItems - maxLinesToShow;
                        if (remaining > 0) {
                            linesSummary.append("\n+ ")
                                    .append(remaining)
                                    .append(" hàng khác");
                        }
                        break; // Dừng vòng lặp
                    }
                }
                tvLines.setText(linesSummary.toString());
            } else {
                tvLines.setText("Đơn hàng trống");
            }

            // OnClickListener
            btnPaidArea.setOnClickListener(v -> {
                // Nếu đã paid (bị khóa) thì không làm gì
                if (isPaid) return;
                //  Hỏi xác nhận
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận thanh toán")
                        .setMessage("Bạn có chắc chắn muốn đánh dấu đơn hàng này là ĐÃ NHẬN TIỀN?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            // Chỉ gọi callback khi người dùng bấm "Xác nhận"
                            paidChangeCb.onChange(order, true);
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            });

            // Gán listener cho toàn bộ card
            itemView.setOnClickListener(v -> {
                if (itemClickCb != null) {
                    itemClickCb.onItemClick(orderWithItems);
                }
            });

            // [MỚI] Gán listener cho nút Xóa (chưa có logic)
            btnDelete.setOnClickListener(v -> {
                // TODO: Gọi callback để xóa (nếu cần)
                Toast.makeText(context, "Chức năng Xóa đang chờ...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}