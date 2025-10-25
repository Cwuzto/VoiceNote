package com.example.voicenote.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.voicenote.R;

public class AddProductSheet extends DialogFragment {

    public interface OnAdded { void onAdded(String name, long price); }
    private final OnAdded cb;

    public AddProductSheet(OnAdded cb){ this.cb = cb; }

    @Override public void onStart() {
        super.onStart();
        // căn giữa + bo rộng ~90% màn
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();
            w.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded); // nền trắng bo tròn
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

        EditText edtName  = v.findViewById(R.id.edtName);
        EditText edtPrice = v.findViewById(R.id.edtPrice);
        TextView btnDec   = v.findViewById(R.id.btnDec);
        TextView btnInc   = v.findViewById(R.id.btnInc);
        TextView btnCancel= v.findViewById(R.id.btnCancel);
        TextView btnAdd   = v.findViewById(R.id.btnAdd);
        View btnClose     = v.findViewById(R.id.btnClose);

        // Format tiền: phẩy mỗi 3 số
        edtPrice.setText("0");
        edtPrice.addTextChangedListener(new CommaMoneyWatcher(edtPrice));

        btnDec.setOnClickListener(view -> step(edtPrice, -1000));
        btnInc.setOnClickListener(view -> step(edtPrice, +1000));
        btnCancel.setOnClickListener(view -> dismiss());
        btnClose.setOnClickListener(view -> dismiss());

        btnAdd.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            long price = parseCleanLong(edtPrice.getText().toString());
            if (name.isEmpty()) { edtName.setError("Nhập tên hàng"); return; }
            if (cb!=null) cb.onAdded(name, price);
            dismiss();
        });

        return v;
    }

    private void step(EditText edt, int delta) {
        long p = parseCleanLong(edt.getText().toString());
        p = Math.max(0, p + delta);
        edt.setText(formatComma(p));
        edt.setSelection(edt.getText().length());
    }

    private long parseCleanLong(String s){
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (Exception e){ return 0; }
    }

    private String formatComma(long v){
        return String.format(java.util.Locale.US, "%,d", v); // dùng dấu phẩy
    }

    /** TextWatcher định dạng phẩy mỗi 3 số khi gõ */
    static class CommaMoneyWatcher implements TextWatcher {
        private final EditText et;
        private String last = "";
        CommaMoneyWatcher(EditText e){ this.et = e; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { last = s.toString(); }
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void afterTextChanged(Editable e) {
            et.removeTextChangedListener(this);
            String raw = e.toString().replaceAll("[^0-9]", "");
            if (raw.isEmpty()) raw = "0";
            try {
                long val = Long.parseLong(raw);
                String fmt = String.format(java.util.Locale.US, "%,d", val);
                et.setText(fmt);
                et.setSelection(fmt.length());
            } catch (Exception ex) {
                et.setText(last); et.setSelection(last.length());
            }
            et.addTextChangedListener(this);
        }
    }
}
