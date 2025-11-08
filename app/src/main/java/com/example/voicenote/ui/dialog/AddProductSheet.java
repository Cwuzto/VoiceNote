// File: com/example/voicenote/ui/dialog/AddProductSheet.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.util.MoneyUtils;

import java.util.Locale;

public class AddProductSheet extends DialogFragment {

    /**
     * Interface chính
     */
    public interface OnSaveListener {
        void onSave(ProductEntity product, String name, long price);
    }

    /**
     * Interface cũ (cho SaleActivity)
     */
    public interface OnAdded {
        void onAdded(String name, long price);
    }

    private final OnSaveListener onSaveListener;
    private ProductEntity existingProduct;

    /**
     * Constructor cho SaleActivity (dùng OnAdded)
     */
    public AddProductSheet(OnAdded cb) {
        // Chuyển đổi callback cũ sang mới
        this.onSaveListener = (product, name, price) -> cb.onAdded(name, price);
        this.existingProduct = null;
    }

    /**
     * Constructor cho ProductListFragment (dùng OnSaveListener)
     */
    public AddProductSheet(ProductEntity product, OnSaveListener listener) {
        this.onSaveListener = listener;
        this.existingProduct = product;
    }

    /**
     * Factory Method (Cách gọi chuẩn)
     */
    public static AddProductSheet newInstance(ProductEntity product, OnSaveListener listener) {
        return new AddProductSheet(product, listener);
    }

    @Override public void onStart() {
        super.onStart();
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

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.dialog_add_product, c, false);

        // Ánh xạ View
        TextView tvDialogTitle = v.findViewById(R.id.tvDialogTitle);
        EditText edtName  = v.findViewById(R.id.edtName);
        EditText edtPrice = v.findViewById(R.id.edtPrice);
        TextView btnDec   = v.findViewById(R.id.btnDec);
        TextView btnInc   = v.findViewById(R.id.btnInc);
        TextView btnCancel= v.findViewById(R.id.btnCancel);
        TextView btnAdd   = v.findViewById(R.id.btnAdd);
        View btnClose     = v.findViewById(R.id.btnClose);

        MoneyUtils.attachMoneyFormatter(edtPrice);

        // Xử lý chế độ Sửa/Thêm
        if (existingProduct != null) {
            // Chế độ Sửa
            tvDialogTitle.setText("Sửa sản phẩm");
            btnAdd.setText("Lưu");
            edtName.setText(existingProduct.name);
            edtPrice.setText(String.format(Locale.US, "%,d", existingProduct.price));
            edtPrice.setSelection(edtPrice.getText().length());
        } else {
            // Chế độ Thêm mới
            tvDialogTitle.setText("Thêm hàng hoá");
            btnAdd.setText("Thêm");
            edtPrice.setText("0");
        }

        // Listeners
        btnDec.setOnClickListener(view -> step(edtPrice, -1000));
        btnInc.setOnClickListener(view -> step(edtPrice, +1000));
        btnCancel.setOnClickListener(view -> dismiss());
        btnClose.setOnClickListener(view -> dismiss());

        btnAdd.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            long price = parseCleanLong(edtPrice.getText().toString());

            if (name.isEmpty()) {
                edtName.setError("Nhập tên hàng");
                return;
            }

            if (onSaveListener != null) {
                onSaveListener.onSave(existingProduct, name, price);
            }
            dismiss();
        });

        return v;
    }

    /**
     * Tăng/giảm giá tiền
     */
    private void step(EditText edt, int delta) {
        long p = parseCleanLong(edt.getText().toString());
        p = Math.max(0, p + delta);
        edt.setText(formatComma(p));
        edt.setSelection(edt.getText().length()); // Di chuyển con trỏ về cuối
    }

    /**
     * Chuyển text ("50,000") về long (50000)
     */
    private long parseCleanLong(String s){
        try {
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        }
        catch (Exception e){
            return 0;
        }
    }

    /**
     * Chuyển long (50000) về text ("50,000")
     */
    private String formatComma(long v){
        return String.format(java.util.Locale.US, "%,d", v);
    }
}