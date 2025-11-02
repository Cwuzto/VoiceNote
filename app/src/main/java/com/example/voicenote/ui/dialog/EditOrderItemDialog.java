package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.OrderItemEntity; // Import
import com.example.voicenote.util.MoneyUtils; // Import MoneyUtils

import java.util.Locale;

/**
 * Dialog chỉnh sửa một món hàng trong giỏ (tên, sl, giá, ghi chú)
 */
public class EditOrderItemDialog extends DialogFragment {

    public interface OnSaveListener {
        void onSave(OrderItemEntity item);
    }
    public interface OnDeleteListener {
        void onDelete(OrderItemEntity item);
    }

    private OrderItemEntity item;
    private OnSaveListener onSaveListener;
    private OnDeleteListener onDeleteListener;

    private EditText edtName, edtQty, edtPrice, edtNote;

    // Sử dụng newInstance để truyền dữ liệu an toàn
    public static EditOrderItemDialog newInstance(OrderItemEntity item) {
        EditOrderItemDialog dialog = new EditOrderItemDialog();
        // [QUAN TRỌNG] Cần implement Parcelable cho OrderItemEntity
        // hoặc truyền từng trường
        Bundle args = new Bundle();
        args.putString("name", item.productName);
        args.putLong("price", item.unitPrice);
        args.putInt("qty", item.quantity);
        args.putString("note", item.note);
        args.putLong("id", item.id);
        args.putLong("orderId", item.orderId);
        dialog.setArguments(args);
        return dialog;
    }

    // Setter cho callbacks
    public EditOrderItemDialog setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
        return this;
    }
    public EditOrderItemDialog setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo item từ arguments
        if (getArguments() != null) {
            item = new OrderItemEntity();
            item.productName = getArguments().getString("name");
            item.unitPrice = getArguments().getLong("price");
            item.quantity = getArguments().getInt("qty");
            item.note = getArguments().getString("note");
            item.id = getArguments().getLong("id");
            item.orderId = getArguments().getLong("orderId");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Căn giữa + bo góc
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();
            w.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_edit_order_item, container, false);

        edtName = v.findViewById(R.id.edtName);
        edtQty = v.findViewById(R.id.edtQty);
        edtPrice = v.findViewById(R.id.edtPrice);
        edtNote = v.findViewById(R.id.edtNote);

        ImageView btnClose = v.findViewById(R.id.btnClose);
        TextView btnDecQty = v.findViewById(R.id.btnDecQty);
        TextView btnIncQty = v.findViewById(R.id.btnIncQty);
        TextView btnDecPrice = v.findViewById(R.id.btnDecPrice);
        TextView btnIncPrice = v.findViewById(R.id.btnIncPrice);
        TextView btnDelete = v.findViewById(R.id.btnDelete);
        TextView btnSave = v.findViewById(R.id.btnSave);

        // Gắn TextWatcher định dạng tiền
        MoneyUtils.attachMoneyFormatter(edtPrice);

        // Set dữ liệu ban đầu
        edtName.setText(item.productName);
        edtQty.setText(String.valueOf(item.quantity));
        edtPrice.setText(String.format(Locale.US, "%,d", item.unitPrice));
        edtNote.setText(item.note);

        // Set Listeners
        btnClose.setOnClickListener(view -> dismiss());

        btnDecQty.setOnClickListener(view -> stepQty(-1));
        btnIncQty.setOnClickListener(view -> stepQty(1));

        // Yêu cầu: +/- 5000
        btnDecPrice.setOnClickListener(view -> stepPrice(-5000));
        btnIncPrice.setOnClickListener(view -> stepPrice(5000));

        btnDelete.setOnClickListener(view -> {
            if (onDeleteListener != null) {
                onDeleteListener.onDelete(item);
            }
            dismiss();
        });

        btnSave.setOnClickListener(view -> {
            // Lấy dữ liệu đã sửa
            item.productName = edtName.getText().toString().trim();
            item.quantity = parseCleanInt(edtQty.getText().toString());
            item.unitPrice = parseCleanLong(edtPrice.getText().toString());
            item.note = edtNote.getText().toString().trim();

            if (item.productName.isEmpty()) {
                edtName.setError("Nhập tên hàng");
                return;
            }
            if (item.quantity <= 0) {
                item.quantity = 1; // Mặc định là 1 nếu nhập sai
            }

            if (onSaveListener != null) {
                onSaveListener.onSave(item);
            }
            dismiss();
        });

        return v;
    }

    private void stepQty(int delta) {
        int qty = parseCleanInt(edtQty.getText().toString());
        qty = Math.max(1, qty + delta); // Số lượng ít nhất là 1
        edtQty.setText(String.valueOf(qty));
    }

    private void stepPrice(long delta) {
        long price = parseCleanLong(edtPrice.getText().toString());
        price = Math.max(0, price + delta); // Giá ít nhất là 0
        edtPrice.setText(String.format(Locale.US, "%,d", price));
        edtPrice.setSelection(edtPrice.getText().length());
    }

    private int parseCleanInt(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private long parseCleanLong(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }
}